from db.sql.model.BaseModel import BaseEntity
from sqlalchemy import Column, BigInteger, Float, DateTime

class WindLogModel(BaseEntity):
    __tablename__ = 'wind_log'  # 数据表名称
    shipId = Column(BigInteger, name='ship_id', nullable=False)  # 船舶ID
    time = Column(DateTime, nullable=False)           # 时间
    voyageId = Column(BigInteger,name='voyage_id', nullable=True)     # 航段ID
    windDirection = Column(Float,name='wind_direction', nullable=True)      # 风向
    windSpeed = Column(Float,name='wind_speed', nullable=True)          # 风速
    waterDirection = Column(Float,name='water_direction', nullable=True)     # 流向
    waterSpeed = Column(Float,name='water_speed', nullable=True)         # 流速
    waveHeight = Column(Float,name='wave_height', nullable=True)         # 波高
    waveDirection = Column(Float,name='wave_direction', nullable=True)      # 波向
    linkId = Column(BigInteger,name='link_id', nullable=True)        # 链接ID