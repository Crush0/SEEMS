import argparse
import os
from datetime import datetime

from apscheduler.schedulers.background import BackgroundScheduler
import asyncio
import yaml

from handler import work_condition_handler, voyage_handler
from realtime import DataWebSocket
from weather import WeatherGetter

ROOT = os.path.dirname(os.path.abspath(__file__))

def get_config():
    parser = argparse.ArgumentParser(description='realtime analysis of tugboat.')
    parser.add_argument('--config', type=str, default=os.path.join(ROOT, 'config.yaml'), help='config file path')
    _args = parser.parse_args()
    with open(_args.config, 'r') as f:
        config = yaml.load(f, Loader=yaml.FullLoader)
    return config



async def run(args_dict):
    wsInstance = DataWebSocket(args_dict)
    weatherGetter = WeatherGetter(args_dict)
    wsInstance.add_handler(work_condition_handler)
    wsInstance.add_handler(voyage_handler)
    scheduler = BackgroundScheduler()
    scheduler.add_job(weatherGetter.get, 'interval',seconds=args_dict["weather"]["interval"], next_run_time=datetime.now())
    scheduler.start()
    await asyncio.gather(
        wsInstance.start_ws(),
    )


if __name__ == '__main__':
    args = get_config()
    asyncio.run(run(args))
