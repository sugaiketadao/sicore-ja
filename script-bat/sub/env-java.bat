rem #
rem # Java環境変数.
rem #

rem # Javaホームディレクトリ.
rem # TODO: OS環境変数で設定されている場合、下記は不要ですので削除してください
rem # TODO: プロジェクトに応じて適切な値に変更してください
set JAVA_HOME=C:\pleiades\java\21

rem # Javaバイナリパス.
set JAVA_BIN=%JAVA_HOME%\bin

rem # JVM ヒープ領域 初期サイズ デフォルト値.
rem # 個別に設定されている場合は上書きしない
if not defined JVM_XMS (
  rem # TODO: プロジェクトに応じて適切な値に変更してください
  set JVM_XMS=-Xms128m
)

rem # JVM ヒープ領域 最大サイズ デフォルト値.
rem # 個別に設定されている場合は上書きしない
if not defined JVM_XMX (
  rem # TODO: プロジェクトに応じて適切な値に変更してください
  set JVM_XMX=-Xmx1024m
)
