@echo off
::setlocal enableextensions,enabledelayedexpansion
SET DRIVE=%~d0
SET CURRENT_DIR=%~p0
SET INSTALL_PATH=%DRIVE%%CURRENT_DIR%

SET ORIGINAL_PATH = %cd%
SET GROOVY_PATH=%INSTALL_PATH%groovy-2.1.0

IF NOT EXIST "%GROOVY_PATH%" GOTO LAUNCH
SET PATH=%PATH%;%GROOVY_PATH%\bin

:LAUNCH
SET MPM_APP=scripts\mpm.groovy

SET MPM_CLASSPATH=scripts\objects
SET MPM_CLASSPATH=%MPM_CLASSPATH%;scripts

cd "%INSTALL_PATH%"

groovy -cp %MPM_CLASSPATH% %MPM_APP% %*

cd %ORIGINAL_PATH%