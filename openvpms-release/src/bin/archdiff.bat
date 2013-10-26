@echo off

call setenv.bat

java -Xmx256M -agentlib:jdwp=transport=dt_shmem,server=y,suspend=n,address=jdbconn org.openvpms.tools.archetype.diff.ArchDiff --context ../conf/applicationContext.xml %*%
