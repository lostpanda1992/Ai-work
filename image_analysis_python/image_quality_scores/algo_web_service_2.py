# -*- coding: utf-8 -*-

import tornado.web
import tornado.ioloop
import tornado.httpserver

import config
from app import application, start_callback


def start_server(port):
    application.listen(port)
    tornado.ioloop.IOLoop.instance().add_callback(start_callback)
    tornado.ioloop.IOLoop.instance().start()
    
    # 多进程调用， 由于tf不是线程安全的，因此
    # http_server = tornado.httpserver.HTTPServer(application)
    # http_server.bind(web_server_port)
    # http_server.start(num_processes=2)
    # tornado.ioloop.IOLoop.instance().start()


if __name__ == '__main__':
    start_server(config.web_server_port_2)