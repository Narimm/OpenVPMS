@echo off

call setenv.bat

java org.openvpms.archetype.rules.finance.account.CustomerBalanceGenerator -c ../conf/applicationContext.xml