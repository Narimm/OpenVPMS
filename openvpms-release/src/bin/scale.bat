@echo off

call setenv.bat

java -classpath %CLASSPATH% org.openvpms.jasperreports.Scaler %*%
