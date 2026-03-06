
import logging
import colorlog


def get_logger(level=logging.INFO):
    # 创建logger对象
    _logger = logging.getLogger()
    _logger.setLevel(level)
    # 创建控制台日志处理器
    console_handler = logging.StreamHandler()
    console_handler.setLevel(level)
    # 定义颜色输出格式
    color_formatter = colorlog.ColoredFormatter(
        ' %(log_color)s%(asctime)s %(levelname)s: %(message)s',
        log_colors={
            'DEBUG': 'cyan',
            'INFO': 'green',
            'WARNING': 'yellow',
            'ERROR': 'red',
            'CRITICAL': 'red,bg_white',
        }
    )
    # 将颜色输出格式添加到控制台日志处理器
    console_handler.setFormatter(color_formatter)
    # 移除默认的handler
    # 将控制台日志处理器添加到logger对象
    _logger.addHandler(console_handler)
    return _logger

logger = get_logger()