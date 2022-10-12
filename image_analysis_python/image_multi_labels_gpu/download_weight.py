# -*- coding: UTF-8 -*-
# from xxxxxx_oss_sdk import xxxxxxOSSClient

import sys
import os

import config
from image_analysis_gpu_service import common
business_logger = common.BusinessLogger()

'''
-模型权重文件夹下载
 -- 如果不存在模型权重文件夹，则根据 ossurl 下载模型权重
 -- 如存在模型权重文件夹，但文件夹日期与 ossurl 中的日期不一致，
    则根据 ossurl 下载模型权重
 -- 如果存在模型权重文件夹，且文件夹日期与 ossurl 中的日期一致，
    则不下载 ossurl 对应文件
- 单个模型权重路径更改
 -- 原模型权重路径没有日期对应目录，而根据 ossurl 下载的权重路径中
    有日期对应目录，则需要再原权重路径中加入日期目录
- ossurl 
 -- 示例：http://pf-hotel-ocr.oss.corp.xxxxxx.com/pf_hotel_ocr_screen_shot_ocr_001/weights/20211008.tar.gz
 -- 格式：http://{account}/{bucket}/{object}
  --- {object}格式：工程中模型权重文件夹/以日期为名的压缩文件
   ---- 如果压缩格式不是 tar.gz ,可根据情况修改解压缩代码
   ---- 示例：weights/20211008.tar.gz
'''

class DownloadWeights():
    def __init__(self, oss_url):
        self.project_dir = os.getcwd()
        self.oss_url = oss_url

    def get_path(self,file_path):
        pathes = file_path.split('/')
        ret = ""
        for i in range(0,len(pathes) -1):
            ret +=pathes[i] + "/"
        return ret

    def get_file_name(self,file_path):
        pathes = file_path.split('/')
        return pathes[-1]

    def down_load_model_file(self,model_path):
        if os.path.exists(model_path):
            return None
        dir_1 = self.get_path(model_path)
        if not os.path.exists(dir_1):
            os.makedirs(dir_1)
        cmd_order = "wget -O " + model_path + " " + self.oss_url
        os.system(cmd_order)

    def get_weights_file_name_from_ossurl(self):
        oss_url_items = self.oss_url.split("/")
        weights_file_name = oss_url_items[-1]
        return weights_file_name

    def get_date_fold_from_ossurl(self):
        oss_url_items = self.oss_url.split("/")
        date_file = oss_url_items[-1]
        date_file_items = date_file.split(".")
        date_fold = date_file_items[0]
        return date_fold

    def get_weights_file_sufix_from_ossurl(self):
        oss_url_items = self.oss_url.split("/")
        weights_file_name = oss_url_items[-1]
        name_items = weights_file_name.split(".")
        sufix = ""
        for i in range (1,len(name_items)):
            sufix += "." + name_items[i]
        return sufix

    def unzip_weights(self,weights_path,date_dir):
        if date_dir[-1] == '/':
            date_dir = date_dir[:-1]
        sufix = self.get_weights_file_sufix_from_ossurl()
        date_dir_4_unzip = date_dir+ "_cp"
        unzip_cmd = ""
        if sufix == ".tar.gz":
            unzip_cmd = "tar -zxvf " + weights_path + " -C "+ date_dir_4_unzip
        # 新建即将解压到的文件夹
        os.makedirs(date_dir_4_unzip)
        # 解压
        os.system(unzip_cmd)
        # 更改文件夹名字
        os.system("mv "+ date_dir_4_unzip + "/* " + date_dir )
        os.system("rm -rf "+ date_dir_4_unzip)

    # 权重文件目录下载
    def down_load_weights(self, weight_fold=None):
        if weight_fold is None:
            weight_fold = "weights"
        weight_dir = os.path.join(self.project_dir,weight_fold)
        if weight_dir[-1] != "/":
            weight_dir = weight_dir + "/"
        if not os.path.exists(weight_dir):
            os.makedirs(weight_dir)
        date_fold =self.get_date_fold_from_ossurl()
        date_dir = weight_dir + date_fold
        if date_dir[-1] != "/":
            date_dir = date_dir + "/"
        if not os.path.exists(date_dir):
            compress_weights_path = weight_dir + self.get_weights_file_name_from_ossurl()
            self.down_load_model_file(compress_weights_path)
            self.unzip_weights(compress_weights_path,date_dir)



    # 修改单个模型权重路径
    def get_model_path_with_date(self,src_model_path,weight_fold=None):
        if weight_fold is None:
            weight_fold = "weights"

        date_dir = self.get_date_fold_from_ossurl()
        src_model_path_items = src_model_path.split("/")
        new_model_path = ""
        add_date = False
        for i in range(0,len(src_model_path_items)):
            if src_model_path_items[i] == weight_fold and not add_date:
                new_model_path += weight_fold + "/" + date_dir + "/"
                add_date = True
                continue

            if i == len(src_model_path_items) -1:
                new_model_path +=  src_model_path_items[i]
            else:
                new_model_path +=  src_model_path_items[i] + "/"
        return new_model_path

download_weights = DownloadWeights(config.weight_oss_url)

