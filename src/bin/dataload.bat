@echo off

call setenv.bat

if ""%1"" == ""base"" goto doBase
if ""%1"" == ""setup"" goto doSetup
goto doOther

echo Usage:  dataload ( commands ... )
echo commands:
echo   base              Load base data
echo   setup             Load setup data
goto end

:doBase
java -Dlog4j.configuration=file:../conf/log4j.properties org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -f ../import/data/base.xml -b 1000
goto end

:doSetup
java -Dlog4j.configuration=file:../conf/log4j.properties org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -f ../import/data/base.xml,../import/data/setup.xml -b 1000
goto end

:doOther
java -Dlog4j.configuration=file:../conf/log4j.properties org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml %*% -b 1000
:end