package com.onepg.app.bat.dataio;

import java.sql.Connection;
import java.util.Map;

import com.onepg.bat.AbstractBatch;
import com.onepg.db.DbUtil;
import com.onepg.db.SqlConst;
import com.onepg.db.SqlUtil;
import com.onepg.db.SqlConst.BindType;
import com.onepg.db.SqlConst.SqlConstBuilder;
import com.onepg.util.FileUtil;
import com.onepg.util.IoItems;
import com.onepg.util.IoTsvReader;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;


/**
 * DBテーブルデータインポートバッチクラス.<br>
 * <ul>
 * <li>入出力用TSVファイルから指定DBテーブルにデータをインポートします。<br>
 * 入出力用TSVファイルは下記を前提としている。
 *   <ul>
 *   <li>ファイルの1行目に列名。</li>
 *   <li>ファイルの文字コードは UTF-8、改行コードは LF。</li>
 *   <li><code>null</code> はエスケープされている。</li>
 *   <li>値内にある改行コード（CRLF・CR・LF）とタブ文字はエスケープされている。</li>
 *   </ul></li>
 * </li>
 * <li>DBMSにあわせたJDBCライブラリ（jarファイル）が必要です。</li>
 * <li><code>main</code>メソッドへの引数は URLパラメータ形式です。（<code>AbstractBatch</code>参照）</li>
 * <li>引数は下記のとおり
 *   <ul>
 *   <li>url：JDBC接続URL ［例］jdbc:postgresql://localhost:5432/db01</li>
 *   <li>user：DBユーザー（省略可能）</li>
 *   <li>pass：DBパスワード（省略可能）</li>
 *   <li>table：対象テーブル物理名（省略可能）</li>
 *   <li>input：入力ファイルパス </li>
 *   </ul></li>
 * <li>対象テーブル物理名が省略された場合は入力ファイル名がテーブル名として使用されます。</li>
 * <li>入力ファイルパスは zip圧縮ファイルも指定可能です。</li>
 * </ul>
 */
public class DbTableImp extends AbstractBatch {

  /**
   * メイン処理.
   * @param args 引数
   */
  public static void main(String[] args) {
    System.exit((new DbTableImp()).callMain(args));
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
    // 引数 - 入力ファイルパス（必須）
    final String inputPath = io.getString("input");
    final String inputFileName = FileUtil.trimTypeMark(FileUtil.getFileName(inputPath));
    // 引数 - 対象テーブル名（省略可能） 小文字で扱う
    final String tableName =  io.getStringOrDefault("table", inputFileName).toLowerCase();

    if (!FileUtil.exists(inputPath)) {
      // 入力ファイルが無ければエラー
      throw new RuntimeException("Input file not exists. " + LogUtil.joinKeyVal("input", inputPath));
    }

    // zip解凍
    final boolean isZip = (FileUtil.getTypeMark(inputPath).equalsIgnoreCase("zip"));
    
    // 入力ファイルパス
    final String inPath;
    if (isZip) {
      inPath = FileUtil.unzip(inputPath, FileUtil.getParentPath(inputPath))[0];
    } else {
      inPath = inputPath;
    } 
    
    super.logger.info("Starting DB data import. " + LogUtil.joinKeyVal("table", tableName, "file", inPath));

    // DB接続
    int count = 0;
    // DB接続
    try (final Connection conn = DbUtil.getConnByUrl(jdbcUrl, dbUser, dbPass, super.traceCode);
      final IoTsvReader tr = new IoTsvReader(inPath)) {

      final String[] keys = tr.getKeys();
      if (ValUtil.isEmpty(keys)) {
        // ヘッダ行が無い場合はファイルがゼロ行として終了する
        super.logger.info("No data found to import. " + LogUtil.joinKeyVal("file", inPath));
        return;
      }
      // テーブル存在チェック
      if (!DbUtil.isExistsTable(conn, tableName)) {
        throw new RuntimeException("Specified table does not exist. " + LogUtil.joinKeyVal("table", tableName));
      }
      
      // DB項目名・バインドタイプマップ
      final Map<String, BindType> bindType = SqlUtil.createItemBindTypeMapByMeta(conn, tableName);
    
      // 登録SQL作成 ※通常は使用しない SqlConstBuilder を使用
      final SqlConstBuilder scb = SqlConst.begin();
      scb.addQuery("INSERT INTO ").addQuery(tableName).addQuery(" ( ");
      for (final String key : keys) {
        scb.addQuery(key).addQuery(",");
      }
      scb.delLastChar();
      scb.addQuery(" ) VALUES ( ");
      for (final String key : keys) {
        if (!bindType.containsKey(key)) {
          throw new RuntimeException("Column name does not exist in the table. " + LogUtil.joinKeyVal("table", tableName, "column", key));
        }
        scb.addQuery("?", key, bindType.get(key)).addQuery(",");
      }
      scb.delLastChar();
      scb.addQuery(" ) ");
      final SqlConst sc = scb.end();
      
      // ファイル読込してDB登録
      for (final IoItems row : tr) {
        SqlUtil.executeOneCache(conn, sc.bind(row));
        // にコミットしてログ出力
        if (tr.getReadedCount() % 5000 == 0) {
          super.logger.info("Intermediate commit every 5000 records. " + LogUtil.joinKeyVal("count", tr.getReadedCount()));
          conn.commit();
        }
      }
      if (tr.getReadedCount() == 0) {
        // ヘッダ行しか無い場合
        super.logger.info("No data found to import. " + LogUtil.joinKeyVal("input", inputPath));
      } else {
        // 最終コミット
        conn.commit();
      }
      count = tr.getReadedCount();
    }

    if (isZip) {
      FileUtil.delete(inPath);
    }

    super.logger.info("DB data imported successfully. " + LogUtil.joinKeyVal("count", count, "file", inputPath));
  }
}
