from sqlalchemy import Column, String, Float, Numeric, BigInteger

from db.sql.model.BaseModel import BaseEntity


class ShipInfoModel(BaseEntity):
    __tablename__ = 'ship_info'

    name = Column(String, nullable=True, comment="船舶名称")
    ship_type = Column(String, name='type', nullable=True, comment="船舶类型(种类)")
    model = Column(String, nullable=True, comment="船舶型号")
    owner_company = Column(String, nullable=True, comment="船舶隶属公司")
    manufacturer = Column(String, nullable=True, comment="船舶制造商")
    ship_origin_number = Column(String, nullable=True, comment="船舶初始登记号")
    mmsi = Column(String, nullable=True, comment="MMSI号")
    imo_number = Column(String, nullable=True, comment="IMO号")
    length = Column(Float, nullable=True, comment="船舶总长")
    lbp_length = Column(Float, nullable=True, comment="垂线间长")
    design_lwl = Column(Float, nullable=True, comment="设计水线长")
    molded_width = Column(Float, nullable=True, comment="型宽")
    width = Column(Float, nullable=True, comment="船舶宽度")
    molded_depth = Column(Float, nullable=True, comment="型深")
    design_draft = Column(Float, nullable=True, comment="设计吃水")
    max_draft = Column(Float, nullable=True, comment="最大吃水")
    no_load_draft = Column(Float, nullable=True, comment="空载押水量")
    max_load_draft = Column(Float, nullable=True, comment="满载押水量")
    progress = Column(Numeric(precision=10, scale=2), nullable=True, comment="船舶吨位 (DWT)")
    towing_force = Column(Numeric(precision=10, scale=2), nullable=True, comment="船舶拖力 (ton)")
    max_battery_capacity = Column(Numeric(precision=10, scale=2), nullable=True, comment="船舶最大电池容量 (kwh)")
