@echo off

set CURR_DIR=%~dp0
set CLASSPATH="%CURR_DIR%lib\*"
set MAIN_CLASS=com.mario.Mario

set LOG4J_CONFIGURATION="file:%CURR_DIR%conf\log4j2.xml"

java -server -cp %CLASSPATH% -Dlog4j.configurationFile=%LOG4J_CONFIGURATION% %MAIN_CLASS% %*