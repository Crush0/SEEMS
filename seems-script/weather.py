import json
from datetime import datetime

import db
from db.sql.model import WindLogModel
from db.sql.snowflakeId import IdWorker
from encoder import JSONEncoder
from logger import logger
from utils import get_weather

worker = IdWorker(1, 3, 0)  # 使用雪花算法生成唯一ID，worker为ID生成器实例

class WeatherGetter:

    def __init__(self, args):
        self.args = args
        self.engine = db.SQLEngine(self.args['mysql']['host'], self.args['mysql']['port'], self.args['mysql']['user'],
                                   self.args['mysql']['password'], self.args['mysql']['database'])  # 数据库连接
        self.redis = db.RedisEngine(self.args['redis']['host'], self.args['redis']['port'], self.args['redis']['db'],
                                    self.args['redis']['password'])  # Redis连接
        self.ship_id = self.args['ship_id']
        self.weather = None
        self.logger = logger
    def get(self):
        self.logger.info('Getting weather for ship {}'.format(self.ship_id))
        self.weather = get_weather()
        wind_log = WindLogModel()
        wind_log.id = worker.get_id()
        wind_log.is_deleted = False
        wind_log.shipId = self.ship_id
        wind_log.create_date = datetime.now()
        print(self.weather)
        wind_log.windDirection = self.weather['windDirection']['data'][0]
        wind_log.windSpeed = self.weather['windSpeed']['data'][0]
        wind_log.waterDirection = self.weather['waterDirection']['data'][0]
        wind_log.waterSpeed = self.weather['waterSpeed']['data'][0]
        wind_log.waveDirection = self.weather['waveDirection']['data'][0]
        wind_log.waveHeight = self.weather['waveHeight']['data'][0]
        wind_log.time = self.weather['updateTime']
        with self.engine.get_session() as session:
            session.add(wind_log)
            session.commit()
            session.refresh(wind_log)
            session.expunge(wind_log)
        self.redis.add_str(f'weather:{self.args["ship_id"]}', json.dumps(wind_log.to_json(), cls=JSONEncoder))
