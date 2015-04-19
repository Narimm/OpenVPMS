@echo off

call setenv.bat

java -Dlog4j.configuration=file:../conf/log4j.properties org.openvpms.tools.archetype.loader.ArchetypeLoader -v -s -c --context ../conf/applicationContext.xml -o -m ../update/archetypes/org/openvpms/archetype/assertionTypes.xml -d ../update/archetypes
