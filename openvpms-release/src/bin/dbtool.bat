@echo off

call setenv.bat

java -classpath %CLASSPATH% -Dlog4j.configuration=file:../conf/log4j.properties org.openvpms.db.tool.DBTool --properties ../conf/hibernate.properties %*%
