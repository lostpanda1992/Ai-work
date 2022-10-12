#! coding=utf-8

import time
import os
import numpy as np
import xgboost as xgb

from robot_service.common import logger

business_logger = logger.BusinessLogger()

local_path = os.path.dirname(os.path.realpath(__file__))


class XgbInitRecom(object):
    """xgboost model predict topN standard questions for init_recommend"""

    def __init__(self, model_file, std_quest_file, id_quest_file, feature_dim=15):
        self.model_file = model_file
        self.feature_dim = feature_dim
        self.xgb_model = xgb.Booster()
        self.class_id_to_name = self.load_class_id_to_name(std_quest_file)
        self.class_id_to_stdqid = self.load_class_id_to_name(id_quest_file)

    def load_class_id_to_name(self, std_q_file):
        class_id_to_name = {}
        with open(std_q_file, 'r') as f:
            for line in f:
                cls_id, cls_name = line.splitlines()[0].split('###')
                class_id_to_name[int(cls_id)] = cls_name
        return class_id_to_name

    def load_weights(self):
        self.xgb_model.load_model(self.model_file)

    def predict(self, feature):
        feature = xgb.DMatrix(np.array(feature))
        pred = self.xgb_model.predict(feature)
        return pred

    def get_top_q(self, session_id, feature, top_k, stdq_mapping = 1):
        '''
        输入的feature是一个list, 每个元素为一个特征值
        '''
        prob_list = []
        quest_list = []
        if len(feature) != self.feature_dim:
            # 输入特征有问题时，默认输入特征全为0
            feature = [0] * self.feature_dim
            business_logger.error("[XgbInitRecom] ### 输入特征维度错误, session_id={}, feature={}".format(session_id, feature))

        start = time.time()
        pred = self.predict([feature])
        time_used = time.time() - start
        business_logger.info('init_recom predict time_used: %.6f' % time_used)

        top_k = max(0, top_k)
        top_k = min(top_k, pred.shape[1])
        top_ids = np.argsort(-pred)[0, :top_k]
        for idx in list(top_ids):
            prob = pred[0, idx]
            if stdq_mapping == 0:
                quest = self.class_id_to_stdqid[idx]
                quest_list.append(quest)
            else:
                quest = self.class_id_to_name[idx]
                quest_list.append(quest)
            prob_list.append(prob)

        return quest_list, prob_list


def init_clients():
    '''
    初始化各业务模型客户端
    :return:
    '''
    clients = {}
    for biz in ['hotel']:
        # todo 暂时写死的路径 pwd 为本项目根目录
        model_path = os.path.join(os.getcwd(), 'models/init_recom/hotel/init_recomm_cls-253.model')
        std_quest_path = os.path.join(os.getcwd(), 'models/init_recom/hotel/std_q.dic')
        id_quest_file = os.path.join(os.getcwd(), 'models/init_recom/hotel/id_q.dic')
        xgb_init_recom = XgbInitRecom(model_path, std_quest_path, id_quest_file)
        xgb_init_recom.load_weights()

        clients[biz] = xgb_init_recom
        business_logger.info('init_recom 模型初始化成功: biz=%s' % biz)
    return clients

