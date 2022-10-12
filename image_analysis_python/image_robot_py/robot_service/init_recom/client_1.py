#! coding=utf-8

import time
import os
import numpy as np

from .model import XgbInitRecomModel
from robot_service.common import logger

business_logger = logger.BusinessLogger()

local_path = os.path.dirname(os.path.realpath(__file__))


class XgbInitRecom1(XgbInitRecomModel):
    """
    xgboost model predict topN standard questions for init_recommend
    模型替换过程中，需要同时提供新模型和老模型服务
    """

    def __init__(self, model_file, cid_bid_file, feature_dim=15):
        XgbInitRecomModel.__init__(self, model_file, cid_bid_file, feature_dim)

    def get_top_q(self, session_id, feature, top_k, stdq_mapping = 1):
        '''
        输入的feature是一个list, 每个元素为一个特征值
        字段std_mapping在此模型中无用，但是保留
        '''
        prob_list = []
        quest_list = []
        if len(feature) != self.feature_dim:
            # 输入特征有问题时，默认输入特征全为0
            feature = [0] * self.feature_dim
            business_logger.error("[XgbInitRecom1] ### 输入特征维度错误, session_id={}, feature={}".format(session_id, feature))

        start = time.time()
        pred = self.predict([feature])
        time_used = time.time() - start
        business_logger.info('init_recom_1 predict time_used: %.6f' % time_used)

        top_k = max(0, top_k)
        top_k = min(top_k, pred.shape[1])
        top_ids = np.argsort(-pred)[0, :top_k]
        for idx in list(top_ids):
            prob = pred[0, idx]
            quest = self.clsid_to_bid[idx]
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
        model_path = os.path.join(os.getcwd(), 'models/init_recom/hotel/init_recomm_cls-60.model')
        bid_quest_path = os.path.join(os.getcwd(), 'models/init_recom/hotel/cid_bid.dic')
        xgb_init_recom = XgbInitRecom1(model_path, bid_quest_path)
        xgb_init_recom.load_weights()

        clients[biz] = xgb_init_recom
        business_logger.info('init_recom_1 模型初始化成功: biz=%s' % biz)
    return clients

