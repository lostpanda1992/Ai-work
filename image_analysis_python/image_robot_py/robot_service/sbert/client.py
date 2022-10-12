# -*- coding: UTF-8 -*-

from typing import List, Tuple
import time

import numpy as np
import torch
from sentence_transformers import SentenceTransformer, losses

import config
from robot_service.common import logger

logger = logger.BusinessLogger()


class SbertQAClient:
    """bert-triplet的预测客户端"""
    _shared_model = dict()

    def __init__(self, model_path: str, stdq_path: str):
        self.model_path = model_path
        self.stdq_path = stdq_path

        # 加载模型
        self.load_model()

        # 距离计算函数
        triplet_loss = losses.TripletLoss(model = self.model, distance_metric = losses.TripletDistanceMetric.COSINE)
        self.distance_func = triplet_loss.distance_metric

        # 加载标准问题
        self.embedding_stdq()

    def load_model(self):
        # 加载模型
        if self.model_path not in SbertQAClient._shared_model:
            self.model = SentenceTransformer(self.model_path)
            SbertQAClient._shared_model[self.model_path] = self.model
        else:
            self.model = SbertQAClient._shared_model[self.model_path]

    def embedding_stdq(self):
        # 加载标准问题, 并计算 embedding
        with open(self.stdq_path, 'r', encoding='utf-8') as fin:
            self.stdq_list = [line.strip() for line in fin]
        stdq_embeddings = self.model.encode(self.stdq_list)
        self.stdq_tensor = torch.from_numpy(np.array(stdq_embeddings))  # shape: (n,768)

    def stdq_contain(self, stdq):
        return stdq in self.stdq_list

    @staticmethod
    def clear_shared_model():
        SbertQAClient._shared_model = dict()

    def predict(self, query: str, topk: int) -> Tuple[List[str], List[float]]:
        """对一句输入 query 计算 topk  stqd"""
        start = time.time()
        query_embedding = self.model.encode([query])
        time_used = time.time() - start
        logger.info('sbert get_embedding time_used: %.6f' % time_used)

        query_tensor = torch.from_numpy(np.array(query_embedding))  # shape: (768)
        distance_tensor = self.distance_func(query_tensor, self.stdq_tensor)

        distance_np = distance_tensor.numpy()
        dis_idx = np.argsort(distance_np)

        result_list = []
        score_list = []
        for no in range(min(topk, len(dis_idx))):
            idx = dis_idx[no]
            result_list.append(self.stdq_list[idx])
            score_list.append(float(distance_np[idx]))
        return result_list, score_list


def init_clients():
    '''
    初始化各业务模型客户端
    :return:
    '''
    SbertQAClient.clear_shared_model()  # 清除共享模型

    common_data = config.models_config['data']
    sbert_config = config.models_config['sbert']
    clients = {}
    for business in sbert_config.keys():
        model_path = sbert_config[business]['model_path']
        stdq_path = common_data[business]['match_stdq']
        client = SbertQAClient(model_path, stdq_path)

        clients[business] = client
        logger.info('sbert 模型初始化成功: biz=%s' % business)
    return clients


if __name__ == '__main__':
    # 测试
    client = init_clients()['hotel']

    topk = 10
    query = '怎么退房啊'
    result_list, prob_list = client.predict(query, topk)
    for stdq, prob in zip(result_list, prob_list):
        print(stdq, prob)
