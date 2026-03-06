package com.example.app.service.excommon;

import com.onepg.util.Io;
import com.onepg.web.AbstractDbAccessWebService;

/**
 * サインイン後実行 Webサービスクラス.
 */
public class ExampleSinginAfter extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   * <ul>
   * <li>セッション情報を作成する。</li>
   * </ul>
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    if (io.hasErrorMsg()) {
      // 認証エラーの場合は処理を抜ける
      return;
    }
    // サインインIDをセッションに保存（※処理例として）
    io.session().put("signin_id", io.getString("signin_id"));
  }
}
