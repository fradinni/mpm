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

groovy -cp %*