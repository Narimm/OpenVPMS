#!/bin/sh

. ./setenv.sh

java -Dlog4j.configuration=file:../conf/log4j.properties -classpath $CLASSPATH org.openvpms.etl.tools.doc.DocumentLoader -c ../conf/applicationContext.xml $*
