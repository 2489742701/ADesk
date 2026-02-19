@echo off
chcp 65001 >nul
echo ========================================
echo     ADesk 快速测试工具
echo ========================================
echo.
echo 正在检查ADB设备...
adb devices
echo.
echo 正在安装APK...
adb install -r app\build\outputs\apk\debug\app-debug.apk
echo.
echo 正在启动应用...
adb shell am start -n com.thanksplay.adesk/.ui.MainActivity
echo.
echo 开始监听日志 (PID过滤)...
for /f "tokens=2" %%i in ('adb shell pidof com.thanksplay.adesk') do set PID=%%i
echo 应用PID: %PID%
echo ----------------------------------------
adb logcat --pid=%PID% *:W
