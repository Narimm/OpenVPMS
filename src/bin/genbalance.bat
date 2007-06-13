@echo off

call setenv.bat

java org.openvpms.archetype.rules.balance.CustomerBalanceGenerator -c ../conf/applicationContext.xml