from db import RedisEngine
from db.sql.model import VoyageLog
from db.sql.snowflakeId import IdWorker
from datetime import datetime
from logger import logger

# 使用雪花算法生成唯一ID，worker为ID生成器实例
worker = IdWorker(1, 4, 0)

REDIS_KEY_PREFIX = 'voyage:'


def voyage_handler(**kwargs):
    """
    处理航次/航段的逻辑，包括创建新航次和结束航次。

    参数：
        kwargs:
            - ship_id: 船舶ID
            - models: 最新的采集数据
            - db_session: 数据库会话对象，用于保存航次日志
            - redis_engine: Redis引擎，用于存储航次状态
    """
    ship_id = kwargs['ship_id']
    models = kwargs['models']
    db_session = kwargs['db_session']
    redis_engine:RedisEngine = kwargs['redis_engine']

    # 提取最新状态
    latest_status = models['gps_log'].workStatus
    redis_key = f"{REDIS_KEY_PREFIX}{ship_id}"

    # 从 Redis 获取航次ID
    voyage_id = redis_engine.get_str(redis_key)

    if latest_status == 'STOPPING_AT_PORT' or latest_status == 'CHARGING':
        if voyage_id:
            # 处理航次结束
            voyage_log = db_session.query(VoyageLog).filter_by(id=voyage_id).first()
            if voyage_log:
                voyage_log.endTime = datetime.now()
                db_session.commit()
                logger.info(f"航次结束，ship_ID: 【{ship_id}】，voyage_ID: 【{voyage_id}】，结束时间: {voyage_log.endTime}")
                redis_engine.delete_key(redis_key)
            else:
                logger.error(f"航次日志不存在，ship_ID: 【{ship_id}】，voyage_ID: 【{voyage_id}】")
    else:
        if not voyage_id:
            # 处理新航次开始
            new_voyage_id = worker.get_id()
            # 创建新的 VoyageLog 对象
            new_voyage = VoyageLog(
                id=new_voyage_id,
                shipId=ship_id,
                departure="未知",
                arrival="未知",
                startTime=datetime.now(),
                voyagePowerConsumption=0.0,
                sailingTime=0,
                voyageDistance=0.0,
            )

            # 保存到数据库
            db_session.add(new_voyage)
            db_session.commit()
            logger.info(f"新航次开始，ship_ID: 【{ship_id}】，voyage_ID: 【{new_voyage_id}】")
            # 将航次ID保存到Redis
            redis_engine.add_str(redis_key, new_voyage_id)
