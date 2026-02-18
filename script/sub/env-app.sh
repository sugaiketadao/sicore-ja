#!/bin/bash
#
# アプリケーション環境変数.
#

# アプリケーション名.
# TODO: プロジェクトに応じて適切な値に変更してください
readonly APP_NAME="example-app"

# アプリケーションホームディレクトリ.
# 本subディレクトリ（APP_HOME/script/sub 想定）の２つ上
readonly APP_HOME=$(cd $(dirname ${BASH_SOURCE[0]})/../.. && pwd)

# ログ出力ディレクトリ.
# TODO: プロジェクトに応じて適切な値に変更してください
# TODO: 権限の上下関係により書き込めない場合はログ出力ディレクトリパスにユーザー名（${USER}）を追加する
readonly LOG_DIR="/tmp/logs"
if [[ ! -d ${LOG_DIR} ]]; then
  # 無ければ作成
  mkdir -p ${LOG_DIR}
fi

# アラートファイルパス.
# 障害監視対象となるログファイルパス
# TODO: プロジェクトに応じて適切な値に変更してください
readonly ALERT_FILE="/tmp/logs/alert.log"
if [[ ! -e ${ALERT_FILE} ]]; then
  # 無ければ作成
  touch ${ALERT_FILE}
fi
