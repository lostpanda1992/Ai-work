# -*- coding: UTF-8 -*-

import requests
import json
import sys

# base_url = 'http://127.0.0.1:9011/'  # local
# base_url = 'http://l-noah65yuzfaaq1.auto.beta.cn0:9011/'  # beta2: wcf2-robot-py
base_url = 'http://l-noah6tuc3n6xo1.auto.beta.cn0:9011/'  # beta: wcf-robot-py

path_list = [
    # init
    'halgo/robot/initRecom?bizType=hotel&feature=[0,10,9,38,0,-999,-999,0,0,2,1,-999,-999,2,2]&sessionId=s123&topk=10',

    # guess
    'halgo/robot/guess?bizType=hotel&userMessage=付款&sessionId=123&strategy=dssm&topk=10',
    'halgo/robot/guess?bizType=flight&userMessage=付款&sessionId=123&strategy=dssm&topk=10',
    'halgo/robot/guess?bizType=train&userMessage=付款&sessionId=123&strategy=dssm&topk=10',

    # match
    'halgo/robot/match?bizType=hotel&userMessage=在线客服&sessionId=123&strategy=cnn&topk=10',
    'halgo/robot/match?bizType=hotel&userMessage=在线客服&sessionId=123&strategy=sbert&topk=10',
    'halgo/robot/match?bizType=travel&userMessage=在线客服&sessionId=123&strategy=sbert&topk=10',
    'halgo/robot/match?bizType=ticket&userMessage=在线客服&sessionId=123&strategy=sbert&topk=10',
    'halgo/robot/match?bizType=common&userMessage=在线客服&sessionId=123&strategy=sbert&topk=10',
    # match 其他算法接口, 线上不用的
    'halgo/robot/match?bizType=hotel&userMessage=在线客服&sessionId=123&strategy=cnn-cubert&topk=10',
    'halgo/robot/match?bizType=hotel&userMessage=在线客服&sessionId=123&strategy=cnn-bert&topk=10',
    # 'halgo/robot/match?bizType=hotel&userMessage=在线客服&sessionId=123&strategy=cnn-albert&topk=10',
    'halgo/robot/match?bizType=hotel&userMessage=在线客服&sessionId=123&strategy=cnn-onnx&topk=10',

]

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0',
    'Accept': 'application/json',
}

for idx in range(len(path_list)):
    url = base_url + path_list[idx]
    response = requests.get(url, headers=headers)
    if response.status_code != 200:
        print('http错误, url=%s' % url, file=sys.stderr)
    else:
        result = json.loads(response.content.decode('utf-8'))
        print(url)
        print(json.dumps(result, ensure_ascii=False, indent='    ', separators=(',', ': ')))
    print('\n\n')
