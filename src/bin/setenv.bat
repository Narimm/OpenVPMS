set CLASSPATH=..\conf

FOR %%J IN (..\lib\*.jar) DO call :addcp %%J
goto gotcp

:addcp
set CLASSPATH=%CLASSPATH%;%1
goto :eof

:gotcp
