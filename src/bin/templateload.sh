#!/bin/sh

. ./setenv.sh

java -classpath $CLASSPATH org.openvpms.report.tools.TemplateLoader -c ../conf/applicationContext.xml -f $*
