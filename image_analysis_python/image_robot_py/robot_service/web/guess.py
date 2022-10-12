# -*- coding: UTF-8 -*-
import json
import time
from collections import OrderedDict

import tornado.web
import traceback
import robot_service.common.logger as logger
import robot_service.dssm as dssm
from robot_service.common.monitor_tools import api_monitor

business_logger = logger.BusinessLogger()


class GuessHandler(tornado.web.RequestHandler):
    """guess handler"""

    @api_monitor('guess')
    def dssm_predict(self, biz, query, topk):
        """DSSM 预测"""
        if biz not in dssm.clients:
            raise ValueError('dssm 不支持的业务线: %s' % biz)
        stdq_list, prob_list = dssm.clients[biz].predict(query, topk=topk)
        prob_list = [float(prob) for prob in prob_list]
        return stdq_list, prob_list

    def predict(self, biz, query, strategy, topk):
        """预测逻辑入口"""
        if strategy == 'dssm':  # 'cnn': cnn
            stdq_list, prob_list = self.dssm_predict(biz, query, topk)
        else:
            raise ValueError('guess 不支持的 strategy : %s' % strategy)
        return stdq_list, prob_list

    def data_received(self, chunk):
        raise NotImplementedError()

    @api_monitor('guess')
    def get(self):
        session_id = None
        try:
            # 读取参数
            biz = self.get_argument('bizType', default='hotel', strip=True)
            query = self.get_argument('userMessage', strip=False)
            session_id = self.get_argument('sessionId', strip=True)
            topk = int(self.get_argument('topk', default='3', strip=True))
            strategy = self.get_argument('strategy', default='dssm', strip=True)  # dssm
            is_test = self.get_argument('is_test', default='false', strip=True) == 'true'

            # 构造结果
            start = time.time()
            result = OrderedDict()
            stdq_list, prob_list = self.predict(biz, query, strategy, topk)
            result['status'] = 0
            result['message'] = 'ok'
            result['time_used'] = 0
            result['data'] = OrderedDict()
            if is_test:  # 测试时, 拼上原句
                result['data']['userMessage'] = query
            result['data']['stdq_list'] = stdq_list
            result['data']['prob_list'] = prob_list
            time_used = (time.time() - start)
            result['time_used'] = time_used

            self.set_header('Content-Type', 'application/json; charset=UTF-8')
            self.write(json.dumps(result, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))
        except Exception as e:
            error = traceback.format_exc()
            business_logger.error('[GuessHandler] session_id: %s##参数错误或预测异常. 参数: %s##exception: %s' % (
                session_id if session_id is not None else 'null', self.request.body.decode('utf-8'), error))
            raise tornado.web.HTTPError(500, error)

    @api_monitor('guess')
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
            topk = int(request.get('topk') if request.get('topk') is not None else '3')
            strategy = request.get('strategy') if request.get('strategy') is not None else 'dssm'  # dssm

            if 'is_test' in request:
                is_test = request.get('is_test')[0].decode('utf-8').lower() == 'true'

            # 构造结果
            start = time.time()
            result = OrderedDict()
            stdq_list, prob_list = self.predict(biz, query, strategy, topk)
            result['status'] = 0
            result['message'] = 'ok'
            result['time_used'] = 0
            result['data'] = OrderedDict()
            if is_test:  # 测试时, 拼上原句
                result['data']['userMessage'] = query
            result['data']['stdq_list'] = stdq_list
            result['data']['prob_list'] = prob_list
            time_used = (time.time() - start)
            result['time_used'] = time_used

            self.set_header('Content-Type', 'application/json; charset=UTF-8')
            self.write(json.dumps(result, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))
        except Exception as e:
            business_logger.error('[GuessHandler] session_id: %s##参数错误或预测异常. 参数: %s##exception: %s' % (
                session_id, self.request.body.decode('utf-8'), str(e)))
            raise tornado.web.HTTPError(500, str(e))

    def head(self):
        self.write('ok')
