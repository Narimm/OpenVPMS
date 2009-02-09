#!/bin/sh

. ./setenv.sh

java -classpath $CLASSPATH org.openvpms.archetype.tools.account.AccountBalanceTool --context ../conf/applicationContext.xml $*
