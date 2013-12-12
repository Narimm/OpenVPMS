#!/bin/sh

. ./setenv.sh

java -Dlog4j.configuration=file:../conf/log4j.properties -classpath $CLASSPATH org.openvpms.tools.archetype.loader.ArchetypeLoader -v -s -c --context ../conf/applicationContext.xml -o -m ../update/archetypes/org/openvpms/archetype/assertionTypes.xml -d ../update/archetypes
