package com.onepg.app.bat.dataio;

import java.io.File;
import java.sql.Connection;

import com.onepg.bat.AbstractBatch;
import com.onepg.db.DbUtil;
import com.onepg.db.SqlBuilder;
import com.onepg.db.SqlResultSet;
import com.onepg.db.SqlUtil;
import com.onepg.util.FileUtil;
import com.onepg.util.IoItems;
import com.onepg.util.IoTsvWriter;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;

/**
 * DBテーブルデータエクスポートバッチクラス.<br>
 * <ul>
 * <li>指定DBテーブルのデータを入出力用TSVファイルに出力します。<br>
 * 入出力用TSVファイルの特徴は下記のとおり。
 *   <ul>
 *   <li>ファイルの1行目に列名を出力する。</li>
 *   <li>ファイルの文字コードは UTF-8、改行コードは LF 固定とする。</li>
 *   <li><code>null</code> はエスケープする。</li>
 *   <li>値内にある改行コード（CRLF・CR・LF）とタブ文字はエスケープする。</li>
 *   </ul></li>
 * </li>
 * <li>DBMSにあわせたJDBCライブラリ（jarファイル）が必要です。</li>
 * <li><code>main</code>メソッドへの引数は URLパラメータ形式です。（<code>AbstractBatch</code>参照）</li>
 * <li>引数は下記のとおり
 *   <ul>
 *   <li>url：JDBC接続URL ［例］jdbc:postgresql://localhost:5432/db01</li>
 *   <li>user：DBユーザー（省略可能）</li>
 *   <li>pass：DBパスワード（省略可能）</li>
 *   <li>table：対象テーブル物理名</li>
 *   <li>output：出力パス ディレクトリ指定可能</li>
 *   <li>where：抽出条件（省略可能）</li>
 *   <li>zip：zip圧縮フラグ 圧縮時 true（省略可能）</li>
 *   </ul></li>
 * <li>出力パスにディレクトリを指定した場合、ファイル名はテーブル名となります。</li>
 * </ul>
 */
public class DbTableExp extends AbstractBatch {

  /**
   * メイン処理.
   * @param args 引数
   */
  public static void main(String[] args) {
    System.exit((new DbTableExp()).callMain(args));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doExecute(final IoItems io) throws Exception {
    // 引数 - JDBC接続URL（必須）
    final String jdbcUrl = io.getString("url");
    // 引数 - DBユーザー（省略可能）
    final String dbUser = io.getStringOrDefault("user", null);
    // 引数 - DBパスワード（省略可能）
    final String dbPass = io.getStringOrDefault("pass", null);
    // 引数 - 対象テーブル名（必須） 小文字で扱う
    final String tableName =  io.getString("table").toLowerCase();
    // 引数 - 出力パス（必須）
    String outputPath = io.getString("output");
    // 引数 - 抽出条件（省略可能）
    final String where = io.getStringOrDefault("where", ValUtil.BLANK);
    // zip圧縮フラグ（省略可能）
    final boolean isZip = io.getBooleanOrDefault("zip", false);
    
    // 出力ファイルパス
    if (FileUtil.isDirectory(outputPath)) {
      // 出力パスがディレクトリの場合、ファイル名はテーブル名とする
      outputPath = FileUtil.joinPath(outputPath, tableName + ".tsv");
    } else {
      if (FileUtil.existsParent(outputPath)) {
        // 親ディレクトリが無ければエラー
        throw new RuntimeException("Output parent directory does not exist. " + LogUtil.joinKeyVal("output", outputPath));
      }
    }

    if (FileUtil.exists(outputPath)) {
      // 出力先ファイル非存在チェック
      throw new RuntimeException("Output path already exists. " + LogUtil.joinKeyVal("output", outputPath));
    }

    int count = 0;
    // DB接続
    try (final Connection conn = DbUtil.getConnByUrl(jdbcUrl, dbUser, dbPass, super.traceCode)) {
      // テーブル存在チェック
      if (!DbUtil.isExistsTable(conn, tableName)) {
        throw new RuntimeException("Specified table does not exist. " + LogUtil.joinKeyVal("table", tableName));
      }

      super.logger.info("Starting DB data export. " + LogUtil.joinKeyVal("table", tableName));
      // プライマリキー取得
      final String[] pkeys = DbUtil.getPrimaryKeys(conn, tableName);
      // SQL
      final SqlBuilder sb = new SqlBuilder();
      sb.addQuery("SELECT * FROM ").addQuery(tableName);
      if (!ValUtil.isBlank(where)) {
        if (where.toUpperCase().startsWith("WHERE ")) {
          sb.addQuery(where);
        } else {
          sb.addQuery("WHERE ").addQuery(where);
        }
      }
      if (!ValUtil.isEmpty(pkeys)) {
        sb.addQuery(" ORDER BY ");
        for (final String key : pkeys) {
          sb.addQuery(key).addQuery(",");
        }
        sb.delLastChar();
      }

      // DB抽出してファイル出力
      try (final SqlResultSet rSet = SqlUtil.select(conn, sb);
          final IoTsvWriter tw = new IoTsvWriter(outputPath)) {
        // 列名を出力
        tw.println(rSet.getItemNames());
        for (final IoItems row : rSet) {
          tw.println(row);
        }
        count = rSet.getReadedCount();
      }
    }

    final String outPath;
    if (isZip) {
      final String zipPath = FileUtil.replaceTypeMark(outputPath, "zip");
      FileUtil.zip(outputPath,  ValUtil.UTF8, zipPath);
      // テキストファイル削除
      (new File(outputPath)).delete();
      outPath = zipPath;
    } else {
      outPath = outputPath;
    }
    if (ValUtil.isBlank(where)) {
      super.logger.info("DB data exported successfully. " + LogUtil.joinKeyVal("table", tableName, "count", count, "file", outPath));
    } else {
      super.logger.info("DB data exported successfully. " + LogUtil.joinKeyVal("table", tableName, "count", count, "file", outPath, "where", where));
    }
  }
}
