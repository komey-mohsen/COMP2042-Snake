@echo off
setlocal
title Snake Game
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-game.ps1"
if errorlevel 1 (
    echo.
    echo The game could not start. See the message above.
    pause
    exit /b 1
)
