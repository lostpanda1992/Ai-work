# -*- coding: utf-8 -*-

import logging
import sys
sys.path.append('image_analysis_gpu_service/common')
import handlers

import threading

import config


# 单例装饰器, 解决同一个 logger 重复打同一条日志的问题
def singleton(cls, *args, **kw):
    _instances = {}
    _instance_lock = threading.Lock()

    def _singleton():
        with _instance_lock:
            if cls not in _instances:
                _instances[cls] = cls(*args, **kw)
        return _instances[cls]

    return _singleton


@singleton
class BusinessLogger:
    def __init__(self):
        # 创建 logger
        self.__logger = logging.getLogger('business')  # log名字用.表示继承关系
        self.__logger.setLevel(config.logging_level)

        # 创建 handler, 写入文件的日志
        self.__handler = handlers.TimedRotatingFileHandler(
            filename=config.word_cut_log_file,
            when='h',
            interval=1,  # 每小时1个新的日志文件
            backupCount=config.log_file_max_count,  # 日志文件保留数量, 0 表示不删除
            encoding='utf-8'
        )
        # self.__handler.suffix = '%Y-%m-%d-%H.log'
        # self.__handler.suffix = '%Y-%m-%d.log'

        # 创建日志输出格式
        self.__formatter = logging.Formatter(
            fmt='[%(asctime)s %(name)s %(levelname)s] %(message)s',
            # datefmt='%Y-%m-%d %H:%M:%S'
        )

        # 组装 logger
        self.__handler.setFormatter(self.__formatter)
        self.__logger.addHandler(self.__handler)
        self.__logger.propagate = False  # 阻断日志向root传递

        # # 在控制台打印日志
        # self.__console_handler = logging.StreamHandler()
        # self.__console_handler.setFormatter(self.__formatter)
        # self.__logger.addHandler(self.__console_handler)

    def debug(self, msg):
        self.__logger.debug(msg)

    def info(self, msg):
        self.__logger.info(msg)

    def warning(self, msg):
        self.__logger.warning(msg)

    def error(self, msg):
        self.__logger.error(msg)

    def critical(self, msg):
        self.__logger.critical(msg)

    def log(self, level, msg):
        self.__logger.log(level, msg)


@singleton
class SystemLogger:
    def __init__(self):
        # 创建 logger
        self.__logger = logging.getLogger('sys')  # log名字用.表示继承关系
        self.__logger.setLevel(config.logging_level)

        # 创建 handler, 写到控制台的日志 (标准错误流)
        self.__console_handler = logging.StreamHandler()

        # 创建日志输出格式
        self.__formatter = logging.Formatter(
            fmt='[%(asctime)s %(name)s %(levelname)s] %(message)s',
            # datefmt='%Y-%m-%d %H:%M:%S'
        )

        # 组装 logger
        self.__console_handler.setFormatter(self.__formatter)
        self.__logger.addHandler(self.__console_handler)
        self.__logger.propagate = False  # 阻断日志向root传递

    def debug(self, msg):
        self.__logger.debug(msg)

    def info(self, msg):
        self.__logger.info(msg)

    def warning(self, msg):
        self.__logger.warning(msg)

    def error(self, msg):
        self.__logger.error(msg)

    def critical(self, msg):
        self.__logger.critical(msg)

    def log(self, level, msg):
        self.__logger.log(level, msg)


###############################################################################
# 测试

if __name__ == '__main__':
    # 创建logger，如果参数为空则返回root logger
    logger = logging.getLogger("nick")
    logger.setLevel(logging.DEBUG)  # 设置logger日志等级

    # 创建handler
    # fh = logging.FileHandler("test.log", encoding="utf-8")
    fh = handlers.TimedRotatingFileHandler(
        filename='test.log',
        when='M',
        interval=1,
        backupCount=0,
        encoding='utf-8'
    )
    ch = logging.StreamHandler()

    # 设置输出日志格式
    formatter = logging.Formatter(
        fmt="%(asctime)s %(name)s %(filename)s %(message)s",
        datefmt="%Y/%m/%d %X"
    )

    # 注意 logging.Formatter的大小写

    # 为handler指定输出格式，注意大小写
    fh.setFormatter(formatter)
    ch.setFormatter(formatter)

    # 为logger添加的日志处理器
    logger.addHandler(fh)
    logger.addHandler(ch)

    import time

    for i in range(30):
        # 输出不同级别的log
        # logger.warning("泰拳警告")
        # logger.info("提示")
        # logger.error("错误")

        logger1 = BusinessLogger()
        logger2 = BusinessLogger()
        syslog1 = SystemLogger()
        syslog2 = SystemLogger()

        logger1.info('logger1: 1')
        logger1.info('logger1: 2')
        logger2.info('logger2: 1')
        logger2.info('logger2: 2')

        syslog1.info('syslog1: 1')
        syslog1.info('syslog1: 2')
        syslog2.info('syslog2: 1')
        syslog2.info('syslog2: 2')

        time.sleep(2)

    logger1 = BusinessLogger()
    logger2 = BusinessLogger()
    syslog1 = SystemLogger()
    syslog2 = SystemLogger()

    logger1.info('logger1: 1')
    logger1.info('logger1: 2')
    logger2.info('logger2: 1')
    logger2.info('logger2: 2')

    syslog1.info('syslog1: 1')
    syslog1.info('syslog1: 2')
    syslog2.info('syslog2: 1')
    syslog2.info('syslog2: 2')
