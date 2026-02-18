rem #
rem # アプリケーション環境変数.
rem #

rem # アプリケーション名.
rem # TODO: プロジェクトに応じて適切な値に変更してください
set APP_NAME=example-app

rem # アプリケーションホームディレクトリ（絶対パス変換）.
rem # 本subディレクトリ（APP_HOME/script/sub 想定）の２つ上
for %%I in ("%~dp0..\..") do set APP_HOME=%%~fI

rem # ログ出力ディレクトリ.
rem # TODO: プロジェクトに応じて適切な値に変更してください
rem # TODO: 権限の上下関係により書き込めない場合はログ出力ディレクトリパスにユーザー名（%USERNAME%）を追加する
set LOG_DIR=c:\tmp\logs
if not exist "%LOG_DIR%" (
  rem # 無ければ作成
  mkdir %LOG_DIR%
)

rem # アラートファイルパス.
rem # 障害監視対象となるログファイルパス
rem # TODO: プロジェクトに応じて適切な値に変更してください
set ALERT_FILE=c:\tmp\logs\alert.log
if not exist "%ALERT_FILE%" (
  rem # 無ければ作成
  type nul > %ALERT_FILE%
)
