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
java org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -f ../import/data/base.xml -b 1000
goto end

:doSetup
java org.openvpms.tools.data.loader.StaxArchetypeDataLoader -c ../conf/applicationContext.xml -f ../import/data/base.xml,../import/data/setup.xml,../import/data/postcodes.xml -b 1000

:end