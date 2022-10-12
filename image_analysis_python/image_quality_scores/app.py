#! coding=utf-8

import os

# 不使用 GPU
os.environ['CUDA_DEVICE_ORDER'] = 'PCI_BUS_ID'
os.environ['CUDA_VISIBLE_DEVICES'] = '0'

import tornado.web
import tornado.httpserver

import image_score_service.web.monitor as monitor
import image_score_service.web.healthcheck as healthcheck
import image_score_service.web.score as score
import image_score_service.web.detect_face as detect_face

from config import *

settings = dict(
    static_path=os.path.join(project_dir, "static")
)

# 请求路由表
application = tornado.web.Application([
    # 系统使用的接口
    (r"/monitor", monitor.QMonitorHandler),
    (r"/healthcheck\.html", healthcheck.HealthCheckHandler),
    #
    # url中必须包含图片的有效url
    # 图片分数接口需要指定计算的分数类型
    (r"/halgo/image/score", score.ScoreHandler),
    (r"/halgo/image/detectface", detect_face.DetectFaceHandler)

], **settings)


def add_healthcheck():
    """创建healthcheck文件,谨慎使用"""
    with open('healthcheck.html', 'w') as f:  # 创建healthcheck 文件
        f.write(str('ok'))
    print('healthcheck.html created')


def start_callback():
    # todo 启动前的操作放这里
    # add_healthcheck()  # healthcheck 文件由 shell 创建, 服务只读不创建
    pass