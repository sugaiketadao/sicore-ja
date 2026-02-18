#!/bin/bash
#
# Java環境変数.
#

# Javaホームディレクトリ.
# TODO: OS環境変数で設定されている場合、下記は不要ですので削除してください
# TODO: プロジェクトに応じて適切な値に変更してください
readonly JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# Javaバイナリパス.
readonly JAVA_BIN=${JAVA_HOME}/bin

# JVM ヒープ領域 初期サイズ デフォルト値.
# 個別に設定されている場合は上書きしない
if [[ ! -v JVM_XMS ]]; then
  # TODO: プロジェクトに応じて適切な値に変更してください
  readonly JVM_XMS="-Xms128m"
fi

# JVM ヒープ領域 最大サイズ デフォルト値.
# 個別に設定されている場合は上書きしない
if [[ ! -v JVM_XMX ]]; then
  # TODO: プロジェクトに応じて適切な値に変更してください
  readonly JVM_XMX="-Xmx1024m"
fi
