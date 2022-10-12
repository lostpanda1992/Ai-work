# -*- coding: UTF-8 -*-
import json
import time
from collections import OrderedDict
import statsd

import tornado.web
import traceback
import base64
import numpy as np
import cv2

import config
import captcha_service.common.logger as logger
from captcha_models.yolov5.yolov5_client import yolov5
from captcha_models.cnn_captcha.cnn_captcha_client import cnn_captcha_predict


business_logger = logger.BusinessLogger()
moniter = statsd.StatsClient('statsd-corp.corp.xxxxxx.com', 8125, prefix=config.WATCHER_PREFIX)


def judge_scale(img):
    # 根据图片尺寸大小过滤区分大小尺寸
    flag = False
    height = img.shape[0]
    width = img.shape[1]
    if height == 40 and width == 100:
        flag = True
    return flag


class TextCaptchaHandler(tornado.web.RequestHandler):
    """text captcha handler"""


    def base64_2_img(self, img_base64):
        img_data = base64.b64decode(img_base64)
        # 转换为np数组
        img_array = np.fromstring(img_data, np.uint8)
        img = cv2.imdecode(img_array, cv2.COLOR_RGB2BGR)
        return img

    def threshold(self, pred_text_result, pred_text_img_list):
        # 直接判定检测框长宽比：长度（x轴方向长度）/宽度（y轴方向长度）大于1.5 则该位置字符为“一”
        # 直接判定检测框宽长比：宽度（y轴方向长度）/长度（x轴方向长度）大于3.5 则该位置不存在字符
        img_text = list(pred_text_result)
        for k in range(len(img_text) - 1, -1, -1):
            x_length = pred_text_img_list[k].shape[1]
            y_length = pred_text_img_list[k].shape[0]
            if x_length / y_length > 1.5:
                img_text[k] = "一"
            if y_length / x_length > 3.5:
                img_text.pop(k)
        pred_text_result = "".join(img_text)
        return pred_text_result

    def predict(self, img, app_code):
        """预测逻辑入口"""
        flag = judge_scale(img)
        pred_text_img_list = yolov5.detect(img, flag, app_code)
        pred_text_result = ''
        for text_img in pred_text_img_list:
            pred_text = cnn_captcha_predict(text_img, flag, app_code)
            pred_text_result += pred_text

        if flag:
            pred_text_result = self.threshold(pred_text_result, pred_text_img_list)
        return pred_text_result

    def process(self, img_base64, app_code):
        img = self.base64_2_img(img_base64)
        pred_text_result = self.predict(img, app_code)
        return pred_text_result


    def get(self):
        start_time = time.time()
        # watcher total统计
        moniter.incr(config.WATCHER_PREFIX + '.TEXT_CAPTCHA_TOTAL')

        app_code = ''
        try:
            # 读取参数
            img_base64 = self.get_argument('img_base64', default='', strip=True)
            app_code = self.get_argument('app_code', strip=False)
            pred_text_result = self.process(img_base64, app_code=app_code)

            business_logger.info("文本验证码任务的入口, appcode为：{}".format(app_code))

            # app_code 统计
            moniter.incr(config.WATCHER_PREFIX + '.TEXT_CAPTCHA' + '.APPCODE_' + app_code.upper())

            # 构造结果
            result = OrderedDict()
            if pred_text_result != '':
                result['status'] = 0
            else:
                result['status'] = 1

            result['pred_text'] = pred_text_result

            self.set_header('Content-Type', 'application/json; charset=UTF-8')
            self.write(json.dumps(result, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))

            # app_code 整体响应时间
            end_time = time.time()
            moniter.timing(config.WATCHER_PREFIX + '.TEXT_CAPTCHA' + '.APPCODE_' + app_code.upper() + ".TIME", end_time - start_time)

            business_logger.info("文本验证码任务最后返回文字结果为：{}, 响应时间为：{}ms".format(result['pred_text'], (end_time - start_time) * 1000))

            # app_code成功量
            moniter.incr(config.WATCHER_PREFIX + '.TEXT_CAPTCHA' + '.APPCODE_' + app_code.upper() + ".SUCCESS")
        except Exception as e:
            error = traceback.format_exc()
            business_logger.error('[TextCaptchaHandler] get method 参数错误或预测异常.appcode:%s,  参数: %s##exception: %s' %
                                  (app_code, self.request.body.decode('utf-8'), error))
            raise tornado.web.HTTPError(500, error)

    def post(self):
        start_time = time.time()
        # watcher total统计
        moniter.incr(config.WATCHER_PREFIX + '.TEXT_CAPTCHA_TOTAL')

        app_code = ''
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
            img_base64 = request.get('img_base64')
            app_code = request.get('app_code')

            business_logger.info("文本验证码任务的入口, appcode为：{}".format(app_code))

            # app_code 统计
            moniter.incr(config.WATCHER_PREFIX + '.TEXT_CAPTCHA' + '.APPCODE_' + app_code.upper())

            pred_text_result = self.process(img_base64, app_code)

            # 构造结果
            result = OrderedDict()
            if pred_text_result != '':
                result['status'] = 0
            else:
                result['status'] = 1

            result['pred_text'] = pred_text_result

            # 加日志 最终返回结果
            self.set_header('Content-Type', 'application/json; charset=UTF-8')
            self.write(json.dumps(result, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))

            # app_code 整体响应时间
            end_time = time.time()
            moniter.timing(config.WATCHER_PREFIX + '.TEXT_CAPTCHA' + '.APPCODE_' + app_code.upper() + ".TIME", end_time - start_time)

            business_logger.info("文本验证码任务最后返回文字结果为：{}, 响应时间为：{}ms".format(result['pred_text'], (end_time - start_time) * 1000))

            # app_code成功量
            moniter.incr(config.WATCHER_PREFIX + '.TEXT_CAPTCHA' + '.APPCODE_' + app_code.upper() + ".SUCCESS")
        except Exception as e:
            # app_code失败量
            moniter.incr(config.WATCHER_PREFIX + '.TEXT_CAPTCHA' + '.APPCODE_' + app_code.upper() + ".FAULT")

            error = traceback.format_exc()
            business_logger.error('[TextCaptchaHandler] post method 参数错误或预测异常.appcode:%s,  参数: %s## error:s% exception: %s' %
                                  (app_code, error, self.request.body.decode('utf-8'), str(e)))
            raise tornado.web.HTTPError(500, str(e))






if __name__ == '__main__':
    import time
    import cv2

    start_time = time.time()
    img_path = ''
    img = cv2.imread(img_path)
    test = TextCaptchaHandler()
    pred_text_result = test.process('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',app_code="111")

    print('=' * 100)
    print('first time used: ', time.time() - start_time)

    result = cnn_captcha_predict(img, flag=True, app_code="111")
    print(result)

    start_time = time.time()
    print('=' * 100)
    print('second time used: ', time.time() - start_time)