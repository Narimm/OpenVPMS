@echo off

call setenv.bat

java -Dlog4j.configuration=file:../conf/log4j.properties org.openvpms.tools.archetype.diff.ArchDiff --context ../conf/applicationContext.xml %*%
