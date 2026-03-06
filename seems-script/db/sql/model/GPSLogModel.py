import enum

from sqlalchemy import Column, BigInteger, Float, DateTime, Enum as SQLAlchemyEnum

from db.sql.model.BaseModel import BaseEntity


class WorkStatus(enum.Enum):
    HOVERING = "HOVERING"
    DRAGGING = "DRAGGING"
    STOPPING_AT_PORT = "STOPPING_AT_PORT"
    UNKNOWN = "UNKNOWN"
    CHARGING = "CHARGING"


class GPSLogModel(BaseEntity):
    __tablename__ = 'gps_log'
    ship_id = Column(BigInteger, nullable=True, comment="船舶ID")
    voyage_id = Column(BigInteger, nullable=True, comment="航次ID")
    longitude = Column(Float, nullable=True, comment="经度")
    latitude = Column(Float, nullable=True, comment="纬度")
    altitude = Column(Float, nullable=True, comment="高度")
    speed = Column(Float, nullable=True, comment="速度")
    direction = Column(Float, nullable=True, comment="方向")
    time = Column(DateTime, nullable=True, comment="时间")
    workStatus = Column(SQLAlchemyEnum('HOVERING', 'DRAGGING', 'STOPPING_AT_PORT', 'UNKNOWN', 'CHARGING'),
                        name='work_status',
                        comment="工况")
    link_id = Column(BigInteger, default="unknown", comment="链接ID")
