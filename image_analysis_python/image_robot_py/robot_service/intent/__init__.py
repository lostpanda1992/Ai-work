from . import client

'''
使用方法: clients[业务线].get_intent(user_query)
返回: 元组 (result, info),如果匹配不到意图,result返回None；成功返回意图,模板的score
'''
clients = client.init_clients()