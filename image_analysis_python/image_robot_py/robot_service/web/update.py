# -*- coding: UTF-8 -*-
import json
import time
from collections import OrderedDict
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime
from typing import Optional, Awaitable

import tornado.web
from tornado.concurrent import run_on_executor

import config
import robot_service.common.logger as logger
import robot_service.update_stdq.crypto_check as crypto_check
import robot_service.update_stdq.update_client as update_client
from robot_service.common.monitor_tools import api_monitor

business_logger = logger.BusinessLogger()


class UpdateHandler(tornado.web.RequestHandler):
    """update handler"""
    executor = ThreadPoolExecutor(1)
    _is_updating = False
    _update_queue = list()

    def __init__(self, *args, **kw):
        super().__init__(*args, **kw)

    @run_on_executor
    @api_monitor('update_all_stdq')
    def update_all_stdq_real(self):
        try:
            # update
            UpdateHandler._is_updating = True
            self.update_stdq_and_embedding()
            if len(UpdateHandler._update_queue) > 0:
                UpdateHandler._update_queue.clear()
                self.update_stdq_and_embedding()
            UpdateHandler._is_updating = False
        except Exception as e:
            business_logger.error('[update_all_stdq_real] error')
            business_logger.error(e)
            raise e
        finally:
            UpdateHandler._is_updating = False


    def update_stdq_and_embedding(self):
        # 拉取标准Q
        start_time = time.time()
        business_logger.info('[update_all_stdq] 开始更新')
        update_client.pull_all_stdq()  # 更新标准Q文件
        all_time_used = time.time() - start_time
        business_logger.info('[update_all_stdq] 拉取所有标准Q完成, time_used: %f. at=%s' % (
            all_time_used, datetime.now().strftime("%Y-%m-%d_%H-%M-%S")))

        # 更新 embedding
        start_time = time.time()
        update_client.reload_stdq_embedding()  # 更新标准Q embedding
        all_time_used = time.time() - start_time
        business_logger.info('[update_all_stdq] standQ embedding finished, time_used: %f. at=%s' % (
            all_time_used, datetime.now().strftime("%Y-%m-%d_%H-%M-%S")))

    @api_monitor('update_all_stdq_api')
    def update_all_stdq(self, msg):

        business_logger.info('[update_all_stdq] 当下的更新状态为 %s' % (UpdateHandler._is_updating))
        if msg != 'all':
            return 'illegal request'
        if not UpdateHandler._is_updating:
            self.update_all_stdq_real()
            return 'called'
        else:
            UpdateHandler._update_queue.append(msg)
            return 'queued'

    def data_received(self, chunk: bytes) -> Optional[Awaitable[None]]:
        raise NotImplementedError()

    @api_monitor('update')
    def get(self):
        try:
            business_logger.info('[get] 收到更新标准q的请求')
            # 读取参数
            update_type = self.get_argument('type', default='all', strip=True)
            msg = self.get_argument('msg', strip=True)
            if update_type == 'all':
                # 构造结果
                start = time.time()
                result = OrderedDict()
                return_msg = self.update_all_stdq(msg)
                result['status'] = 0
                result['message'] = return_msg
                result['time_used'] = 0
                time_used = (time.time() - start)
                result['time_used'] = time_used

                self.set_header('Content-Type', 'application/json; charset=UTF-8')
                self.write(json.dumps(result, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))
            else:
                business_logger.error('[UpdateHandler] 不支持的 update_type=%s, request.body=%s' % (
                    update_type, self.request.body.decode('utf-8')))
                raise tornado.web.HTTPError(500, '不支持的 update_type=%s' % update_type)
        except Exception as e:
            business_logger.error('[UpdateHandler] request.body=%s, error=%s' % (
                self.request.body.decode('utf-8'), str(e)))
            raise tornado.web.HTTPError(500, str(e))

    @api_monitor('update')
    def post(self):
        business_logger.info('[post] 收到更新标准q的请')
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
            update_type = request.ge('type')
            msg = request.get('msg')
            if update_type == 'all':
                # 构造结果
                start = time.time()
                result = OrderedDict()
                return_msg = self.update_all_stdq(msg)
                result['status'] = 0
                result['message'] = return_msg
                result['time_used'] = 0
                time_used = (time.time() - start)
                result['time_used'] = time_used

                self.set_header('Content-Type', 'application/json; charset=UTF-8')
                self.write(json.dumps(result, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))

            else:
                business_logger.error('[UpdateHandler] 不支持的 update_type=%s, request.body=%s' % (
                    update_type, self.request.body.decode('utf-8')))
                raise tornado.web.HTTPError(500, '不支持的 update_type=%s' % update_type)
        except Exception as e:
            business_logger.error('[UpdateHandler] request.body=%s, error=%s' % (
                self.request.body.decode('utf-8'), str(e)))
            raise tornado.web.HTTPError(500, str(e))


class ApiKeyCheckHandler(tornado.web.RequestHandler):
    """apkKeyCheck handler, 仅用于测试"""

    def data_received(self, chunk: bytes) -> Optional[Awaitable[None]]:
        raise NotImplementedError()

    def get(self):
        try:
            # 读取参数
            msg = self.get_argument('msg', strip=True)

            start = time.time()
            result = OrderedDict()
            decrypted_text = crypto_check.rsa_decode(msg, config.update_api_private_key)
            result['status'] = 0
            result['message'] = decrypted_text
            result['time_used'] = 0
            time_used = (time.time() - start)
            result['time_used'] = time_used

            self.set_header('Content-Type', 'application/json; charset=UTF-8')
            self.write(json.dumps(result, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))
        except Exception as e:
            business_logger.error('[UpdateHandler] request.body=%s, error=%s' % (
                self.request.body.decode('utf-8'), str(e)))
            raise tornado.web.HTTPError(500, str(e))

    @api_monitor('update')
    def post(self):
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
            msg = self.get_argument('msg', strip=True)

            start = time.time()
            result = OrderedDict()
            decrypted_text = crypto_check.rsa_decode(msg, config.update_api_private_key)
            result['status'] = 0
            result['message'] = decrypted_text
            result['time_used'] = 0
            time_used = (time.time() - start)
            result['time_used'] = time_used

            self.set_header('Content-Type', 'application/json; charset=UTF-8')
            self.write(json.dumps(result, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))
        except Exception as e:
            business_logger.error('[UpdateHandler] request.body=%s, error=%s' % (
                self.request.body.decode('utf-8'), str(e)))
            raise tornado.web.HTTPError(500, str(e))
