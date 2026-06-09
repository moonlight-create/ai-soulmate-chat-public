#!/bin/bash

cd $(dirname $0) || exit

# ==================== 配置区 ====================
APP_PORT=4321
# 日志统一放到 logs 目录下
LOG_DIR="./logs"
LOG_FILE="${LOG_DIR}/app.log"
GC_LOG_FILE="${LOG_DIR}/gc.log"
PID_FILE="./app.pid"

# JDK17 G1 配置（日志已修改到 logs 目录）
JVM_OPTS="-Xms1g -Xmx1g \
-XX:MaxGCPauseMillis=150 \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=./heapdump.hprof \
-Xlog:gc*:file=${GC_LOG_FILE}:time,uptime:filecount=5,filesize=100M"
# ================================================

# 启动应用（支持传自定义 jar 包名）
start() {
    local JAR_NAME=$1
    if [ -z "$JAR_NAME" ];then
        echo "错误：请指定 jar 包名，例如 ./app.sh start app.jar"
        return 1
    fi

    # 关键：自动创建日志目录，不存在就新建
    mkdir -p "$LOG_DIR"

    if [ -f "$PID_FILE" ];then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null;then
            echo "应用已在运行，PID: $PID"
            return
        else
            rm -f $PID_FILE
        fi
    fi

    echo "正在启动 $JAR_NAME ..."
    nohup java $JVM_OPTS -jar $JAR_NAME > $LOG_FILE 2>&1 &
    echo $! > $PID_FILE
    sleep 2
    echo "启动成功，PID: $(cat $PID_FILE)"
    echo "业务日志查看：tail -f $LOG_FILE"
    echo "GC日志查看：tail -f ${GC_LOG_FILE}.0"
}

# 停止应用
stop() {
    if [ ! -f "$PID_FILE" ];then
        echo "未找到进程文件，应用未运行"
        return
    fi
    PID=$(cat $PID_FILE)
    if ! ps -p $PID > /dev/null;then
        echo "进程 $PID 已不存在"
        rm -f $PID_FILE
        return
    fi

    echo "正在停止进程 $PID ..."
    kill -15 $PID
    sleep 3
    if ps -p $PID > /dev/null;then
        echo "停止失败，强制杀死进程"
        kill -9 $PID
    fi
    rm -f $PID_FILE
    echo "应用已停止"
}

# 重启应用
restart() {
    stop
    start $1
}

# 查看状态
status() {
    if [ -f "$PID_FILE" ];then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null;then
            echo "应用运行中，PID: $PID，端口: $APP_PORT"
        else
            echo "进程已异常退出"
            rm -f $PID_FILE
        fi
    else
        echo "应用未启动"
    fi
}

# 命令分发
case "$1" in
    start)
        start $2
        ;;
    stop)
        stop
        ;;
    restart)
        restart $2
        ;;
    status)
        status
        ;;
    *)
        echo "用法: $0 {start|stop|restart|status} [jar文件名]"
        echo "示例:"
        echo "  ./app.sh start ai-soulmate-chat-0.0.1-SNAPSHOT.jar"
        echo "  ./app.sh stop"
        echo "  ./app.sh restart ai-soulmate-chat-0.0.1-SNAPSHOT.jar"
        echo "  ./app.sh status"
        ;;
esac
