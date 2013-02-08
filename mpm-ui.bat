@echo off

SET MPM_APP=ui\mpm-ui.groovy
SET MPM_CLASSPATH=%MPM_CLASSPATH%;ui
SET MPM_CLASSPATH=%MPM_CLASSPATH%;scripts\objects
SET MPM_CLASSPATH=%MPM_CLASSPATH%;scripts

cd "%INSTALL_PATH%"

wscript.exe launcher.vbs launcher.bat "%MPM_CLASSPATH%" "%MPM_APP%" %*

cd %ORIGINAL_PATH%