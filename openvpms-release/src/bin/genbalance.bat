@echo off

call setenv.bat

java org.openvpms.archetype.rules.finance.account.CustomerBalanceGenerator --context ../conf/applicationContext.xml %*%