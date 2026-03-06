from objtyping import to_primitive
from sqlalchemy import Column, BigInteger, DateTime, Boolean
from sqlalchemy.orm import declarative_base
import datetime

Base = declarative_base()


class BaseEntity(Base):
    __abstract__ = True  # This class will not be mapped to a database table

    id = Column(BigInteger, primary_key=True, autoincrement=False, comment="主键ID")
    create_date = Column(DateTime, default=datetime.datetime.now, comment="创建时间")
    update_date = Column(DateTime, onupdate=datetime.datetime.now, comment="更新时间")
    is_deleted = Column(Boolean, default=False, comment="是否删除")

    def to_json(self):
        return to_primitive(self)
