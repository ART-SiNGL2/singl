#!/bin/bash
#ZINGG_HOME=./assembly/target
SINGL_JARS=$SINGL_HOME/singl-2.0-0.3.3-SNAPSHOT.jar
EMAIL=xxx@yyy.com
LICENSE="test"
##for local
export SPARK_MEM=10g

$SPARK_HOME/bin/spark-submit --master $SPARK_MASTER --conf spark.serializer=org.apache.spark.serializer.KryoSerializer --conf spark.es.nodes="127.0.0.1" --conf spark.es.port="9200" --conf spark.es.resource="cluster/cluster1" --conf spark.default.parallelism="8" --conf spark.executor.extraJavaOptions="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -Xloggc:/tmp/memLog.txt -XX:+UseCompressedOops" --conf spark.executor.memory=10g --conf spark.debug.maxToStringFields=200 --driver-class-path $SINGL_JARS --class singl.client.Client $SINGL_JARS $@ --email $EMAIL --license $LICENSE 
