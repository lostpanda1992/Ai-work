# -- coding: utf-8 -- 
# @Time : 2021/12/23 19:38
# @Author : Jonny.chen
# @File : asl_client.py

import torch
import torch.nn.parallel
import torch.optim
import torch.utils.data.distributed
import torchvision.transforms as transforms
from image_analysis_gpu_service.common.new_factory import create_model
import os
import config
from image_analysis_gpu_service import common
business_logger = common.BusinessLogger()
# 使用第三张GPU卡
os.environ["CUDA_VISIBLE_DEVICES"] = "3"


class ASLModel:
    def __init__(self):

        # 设置参数
        self.class_to_id = {'头图压字': 0, '图片拼接': 1, '其他': 2}
        self.id_to_class = dict([val, key] for key, val in self.class_to_id.items())
        self.args = {"model_name": 'tresnet_xl', "num_classes": len(self.class_to_id)}
        self.tag_max_thre_dic = {'头图压字': 0.988, '图片拼接': 0.962}

        self.Sig = torch.nn.Sigmoid()

        # 加载模型
        self.model = create_model(self.args)
        self.state = torch.load(config.image_word_and_joint_model_path)
        self.model.load_state_dict(self.state, strict=True)

        # 将模型放入gpu中
        self.model.cuda()
        self.model.eval()

    def image_preprocess(self, image):
        trans_resize = transforms.Resize((640, 640))
        image = trans_resize(image)
        image = transforms.ToTensor()(image)
        image = torch.unsqueeze(image, dim=0)
        return image

    def predict(self, image):
        # 图片预处理
        image = self.image_preprocess(image)
        # 模型预测
        with torch.no_grad():
            output = self.Sig(self.model(image.cuda()).cuda())
        # 预测结果转化为list
        image_predict_output_list = output[0].cpu().numpy().tolist()[:-1]
        predict_result = []

        # 遍历比较模型预测结果与对应标签阈值
        for key, value in enumerate(image_predict_output_list):
            if value >= self.tag_max_thre_dic[self.id_to_class[key]]:
                predict_result.append(self.id_to_class[key])
        return predict_result


model_asl = ASLModel()
