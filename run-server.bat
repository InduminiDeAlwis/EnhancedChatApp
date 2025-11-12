@echo off
REM Start the Chat Server
echo ================================================
echo   Starting Chat Server...
echo ================================================
echo.

if not exist "bin\server\Server.class" (
    echo ERROR: Server not compiled. Run build.bat first.
    pause
    exit /b 1
)

cd bin
java server.Server
pause
