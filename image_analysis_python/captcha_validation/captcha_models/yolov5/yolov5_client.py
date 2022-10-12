#! coding=utf-8
import numpy as np
import sys
sys.path.append('./captcha_models/yolov5')

from captcha_service.web import service_config
from captcha_service.web import text_captcha
from utils.torch_utils import select_device
from models.experimental import attempt_load
from utils.datasets import letterbox
from utils.general import check_img_size, non_max_suppression, scale_coords
import torch
import config
import captcha_service.common.logger as logger

business_logger = logger.BusinessLogger()




class YoloV5():
	def __init__(self, service_config):
		self.device_type = service_config.yolov5_device_type
		self.device = select_device(self.device_type)
		self.half = self.device.type != 'cpu'  # half precision only supported on CUDA
		self.model_path = service_config.yolov5_model_path
		self.small_scale_text_model_path = service_config.yolov5_small_scale_model_path

		self.imgsz = service_config.yolov5_imgsz
		self.conf_thres = service_config.yolov5_conf_thres
		self.iou_thres = service_config.yolov5_iou_thres
		self.augment = True
		self.classes = 0
		self.agnostic_nms = True

		# load model
		self.model = self.load_model(self.model_path, self.device)
		self.small_scale_text_model = self.load_model(self.small_scale_text_model_path, self.device)
		self.imgsz = check_img_size(self.imgsz, s=self.model.stride.max())  # check img_size



	
	def load_model(self, model_path, device):
		print('yolov5 model_path : =================> ' + model_path)
		model = attempt_load(model_path, map_location=device)  # load FP32 model
		return model
	

	def preprocessing(self, img):
		# Padded resize
		img = letterbox(img, new_shape=self.imgsz)[0]

		# Convert
		img = img[:, :, ::-1].transpose(2, 0, 1)  # BGR to RGB, to 3x416x416
		img = np.ascontiguousarray(img)

		img = torch.from_numpy(img).to(self.device)
		img = img.half() if self.half else img.float()  # uint8 to fp16/32
		img /= 255.0  # 0 - 255 to 0.0 - 1.0
		if img.ndimension() == 3:
			img = img.unsqueeze(0)
		return img

	def calculateIoU(self, candidateBound, groundTruthBound):
		cx1 = candidateBound[0]
		cy1 = candidateBound[1]
		cx2 = candidateBound[2]
		cy2 = candidateBound[3]

		gx1 = groundTruthBound[0]
		gy1 = groundTruthBound[1]
		gx2 = groundTruthBound[2]
		gy2 = groundTruthBound[3]

		garea = (gx2 - gx1) * (gy2 - gy1)  # G的面积

		x1 = max(cx1, gx1)
		y1 = max(cy1, gy1)
		x2 = min(cx2, gx2)
		y2 = min(cy2, gy2)
		w = max(0, x2 - x1)
		h = max(0, y2 - y1)
		area = w * h  # C∩G的面积

		iou = area / garea

		return iou

	def remove_rect(self, pos_list_sorted):
		# 去除包含在大检测框中的小检测框 或者 小检测框边界与大检测框相连且其他部分都在大检测框中
		temp = []
		for i in range(len(pos_list_sorted)):
			for j in range(len(pos_list_sorted)):
				if i != j:
					ret = self.calculateIoU(pos_list_sorted[i], pos_list_sorted[j])
					if ret == 1:
						temp.append(j)
		# 使用列表倒序pop功能去除
		if temp:
			for k in range(len(temp) - 1, -1, -1):
				pos_list_sorted.pop(temp[k])
		return pos_list_sorted

	def detect(self, img_ori, flag, app_code):
		start_time = time.time()
		img = self.preprocessing(img_ori)
		# 根据图片尺寸大小使用不同的模型
		if flag:
			pred = self.small_scale_text_model(img, self.augment)[0]
			scale_symbol = "SMALL"
		else:
			pred = self.model(img, self.augment)[0]
			scale_symbol = "LARGE"

		pred = non_max_suppression(pred, self.conf_thres, self.iou_thres, classes=self.classes, agnostic=self.agnostic_nms)
		pred_text_list = []
		for i, det in enumerate(pred):  # detections per image
			if len(det):
				# Rescale boxes from img_size to im0 size
				det[:, :4] = scale_coords(img.shape[2:], det[:, :4], img_ori.shape).round()
				pos_list = []
				for *xyxy, conf, cls in reversed(det):
					pos_list.append([int(xyxy[0].item()), int(xyxy[1].item()), int(xyxy[2].item()), int(xyxy[3].item())])

				pos_list_sorted = sorted(pos_list, key=lambda x: x[0], reverse=False)

				if flag:
					pos_list_sorted = self.remove_rect(pos_list_sorted)

				for pos in pos_list_sorted:
					img_sub = img_ori[pos[1]:pos[3], pos[0]:pos[2]]
					pred_text_list.append(img_sub)

		end_time = time.time()
		# app_code对应YOLOV5时间统计
		text_captcha.moniter.timing(config.WATCHER_PREFIX + '.TEXT_CAPTCHA' + '.APPCODE_' + app_code.upper() + "." + scale_symbol + 'SCALE_YOLOV5_TIME', end_time - start_time)

		business_logger.info("文本验证码任务appcode为：{}, 尺寸大小类型为：{}SCALE, YOLOV5模型运行时间：{}ms".format(app_code, scale_symbol, (end_time - start_time) * 1000))
		return pred_text_list



	

yolov5 = YoloV5(service_config.ServiceConfig)

import time
import cv2
if __name__ == '__main__':

	start_time = time.time()
	img_path = '/home/zhangyu/project/captcha/orig/img-601-900/900.jpg'
	img = cv2.imread(img_path)
	pred = yolov5.detect(img, flag=True)

	print(pred)
	print('=' * 100)
	print('first time used: ', time.time() - start_time)

	start_time = time.time()
	print('=' * 100)
	print('second time used: ', time.time() - start_time)
