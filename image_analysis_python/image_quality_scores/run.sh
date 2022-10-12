#!/usr/bin/env bash

set -x  # 回显命令
#set -u  # 引用未定义变量时报错
#set -e  # 有错误时退出脚本
#set -o pipefail  # 管道有失败时停止


PROJECTLOC=$(cd $( dirname "$0" ); pwd)
# 服务进程ID查询时使用
STARTSERVICE="algo_web_service"
# tornado的web服务采用单进程多实例的方式
STARTSERVICE_0="algo_web_service_0.py"
STARTSERVICE_1="algo_web_service_1.py"
STARTSERVICE_2="algo_web_service_2.py"
STARTSERVICE_3="algo_web_service_3.py"

PYTHON="/home/q/q-img/virtualenv3.5_scores/bin/python3"

# 模型文件下载url
WEIGHT_URL=https://owncloud.corp.xxxxxx.com/index.php/s/Txl0fC5p1OJbi3I/download

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

    if [ ! -d "${PROJECTLOC}/weights" ]; then
        wget ${WEIGHT_URL} --no-check-certificate -O ${PROJECTLOC}/weights.tar.gz
        tar -zxvf ${PROJECTLOC}/weights.tar.gz -C ${PROJECTLOC}/
    fi

    echo "==========> starting service"
    cd "${PROJECTLOC}"
    nohup ${PYTHON} "${PROJECTLOC}/${STARTSERVICE_0}" >>${PROJECTLOC}/logs/business.log 2>&1 &
    sleep 5
    nohup ${PYTHON} "${PROJECTLOC}/${STARTSERVICE_1}" >>${PROJECTLOC}/logs/business.log 2>&1 &
    sleep 5
    nohup ${PYTHON} "${PROJECTLOC}/${STARTSERVICE_2}" >>${PROJECTLOC}/logs/business.log 2>&1 &
    sleep 5
    nohup ${PYTHON} "${PROJECTLOC}/${STARTSERVICE_3}" >>${PROJECTLOC}/logs/business.log 2>&1 &
    code=$?
    # 载入模型需要花费一些时间
    sleep 20

    echo "==========> service has started"
    echo "==========> service has started" >> ${PROJECTLOC}/logs/business.log
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
    PIDS=`ps -ef | grep python | grep ${STARTSERVICE} | awk '{print $2}'`
    if [[ "$PIDS" = "" ]]; then
        echo "==========> server is not running"
        return
    fi
    for PID in ${PIDS}; do
      kill -9 ${PID}
    done
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
