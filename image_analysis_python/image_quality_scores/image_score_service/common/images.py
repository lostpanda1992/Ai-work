#! coding=utf-8

import io
import numpy as np
from PIL import Image
from urllib import request


def download_img_rgb(img_url):
	req = request.Request(img_url)
	req.add_header("Referer", 'https://ugc.dujia.xxxxxx.com')
	try:
		res = request.urlopen(req)
		img = Image.open(io.BytesIO(res.read()))
		if img.mode != 'BGR':
			img = img.convert('RGB')
	except Exception as e:
		raise Exception("图片下载失败,无效的imgUrl, {}".format(e))
	return np.array(img)