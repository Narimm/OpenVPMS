#!/bin/sh

. ./setenv.sh

if [ "$1" = "base" ] ;
then
    java -Dlog4j.configuration=file:../conf/log4j.properties -classpath $CLASSPATH org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -f ../import/data/base.xml -b 1000
elif [ "$1" = "setup" ] ;
then
    java -Dlog4j.configuration=file:../conf/log4j.properties -classpath $CLASSPATH org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -f ../import/data/base.xml,../import/data/setup.xml -b 1000
else
    java -Dlog4j.configuration=file:../conf/log4j.properties -classpath $CLASSPATH org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -f "$1" -b 1000
fi
