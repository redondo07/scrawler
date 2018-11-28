#!/usr/bin/env bash

git pull

mvn clean package -Pprod -Dmaven.test.skip=true
ret=$?
if [ $ret -ne 0 ];then
    echo "===== maven build failure ====="
    exit $ret
else
    echo \n "===== maven build successfully! ====="
fi

rm -rf output
mkdir output
cp deploy.sh output/  # 拷贝control.sh脚本 至output目录下
mv target/scrawler-0.0.1-SNAPSHOT.jar output/  #拷贝目标war包或者jar包等 至output目录下

cd output

ps aux | grep 'scrawler-0.0.1-SNAPSHOT' | awk '{print $2}' | xargs kill

app="scrawler-0.0.1-SNAPSHOT.jar"
action=$1

case $action in
    "start" )
        # 启动服务
		nohup java -jar  -server -Dspring.profiles.active=prod -Xms1G -Xmx1G -XX:+DisableExplicitGC -XX:+UseG1GC -XX:MaxGCPauseMillis=500 -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintTenuringDistribution -XX:+PrintHeapAtGC -Djava.awt.headless=true -Xloggc:/home/scrawler/logs/gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/scrawler/logs -XX:ErrorFile=/home/scrawler/logs/hs_err_pid%p.log ${app} &
		echo "App started"
        ;;
    * )
        echo "unknown command, support command [start]"
        exit 1
        ;;
esac


