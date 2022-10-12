#! coding=utf-8

import os
import cv2
import numpy as np

import importlib
from keras.models import Model
from keras.layers import Dropout, Dense

from image_score_service.common import logger


business_logger = logger.BusinessLogger()


class Nima(object):
    """  图片打分模型NIMA  """
    
    def __init__(self, n_classes=10):
        self.n_classes = n_classes
        self.nima_model = None

    def load_weights(self, weight_path):
        self.base_module = importlib.import_module('keras.applications.mobilenet')
        # get base model class
        BaseCnn = getattr(self.base_module, 'MobileNet')

        # load pre-trained model
        weights = os.path.join(os.getcwd(), 'weights/nima/mobilenet_1_0_224_tf_no_top.h5')
        self.base_model = BaseCnn(input_shape=(224, 224, 3), weights=weights, include_top=False, pooling='avg')

        # add dropout and dense layer
        x = Dropout(0)(self.base_model.output)
        x = Dense(units=self.n_classes, activation='softmax')(x)

        self.nima_model = Model(self.base_model.inputs, x)
        self.nima_model.load_weights(weight_path)

    def preprocessing_function(self):
        return self.base_module.preprocess_input
    
    def process(self, img, size=(224, 224)):
        """ 输入rgb图片 """
        x = np.empty((1, size[0], size[1], 3))
        img = cv2.resize(img, size, interpolation=cv2.INTER_NEAREST)
        x[0, ] = img
        return self.base_module.preprocess_input(x)
        
    def predict(self, img):
        x = self.process(img)
        scores = self.nima_model.predict(x)[0]
        scores = np.array(scores)
        scores_norm = scores / scores.sum()
        score = (scores_norm * np.arange(1, 11)).sum()
        return score
    
        
    

# 图像打分:
#  图片美观度打分
#  图片清晰度打分
model_aest_path = os.path.join(os.getcwd(), 'weights/nima/mobilenet_aesthetic.hdf5')
model_qual_path = os.path.join(os.getcwd(), 'weights/nima/mobilenet_technical.hdf5')

# 载入模型
client_nima = {}
# 美观度模型，调用方需要指定：aest
model_aest = Nima()
model_aest.load_weights(model_aest_path)
client_nima['aesthetic'] = model_aest
business_logger.info("[nima] *** 载入美观度模型")
# 清晰度模型，调用方需要指定：qual
model_qual = Nima()
model_qual.load_weights(model_qual_path)
client_nima['quality'] = model_qual
business_logger.info("[nima] *** 载入清晰度模型")
