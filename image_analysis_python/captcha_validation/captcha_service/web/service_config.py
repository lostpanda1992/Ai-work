# -*- coding: UTF-8 -*-


class ServiceConfig:
    def __init__(self):
        pass

    ##################################################################

    yolov5_model_path = 'data/model/yolov5/last.pt'
    yolov5_device_type = 'cpu'
    yolov5_imgsz=640
    yolov5_conf_thres = 0.25
    yolov5_iou_thres = 0.45
    yolov5_small_scale_model_path = 'data/model/yolov5/small_scale_text_last.pt'

    ##################################################################





    ##################################################################

    cnn_captcha_image_height = 55
    cnn_captcha_image_width = 55
    cnn_captcha_max_captcha = 1
    cnn_captcha_char_set_path = 'data/captcha_char_set.txt'
    cnn_captcha_model_path = 'data/model/cnn_captcha/'
    small_scale_cnn_captcha_model_path = 'data/model/small_scale_text_cnn_captcha_v3/'

    ##################################################################