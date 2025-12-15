package com.example.app.service.exmodule;

import com.onepg.db.SqlBuilder;
import com.onepg.db.SqlUtil;
import com.onepg.util.Io;
import com.onepg.util.Io.MsgType;
import com.onepg.util.IoItems;
import com.onepg.util.IoRows;
import com.onepg.util.ValUtil;
import com.onepg.web.AbstractDbAccessWebService;

/**
 * データ取得 Webサービスクラス.
 */
public class ExampleLoad extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    // ヘッダー取得
    getHead(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // 明細取得
    getDetail(io);
  }

  /**
   * ヘッダー取得.
   * 
   * @param io 引数かつ戻値（リクエストかつレスポンス）
   */
  private void getHead(final Io io) {
    // DB抽出SQL
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  u.user_nm ");
    sb.addQuery(", u.email ");
    sb.addQuery(", u.country_cs ");
    sb.addQuery(", u.gender_cs ");
    sb.addQuery(", u.spouse_cs ");
    sb.addQuery(", u.income_am ");
    sb.addQuery(", u.birth_dt ");
    sb.addQuery(" FROM t_user u ");
    sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));
    sb.addQuery("   AND u.upd_ts = ? ", io.getSqlTimestampNullable("upd_ts"));

    // DB１件抽出
    final IoItems head = SqlUtil.selectOne(getDbConn(), sb);
    if (ValUtil.isNull(head)) {
      // データが見つからない場合は排他制御エラーメッセージセット
      io.putMsg(MsgType.ERROR, "e0002", new String[]{io.getString("user_id")});
      return;
    }
    // DB抽出結果セット
    io.putAll(head);
  }

  /**
   * 明細取得.
   * 
   * @param io 引数かつ戻値（リクエストかつレスポンス）
   */
  private void getDetail(final Io io) {
    // DB抽出SQL
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  d.pet_no ");
    sb.addQuery(", d.pet_nm ");
    sb.addQuery(", d.type_cs ");
    sb.addQuery(", d.gender_cs ");
    sb.addQuery(", d.vaccine_cs ");
    sb.addQuery(", d.weight_kg ");
    sb.addQuery(", d.birth_dt ");
    sb.addQuery(" FROM t_user_pet d ");
    sb.addQuery(" WHERE d.user_id = ? ", io.getString("user_id"));
    sb.addQuery(" ORDER BY d.pet_no");
    // DB一括抽出
    final IoRows detail = SqlUtil.selectBulkAll(getDbConn(), sb);
    // DB抽出結果セット
    io.putRows("detail", detail);
  }

}
