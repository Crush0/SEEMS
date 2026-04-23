import argparse
import os
import signal
import sys
from datetime import datetime

from apscheduler.schedulers.background import BackgroundScheduler
import asyncio
import yaml

from handler import work_condition_handler, voyage_handler
from realtime import DataWebSocket
from weather import WeatherGetter
from ship_position_api import ShipPositionAPI

ROOT = os.path.dirname(os.path.abspath(__file__))

# 全局变量，用于保存实例以便在信号处理时访问
ws_instance = None
ship_position_api = None
scheduler = None

def get_config():
    parser = argparse.ArgumentParser(description='realtime analysis of tugboat.')
    parser.add_argument('--config', type=str, default=os.path.join(ROOT, 'config.yaml'), help='config file path')
    _args = parser.parse_args()
    with open(_args.config, 'r', encoding='utf-8') as f:
        config = yaml.load(f, Loader=yaml.FullLoader)
    return config


def signal_handler(signum, frame):
    """
    信号处理函数，用于优雅关闭
    """
    print(f"\n[SHUTDOWN] Received signal {signum}, shutting down gracefully...")

    # 停止WebSocket连接
    if ws_instance:
        ws_instance.stop()

    # 停止船舶位置API
    if ship_position_api:
        ship_position_api.stop()

    # 关闭调度器
    if scheduler:
        scheduler.shutdown()

    print("[SHUTDOWN] All services stopped. Exiting...")
    sys.exit(0)


async def run(args_dict):
    global ws_instance, ship_position_api, scheduler

    # 注册信号处理器
    signal.signal(signal.SIGINT, signal_handler)   # Ctrl+C
    signal.signal(signal.SIGTERM, signal_handler)  # kill命令

    # 创建船舶位置API调用实例，使用MMSI从shipxy.com获取数据
    api_config = args_dict.get('ship_position_api', {})

    if api_config.get('enabled', False):
        ship_position_api = ShipPositionAPI(
            mmsi=args_dict['mmsi'],  # 使用MMSI编号
            interval=api_config.get('interval', 5)  # 5秒间隔
        )
        print(f"[INIT] Ship position API enabled for MMSI: {args_dict['mmsi']}")
    else:
        ship_position_api = None
        print("[INIT] Ship position API disabled, using WebSocket data only")

    # 创建WebSocket实例，传入位置缓存（如果启用）
    position_cache = ship_position_api.get_cache() if ship_position_api else None
    ws_instance = DataWebSocket(args_dict, position_cache=position_cache)

    weatherGetter = WeatherGetter(args_dict)
    ws_instance.add_handler(work_condition_handler)
    ws_instance.add_handler(voyage_handler)

    # 启动定时任务
    scheduler = BackgroundScheduler()
    scheduler.add_job(weatherGetter.get, 'interval', seconds=args_dict["weather"]["interval"], next_run_time=datetime.now())
    scheduler.start()

    # 启动船舶位置API
    if ship_position_api:
        ship_position_api.start()

    try:
        # 在单独的线程中运行WebSocket，因为run_forever()是阻塞的
        await asyncio.gather(
            asyncio.to_thread(ws_instance.start_ws),
        )
    except KeyboardInterrupt:
        print("\n[SHUTDOWN] Keyboard interrupt received")
    finally:
        # 清理资源
        if ship_position_api:
            ship_position_api.stop()
        if ws_instance:
            ws_instance.stop()
        if scheduler:
            scheduler.shutdown()


if __name__ == '__main__':
    args = get_config()
    try:
        asyncio.run(run(args))
    except KeyboardInterrupt:
        print("\n[SHUTDOWN] Program terminated by user")
        sys.exit(0)
