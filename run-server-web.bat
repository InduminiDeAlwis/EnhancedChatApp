@echo off
echo ================================================
echo   Enhanced Chat Server with Web Admin
echo ================================================
echo.
echo Starting server...
echo Web Admin Console: http://localhost:8080/admin
echo.

java -cp "bin;lib\Java-WebSocket-1.5.3.jar" server.Server

pause
