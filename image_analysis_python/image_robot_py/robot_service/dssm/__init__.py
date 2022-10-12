# -*- coding: UTF-8 -*-

from . import client

'''
使用方法: clients[业务线].predict(user_query, topk)
返回: 元组 (stdq_list, score_list), 长度相同, 最多 topk 个
'''
clients = client.init_clients()
