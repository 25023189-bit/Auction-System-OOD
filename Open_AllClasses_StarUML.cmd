@echo off
setlocal
set "ROOT=%~dp0"
set "STARUML=%ROOT%tools\StarUML-patched\StarUML.exe"
set "USER_DATA=%ROOT%tools\StarUML-user-data"
set "MODEL=%ROOT%AuctionSystem_AllClasses_StarUML.mdj"

if not exist "%STARUML%" (
  echo Patched StarUML runtime not found:
  echo %STARUML%
  exit /b 1
)

if not exist "%MODEL%" (
  echo StarUML model not found:
  echo %MODEL%
  exit /b 1
)

start "" "%STARUML%" "--user-data-dir=%USER_DATA%" "%MODEL%"
