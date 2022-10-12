# -*- coding: utf-8 -*-

import os

import tornado.ioloop
import tornado.web

import before_start

# 在 import 所有模型之前执行
before_start.pull_all_stdq_when_start()

import config
import robot_service.common.logger as logger
import robot_service.web.guess as guess
import robot_service.web.healthcheck as healthcheck
import robot_service.web.init_recom as initRecom
import robot_service.web.match as match
import robot_service.web.monitor as monitor
import robot_service.web.update as update

business_logger = logger.BusinessLogger()

settings = dict(
    static_path=os.path.join(config.project_dir, "static")
)

# 请求路由表
application = tornado.web.Application([
    # 系统使用的接口
    (r"/monitor", monitor.QMonitorHandler),
    (r"/healthcheck\.html", healthcheck.HealthCheckHandler),
    (r"/apiKeyCheck", update.ApiKeyCheckHandler),

    # url安排规则: 可区分的部门名/项目名/接口名
    # 业务接口
    (r"/halgo/robot/match", match.MatchHandler),
    (r"/halgo/robot/guess", guess.GuessHandler),
    (r"/halgo/robot/initRecom", initRecom.InitRecomHandler),
    (r"/halgo/robot/update", update.UpdateHandler),

], **settings)


def add_healthcheck():
    """创建healthcheck文件,谨慎使用"""
    with open('healthcheck.html', 'w') as f:  # 创建healthcheck 文件
        f.write(str('ok'))
    print('healthcheck.html created')


def start_callback():
    # todo 启动后的回调操作放这里
    # add_healthcheck()  # healthcheck 文件由 shell 创建, 服务只读不创建
    pass


def start_server():
    # 启动服务
    application.listen(config.web_server_port)
    tornado.ioloop.IOLoop.instance().add_callback(start_callback)
    tornado.ioloop.IOLoop.instance().start()


if __name__ == '__main__':
    start_server()
