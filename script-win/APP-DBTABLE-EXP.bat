@echo off
rem #
rem # DBテーブルデータエクスポートバッチ実行
rem #
rem # url：JDBC接続URL ［例］jdbc:postgresql://localhost:5432/db01
rem # user：DBユーザー（省略可能）
rem # pass：DBパスワード（省略可能）
rem # table：対象テーブル物理名
rem # output：出力パス ディレクトリ指定可能
rem # where：抽出条件（省略可能）
rem # zip：zip圧縮フラグ 圧縮時 true（省略可能）
rem #

rem # SqliteのDBファイルパス（絶対パス変換）
for %%I in ("%~dp0..") do set PARENT_DIR=%%~fI
set JDBC_URL=jdbc:sqlite:%PARENT_DIR:\=/%/example_db/data/example.dbf

call %~dp0sub\java-exec.bat %~n0 com.onepg.app.bat.dataio.DbTableExp "url=%JDBC_URL%&table=t_user&output=C:\tmp"
