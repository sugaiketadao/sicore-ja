package com.example.app.bat.exmodule;

import com.onepg.bat.AbstractDbAccessBatch;
import com.onepg.db.SqlConst;
import com.onepg.db.SqlResultSet;
import com.onepg.db.SqlUtil;
import com.onepg.util.CsvWriter;
import com.onepg.util.FileUtil;
import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.CsvType;
import com.onepg.util.ValUtil.LineSep;
import com.onepg.util.IoItems;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;

/**
 * データエクスポートバッチクラス.
 */
public class ExampleExport extends AbstractDbAccessBatch {

    /** SQL定義：ユーザー抽出. */
    private static final SqlConst SQL_SEL_USER = SqlConst.begin()
      .addQuery("SELECT ")
      .addQuery("  u.user_id ")
      .addQuery(", u.user_nm ")
      .addQuery(", u.email ")
      .addQuery(", u.country_cs ")
      .addQuery(", u.gender_cs ")
      .addQuery(", u.spouse_cs ")
      .addQuery(", u.income_am ")
      .addQuery(", u.birth_dt ")
      .addQuery(", u.upd_ts ")
      .addQuery(" FROM t_user u ")
      .addQuery(" ORDER BY u.user_id ")
      .end();

  /**
   * メイン処理.
   * @param args 引数
   */
  public static void main(String[] args) {
    final ExampleExport batch = new ExampleExport();
    batch.callMain(args);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int doExecute(final IoItems io) throws Exception {
    // 出力ファイルパス取得
    final String outputPath = io.getString("output");

    if (ValUtil.isBlank(outputPath)) {
      // 'output' パラメーター必須チェック
      throw new RuntimeException("'output' is required.");
    }
    if (FileUtil.exists(outputPath)) {
      // 出力先ファイル非存在チェック
      throw new RuntimeException("Output path already exists. " + LogUtil.joinKeyVal("output", outputPath));
    }
    if (!FileUtil.existsParent(outputPath)) {
      // 出力先ディレクトリ存在チェック
      throw new RuntimeException("Output parent directory not exists. " + LogUtil.joinKeyVal("output", outputPath));
    }

    // DB抽出してファイル出力
    try (final SqlResultSet rSet = SqlUtil.select(getDbConn(), SQL_SEL_USER);
        final CsvWriter cw = new CsvWriter(outputPath, LineSep.CRLF, CharSet.UTF8, CsvType.DQ_ALL_LF)) {
      // 列名を出力
      cw.println(rSet.getItemNames());
      for (final IoItems row : rSet) {
        cw.println(row);
      }
      if (rSet.getReadedCount() == 0) {
        super.logger.info("No data found to export. " + LogUtil.joinKeyVal("output", outputPath));
      }
    }
    return 0;
  }
}
