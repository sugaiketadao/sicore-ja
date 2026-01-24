package com.onepg.web;

import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

/**
 * HTTPハンドラー 基底クラス.<br>
 * <ul>
 * <li>共通のエラーハンドリングを提供します。</li>
 * <li>サブクラスで <code>doExecute</code>メソッドを実装することで具体的な HTTPリクエスト処理を定義します。</li>
 * <li>サブクラスでクラス変数を使用した場合、その変数は複数のリクエストで共有されます。</li>
 * </ul>
 * @hidden
 */
abstract class AbstractHttpHandler implements HttpHandler {

  /** ログライター. */
  protected final LogWriter logger;

  /**
   * メイン処理.<br>
   * <ul>
   * <li>サブクラスで具体的な HTTPリクエスト処理を実装します。</li>
   * </ul>
   *
   * @param exchange HTTP送受信データ
   * @throws Exception 例外エラー
   */
  protected abstract void doExecute(final HttpExchange exchange) throws Exception;

  /**
   * コンストラクタ.
   */
  AbstractHttpHandler() {
    super();
    this.logger = LogUtil.newLogWriter(getClass());
  }

  /**
   * メイン処理の呼び出し.<br>
   * <ul>
   * <li>例外発生時は適切なエラーレベルでログ出力し、エラーレスポンスを返却します。</li>
   * </ul>
   *
   * @param exchange HTTP送受信データ
   * @throws IOException I/O例外エラー
   */
  @Override
  public void handle(final HttpExchange exchange) throws IOException {
    try {
      doExecute(exchange);
    } catch (final Exception | Error e) {
      this.logger.error(e, "An exception error occurred in HTTP handler processing. ");
      try {
        ServerUtil.responseText(exchange, e, "Unexpected http handler error. ");
      } catch (IOException re) {
        this.logger.error(re, "An exception error occurred while outputting HTTP handler error response. ");
        try {
          exchange.close();
        } catch (Exception ce) {
          this.logger.error(ce, "An exception error occurred in HttpExchange close. ");
        }
      }
    }
    // 開発者モードの場合はログをフラッシュする
    if (this.logger.isDevelopMode()) {
      this.logger.flush();
    }
  }
}
