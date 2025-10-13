@echo off
setlocal enabledelayedexpansion

:: Get the directory containing this script
set "SCRIPT_DIR=%~dp0"
set "APP_HOME=%SCRIPT_DIR%.."

:: Find the management-console JAR file
for %%F in ("%APP_HOME%\lib\management-console-*.jar") do (
    set "JAR=%%F"
)

if not defined JAR (
    echo Error: Could not find management-console JAR file
    exit /b 1
)

:: Determine temp directory
if defined TMPDIR (
    set "RESOLVED_TMP_DIR=%TMPDIR%"
) else (
    set "RESOLVED_TMP_DIR=%TEMP%"
)

:: Remove trailing backslash if present
if "%RESOLVED_TMP_DIR:~-1%"=="\" set "RESOLVED_TMP_DIR=%RESOLVED_TMP_DIR:~0,-1%"

set "LOG_FOLDER=%RESOLVED_TMP_DIR%\chromia"

:: Use RELL_JAVA if set, otherwise use java
if defined RELL_JAVA (
    set "JAVA_CMD=%RELL_JAVA%"
) else (
    set "JAVA_CMD=java"
)

:: Run the application
"%JAVA_CMD%" %JAVA_ARGS% -Duser.language=en -Duser.country=US -DCHR_LOG_FOLDER="%LOG_FOLDER%" -cp "%JAR%;%APP_HOME%\lib\*" net.postchain.mc.Directory1Kt %*
