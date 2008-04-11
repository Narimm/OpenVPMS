#!/bin/sh

. setenv.sh

java -classpath $CLASSPATH org.openvpms.archetype.rules.finance.account.CustomerBalanceGenerator --context ../conf/applicationContext.xml $*
