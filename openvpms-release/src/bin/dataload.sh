#!/bin/sh

. ./setenv.sh

if [ "$1" = "base" ] ;
then
    java -classpath $CLASSPATH org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -f ../import/data/base.xml -b 1000
elif [ "$1" = "setup" ] ;
then
    java -classpath $CLASSPATH org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -f ../import/data/base.xml,../import/data/setup.xml,../import/data/postcodes.xml -b 1000
else
    echo "Usage:  dataload.sh ( commands ... )"
    echo "commands:"
    echo "  base              Load base data"
    echo "  setup             Load setup data"
    exit 1
fi
