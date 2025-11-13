@echo off
REM Start the Admin Console
echo ================================================
echo   Starting Admin Console...
echo ================================================
echo.

if not exist "bin\server\AdminConsole.class" (
    echo ERROR: Admin Console not compiled. Run build.bat first.
    pause
    exit /b 1
)

cd bin
java server.AdminConsole
