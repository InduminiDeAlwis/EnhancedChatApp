@echo off
REM Enhanced Chat System - Build Script
echo ================================================
echo   Enhanced Chat System - Build Script
echo ================================================
echo.

REM Create bin directory if it doesn't exist
if not exist "bin" (
    echo Creating bin directory...
    mkdir bin
)

echo Compiling common classes...
javac -d bin src\common\*.java
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile common classes
    pause
    exit /b 1
)

echo Compiling server classes...
javac -d bin -cp bin src\server\*.java
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile server classes
    pause
    exit /b 1
)

echo Compiling client classes...
javac -d bin -cp bin src\client\*.java
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile client classes
    pause
    exit /b 1
)

echo.
echo ================================================
echo   BUILD SUCCESSFUL!
echo ================================================
echo.
echo To run the application:
echo   1. Start Server:       run-server.bat
echo   2. Start Admin Console: run-admin.bat
echo   3. Start Client:       run-client.bat
echo.
pause
