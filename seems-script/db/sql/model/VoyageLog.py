from sqlalchemy import Column, BigInteger, String, Double, DateTime

from db.sql.model.BaseModel import BaseEntity


class VoyageLog(BaseEntity):
    __tablename__ = 'voyage_log'
    shipId = Column(BigInteger, name='ship_id', nullable=False)  # 船舶ID
    departure = Column(String, name='departure', nullable=False)  # 出发地
    arrival = Column(String, name='arrival', nullable=False)  # 目的地
    startTime = Column(DateTime, name='start_time', nullable=False)  # 开始时间
    endTime = Column(DateTime, name='end_time', nullable=False)  # 结束时间
    voyagePowerConsumption = Column(Double, name='voyage_power_consumption', nullable=False)  # 航行消耗能量
    voyageDistance = Column(Double, name='voyage_distance', nullable=False)  # 航行距离
    sailingTime = Column(BigInteger, name='sailing_time', nullable=False)  # 航行时间