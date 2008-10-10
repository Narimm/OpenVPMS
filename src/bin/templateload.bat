@echo off

call setenv.bat

java org.openvpms.report.tools.TemplateLoader -c ../conf/applicationContext.xml -f %*%