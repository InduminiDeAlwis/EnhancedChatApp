@echo off
REM Start Server - Run this in Terminal 1
echo ╔══════════════════════════════════════════════════════╗
echo ║          Starting Enhanced Chat Server               ║
echo ╚══════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0"
java -cp bin server.Server

pause
