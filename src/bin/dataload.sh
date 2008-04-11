#!/bin/sh

if [ "$1" = "base" ] ;
then
    SRCDIR=../import/data/base.xml
elif [ "$1" = "setup" ] ;
then
    SRCDIR=../import/data
else
    echo "Usage:  dataload.sh ( commands ... )"
    echo "commands:"
    echo "  base              Load base data"
    echo "  setup             Load setup data"
    exit 1
fi

. setenv.sh

java -classpath $CLASSPATH org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -f $SRCDIR
