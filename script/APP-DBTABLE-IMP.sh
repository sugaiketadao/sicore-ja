#!/bin/bash
#
# DBテーブルデータインポートバッチ実行
#
# url：JDBC接続URL ［例］jdbc:postgresql://localhost:5432/db01
# user：DBユーザー（省略可能）
# pass：DBパスワード（省略可能）
# table：対象テーブル物理名
# output：出力パス ディレクトリ指定可能
# where：抽出条件（省略可能）
# zip：zip圧縮フラグ 圧縮時 true（省略可能）
#

# SqliteのDBファイルパス
readonly PARENT_DIR=$(cd $(dirname $0)/.. && pwd)
readonly JDBC_URL="jdbc:sqlite:${PARENT_DIR}/example_db/data/example.dbf"

bash $(dirname $0)/sub/java-exec.sh $(basename $0 .sh) com.onepg.app.bat.dataio.DbTableImp "url=${JDBC_URL}&input=/tmp/t_user.tsv"
