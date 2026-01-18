@echo off
REM Build and Package Script for Spigot-HologramUILib (Windows)

setlocal enabledelayedexpansion

echo ======================================
echo Spigot-HologramUILib Build Script
echo ======================================

REM Check Java
echo Checking Java version...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found! Please install Java 17+
    exit /b 1
)

REM Build with Gradle
echo Building with Gradle...
call gradlew.bat clean build

if errorlevel 1 (
    echo ERROR: Build failed!
    exit /b 1
)

echo.
echo ✓ Build successful!
echo JAR location: build\libs\spigot-hologramuilib-1.0.0.jar
echo.

REM Copy to local server (optional)
if exist "..\minecraft-server\plugins" (
    echo Copying to local server...
    copy "build\libs\spigot-hologramuilib-1.0.0.jar" "..\minecraft-server\plugins\"
    echo ✓ Copied to server!
)

echo.
echo ======================================
echo Build Complete!
echo ======================================
