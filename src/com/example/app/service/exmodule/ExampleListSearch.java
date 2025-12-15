package com.example.app.service.exmodule;

import com.onepg.db.SqlBuilder;
import com.onepg.db.SqlUtil;
import com.onepg.util.Io;
import com.onepg.util.Io.MsgType;
import com.onepg.util.IoRows;
import com.onepg.util.ValUtil;
import com.onepg.web.AbstractDbAccessWebService;

/**
 * 一覧検索 Webサービスクラス.
 */
public class ExampleListSearch extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    // DB抽出条件バリデーション
    validate(io);
    if (io.hasErrorMsg()) {
      // バリデーションエラー時は処理を抜ける
      return;
    }
    
    // DB抽出
    getList(io);
  }

  /**
   * DB抽出.
   * 
   * @param io 引数かつ戻値（リクエストかつレスポンス）
   */
  private void getList(final Io io) {
    // DB抽出SQL
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  u.user_id ");
    sb.addQuery(", u.user_nm ");
    sb.addQuery(", u.email ");
    sb.addQuery(", CASE WHEN u.gender_cs = 'M' THEN '男性' WHEN u.gender_cs = 'F' THEN '女性' ELSE 'その他' END gender_dn ");
    sb.addQuery(", u.income_am ");
    sb.addQuery(", u.birth_dt ");
    sb.addQuery(", u.upd_ts ");
    sb.addQuery(" FROM t_user u ").addQuery(" WHERE 1=1 ");
    sb.addQnotB("   AND u.user_id = ? ", io.getString("user_id"));
    sb.addQnotB("   AND u.user_nm LIKE '%' || ? || '%' ", io.getString("user_nm"));
    sb.addQnotB("   AND u.email LIKE ? || '%' ", io.getString("email"));
    sb.addQnotB("   AND u.country_cs = ? ", io.getString("country_cs"));
    sb.addQnotB("   AND u.gender_cs = ? ", io.getString("gender_cs"));
    sb.addQnotB("   AND u.spouse_cs = ? ", io.getString("spouse_cs"));
    sb.addQnotB("   AND u.income_am >= ? ", io.getBigDecimalNullable("income_am"));
    sb.addQnotB("   AND u.birth_dt = ? ", io.getDateNullable("birth_dt"));
    sb.addQuery(" ORDER BY u.user_id ");
    // DB一括抽出
    final IoRows rows = SqlUtil.selectBulk(getDbConn(), sb, 5);
    // 抽出結果セット
    io.putRows("list", rows);
    // 抽出件数セット
    io.put("list_size", rows.size());
    if (rows.size() <= 0) {
      // 抽出件数ゼロ件時メッセージセット
      io.putMsg(MsgType.INFO, "i0004", new String[] { String.valueOf(rows.size()) });
    }
  }

  
  /**
   * DB抽出条件バリデーション.
   * 
   * @param io 引数かつ戻値（リクエストかつレスポンス）
   * @throws Exception バリデーションエラー
   */
  private void validate(final Io io) throws Exception {

    // 年収チェック
    final String incomeAm = io.getString("income_am");
    if (!ValUtil.isBlank(incomeAm) ) {
      if (!ValUtil.isNumber(incomeAm)) {
        // 数値不正メッセージセット
        io.putMsg(MsgType.ERROR, "ev012", new String[] { "年収" }, "income_am");
      }
    }

    // 誕生日チェック
    final String birthDt = io.getString("birth_dt");
    if (!ValUtil.isBlank(birthDt)) {
      if (!ValUtil.isDate(birthDt)) {
        // 日付不正メッセージセット
        io.putMsg(MsgType.ERROR, "ev013", new String[] { "誕生日" }, "birth_dt");
      }
    }
  }

}
