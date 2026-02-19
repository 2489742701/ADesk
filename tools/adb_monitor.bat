@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo        ADB Logcat 监听工具
echo ========================================
echo.
echo 使用说明:
echo   - 输入数字(如123)作为进程PID过滤
echo   - 输入 "start" 或 "开始测试" 开始监听所有日志
echo   - 输入 "error" 只显示错误日志
echo   - 输入 "clear" 清屏
echo   - 输入 "exit" 退出
echo.
echo ========================================
echo.

:check_adb
adb version >nul 2>&1
if errorlevel 1 (
    echo [错误] ADB未找到，请确保ADB在PATH中
    pause
    exit /b 1
)

:check_device
echo 正在检查设备连接...
for /f "tokens=*" %%i in ('adb devices ^| findstr /v "List" ^| findstr /v "^$"') do (
    set device=%%i
    goto :device_found
)
echo [警告] 未检测到设备，请连接设备后按任意键重试...
pause >nul
goto :check_device

:device_found
echo [OK] 设备已连接: %device%
echo.

:main_loop
echo.
set /p input="请输入命令或PID: "

if "%input%"=="exit" goto :end
if "%input%"=="clear" cls & goto :main_loop
if "%input%"=="start" goto :listen_all
if "%input%"=="开始测试" goto :listen_all
if "%input%"=="error" goto :listen_error

echo %input%| findstr /r "^[0-9][0-9]*$" >nul
if errorlevel 1 (
    echo [提示] 无效输入，请输入数字PID或命令
    goto :main_loop
)

:listen_pid
echo.
echo [监听] PID: %input% - 只显示错误和警告...
echo ----------------------------------------
adb logcat --pid=%input% *:W | findstr /v "^$" | findstr /v "^\[" | findstr /v "^$" 
goto :main_loop

:listen_all
echo.
echo [监听] 所有日志 - 过滤无用信息...
echo ----------------------------------------
adb logcat *:W | findstr /v "^$" | findstr /v "^\[" 
goto :main_loop

:listen_error
echo.
echo [监听] 仅错误日志...
echo ----------------------------------------
adb logcat *:E
goto :main_loop

:end
echo.
echo 再见!
exit /b 0
