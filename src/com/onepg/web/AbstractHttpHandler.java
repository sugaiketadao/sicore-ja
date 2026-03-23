package com.onepg.web;

import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.HttpURLConnection;

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

  /** JWT検証フラグ. */
  private final boolean isJwtValidate;

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
    this(true);
  }

  /**
   * コンストラクタ.
   * 
   * @param isJwtValidate JWT検証フラグ（JWT検証不要な場合は <code>false</code> を指定）
   */
  AbstractHttpHandler(final boolean isJwtValidate) {
    super();
    this.logger = LogUtil.newLogWriter(getClass());
    this.isJwtValidate = (ServerUtil.LDAP_ENABLED && isJwtValidate);
    if (!this.isJwtValidate) {
      this.logger.info("Jwt validation is disabled.");
    }
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
      // JWT 検証
      if (this.isJwtValidate) {
        if (!validateJwt(exchange)) {
          // JWT 検証失敗はエラーレスポンスを返して処理終了
          ServerUtil.responseText(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized. ");
          return;
        }
      }
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

  /**
   * JWT検証.<br>
   * <ul>
   * <li>AuthorizationヘッダーからJWTを取得し、検証します。</li>
   * <li>JWTが無効な場合はエラーログを出力し、<code>false</code> を返します。</li>
   * <li>通常ここではJWTが有効であることを前提としエラーログを出力します。</li>
   * </ul>
   *
   * @param exchange HTTP送受信データ
   * @return 検証成功の場合は <code>true</code>
   */
  private boolean validateJwt(final HttpExchange exchange) {
    final String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      this.logger.info("Authorization header is missing or invalid. " + LogUtil.joinKeyVal("request", exchange.getRequestURI().getPath()));
      return false;
    }
    try {
      JwtUtil.validateToken(authHeader.substring(7));
      return true;
    } catch (final Exception e) {
      this.logger.error(e, "JWT validation failed. " + LogUtil.joinKeyVal("request", exchange.getRequestURI().getPath()));
      return false;
    }
  }
  
  /**
   * サービスインスタンスの生成.<br>
   * <ul>
   * <li>クラス名からサービスインスタンスを生成し、型チェックを行います。</li>
   * </ul>
   *
   * @param clsName クラス名
   * @return サービスインスタンス
   * @throws Exception インスタンス生成エラー
   */
  protected AbstractWebService createWebServiceClsInstance(final String clsName) throws Exception {
    final Class<?> cls = getCls(clsName);
    final Object clsObj = cls.getDeclaredConstructor().newInstance();

    if (!(clsObj instanceof AbstractWebService)) {
      throw new RuntimeException("Classes not inheriting from web service base class (AbstractWebService) cannot be executed. ");
    }

    return (AbstractWebService) clsObj;
  }

  /**
   * クラスの取得.<br>
   * <ul>
   * <li>クラス名からClassオブジェクトを取得します。</li>
   * </ul>
   *
   * @param clsName クラス名
   * @return Classオブジェクト
   */
  private Class<?> getCls(final String clsName) {
    try {
      return Class.forName(clsName);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException("Web service class not found. " + LogUtil.joinKeyVal("class", clsName), e);
    }
  }
  
}
