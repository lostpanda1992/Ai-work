# -*- coding: utf-8 -*-

import config

# 下载模型
from download_weight import download_weights as download_weights
download_weights.down_load_weights(config.weights_fold)

import os
import tornado.ioloop
import tornado.web
from image_analysis_gpu_service import common
business_logger = common.BusinessLogger()

import image_analysis_gpu_service.web.healthcheck as healthcheck
import image_analysis_gpu_service.web.image_word_joint_label as label

settings = dict(
    static_path=os.path.join(config.project_dir, "static")
)

# 请求路由表
application = tornado.web.Application([
    # 系统使用的接口
    (r"/healthcheck\.html", healthcheck.HealthCheckHandler),

    # url安排规则: 可区分的部门名/项目名/接口名
    # 业务接口
    # 图片水印检测
    (r"/image/imageWordJoint", label.ImageWordJointHandler),

], **settings)


def add_healthcheck():
    """创建healthcheck文件,谨慎使用"""
    with open('healthcheck.html', 'w') as f:  # 创建healthcheck 文件
        f.write(str('ok'))
    print('healthcheck.html created')


def start_callback():
    # todo 启动后的回调操作放这里
    # add_healthcheck()  # healthchk 文件由 shell 创建, 服务只读不创建
    pass


def start_server():
    # 启动服务
    application.listen(config.web_server_port)
    tornado.ioloop.IOLoop.instance().add_callback(start_callback)
    tornado.ioloop.IOLoop.instance().start()


if __name__ == '__main__':
    start_server()
