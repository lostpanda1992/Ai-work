# -*- coding: utf-8 -*-

import logging
import os

#################################################################################
# oss download
weights_fold = 'data/models'
weight_oss_url="http://pf-algo-image-analysis-gpu.oss.corp.xxxxxx.com/pf_algo_image_analysis_gpu_word_joint_model_001/20211228.tar.gz"

def get_date_fold_from_ossurl():
  oss_url_items = weight_oss_url.split("/")
  date_file = oss_url_items[-1]
  date_file_items = date_file.split(".")
  date_fold = date_file_items[0]
  return date_fold

def get_model_dir_from_src(src_path):
  '''
  :return: /home/q/www/pf_algo_image_analysis_py/data/models/20211115/src_path
  '''
  weight_dir = os.path.join(os.getcwd(), weights_fold)
  if weight_dir[-1] != "/":
    weight_dir = weight_dir + "/"

  return weight_dir + get_date_fold_from_ossurl() + "/" + src_path

###############################################################################
# directory setting
project_dir = os.path.split(os.path.realpath(__file__))[0]
logs_dir = os.path.join(project_dir, 'logs')


###############################################################################
# logging setting
word_cut_log_file = os.path.join(logs_dir, 'business.log')
logging_level = logging.INFO
log_file_max_size = 100 * 1024 * 1024
log_file_max_count = 100



###############################################################################
# service setting
web_server_port = 8080


###############################################################################
# watcher 指标前缀
WATCHER_PREFIX = 's.corp.pf_algo_image_analysis_gpu'



#################################################################################
# 头图压字和图片拼接asl模型配置
image_word_and_joint_model_path = get_model_dir_from_src('model-highest-25.ckpt')



#################################################################################
# 日志文件地址
logs_file_path = os.path.join(logs_dir, 'run.log')
logs_file_path_collect = os.path.join(logs_dir, 'business.log')
