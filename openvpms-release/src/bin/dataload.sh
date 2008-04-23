#!/bin/sh

. setenv.sh

if [ "$1" = "base" ] ;
then
    java -classpath $CLASSPATH org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -f ../import/data/base.xml
elif [ "$1" = "setup" ] ;
then
    java -classpath $CLASSPATH org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -d ../import/data
else
    echo "Usage:  dataload.sh ( commands ... )"
    echo "commands:"
    echo "  base              Load base data"
    echo "  setup             Load setup data"
    exit 1
fi
