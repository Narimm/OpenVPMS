#!/bin/sh

. ./setenv.sh

java -Xmx512m -classpath $CLASSPATH org.openvpms.archetype.tools.account.AccountBalanceTool --context ../conf/applicationContext.xml $*
