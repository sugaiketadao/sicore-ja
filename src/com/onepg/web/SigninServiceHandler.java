package com.onepg.web;

import com.onepg.util.Io;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;
import com.sun.net.httpserver.HttpExchange;
import java.net.HttpURLConnection;

/**
 * サインインサービスハンドラークラス.
 * @hidden
 */
final class SigninServiceHandler extends AbstractHttpHandler {

  /**
   * コンストラクタ.
   */
  SigninServiceHandler() {
    // サインインサービスはJWT検証不要
    super(false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doExecute(final HttpExchange exchange) throws Exception {
    try {
      // リクエストパラメーターの処理
      final Io io = reqToIoParams(exchange);
      
      // サインインサービス処理実行
      (new SigninService()).execute(io);

      // サインイン後サービス処理実行
      if (!ValUtil.isBlank(ServerUtil.SIGNIN_AFTER_SERVICE_CLS)) {
        final AbstractWebService serviceObj = createWebServiceClsInstance(ServerUtil.SIGNIN_AFTER_SERVICE_CLS);
        serviceObj.execute(io);
      }

      // レスポンス
      final String resJson = io.createJsonWithMsg(ServerUtil.MSG_MAP);
      ServerUtil.responseJson(exchange, resJson);
      
    } catch (final Exception | Error e) {
      super.logger.error(e, "An exception error occurred in signin service execution. ");
      ServerUtil.responseText(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "Unexpected signin service error. ");
    }
  }
  
  /**
   * リクエストパラメーターをI/Oパラメーターに変換.<br>
   * <ul>
   * <li>POST メソッド以外は例外エラーとする。</li>
   * </ul>
   *
   * @param exchange HTTP送受信データ
   * @return パラメーターを含むIoオブジェクト
   * @throws Exception パラメーター処理エラー
   */
  private Io reqToIoParams(final HttpExchange exchange) throws Exception {
    final String reqMethod = exchange.getRequestMethod();
    final Io io = new Io();

    if ("POST".equals(reqMethod)) {
      final String body = ServerUtil.getRequestBody(exchange);
      io.putAllByJson(body);
    } else {
      throw new RuntimeException("Only POST method is valid. " + LogUtil.joinKeyVal("method", reqMethod));
    }
    return io;
  }
}
