@echo off
rem #
rem # Javaクラス実行サブスクリプト.
rem # ジョブID はログ出力用に使用され、呼び出し元バッチごとのユニークな値を想定。
rem #
rem # @param $1 ジョブID
rem # @param $2 Javaクラス
rem # @param $3以降 引数（3番目以降すべて）
rem # @return Javaコマンドの終了ステータス
rem #

rem # 環境変数の影響を及ぼさないようにする
rem # 遅延展開を有効化
setlocal enabledelayedexpansion

rem # 実行ユーザーチェック
rem # TODO: チェック不要な場合は下記を削除してください
rem # TODO: プロジェクトに応じて適切な値に変更してください
set ALLOW_USER=batchuser
for /f "tokens=*" %%i in ('whoami') do set "CURRENT_USER=%%i"
if not "%ALLOW_USER%" == "%CURRENT_USER%" (
  call :printStdErr "ERROR: %CURRENT_USER% is not allowed. Please execute as %ALLOW_USER% user."
  exit /b 1
)

rem # 必須引数チェック
if "%1" == "" (
  call :printStdErr "ERROR: First argument (Job ID) is required" 
  exit /b 1
)
if "%2" == "" (
  call :printStdErr "ERROR: Second argument (Java class) is required"
  exit /b 1
)

rem # 引数格納
rem # [$1]ジョブID
set JOB_ID=%1
rem # [$2]Javaクラス
set EXEC_CLS=%2
rem # [$3以降]引数（9個まで対応）
set EXEC_ARGS=%3 %4 %5 %6 %7 %8 %9

rem # プロセスID（親プロセスIDを取得）
powershell -Command "try { $process = Get-CimInstance Win32_Process -Filter \"ProcessId=$pid\" -ErrorAction Stop; if ($process) { exit $process.ParentProcessId } else { exit 0 } } catch { exit 0 }"
set PS_ID=%ERRORLEVEL%

rem # スクリプトディレクトリ（絶対パス変換）.
for %%I in ("%~dp0") do set SCRIPT_DIR=%%~fI

rem # アプリケーション環境変数読込
call %SCRIPT_DIR%\env-app.bat
rem # アプリケーション環境変数チェック
if "%APP_NAME%"=="" (
  call :printStdErr "ERROR: APP_NAME is blank in env-app.bat"
  exit /b 1
)
if "%APP_HOME%"=="" (
  call :printStdErr "ERROR: APP_HOME is blank in env-app.bat"
  exit /b 1
)
if "%LOG_DIR%"=="" (
  call :printStdErr "ERROR: LOG_DIR is blank in env-app.bat"
  exit /b 1
)
if not exist "%APP_HOME%" (
  call :printStdErr "ERROR: APP_HOME not exist: %APP_HOME%"
  exit /b 1
)
if not exist "%LOG_DIR%" (
  call :printStdErr "ERROR: LOG_DIR not exist: %LOG_DIR%"
  exit /b 1
)

rem # Java環境変数読込
call %SCRIPT_DIR%env-java.bat

rem # Java環境変数チェック
if "%JAVA_BIN%"=="" (
  call :printStdErr "ERROR: JAVA_BIN is blank in env-java.bat"
  exit /b 1
)
if not exist "%JAVA_BIN%" (
  call :printStdErr "ERROR: JAVA_BIN not exist: %JAVA_BIN%"
  exit /b 1
)

rem # ログファイルパス（日付_ジョブID.printLog）
rem # 標準出力をリダイレクトするファイルはロックされ、複数の処理（Javaコマンド）が同時に実行することができない為、ファイル名にジョブIDを含める。
call :getNowDate LOG_YMD
set LOG_FILE=%LOG_DIR%\%LOG_YMD%_%JOB_ID%.log

rem # 開始ログ
call :printLog "[START] %EXEC_CLS%"

rem # Javaクラスパス
set JAVA_CP=%APP_HOME%\lib\*;%APP_HOME%\classes

rem # Java実行
%JAVA_BIN%\java %JVM_XMS% %JVM_XMX% -cp "%JAVA_CP%" %EXEC_CLS% %EXEC_ARGS%>>%LOG_FILE% 2>&1
set EXIT_STATUS=%ERRORLEVEL%

rem # 終了ログ
call :printLog "[END] %EXIT_STATUS%"

rem # エラー時アラート出力
if not "%EXIT_STATUS%" == "0" (
  call :printAlert "[ERROR] %EXEC_CLS%(%EXIT_STATUS%)"
)

rem # 終了
exit /b %EXIT_STATUS%


rem # 以下、サブ関数

rem #
rem # 現在日付取得.
rem # フォーマット YYYYMMDD の文字列を引数変数にセットする。
rem #
rem # @param $1 日付をセットする変数名
rem #
:getNowDate
rem # %date% = YYYY/MM/DD 想定
set "%~1=%date:~0,4%%date:~5,2%%date:~8,2%"
rem # %date% = MM/DD/YYYY または MM.DD.YYYY 想定
rem # set "%~1=%date:~6,4%%date:~0,2%%date:~3,2%"
exit /b

rem #
rem # 現在タイムスタンプ取得.
rem # フォーマット YYYYMMDD"T"HHMMSS の文字列を引数変数にセットする。
rem #
rem # @param $1 タイムスタンプをセットする変数名
rem #
:getNowTimestamp
call :getNowDate YMD
rem # 時刻をゼロパディング
set ZPTIME=%time: =0%
rem # 日付に時刻を追加して戻値にセット
set "%~1=%YMD%T%ZPTIME:~0,2%%ZPTIME:~3,2%%ZPTIME:~6,2%"
exit /b

rem #
rem # 標準エラー出力.
rem # このスクリプト実行時の前提条件エラー（引数不足、環境変数未設定等）の出力を想定している。
rem #
rem # @param $1 メッセージ（ブランクを含む場合はダブルクォーテーションで囲む）
rem #
:printStdErr
set MSG=%1
call :getNowTimestamp YMDHMS
echo %YMDHMS% %MSG:"=%>&2
exit /b

rem #
rem # ログ出力.
rem # ログファイルに出力する。
rem #
rem # @param $1 メッセージ（ブランクを含む場合はダブルクォーテーションで囲む）
rem #
:printLog
set MSG=%1
call :getNowTimestamp YMDHMS
echo %YMDHMS% %APP_NAME%/%JOB_ID% pid=%PS_ID% %MSG:"=%>>%LOG_FILE% 2>&1
exit /b

rem #
rem # アラート出力.
rem # ログファイルとアラートファイルの両方に出力する。
rem # アラートファイルへの出力は障害監視アプリでの発報を想定している。
rem # %ALERT_FILE% 環境変数が設定されていない場合、アラートファイルへの出力は行わない。
rem #
rem # @param $1 メッセージ（ブランクを含む場合はダブルクォーテーションで囲む）
rem #
:printAlert
set MSG=%1
call :getNowTimestamp YMDHMS
echo %YMDHMS% %APP_NAME%/%JOB_ID% pid=%PS_ID% %MSG:"=%>>%LOG_FILE% 2>&1
if "%ALERT_FILE%"=="" (
  exit /b
)
echo %YMDHMS% %APP_NAME%/%JOB_ID% pid=%PS_ID% %MSG:"=%>>%ALERT_FILE% 2>&1
exit /b
