# -*- coding: UTF-8 -*-

import os
import time
from datetime import datetime

import robot_service.common.logger as logger
import robot_service.update_stdq.update_client as update_client

# 使用 GPU
os.environ['CUDA_DEVICE_ORDER'] = 'PCI_BUS_ID'
os.environ['CUDA_VISIBLE_DEVICES'] = ''

business_logger = logger.BusinessLogger()


def pull_all_stdq_when_start():
    # 拉取所有标准Q
    start_time = time.time()
    update_client.pull_all_stdq()
    all_time_used = time.time() - start_time
    business_logger.info('[start_server] 拉取所有标准Q完成, time_used: %f. at=%s' % (
        all_time_used, datetime.now().strftime("%Y-%m-%d_%H-%M-%S")))



