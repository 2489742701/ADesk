@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo     ADesk ADB 智能监听工具
echo ========================================
echo.

:check_adb
adb version >nul 2>&1
if errorlevel 1 (
    echo [错误] ADB未找到
    pause
    exit /b 1
)

:wait_input
echo.
echo 输入说明:
echo   - 输入任意数字(如123) = 监听该PID的错误日志
echo   - 输入 "start" = 开始测试并自动获取PID
echo   - 输入 "install" = 安装APK
echo   - 输入 "run" = 启动应用
echo   - 输入 "log" = 查看所有错误日志
echo   - 输入 "exit" = 退出
echo.
set /p cmd="请输入: "

if "%cmd%"=="exit" exit /b 0
if "%cmd%"=="start" goto :auto_test
if "%cmd%"=="install" goto :install
if "%cmd%"=="run" goto :run_app
if "%cmd%"=="log" goto :all_errors

echo %cmd%| findstr /r "^[0-9][0-9]*$" >nul
if errorlevel 1 (
    echo [提示] 请输入数字PID或命令
    goto :wait_input
)

echo.
echo [监听PID: %cmd%] 只显示错误和警告...
echo ========================================
adb logcat --pid=%cmd% *:W 2>&1 | findstr /v /c:"[          ]" /c:"---"
goto :wait_input

:auto_test
echo.
echo [1/3] 安装APK...
adb install -r app\build\outputs\apk\debug\app-debug.apk 2>nul
echo.
echo [2/3] 启动应用...
adb shell am start -n com.thanksplay.adesk/.ui.MainActivity >nul 2>&1
echo.
echo [3/3] 获取PID并监听...
for /f "tokens=*" %%p in ('adb shell pidof com.thanksplay.adesk 2^>nul') do set PID=%%p
if "%PID%"=="" (
    echo [错误] 无法获取PID，请手动输入
    goto :wait_input
)
echo 应用PID: %PID%
echo ========================================
echo 只显示错误和警告日志:
echo ========================================
adb logcat --pid=%PID% *:W 2>&1 | findstr /v /c:"[          ]"
goto :wait_input

:install
echo.
echo 正在安装APK...
adb install -r app\build\outputs\apk\debug\app-debug.apk
goto :wait_input

:run_app
echo.
echo 正在启动应用...
adb shell am start -n com.thanksplay.adesk/.ui.MainActivity
goto :wait_input

:all_errors
echo.
echo [监听所有错误日志]...
echo ========================================
adb logcat *:E
goto :wait_input
