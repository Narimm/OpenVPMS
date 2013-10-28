@echo off

call setenv.bat

java org.openvpms.tools.archetype.diff.ArchDiff --context ../conf/applicationContext.xml %*%
