@echo off
rem
rem Script to install the StartCom Certification Authority to cacerts.
rem This is required in order to connect to Smart Flow Sheet over https
rem

set cacerts=%JAVA_HOME%/jre/lib/security/cacerts
set storepass=changeit
echo Installing StartCom Certification Authority to "%cacerts%"
keytool -import -trustcacerts -alias "StartCom Certification Authority" -file ../conf/startcom-ca.crt -keystore "%cacerts%" -storepass %storepass% -noprompt