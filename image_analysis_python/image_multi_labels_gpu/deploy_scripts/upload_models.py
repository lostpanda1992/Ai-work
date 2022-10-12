from xxxxxx_oss_sdk import xxxxxxOSSClient

import sys
TEST_CONFIG = {
    "appcode": "pf_algo_image_analysis_gpu",
    "account": "pf_algo_image_analysis_gpu",
    "access_key": "aeeb2d60de60d12a62e8e1e780fc7369",
    "secret_key": "aeeb2d60de60d12a62e8e1e780fc7369",
    "endpoint_url": "http://pf-algo-image-analysis-gpu.oss.corp.xxxxxx.com",
    "bucket_name": "pf_algo_image_analysis_gpu_word_joint_model_001",
}


client = xxxxxxOSSClient(**TEST_CONFIG)

model_path = "/home/q/home/junjun.pan/ossTest/pf_algo_image_analys_gpu/20211228.tar.gz"
key_name = "20211228.tar.gz"
ocr_url = client.upload_file(filename=model_path,key=key_name)
print(model_path)
print(ocr_url)
