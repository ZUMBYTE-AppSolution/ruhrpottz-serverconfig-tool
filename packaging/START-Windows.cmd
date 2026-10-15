@echo off
setlocal
cd /d "%~dp0"
set "APP=%~dp0PZ-Server-Architect-0.3.0.jar"
set "JAVA=java.exe"
set "JAVAW=javaw.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" (
  set "JAVA=%JAVA_HOME%\bin\java.exe"
  set "JAVAW=%JAVA_HOME%\bin\javaw.exe"
)
if exist "%~dp0runtime\bin\java.exe" (
  set "JAVA=%~dp0runtime\bin\java.exe"
  set "JAVAW=%~dp0runtime\bin\javaw.exe"
)
if not exist "%APP%" (
  echo PZ Server Architect: Bitte das gesamte ZIP zuerst entpacken.
  pause
  exit /b 1
)
"%JAVA%" -jar "%APP%" --version >nul 2>&1
if errorlevel 1 (
  echo PZ Server Architect benoetigt Java 21 oder neuer.
  echo Java fehlt, ist zu alt oder wurde nicht ueber PATH/JAVA_HOME gefunden.
  echo Die native Ausgabe mit Laufzeit benoetigt keine separate Java-Installation.
  echo Weitere Hinweise stehen in HANDBUCH.md.
  pause
  exit /b 1
)
if "%~1"=="--version" (
  "%JAVA%" -jar "%APP%" --version
  exit /b 0
)
start "" "%JAVAW%" -jar "%APP%" %*
