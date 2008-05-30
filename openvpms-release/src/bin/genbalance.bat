@echo off

call setenv.bat

java org.openvpms.archetype.tools.account.AccountBalanceTool --context ../conf/applicationContext.xml %*%