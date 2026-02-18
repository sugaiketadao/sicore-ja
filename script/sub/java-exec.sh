#!/bin/bash
#
# Javaクラス実行サブスクリプト.
# ジョブID はログ出力用に使用され、呼び出し元バッチごとのユニークな値を想定。
#
# @param $1 ジョブID
# @param $2 Javaクラス
# @param $3以降 引数（3番目以降すべて）
# @return Javaコマンドの終了ステータス
#

# 変数宣言必須
set -u
# 戻値0以外で中断
set -e
set -E
# 新規作成ディレクトリ・ファイルの権限設定 755
umask 022

# エラー発生時のコマンドと行番号を表示（デバッグ時に使用）
# trap 'echo "ERROR at line ${LINENO}: ${BASH_COMMAND}" >&2' ERR

#
# 現在日付取得.
# フォーマット YYYYMMDD の文字列を返す。
#
getNowDate() {
  date +"%Y%m%d"
}

#
# 現在タイムスタンプ取得.
# フォーマット YYYYMMDD"T"HHMMSS の文字列を返す。
#
getNowTimestamp() {
  date +"%Y%m%dT%H%M%S"
}

#
# 標準エラー出力.
# このスクリプト実行時の前提条件エラー（引数不足、環境変数未設定等）の出力を想定している。
#
# @param $1 メッセージ
#
printStdErr() {
  local MSG="$1"
  local YMDHMS=$(getNowTimestamp)
  echo ${YMDHMS} ${MSG} >&2
}

#
# ログ出力.
# ログファイルに出力する。
#
# @param $1 メッセージ
#
printLog() {
  local MSG="$1"
  local YMDHMS=$(getNowTimestamp)
  echo ${YMDHMS} ${APP_NAME}/${JOB_ID} "#${PS_ID}" ${MSG} >> ${LOG_FILE} 2>&1
}

#
# アラート出力.
# ログファイルとアラートファイルの両方に出力する。
# アラートファイルへの出力は障害監視アプリでの発報を想定している。
# ALERT_FILE 環境変数が設定されていない場合、アラートファイルへの出力は行わない。
#
# @param $1 メッセージ
#
printAlert() {
  local MSG="$1"
  local YMDHMS=$(getNowTimestamp)
  echo ${YMDHMS} ${APP_NAME}/${JOB_ID} "#${PS_ID}" ${MSG} >> ${LOG_FILE} 2>&1
  if [[ "${ALERT_FILE}" != "" ]] ; then
    echo ${YMDHMS} ${APP_NAME}/${JOB_ID} "#${PS_ID}" ${MSG} >> ${ALERT_FILE} 2>&1
  fi
}

# 実行ユーザーチェック
# TODO: チェック不要な場合は下記を削除してください
# TODO: プロジェクトに応じて適切な値に変更してください
readonly ALLOW_USER=batchuser
readonly CURRENT_USER=$(whoami)
if [[ "${ALLOW_USER}" != "${CURRENT_USER}" ]] ; then
  printStdErr "ERROR: ${CURRENT_USER} is not allowed. Please execute as ${ALLOW_USER} user."
  exit 1
fi

# 必須引数チェック
if [[ $# -lt 1 ]] ; then
  printStdErr "ERROR: First argument (Job ID) is required"
  exit 1
fi
if [[ $# -lt 2 ]] ; then
  printStdErr "ERROR: Second argument (Java class) is required"
  exit 1
fi

# 引数格納
# [$1]ジョブID
readonly JOB_ID=$1
# [$2]Javaクラス
readonly EXEC_CLS=$2
# [$3以降]引数（すべて）
shift 2
readonly EXEC_ARGS=("$@")

# プロセスID（現在のプロセスIDを取得）
readonly PS_ID=$$

# スクリプトディレクトリ（絶対パス変換）.
readonly SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)

# アプリケーション環境変数読込
source ${SCRIPT_DIR}/env-app.sh

# アプリケーション環境変数チェック
if [[ "${APP_NAME:-}" = "" ]]; then
  printStdErr "ERROR: APP_NAME is blank in env-app.sh"
  exit 1
fi
if [[ "${APP_HOME:-}" = "" ]]; then
  printStdErr "ERROR: APP_HOME is blank in env-app.sh"
  exit 1
fi
if [[ "${LOG_DIR:-}" = "" ]]; then
  printStdErr "ERROR: LOG_DIR is blank in env-app.sh"
  exit 1
fi
if [[ ! -d ${APP_HOME} ]]; then
  printStdErr "ERROR: APP_HOME not exist: ${APP_HOME}"
  exit 1
fi
if [[ ! -d ${LOG_DIR} ]]; then
  printStdErr "ERROR: LOG_DIR not exist: ${LOG_DIR}"
  exit 1
fi

# Java環境変数読込
source ${SCRIPT_DIR}/env-java.sh

# Java環境変数チェック
if [[ "${JAVA_BIN:-}" = "" ]]; then
  printStdErr "ERROR: JAVA_BIN is blank in env-java.sh"
  exit 1
fi
if [[ ! -d ${JAVA_BIN} ]]; then
  printStdErr "ERROR: JAVA_BIN not exist: ${JAVA_BIN}"
  exit 1
fi

# ログファイルパス（日付_ジョブID.log）
# 標準出力をリダイレクトするファイルはロックされ、複数の処理（Javaコマンド）が同時に実行することができない為、ファイル名にジョブIDを含める。
readonly LOG_YMD=$(getNowDate)
readonly LOG_FILE=${LOG_DIR}/${LOG_YMD}_${JOB_ID}.log

# 開始ログ
printLog "[START] ${EXEC_CLS}"

# Javaクラスパス
readonly JAVA_CP=${APP_HOME}/lib/*:${APP_HOME}/classes

# Java実行
set +e
${JAVA_BIN}/java ${JVM_XMS} ${JVM_XMX} -cp "${JAVA_CP}" ${EXEC_CLS} "${EXEC_ARGS[@]}" >> ${LOG_FILE} 2>&1
readonly EXIT_STATUS=$?
set -e

# 終了ログ
printLog "[END] ${EXIT_STATUS}"

# エラー時アラート出力
if [[ "${EXIT_STATUS}" != "0" ]] ; then
  printAlert "[ERROR] ${EXEC_CLS}(${EXIT_STATUS})"
fi

# 終了
exit ${EXIT_STATUS}
