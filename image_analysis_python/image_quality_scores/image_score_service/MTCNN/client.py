#! coding=utf-8

from mtcnn.mtcnn import MTCNN
from image_score_service.common import logger

bussines_logger = logger.BusinessLogger()

# 输入图片为rgb格式
client_face = MTCNN(min_face_size=20)
bussines_logger.info("[mtcnn] *** 载入人脸检测模型")
