import datetime
import enum

from sqlalchemy import DateTime, Column, Float, String, BigInteger, Enum as SQLAlchemyEnum

from db.sql.model.BaseModel import BaseEntity


class PropellerWorkStatus(enum.Enum):
    RUNNING = "RUNNING"
    STOPPED = "STOPPED"
    ERROR = "ERROR"
    UNKNOWN = "UNKNOWN"

class PropellerDataModel(BaseEntity):
    __tablename__ = 'propeller_data'
    voyageId = Column(BigInteger, name='voyage_id', nullable=True)  # 航段ID
    time = Column(DateTime, default=datetime.datetime.utcnow, comment="时间")
    rpm = Column(Float, nullable=True, comment="转速")
    degrees = Column(Float, nullable=True, comment="角度")
    power = Column(Float, nullable=True, comment="功率")
    position = Column(String, nullable=True, comment="位置")
    ship_id = Column(BigInteger, nullable=True, comment="船舶ID")
    status = Column(SQLAlchemyEnum('RUNNING', 'STOPPED', 'ERROR', 'UNKNOWN'), comment="状态")
    link_id = Column(BigInteger, default="unknown", comment="链接ID")

