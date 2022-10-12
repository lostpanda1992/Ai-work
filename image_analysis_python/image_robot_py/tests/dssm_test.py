# -*- coding: UTF-8 -*-
from robot_service.dssm.client import DssmClient
import numpy as np
from robot_service.dssm import dssm_model

if __name__ == '__main__':
    model_path = '/home/zhangyu/PycharmProjects/h_algo_robot_py/models/dssm/hotel/'
    str = '怎么退款呢啊'
    conf = dssm_model.Config(
        vocab_path='/home/zhangyu/PycharmProjects/h_algo_robot_py/models/dssm/hotel/vocab.txt',
        stdq_path='')
    client = DssmClient(model_path, conf)
    embedding = client.get_embedding(str)
    print(np.array(embedding).shape)
    print(embedding)