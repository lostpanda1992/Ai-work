# -*-coding:utf-8-*-

import os
import time
import yaml
from robot_service.common import logger
from robot_service.intent import intent

logger = logger.BusinessLogger()

local_path = os.path.dirname(os.path.realpath(__file__))


class IntentlClient:
    '''
    意图识别
    '''

    def __init__(self, business, param):
        self.business = business
        self.param = param

        dic_file = os.path.join(local_path, param['dic_file'])
        fat_file = os.path.join(local_path, param['pat_file'])
        stdq_file = os.path.join(local_path, param['stdq_file'])
        self.intent = intent.intent(dic_file, fat_file, stdq_file)

    def get_intent(self, sentence):
        pats = self.intent.cut(sentence)
        # print(pats)
        result, info = self.intent.matchPat(pats)
        # print(result, info)
        return result, info


def init_clients():
    '''
    初始化各业务模型客户端
    :return:
    '''
    clients = {}
    params = yaml.load(open(os.path.join(local_path, 'params.yaml'), 'r'), Loader=yaml.SafeLoader)
    for business in params.keys():
        clients[business] = IntentlClient(business, params[business])
        logger.info("意图识别模型初始化成功: %s" % (business))
    return clients
