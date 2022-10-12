# -*- coding: UTF-8 -*-

import requests
import json
import sys

# base_url = 'http://127.0.0.1:9012/'  # local
base_url = 'http://l-noah6iqmuw1qf1.auto.beta.cn0:9012/'  # beta: wcf-robot-j

path_list = [
    # match
    'dubbo/match?sessionId=s123&msgId=123&query=怎么退票&userId=wcftest&biz=hotel',

    # guess
    'dubbo/guess?sessionId=s123&msgId=123&query=怎么退票&userId=wcftest&biz=hotel',
    
    # init
    'dubbo/init?sessionId=s123&msgId=123&orderNo=102211001035&userId=wcftest&biz=hotel',

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
