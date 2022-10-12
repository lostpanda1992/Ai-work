#!/usr/bin/env python
# coding: utf-8

import os

import torch
from numpy import random

from captcha_models.yolov5.models.experimental import attempt_load
from captcha_models.yolov5.utils.datasets import LoadImages
from captcha_models.yolov5.utils.general import check_img_size, non_max_suppression, scale_coords
from captcha_models.yolov5.utils.torch_utils import select_device



device_type = 'cpu'
device = select_device(device_type)
half = device.type != 'cpu'  # half precision only supported on CUDA


# Load model
model_file = '/home/q/home/zhaohai.li/yolov5/runs/train/exp14/weights/last.pt'
print('load model : %s' % model_file)
imgsz = 640
model = attempt_load(model_file, map_location=device)  # load FP32 model
imgsz = check_img_size(imgsz, s=model.stride.max())  # check img_size



augment = True
conf_thres = 0.25
iou_thres = 0.45
classes = 0
agnostic_nms=True


def detect(file_dir, stop_idx = 10):
    dataset = LoadImages(file_dir, img_size=imgsz)
    img_file_list = []
    file_pos_list = []
    img_text_list = []
    idx = 0
    for path, img, im0s, vid_cap in dataset:
        file_name = os.path.basename(path)
        label = file_name[0:file_name.find("_")]
        if not is_Chinese(label):
            continue
        if im0s.shape != (40, 100, 3):
            continue
            
        idx += 1
        if idx == stop_idx:
            break
        img = torch.from_numpy(img).to(device)
        img = img.half() if half else img.float()  # uint8 to fp16/32
        img /= 255.0  # 0 - 255 to 0.0 - 1.0
        if img.ndimension() == 3:
            img = img.unsqueeze(0)

        pred = model(img, augment)[0]
        pred = non_max_suppression(pred, conf_thres, iou_thres, classes=classes, agnostic=agnostic_nms)
        
        img_sub_list = []
        for i, det in enumerate(pred):  # detections per image
            if len(det):
                img_text_list.append(label)
                # Rescale boxes from img_size to im0 size
                det[:, :4] = scale_coords(img.shape[2:], det[:, :4], im0s.shape).round()
                pos_list = []
                for *xyxy, conf, cls in reversed(det):
                    print(xyxy)
                    pos_list.append([int(xyxy[0].item()), int(xyxy[1].item()), int(xyxy[2].item()), int(xyxy[3].item())])

                pos_list_sorted = sorted(pos_list, key=lambda x: x[0], reverse=False)   

                for pos in pos_list_sorted:
                    img_sub = im0s[pos[1]:pos[3], pos[0]:pos[2]]
                    img_sub_list.append(img_sub)
        file_pos_list.append(pos_list)
        img_file_list.append(img_sub_list)
    return img_file_list

def is_Chinese(word):
    result =  True
    for ch in word:
        if ch < '\u4e00' or ch > '\u9fff':
            return False
    return True
