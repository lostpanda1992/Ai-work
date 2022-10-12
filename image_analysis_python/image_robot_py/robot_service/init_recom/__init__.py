#! coding=utf-8

from . import client
from . import client_1

'''
使用方法: clients[业务线].predict([(query,stdq),(query,stdq),...]])
返回: 1 维 ndarray, 表示对应 query 使用对应 stdq 回复的合适程度
'''
clients = client.init_clients()

# 新模型上线时需要确保新模型和老模型同时能提供服务
# 新模型上线之后，需要注释掉老模型，否则老模型还会载入，占用内存资源
clients_1 = client_1.init_clients()
