# -*- coding: UTF-8 -*-
import json
import time
from collections import OrderedDict

import tornado.web
import traceback

import robot_service.common.logger as logger
import robot_service.intent as intent
import robot_service.sbert as sbert
from robot_service.common.monitor_tools import api_monitor

business_logger = logger.BusinessLogger()


class MatchHandler(tornado.web.RequestHandler):
    """match handler"""

    @api_monitor('intent')
    def intent_predict(self, biz, query):
        """intent 预测"""
        if biz == "hotel_agent":
            biz = "hotel"
        if biz not in intent.clients:
            raise ValueError('intent 不支持的业务线: %s' % biz)
        stdq, info = intent.clients[biz].get_intent(query)
        print('intent stdq: %s' % stdq)
        if not sbert.clients[biz].stdq_contain(stdq):
            print('intent stdq not exist')
            return None, '不存在标准Q'
        return stdq, info

    @api_monitor('sbert')
    def sbert_predict(self, biz, query, topk):
        """sentence-bert 预测"""
        if biz not in sbert.clients:
            raise ValueError('sbert 不支持的业务线: %s' % biz)
        stdq_list, prob_list = sbert.clients[biz].predict(query, topk=topk)
        return stdq_list, prob_list

    def predict(self, biz, query, strategy, topk, is_search, is_intent):
        """预测逻辑入口"""
        # 先精确搜索
        #if is_search:
        #    search_biz = search_client.search(query.strip(), stype='match')
        #    if search_biz is not None:
        #        return 'search', [query.strip()], [-2.]

        # 然后查找意图
        if is_intent:
            try:
                stdq, info = self.intent_predict(biz, query)
                if stdq is not None:
                    return 'intent', [stdq], [float(-int(info))]
            except ValueError:
                pass

        # 最后用模型匹配
        if strategy == 'sbert':  # 'sbert': sentence-bert
            stdq_list, prob_list = self.sbert_predict(biz, query, topk)
        else:
            raise ValueError('match 不支持的 strategy : %s' % strategy)
        return 'model', stdq_list, prob_list

    def data_received(self, chunk):
        raise NotImplementedError()

    @api_monitor('match')
    def get(self):
        session_id = None
        try:
            # 读取参数
            biz = self.get_argument('bizType', default='hotel', strip=True)
            query = self.get_argument('userMessage', strip=False)
            session_id = self.get_argument('sessionId', strip=True)
            topk = int(self.get_argument('topk', default='10', strip=True))
            strategy = self.get_argument('strategy', default='cnn-bert', strip=True)  # cnn  cnn-bert
            is_search = self.get_argument('is_search', default='true', strip=True) == 'true'
            is_intent = self.get_argument('is_intent', default='true', strip=True) == 'true'
            is_test = self.get_argument('is_test', default='false', strip=True) == 'true'

            #意图用户侧、用户侧生效
            #20200921
            if biz == "hotel":
                is_intent = True
            elif biz == "hotel_agent":
                is_intent = True
            # 构造结果
            start = time.time()
            result = OrderedDict()
            algo, stdq_list, prob_list = self.predict(biz, query, strategy, topk, is_search, is_intent)
            result['status'] = 0
            result['message'] = 'ok'
            result['time_used'] = 0
            result['data'] = OrderedDict()
            if is_test:  # 测试时, 拼上原句
                result['data']['userMessage'] = query
            result['data']['stdq_list'] = stdq_list
            result['data']['prob_list'] = prob_list
            result['data']['algo'] = algo
            time_used = (time.time() - start)
            result['time_used'] = time_used

            self.set_header('Content-Type', 'application/json; charset=UTF-8')
            self.write(json.dumps(result, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))
        except Exception as e:
            error = traceback.format_exc()
            business_logger.error('[MatchHandler] session_id: %s##参数错误或预测异常. 参数: %s##exception: %s' % (
                session_id if session_id is not None else 'null', self.request.body.decode('utf-8'), error))
            raise tornado.web.HTTPError(500, error)

    @api_monitor('match')
    def post(self):
        # 转换参数
        session_id = None
        is_test = False
        if 'application/json' == self.request.headers["Content-Type"]:
            request = tornado.escape.json_decode(self.request.body)
        else:
            request = dict()
            body = tornado.escape.url_unescape(self.request.body, plus=True)
            for item_line in body.split('&'):
                key, value = item_line.split('=', maxsplit=1)
                request[key] = value
        try:
            # 读取参数
            biz = request.get('bizType')
            query = request.get('userMessage')
            session_id = request.get('sessionId')
            topk = int(request.get('topk') if request.get('topk') is not None else '10')
            strategy = request.get('strategy') if request.get('strategy') is not None else 'cnn'  # cnn  cnn-bert
            is_search = self.get_argument('is_search', default='true', strip=True) == 'true'
            is_intent = self.get_argument('is_intent', default='true', strip=True) == 'true'

            if 'is_test' in request:
                is_test = request.get('is_test')[0].decode('utf-8').lower() == 'true'

            #意图用户侧、用户侧生效
            #20200921
            if biz == "hotel":
                is_intent = True
            elif biz == "hotel_agent":
                is_intent = True
            # 构造结果
            start = time.time()
            result = OrderedDict()
            algo, stdq_list, prob_list = self.predict(biz, query, strategy, topk, is_search, is_intent)
            result['status'] = 0
            result['message'] = 'ok'
            result['time_used'] = 0
            result['data'] = OrderedDict()
            if is_test:  # 测试时, 拼上原句
                result['data']['userMessage'] = query
            result['data']['stdq_list'] = stdq_list
            result['data']['prob_list'] = prob_list
            result['data']['algo'] = algo
            time_used = (time.time() - start)
            result['time_used'] = time_used

            self.set_header('Content-Type', 'application/json; charset=UTF-8')
            self.write(json.dumps(result, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))
        except Exception as e:
            business_logger.error('[MatchHandler] session_id: %s##参数错误或预测异常. 参数: %s##exception: %s' % (
                session_id, self.request.body.decode('utf-8'), str(e)))
            raise tornado.web.HTTPError(500, str(e))

    def head(self):
        self.write('ok')
