@echo off

call setenv.bat

java -Dlog4j.configuration=file:../conf/log4j.properties org.openvpms.etl.tools.doc.DocumentLoader -c ../conf/applicationContext.xml %*%