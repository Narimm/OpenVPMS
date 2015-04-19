#!/bin/sh

. ./setenv.sh

java -classpath $CLASSPATH -Dlog4j.configuration=file:../conf/log4j.properties org.openvpms.report.tools.TemplateLoader -c ../conf/applicationContext.xml -f $*
