#! coding=utf-8

import os
import logging

###############################################################################
# directory setting
project_dir = os.path.split(os.path.realpath(__file__))[0]
logs_dir = os.path.join(project_dir, 'logs')

###############################################################################
# logging setting
word_cut_log_file = os.path.join(logs_dir, 'business.log')
logging_level = logging.INFO
log_file_max_size = 100 * 1024 * 1024
log_file_max_count = 30

###############################################################################
# 采用单进程多实例的方式提供web服务
# service setting
web_server_port_0 = 8080
web_server_port_1 = 8081
web_server_port_2 = 8082
web_server_port_3 = 8083

###############################################################################
# 调用方的token, 防止任意调用
token = ""