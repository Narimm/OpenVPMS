#!/bin/sh

. ./setenv.sh

java -classpath $CLASSPATH org.openvpms.etl.tools.doc.DocumentLoader -c ../conf/applicationContext.xml $*
