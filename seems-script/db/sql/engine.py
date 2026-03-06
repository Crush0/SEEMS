from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker


class Engine:
    engine = None

    def __init__(self, url, port, user, password, database):
        connection_url = f'mysql+pymysql://{user}:{password}@{url}:{port}/{database}?charset=utf8mb4'
        self.engine = create_engine(
            connection_url,
            max_overflow=0,
            pool_size=5,
            pool_recycle=1,
            pool_timeout=10,
            echo=None,

        )
        self.Session = sessionmaker(bind=self.engine)

    def get_session(self):
        return self.Session()