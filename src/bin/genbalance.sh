#!/bin/sh

. ./setenv.sh

java -Xmx512m -classpath $CLASSPATH -Dlog4j.configuration=file:../conf/log4j.properties org.openvpms.archetype.tools.account.AccountBalanceTool --context ../conf/applicationContext.xml $*
