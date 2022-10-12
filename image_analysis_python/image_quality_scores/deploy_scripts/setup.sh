#!/usr/bin/env bash

set -x

QIMG=/home/q/q-img
# generate logs directory
if [ ! -d "${QIMG}" ]; then
    echo "env is good"
    return 1
fi


yum groupinstall "Development tools"

yum install zlib-devel bzip2-devel openssl-devel ncurses-devel sqlite-devel readline-readline-devel tk-devel gdbm-devel db4-devel libpcap-devel xz-devel

yum install libXext libSM libXrender

yum install mysql-devel

yum install mysql

yum -y install python-pip

pip install supervisor -i https://pypi.tuna.tsinghua.edu.cn/simple


mkdir /home/q/q-img
cd /home/q/q-img

wget https://www.python.org/ftp/python/3.5.2/Python-3.5.2.tar.xz

tar -xvf Python-3.5.2.tar.xz

cd /home/q/q-img/Python-3.5.2

./configure --prefix=/home/q/python35

make -j5

make install

cd /home/q/python35/bin/

./pip3 install virtualenv -i https://pypi.tuna.tsinghua.edu.cn/simple

./virtualenv -p /home/q/python35/bin/python3.5  --always-copy --download /home/q/q-img/virtualenv3.5_scores


source /home/q/q-img/virtualenv3.5_scores/bin/activate

pip install setuptools==39.1.0 -i https://pypi.tuna.tsinghua.edu.cn/simple

#pip install -r /home/q/www/crawl_screenshot_scores/deploy_scripts/requirements.txt -i  https://pypi.tuna.tsinghua.edu.cn/simple
