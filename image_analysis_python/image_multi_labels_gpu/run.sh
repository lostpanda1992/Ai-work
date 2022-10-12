#!/usr/bin/env bash

set -x  # 回显命令
#set -u  # 引用未定义变量时报错
#set -e  # 有错误时退出脚本
#set -o pipefail  # 管道有失败时停止

PYTHON="/home/q/home/jonny.chen/miniconda/envs/multi/bin/python"
PROJECTLOC=$(cd $( dirname "$0" ); pwd)
STARTSERVICE="image_analysis_gpu_service.py"


# 配置环境变量
PATH=/home/q/miniconda3/bin:$PATH


# 准备对应的配置文件
if [[ "$2" == "beta" ]];then
  echo "beta"
  cp ${PROJECTLOC}/config_beta.py ${PROJECTLOC}/config.py
else
  echo "prod"
  cp ${PROJECTLOC}/config_prod.py ${PROJECTLOC}/config.py
fi

start_server() {
    #pid exists, check if running
    PID=`ps -ef | grep python | grep ${STARTSERVICE} | awk '{print $2}'`
    if [[ ! "$PID" = "" ]]; then
        echo "service is running"
        return 1
    fi
    # generate logs directory
    if [ ! -d "${PROJECTLOC}/logs" ]; then
        mkdir ${PROJECTLOC}/logs
    fi

    echo "==========> starting service"
    cd "${PROJECTLOC}"
    nohup ${PYTHON} "${PROJECTLOC}/${STARTSERVICE}" >>${PROJECTLOC}/logs/run.log 2>&1 &
    code=$?

    sleep 30
    echo "==========> service has started"
    echo "==========> service has started" >> ${PROJECTLOC}/logs/run.log
    touch ${PROJECTLOC}/healthcheck.html
    return ${code}
}


stop_server() {
    echo "==========> stopping server"
    cd "${PROJECTLOC}"
    if [[ -f healthcheck.html ]]; then
        echo "==========> remove healthcheck.html"
        rm healthcheck.html
        # NG 需要等10s后生效, 这个等待时间不能低于10s
        sleep 10
    fi
    PID=`ps -ef | grep python | grep ${STARTSERVICE} | awk '{print $2}'`
    if [[ "$PID" = "" ]]; then
        echo "==========> server is not running"
        return
    fi
    kill -9 "${PID}"
}


case "$1" in
'start')
    start_server
    ;;
'stop')
    stop_server
    ;;
'restart')
    stop_server
    sleep 2
    start_server
    ;;
*)
    echo "Usage: $0 { start | stop | restart } { beta | prod }"
    ;;
esac

exit 0
