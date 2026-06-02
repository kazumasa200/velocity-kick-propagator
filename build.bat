@echo off
setlocal

:: Java の確認
where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java が見つかりません。Java 17 以上をインストールしてください。
    pause
    exit /b 1
)

cd /d "%~dp0"

:: Maven の解決（システム → ローカルキャッシュ → 自動ダウンロード）
set MVN_VERSION=3.9.8
set MVN_DIR=%~dp0.mvn-bin\apache-maven-%MVN_VERSION%
set MVN=%MVN_DIR%\bin\mvn.cmd

where mvn >nul 2>&1
if not errorlevel 1 (
    set MVN=mvn
    goto :build
)

if exist "%MVN%" goto :build

echo [INFO] Maven が見つかりません。自動ダウンロードします...
set MVN_ZIP=%TEMP%\apache-maven-%MVN_VERSION%-bin.zip
set MVN_URL=https://downloads.apache.org/maven/maven-3/%MVN_VERSION%/binaries/apache-maven-%MVN_VERSION%-bin.zip

curl -L "%MVN_URL%" -o "%MVN_ZIP%" --progress-bar
if errorlevel 1 (
    echo [ERROR] Maven のダウンロードに失敗しました。
    pause
    exit /b 1
)

mkdir "%MVN_DIR%" >nul 2>&1
tar -xf "%MVN_ZIP%" -C "%~dp0.mvn-bin"
del "%MVN_ZIP%"
echo [INFO] Maven をダウンロードしました: %MVN_DIR%

:build
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
