package com.example.app.service.exmodule;

import java.sql.Connection;

import com.onepg.db.SqlUtil;
import com.onepg.util.Io;
import com.onepg.util.LogUtil;
import com.onepg.util.Io.MsgType;
import com.onepg.web.AbstractDbAccessWebService;

/**
 * 削除 Webサービスクラス.
 */
public class ExampleDelete extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    // ヘッダー削除
    deleteHead(io);
    if (io.hasErrorMsg()) {
      // 排他制御エラー時は処理を抜ける
      return;
    }
    // 明細削除
    deleteDetail(io);
    // 成功メッセージ設定
    io.putMsg(MsgType.INFO, "i0003", new String[] { io.getString("user_id") });
  }

  /**
   * ヘッダー削除.
   * 
   * @param io 引数かつ戻値（リクエストかつレスポンス）
   */
  private void deleteHead(final Io io) {
    // DB１件削除
    if (!SqlUtil.deleteOne(getDbConn(), "t_user", io, new String[]{"user_id"}, "upd_ts")) {
      io.putMsg(MsgType.ERROR, "e0002", new String[] { io.getString("user_id") }, "user_id");
    }
  }

  /**
   * 明細削除登録.
   * 
   * @param io 引数かつ戻値（リクエストかつレスポンス）
   */
  private void deleteDetail(final Io io) {
    final Connection conn = getDbConn();
    // DB複数件削除
    final int delCnt = SqlUtil.delete(conn, "t_user_pet", io, new String[] { "user_id" });

    if (super.logger.isDevelopMode()) {
      super.logger.develop(LogUtil.joinKeyVal("deleted count", delCnt));
    }
  }
}