# -*- coding: utf-8 -*-

import logging
import os
import yaml
import robot_service.common.db_namespace_agent as db

###############################################################################
# directory setting
project_dir = os.path.split(os.path.realpath(__file__))[0]
logs_dir = os.path.join(project_dir, 'logs')
with open(os.path.join(project_dir, 'models_config.yaml'), 'r') as fin:
    models_config = yaml.load(fin, Loader=yaml.SafeLoader)

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
# mysql db setting
stdq_db_agent = db.PyMysqlPXC(
    namespace='tc_801_pxc',
    user_name='h_algo_robot_r', password='AT6d8zenqb30NoZW',
    dbname='order_complain',
    conn_type='read', env='prod')

###############################################################################
# api key setting
with open(os.path.join(project_dir, 'apiKey', 'update_private_key'), 'r') as fin:
    update_api_private_key = fin.read()
