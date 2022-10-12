#! coding=utf-8

import json
import time
import tornado.web
import collections

import config
from image_score_service.common import logger
from image_score_service.MTCNN import client_face
from image_score_service.common.images import download_img_rgb
from image_score_service.common.monitor_tools import api_monitor


business_logger = logger.BusinessLogger()


class DetectFaceHandler(tornado.web.RequestHandler):
	""" 检测图片中的人脸 """
	
	def get_response(self, data=[], msg=''):
		resp = collections.OrderedDict()
		resp['ret'] = False
		resp["hasFace"] = False
		resp['data'] = data
		resp['msg'] = msg
		if data is not None:
			resp['ret'] = True
			if len(data) > 0:
				resp['hasFace'] = True
		return resp
	
	def detect_face(self, img_url, score_thres, area_ratio_thres):
		detect = []
		img = download_img_rgb(img_url)
		height, width, _ = img.shape
		# 模型检测
		faces = client_face.detect_faces(img)
		for face in faces:
			box = face['box']
			score = face['confidence']
			area_ratio = box[2] * box[3] / height * width
			if area_ratio >= area_ratio_thres and score >= score_thres:
				detect.append({"box": box, "score": score})
		return detect
	
	@api_monitor('detect_face')
	def get(self):
		time_start = time.time()
		token = self.get_argument("token", "")
		img_url = self.get_argument("imgUrl", None)
		score_threshold = float(self.get_argument("scoreThres", default='0.98'))
		area_ratio_threshold = float(self.get_argument("areaRatioThres", default="0.01"))
		try:
			if token != config.token:
				raise Exception("调用token错误，请申请调用权限")
			detect = self.detect_face(img_url, score_threshold, area_ratio_threshold)
			time_end = time.time()
			response = self.get_response(detect, msg="{:.4f} s".format(time_end - time_start))
		except Exception as e:
			business_logger.error("[DetectFaceHandler] *** 人脸检测失败, imgUrl={}, msg={}".format(img_url, e))
			response = self.get_response(data=None, msg="{}".format(e))
		finally:
			self.set_header('Content-Type', 'application/json; charset=UTF-8')
			self.write(json.dumps(response, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))
		
	@api_monitor('detect_face')
	def post(self):
		pass