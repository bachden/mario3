#!/bin/bash

MAIN_CLASS=com.mario.Mario

kill $(jps -l | grep $MAIN_CLASS | awk '{print $1}');

count=0
while sleep 0.5
do
	pcount=$(jps -l | grep $MAIN_CLASS | wc -l)
	if [ $pcount -gt 0 ]
	then
		let count="$count + 1"
		if [ $count -gt 6 ]
		then
			kill -9 $(jps -l | grep $MAIN_CLASS | awk '{print $1}');
			break
		fi
	else 
		break
	fi
done

WORKING_DIR=${PWD}

$(java -version)

echo "Working directory: $WORKING_DIR"

# test -e /opt/mario-consumer && mv /opt/mario-consumer /opt/mario-consumer.$(date +%Y%m%d%H%M%S);

JVM_REMOTE_DEBUGGING="-Xdebug -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n"

JMX_OPTS="-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=9001 \
-Dcom.sun.management.jmxremote.local.only=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false"

GC_TUNE="-XX:+UseParNewGC \
-XX:NewRatio=3 \
-XX:SurvivorRatio=4 \
-XX:TargetSurvivorRatio=90 \
-XX:MaxTenuringThreshold=8 \
-XX:+UseConcMarkSweepGC \
-XX:+UseParNewGC \
-XX:ConcGCThreads=4 -XX:ParallelGCThreads=4 \
-XX:+CMSScavengeBeforeRemark \
-XX:PretenureSizeThreshold=64m \
-XX:+UseCMSInitiatingOccupancyOnly \
-XX:CMSInitiatingOccupancyFraction=50 \
-XX:CMSMaxAbortablePrecleanTime=6000 \
-XX:+CMSParallelRemarkEnabled \
-XX:+ParallelRefProcEnabled"

JVM_OPTS="$GC_TUNE \
$JVM_REMOTE_DEBUGGING \
-server \
-d64 -Xms4g -Xmx4g \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath='logs/dump-$(date +%Y%m%d%H%M%S).hprof' \
-Dfile.encoding=UTF-8 \
-Dlog4j.configurationFile=conf/log4j2.xml"

CLASS_PATH=".:./lib/*:./extensions/__lib__/*:"

CMD=$(echo "$JVM_OPTS -cp $CLASS_PATH $MAIN_CLASS $@")

echo "Starting application with command: $CMD"

java $JVM_OPTS -cp $CLASS_PATH $MAIN_CLASS $@ > /dev/null 2>&1 &
