@echo off
REM Start a Chat Client
echo ================================================
echo   Starting Chat Client...
echo ================================================
echo.

if not exist "bin\client\Client.class" (
    echo ERROR: Client not compiled. Run build.bat first.
    pause
    exit /b 1
)

cd bin
java client.Client
pause
