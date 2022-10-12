#! coding=utf-8

import json
import time
import collections
import tornado.web

import config
from image_score_service.common import logger
from image_score_service.NIMA import client_nima
from image_score_service.common.images import download_img_rgb
from image_score_service.common.monitor_tools import api_monitor


business_logger = logger.BusinessLogger()


class ScoreHandler(tornado.web.RequestHandler):
	""" 图片打分：
		清晰度 scoreType： quality
		美观度 scoreType: aesthetic
	"""
	
	def get_response(self, data={}, msg=''):
		resp = collections.OrderedDict()
		resp['ret'] = False
		resp['data'] = data
		resp['msg'] = msg
		if data is not None and len(data) > 0:
			resp['ret'] = True
		return resp
	
	def calculate_score(self, score_type, img_url):
		score = None
		if score_type in client_nima:
			img = download_img_rgb(img_url)
			score = client_nima[score_type].predict(img)
		else:
			raise Exception("scoreType={} 不存在".format(score_type))
		return score
	
	@api_monitor('image_score')
	def get(self):
		time_start = time.time()
		token = self.get_argument('token', default="")
		img_url = self.get_argument('imgUrl', default=None)
		score_type = self.get_argument('scoreType', default='quality')
		try:
			if token != config.token:
				raise Exception("调用token错误，请申请调用权限")
			score = self.calculate_score(score_type, img_url)
			time_end = time.time()
			response = self.get_response({"scoreType": score_type, "score": score}, msg="{:.4f} s".format(time_end - time_start))
		except Exception as e:
			business_logger.error("[ScoreHandler] *** 分数计算错误, imgUrl={}, msg={}".format(img_url, e))
			response = self.get_response(None, msg="{}".format(e))
		finally:
			self.set_header('Content-Type', 'application/json; charset=UTF-8')
			self.write(json.dumps(response, ensure_ascii=False, separators=(',', ':')).encode('utf-8'))
	
	@api_monitor('image_score')
	def post(self):
		pass