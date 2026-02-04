package com.example.app.bat.exmodule;

import com.onepg.bat.AbstractDbAccessBatch;
import com.onepg.db.SqlConst;
import com.onepg.db.SqlUtil;
import com.onepg.db.SqlConst.BindType;
import com.onepg.util.CsvReader;
import com.onepg.util.FileUtil;
import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.CsvType;
import com.onepg.util.IoItems;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;

/**
 * データインポートバッチクラス.
 */
public class ExampleImport extends AbstractDbAccessBatch {

    /** SQL定義：ユーザー登録. */
    private static final SqlConst SQL_INS_USER = SqlConst.begin()
      .addQuery("INSERT INTO t_user ( ")
      .addQuery("  user_id ")
      .addQuery(", user_nm ")
      .addQuery(", email ")
      .addQuery(", country_cs ")
      .addQuery(", gender_cs ")
      .addQuery(", spouse_cs ")
      .addQuery(", income_am ")
      .addQuery(", birth_dt ")
      .addQuery(", upd_ts ")
      .addQuery(" ) VALUES ( ")
      .addQuery("  ? ", "user_id", BindType.STRING)
      .addQuery(", ? ", "user_nm", BindType.STRING)
      .addQuery(", ? ", "email", BindType.STRING)
      .addQuery(", ? ", "country_cs", BindType.STRING)
      .addQuery(", ? ", "gender_cs", BindType.STRING)
      .addQuery(", ? ", "spouse_cs", BindType.STRING)
      .addQuery(", ? ", "income_am", BindType.BIGDECIMAL)
      .addQuery(", ? ", "birth_dt", BindType.DATE)
      .addQuery(", ? ", "upd_ts", BindType.TIMESTAMP)
      .addQuery(" ) ")
      .end();

    /** SQL定義：ユーザー更新. */
    private static final SqlConst SQL_UPD_USER = SqlConst.begin()
      .addQuery("UPDATE t_user SET ")
      .addQuery("  user_nm = ? ", "user_nm", BindType.STRING)
      .addQuery(", email = ? ", "email", BindType.STRING)
      .addQuery(", country_cs = ? ", "country_cs", BindType.STRING)
      .addQuery(", gender_cs = ? ", "gender_cs", BindType.STRING)
      .addQuery(", spouse_cs = ? ", "spouse_cs", BindType.STRING)
      .addQuery(", income_am = ? ", "income_am", BindType.BIGDECIMAL)
      .addQuery(", birth_dt = ? ", "birth_dt", BindType.DATE)
      .addQuery(", upd_ts = ? ", "upd_ts", BindType.TIMESTAMP)
      .addQuery(" WHERE user_id = ? ", "user_id", BindType.STRING)
      .end();

  /**
   * メイン処理.
   * @param args 引数
   */
  public static void main(String[] args) {
    final ExampleImport batch = new ExampleImport();
    batch.callMain(args);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int doExecute(final IoItems io) throws Exception {
    // 入力ファイルパス取得
    final String inputPath = io.getString("input");

    if (ValUtil.isBlank(inputPath)) {
      // 'input' パラメーター必須チェック
      throw new RuntimeException("'input' is required.");
    }
    if (!FileUtil.exists(inputPath)) {
      // 入力先ファイル存在チェック
      throw new RuntimeException("Input path not exists. " + LogUtil.joinKeyVal("input", inputPath));
    }

    // DB抽出してファイル出力
    try (final CsvReader cr = new CsvReader(inputPath, CharSet.UTF8, CsvType.DQ_ALL)) {
      for (final IoItems row : cr) {
        if (!SqlUtil.executeOne(getDbConn(), SQL_UPD_USER.bind(row))) {
          // 更新件数０件の場合は登録実行
          SqlUtil.executeOne(getDbConn(), SQL_INS_USER.bind(row));
        }
      }
      if (cr.getReadedCount() <= 1) {
        // ゼロ行またはヘッダ行しか無い場合
        super.logger.info("No data found to import. " + LogUtil.joinKeyVal("input", inputPath));
      }
    }
    return 0;
  }
}
