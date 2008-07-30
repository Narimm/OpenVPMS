@echo off

call setenv.bat

if ""%1"" == ""base"" goto doBase
if ""%1"" == ""setup"" goto doSetup

echo Usage:  dataload ( commands ... )
echo commands:
echo   base              Load base data
echo   setup             Load setup data
goto end

:doBase
java org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -f ../import/data/base.xml
goto end

:doSetup
java org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -d ../import/data

:end