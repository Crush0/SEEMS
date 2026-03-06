import datetime

from sqlalchemy import Numeric, String, Column, BigInteger, DateTime

from db.sql.model.BaseModel import BaseEntity


class BatteryLogModel(BaseEntity):
    __tablename__ = 'battery_log'
    ship_id = Column(BigInteger, nullable=False, comment="船舶ID")
    time = Column(DateTime, default=datetime.datetime.utcnow, comment="时间")
    soc = Column(Numeric(precision=10, scale=2), nullable=True, comment="电量")
    voltage = Column(Numeric(precision=10, scale=2), nullable=True, comment="电压")
    electricity = Column(Numeric(precision=10, scale=2), nullable=True, comment="电流")
    power = Column(Numeric(precision=10, scale=2), nullable=True, comment="电功率")
    temperature = Column(Numeric(precision=10, scale=2), nullable=True, comment="环境温度")
    position = Column(String, default="unknown", comment="电池位置 (l_0, l_1, ...)")
    link_id = Column(BigInteger, default="unknown", comment="链接ID")
    voyageId = Column(BigInteger, name='voyage_id', nullable=True)  # 航段ID