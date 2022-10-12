# -*- coding: utf-8 -*-

import logging
import os


###############################################################################
# directory setting
project_dir = os.path.split(os.path.realpath(__file__))[0]
logs_dir = os.path.join(project_dir, 'logs')

###############################################################################
# logging setting
word_cut_log_file = os.path.join(logs_dir, 'business.log')
logging_level = logging.INFO
log_file_max_size = 100 * 1024 * 1024
log_file_max_count = 15

###############################################################################
# service setting
web_server_port = 9011

###############################################################################
# watcher 指标前缀
WATCHER_PREFIX = 's.corp.pf_algo_captcha'