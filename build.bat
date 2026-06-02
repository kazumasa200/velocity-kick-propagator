@echo off
setlocal

:: Java の確認
where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java が見つかりません。Java 17 以上をインストールしてください。
    pause
    exit /b 1
)

:: プロジェクトルートに移動
cd /d "%~dp0"

:: 同梱 Maven を使用
set MVN=%~dp0maven\bin\mvn.cmd

if not exist "%MVN%" (
    echo [ERROR] maven\bin\mvn.cmd が見つかりません。
    pause
    exit /b 1
)

echo [INFO] ビルドを開始します...
call "%MVN%" package -q

if errorlevel 1 (
    echo [ERROR] ビルドに失敗しました。
    pause
    exit /b 1
)

echo.
echo [SUCCESS] ビルド完了！
echo 出力ファイル: target\velocity-kick-propagator-1.0.0.jar
echo.
echo Velocity の plugins フォルダにコピーしてください。
pause
