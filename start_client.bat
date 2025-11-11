@echo off
REM Start Test Client - Run this in Terminal 2 or 3
echo ╔══════════════════════════════════════════════════════╗
echo ║          Starting Test Client                        ║
echo ╚══════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0"
java -cp bin test.TestClient

pause
