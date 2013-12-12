@echo off

call setenv.bat

java -Xmx512m -Dlog4j.configuration=file:../conf/log4j.properties org.openvpms.archetype.tools.account.AccountBalanceTool --context ../conf/applicationContext.xml %*%