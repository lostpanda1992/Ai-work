#! coding=utf-8

import tensorflow.compat.v1 as tf
tf.disable_v2_behavior()

from captcha_service.web import text_captcha
from captcha_service.web.service_config import ServiceConfig
from captcha_models.cnn_captcha.cnnlib.network import CNN
import config
import captcha_service.common.logger as logger

business_logger = logger.BusinessLogger()


with open(ServiceConfig.cnn_captcha_char_set_path, "r", encoding="utf-8") as f:
	char_set = f.read().strip()


# 加载小尺寸模型
tf.reset_default_graph()
small_scale_text_cnn_captcha = CNN(ServiceConfig.cnn_captcha_image_height, ServiceConfig.cnn_captcha_image_width, ServiceConfig.cnn_captcha_max_captcha, char_set, ServiceConfig.small_scale_cnn_captcha_model_path)
small_scale_model = small_scale_text_cnn_captcha.model()
small_scale_saver = tf.train.Saver()
small_scale_sess = tf.Session()
small_scale_saver.restore(small_scale_sess, ServiceConfig.small_scale_cnn_captcha_model_path)

# 加载大尺寸模型
tf.reset_default_graph()
cnn_captcha = CNN(ServiceConfig.cnn_captcha_image_height, ServiceConfig.cnn_captcha_image_width, ServiceConfig.cnn_captcha_max_captcha, char_set, ServiceConfig.cnn_captcha_model_path)
model = cnn_captcha.model()
saver = tf.train.Saver()
sess = tf.Session()
saver.restore(sess, ServiceConfig.cnn_captcha_model_path)

def cnn_captcha_predict(text_img, flag, app_code):
	start_time = time.time()
	captcha_array = cv2.resize(text_img, (55, 55))
	# 大小尺寸图片使用不同的参数识别文字
	if flag:
		# app_code小尺寸图片统计
		text_captcha.moniter.incr(config.WATCHER_PREFIX + '.TEXT_CAPTCHA' + '.APPCODE_' + app_code.upper() + ".SMALLSCALE")
		image = small_scale_text_cnn_captcha.convert2gray(captcha_array)
		image = image.flatten() / 255
		predict = tf.argmax(tf.reshape(small_scale_model, [-1, small_scale_text_cnn_captcha.max_captcha,
															   small_scale_text_cnn_captcha.char_set_len]), 2)
		pred_result = small_scale_sess.run(predict, feed_dict={small_scale_text_cnn_captcha.X: [image],
																   small_scale_text_cnn_captcha.keep_prob: 1.})
		text_list = pred_result[0].tolist()
		predict_text = ""
		for t in text_list:
			predict_text += str(small_scale_text_cnn_captcha.char_set[t])
		scale_symbol = "SMALL"

		end_time = time.time()
		# app_code小尺寸图片CNN时间统计
		text_captcha.moniter.timing(config.WATCHER_PREFIX + '.TEXT_CAPTCHA' + '.APPCODE_' + app_code.upper() + ".SMALLSCALE_CNN_TIME", end_time - start_time)
	else:
		# app_code大尺寸图片统计
		text_captcha.moniter.incr(config.WATCHER_PREFIX + '.TEXT_CAPTCHA' + '.APPCODE_' + app_code.upper() + ".LARGESCALE")
		# 时间日志 接口 app_code 尺寸大小 毫秒
		image = cnn_captcha.convert2gray(captcha_array)
		image = image.flatten() / 255
		predict = tf.argmax(tf.reshape(model, [-1, cnn_captcha.max_captcha, cnn_captcha.char_set_len]), 2)
		pred_result = sess.run(predict, feed_dict={cnn_captcha.X: [image], cnn_captcha.keep_prob: 1.})
		text_list = pred_result[0].tolist()
		predict_text = ""
		for t in text_list:
			predict_text += str(cnn_captcha.char_set[t])
		scale_symbol = "LARGE"

		end_time = time.time()
		# app_code大尺寸图片YOLOV5时间统计
		text_captcha.moniter.timing(config.WATCHER_PREFIX + '.TEXT_CAPTCHA' + '.APPCODE_' + app_code.upper() + ".LARGESCALE_CNN_TIME", end_time - start_time)

	business_logger.info("文本验证码任务appcode为：{}, 尺寸大小类型为：{}SCALE, CNN模型运行时间：{}ms".format(app_code, scale_symbol, (end_time - start_time) * 1000))
	return predict_text




import time
import cv2
if __name__ == '__main__':

	start_time = time.time()
	text_img_path = ''
	text_img = cv2.imread(text_img_path)

	print('=' * 100)
	print('first time used: ', time.time() - start_time)

	result = cnn_captcha_predict(text_img, flag=True, app_code= "111")
	print(result)

	start_time = time.time()
	print('=' * 100)
	print('second time used: ', time.time() - start_time)
