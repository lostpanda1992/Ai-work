#! coding=utf-8

import json
import time
from collections import OrderedDict

import tornado.web
import traceback

import robot_service.common.logger as logger
import robot_service.init_recom as init_recom
from robot_service.common.monitor_tools import api_monitor

business_logger = logger.BusinessLogger()


class InitRecomHandler(tornado.web.RequestHandler):
    """InitRecomHandler"""

    def predict(self, biz, session_id, feature, top_k, stdq_mapping = 1, m_version=0):
        quest_list, prob_list = [], []
        if m_version == 0:
            if biz not in init_recom.clients:
                raise ValueError('init recom model : {} 当前不支持业务线: {}'.format(m_version, biz))
            quest_list, prob_list = init_recom.clients[biz].get_top_q(session_id, feature, top_k, stdq_mapping)
        elif m_version == 1:
            if biz not in init_recom.clients_1:
                raise ValueError("init recom model : {} 当前不支持业务线: {}".format(m_version, biz))
            quest_list, prob_list = init_recom.clients_1[biz].get_top_q(session_id, feature, top_k, stdq_mapping)
        prob_list = [float(x) for x in prob_list]
        return quest_list, prob_list

    @api_monitor('init_recom')
    def get(self):
        session_id = None
        try:
            # 读取参数
            biz = self.get_argument('bizType', default='hotel', strip=True)
            session_id = self.get_argument('sessionId', strip=True)
            top_k = int(self.get_argument('topk', default='10', strip=True))
            feature = self.get_argument('feature', default='[]', strip=True)
            feature = json.loads(feature, encoding='utf-8')
            stdq_mapping = int(self.get_argument('stdq_mapping', default='1', strip=True))
            m_version = int(self.get_argument("m_version", default='0', strip=True))

            # predict
            start = time.time()

            stdq_list, prob_list = self.predict(biz, session_id, feature, top_k, stdq_mapping, m_version)

            result = OrderedDict()
            result['status'] = 0
            result['message'] = 'ok'
            result['time_used'] = 0
            result['data'] = OrderedDict()
            result['data']['stdq_list'] = stdq_list
            result['data']['prob_list'] = prob_list
            time_used = (time.time() - start)
            result['time_used'] = time_used

            self.set_header('Content-Type', 'application/json; charset=UTF-8')
            self.write(json.dumps(result, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))
        except Exception as e:
            error = traceback.format_exc()
            business_logger.error('[InitRecomHandler] session_id: {}##参数错误或预测异常. 参数: {}##exception: {}'.format(
                session_id if session_id is not None else 'null', self.request.body.decode('utf-8'), error))
            raise tornado.web.HTTPError(500, error)

    @api_monitor('init_recom')
    def post(self):
        # 转换参数
        session_id = None
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
            session_id = request.get('sessionId')
            top_k = int(request.get('topk') if request.get('topk') is not None else '10')
            feature = request.get('feature')
            stdq_mapping = int(request.get('stdq_mapping') if request.get('stdq_mapping') is not None else '1')
            m_version = int(request.get('m_version') if request.get("m_version") is not None else '0')
            if feature is None or len(feature) == 0:
                raise ValueError('[InitRecomHandler] session_id: {}##特征参数丢失，预测失败. 参数: {}'.format(
                    session_id if session_id is not None else 'null', request))

            # 构造结果
            start = time.time()

            stdq_list, prob_list = self.predict(biz, session_id, feature, top_k, stdq_mapping, m_version)

            result = OrderedDict()
            result['status'] = 0
            result['message'] = 'ok'
            result['time_used'] = 0
            result['data'] = OrderedDict()
            result['data']['stdq_list'] = stdq_list
            result['data']['prob_list'] = prob_list
            time_used = (time.time() - start)
            result['time_used'] = time_used

            self.set_header('Content-Type', 'application/json; charset=UTF-8')
            self.write(json.dumps(result, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))
        except Exception as e:
            business_logger.error('[InitRecomHandler] session_id: %s##参数错误或预测异常. 参数: %s##exception: %s' % (
                session_id, self.request.body.decode('utf-8'), str(e)))
            raise tornado.web.HTTPError(500, str(e))
