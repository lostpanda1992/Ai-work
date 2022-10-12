#!/usr/bin/env bash

sudo yum install bzip2    # miniconda 安装需要tar解压zip

# need conda installed
# 一份miniconda安装文件: https://owncloud.corp.xxxxxx.com/index.php/s/NhEc8Ln9QSiGe4O/download
wget https://owncloud.corp.xxxxxx.com/index.php/s/NhEc8Ln9QSiGe4O/download --no-check-certificate -O Miniconda3-4.3.30-Linux-x86_64.sh
chmod +x Miniconda3-4.3.30-Linux-x86_64.sh
sudo ./Miniconda3-4.3.30-Linux-x86_64.sh

PATH=/home/q/miniconda3/bin:$PATH

# 创建 Python 虚拟环境
echo "
channels:
  - defaults
show_channel_urls: true
channel_alias: https://mirrors.tuna.tsinghua.edu.cn/anaconda
default_channels:
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/free
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/r
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/pro
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/msys2
custom_channels:
  conda-forge: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  msys2: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  bioconda: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  menpo: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  pytorch: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  simpleitk: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
" > ~/.condarc

#sudo conda create --name robot python=3.6

# 激活 Python 虚拟环境
source activate root

# set pip source (先看看 ~/.pip/pip.conf 里面有没有配置, 这个只执行一次)
if [ ! -d "~/.pip" ]; then
    mkdir ~/.pip
fi
echo "
[global]
index_url = http://devpi.corp.xxxxxx.com/xxxxxx/dev/+simple/
trusted-host = devpi.corp.xxxxxx.com
#index-url = https://pypi.tuna.tsinghua.edu.cn/simple
" > ~/.pip/pip.conf

## 安装需要的包, 建议使用 conda 安装
# requirements.txt


# # 安装 cuBERT
sudo tar -zxvf mklml_lnx_2019.0.3.20190220.tgz
sudo cp mklml_lnx_2019.0.3.20190220/lib/* /usr/local/lib
sudo cp mklml_lnx_2019.0.3.20190220/include/* /usr/local/include/
sudo pip install cuBERT-0.0.5-cp36-cp36m-linux_x86_64.whl

# 刷新动态链接库查找路径
sudo vim /etc/ld.so.conf.d/mkldnn.conf
写入下面一行
/usr/local/lib

sudo ldconfig

# 或者, 推荐上一种
export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH

# test
python -c 'import libcubert'
