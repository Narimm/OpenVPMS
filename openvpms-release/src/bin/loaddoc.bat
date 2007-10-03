@echo off

call setenv.bat

java org.openvpms.etl.tools.doc.DocumentLoader -c ../conf/applicationContext.xml %*%