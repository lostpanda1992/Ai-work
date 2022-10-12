# -*- coding: UTF-8 -*-
from robot_service.sbert.client import SbertQAClient
import numpy as np

if __name__ == '__main__':
    model_path = '/home/zhangyu/PycharmProjects/h_algo_robot_py/models/sbert/hotel/bert-base-qrobot-mean-tokens-2020-03-20_17-11-45/'
    stdq_path = ''
    str = '怎么退款呢啊'
    client = SbertQAClient(model_path, stdq_path)
    embedding = client.embedding_str(str)
    print(np.array(embedding).shape)
    print(embedding)