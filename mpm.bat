@echo off
::setlocal enableextensions,enabledelayedexpansion
SET DRIVE=%~d0
SET CDIR=%~p0
SET INSTALL_PATH=%DRIVE%%CDIR%

SET ORIGINAL_PATH = %cd%
SET GROOVY_PATH=%INSTALL_PATH%groovy-2.1.00

IF NOT EXIST "%GROOVY_PATH%" GOTO NO_GROOVY
SET PATH=%PATH%;%GROOVY_PATH%\bin
cd "%INSTALL_PATH%"

:NO_GROOVY

groovy -cp scripts\objects\ scripts\mpm.groovy %*

cd %ORIGINAL_PATH%