@echo off

call setenv.bat

java -Xmx512m org.openvpms.archetype.tools.account.AccountBalanceTool --context ../conf/applicationContext.xml %*%