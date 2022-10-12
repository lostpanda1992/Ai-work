# -*- coding: UTF-8 -*-
import json
import time
import statsd
import tornado.web
import traceback
import numpy as np
import cv2
import base64
import config
from image_analysis_gpu_service import common
from urllib import request
from PIL import Image
import io
from concurrent.futures import ThreadPoolExecutor
from tornado import gen
from tornado.concurrent import run_on_executor
from image_models.asl_client import model_asl

business_logger = common.BusinessLogger()
moniter = statsd.StatsClient('statsd-corp.corp.xxxxxx.com', 8125, prefix=config.WATCHER_PREFIX)


class ImageWordJointHandler(tornado.web.RequestHandler):
    """
    图片标签自动化审核：图片压字和图片拼接
    """

    executor = ThreadPoolExecutor(5)

    def gen_label_result(self, service_name='image_word_and_joint', original_id="default", image_id="default", predict_result_list=[], predict_status=0):
        result = {
            "service_name": service_name,  # 服务名
            "original_id": original_id,  # 帖子id
            "image_id": image_id,  # 图片id
            "predict_result_list": predict_result_list,  # 预测结果 例：["图片压字","图片拼接"]
            "predict_status": predict_status,  # 0:返回正常, 1:返回异常
        }
        return result

    def base64_2_img(self, img_base64):
        img_data = base64.b64decode(img_base64)
        # 转换为np数组
        img_array = np.fromstring(img_data, np.uint8)
        img = cv2.imdecode(img_array, cv2.COLOR_RGB2BGR)
        return img

    def download_image(self, url):
        req = request.Request(url)
        time_out = 3.0
        retry_num = 3
        retry = 0
        is_downlod = False
        while retry <= retry_num and not is_downlod:
            try:
                resp = request.urlopen(req, timeout=time_out)
                is_downlod = True
            except Exception as e:
                retry += 1
                error = traceback.format_exc()
                business_logger.error("[watermark_detect]***图片下载超时, original_url={}, ERROR={}".format(url, error))
        img = Image.open(io.BytesIO(resp.read())).convert('RGB')
        return img

    def get_image(self, img_value, img_type):
        if img_type == "img_base64":
            image = self.base64_2_img(img_value)
        elif img_type == "img_url":
            image = self.download_image(img_value)
        business_logger.info("image简单信息:{}".format(image))
        return image


    def prdict_label(self, service_name, image, trace_id):
        if service_name == "image_word_and_joint":
            predict_result_list = model_asl.predict(image)
        else:
            raise Exception("service_name={}不存在, trace_id为:{}".format(service_name, trace_id))
        return predict_result_list

    @gen.coroutine
    def post(self):
        result = yield self.doing()
        self.set_header('Content-Type', 'application/json; charset=UTF-8')
        self.write(json.dumps(result, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))
        self.finish()

    @run_on_executor
    def doing(self):
        start_time = time.time()

        # watcher total统计 服务维度统计
        moniter.incr(config.WATCHER_PREFIX + '.IMAGE_WORD_AND_JOINT_LABEL_SERVICE_TOTAL')

        # 读取参数
        business_logger.info("图片标签自动化审核：图片压字和图片拼接的入口参数, param：{}".format(self.request.body))

        if 'application/json' == self.request.headers["Content-Type"]:
            request = tornado.escape.json_decode(self.request.body)
            business_logger.info("request内容为：{}".format(request))
        else:
            request = dict()
            body = tornado.escape.url_unescape(self.request.body, plus=True)
            for item_line in body.split('&'):
                key, value = item_line.split('=', maxsplit=1)
                request[key] = value


        app_code = request.get("app_code", "")
        trace_id = request.get("trace_id", "")
        result = []
        try:
            params = request.get("params", [])  # list

            # watcher total统计 服务被成功接收维度统计
            moniter.incr(config.WATCHER_PREFIX + '.IMAGE_WORD_AND_JOINT_LABEL_SERVICE_SUCCESS_RECIVE_TOTAL')
            # 记录成功接收到的请求参数和trace_id 基于成功接受到请求参数维度
            business_logger.info("图片标签自动化审核：图片压字和图片拼接成功接受到请求, params为{},对应trace_id为：{}".format(params, trace_id))

            for image_info in params:
                original_id = image_info.get("original_id")  # 帖子id
                image_id = image_info.get("image_id")  # 图片id
                img_type = image_info.get("img_type")  # 图片格式
                img_value = image_info.get("image_value", None)  # 图片内容
                service_name = image_info.get("service_name", "image_word_and_joint")  # 服务名
                label_result = self.gen_label_result(service_name=service_name, original_id=original_id, image_id=image_id)  # 初始化返回结果

                # watcher total统计 图片维度统计
                moniter.incr(config.WATCHER_PREFIX + '.IMAGE_WORD_AND_JOINT_LABEL_SINGLE_PICTURE_SERVICE_TOTAL')
                try:
                    time_two = time.time()
                    image = self.get_image(img_value, img_type)
                    business_logger.info("图片加载成功，图片的类型:{},trace_id为:{}".format(type(image), trace_id))
                    predict_result_list = self.prdict_label(service_name, image, trace_id)
                    time_three = time.time()
                    business_logger.info("图片标签自动化审核：图片压字和图片拼接, 运行单张图片模型运行时间：{},trace_id为:{}".format((time_three - time_two) * 1000, trace_id))
                    moniter.timing(config.WATCHER_PREFIX + '.APPCODE_' + app_code.upper() + '.IMAGE_WORD_AND_JOINT_SINGLE_PICTURE_RUNNING' + ".TIME", (time_three - time_two) * 1000)
                    if predict_result_list is not None:
                        label_result["predict_result_list"] = predict_result_list
                        label_result["predict_status"] = 0
                    result.append(label_result)

                    # watcher total统计 图片维度服务成功统计
                    moniter.incr(config.WATCHER_PREFIX + '.IMAGE_WORD_AND_JOINT_LABEL_SINGLE_PICTURE_SERVICE_SUCCESS_TOTAL')

                except Exception as e:
                    # watcher total统计 图片维度服务失败统计
                    moniter.incr(config.WATCHER_PREFIX + '.IMAGE_WORD_AND_JOINT_LABEL_SINGLE_PICTURE_SERVICE_ERROR_TOTAL')
                    # 图片维度服务失败日志记录
                    business_logger.error("[ImageWordJointHandler] *** 图片标签自动化审核：图片压字和图片拼接任务单张图片失败, trace_id={}, img_value={}, image_id={}, msg={}".format(trace_id, img_value, image_id, e))
                    label_result["predict_status"] = 1
                    result.append(label_result)

            # app_code 整体服务响应时间
            end_time = time.time()
            moniter.timing(config.WATCHER_PREFIX + '.IMAGE_WORD_AND_JOINT_LABEL_SERVICE_REQUEST' + '.APPCODE_' + app_code.upper() + ".TIME", (end_time - start_time) * 1000)

            # 返回数据和响应时间记录
            business_logger.info("图片标签自动化审核：图片压字和图片拼接任务最后返回trace_id为：{}, 结果为：{}, 响应时间为：{}ms".format(trace_id, result, (end_time - start_time) * 1000))

            # app_code 整体服务成功量
            moniter.incr(config.WATCHER_PREFIX  + '.IMAGE_WORD_AND_JOINT_LABEL_SERVICE' + '.APPCODE_' + app_code.upper() + ".SUCCESS")
        except Exception as e:
            label_result = self.gen_label_result()
            label_result["predict_status"] = 1
            result.append(label_result)
            # app_code 整体服务失败量
            moniter.incr(config.WATCHER_PREFIX + '.IMAGE_WORD_AND_JOINT_LABEL_SERVICE' + '.APPCODE_' + app_code.upper() + ".FAULT")
            error = traceback.format_exc()
            business_logger.error("图片标签自动化审核：图片压字和图片拼接任务trace_id为：{}，错误原因为：{}, 参数为：{}".format(trace_id, str(e), error))
            raise tornado.web.HTTPError(500, str(e))
        return result