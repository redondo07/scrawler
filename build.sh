#!/usr/bin/env bash

mvn clean package -Dmaven.test.skip=true
ret=$?
if [ $ret -ne 0 ];then
    echo "===== maven build failure ====="
    exit $ret
else
    echo \n "===== maven build successfully! ====="
fi

rm -rf output
mkdir output
app="scrawler-0.0.1-SNAPSHOT.jar"
mv target/${app} output/  #拷贝目标war包或者jar包等 至output目录下

nohup $JAVA_HOME/bin/java -jar -server -Dspring.profiles.active=prod -Xms1G -Xmx1G -XX:+DisableExplicitGC -XX:+UseG1GC -XX:MaxGCPauseMillis=500 ~/gitlab/scrawler/output/${app} > ~/gitlab/scrawler/nohup.log 2>&1 &
echo "App started"