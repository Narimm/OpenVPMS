@echo off
setlocal enableextensions enabledelayedexpansion

if "%1" == "" goto :usage

for %%I in ("%~dp0\..") do set "OPENVPMS_HOME=%%~fI"
if not exist "%OPENVPMS_HOME%\bin\setenv.bat" (
    echo templateload: OpenVPMS installation not found
    exit /b 1
)

if "%1" == "documents" (
    call :loadsize %1 %2
) else (
    if "%1" == "reports" (
        call :loadsize %1 %2
    ) else (
        if exist "%~dpnx1" (
            call :load "%~dpnx1"
        ) else (
            echo templateload: File "%1" does not exist
            exit /b 1
        )
    )
)
exit /b %ERRORLEVEL%


:loadsize
set valid=0
if "%2" == "A4" (
    set valid=1
) else (
    if "%2" == "A5" (
        set valid=1
    ) else (
        if "%2" == "Letter" (
            set valid=1
        )
    )
)

if %valid% == 1 (
    set file="%OPENVPMS_HOME%\reports\%1-%2.xml"
    if exist !file! (
        call :load !file!
    ) else (
        echo templateload: %1 are not available in size %2
        exit /b 1
    )
) else (
    goto :usage
)
goto :eof

:load
cd "%OPENVPMS_HOME%\bin"
call setenv.bat

java "-Dlog4j.configuration=file:../conf/log4j.properties" org.openvpms.report.tools.TemplateLoader -c "../conf/applicationContext.xml" -f "%1"
goto :eof

:usage
@echo.
@echo Loads OpenVPMS document and report templates
@echo.
@echo usage: templateload [type size]^|[file]
@echo    type - the template type. One of: documents, reports
@echo    size - the page size. One of: A4, A5, Letter
@echo    file - templates.xml file path
@echo.
@echo NOTE: existing templates with the same name and content file name will be replaced
@echo.
@echo E.g.:
@echo    templateload documents A4
@echo    templateload reports Letter
@echo    templateload c:\myreports\my-custom-A4.xml

exit /b 1
