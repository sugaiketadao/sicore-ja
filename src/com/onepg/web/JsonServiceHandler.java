package com.onepg.web;

import com.onepg.util.Io;
import com.onepg.util.IoItems;
import com.onepg.util.LogUtil;
import com.onepg.util.ResourcesUtil;
import com.onepg.util.ResourcesUtil.FwResourceName;
import com.sun.net.httpserver.HttpExchange;
import java.net.HttpURLConnection;

/**
 * JSON サービスハンドラークラス.<br>
 * <ul>
 * <li>HTTPリクエストを受信し、対応するWebサービスクラスに処理を委譲します。</li>
 * <li>URLパスからサービスクラス名を動的に解決し、リフレクションで実行します。</li>
 * </ul>
 * @hidden
 */
final class JsonServiceHandler extends AbstractHttpHandler {

  /** 自コンテキストパス. */
  private final String contextPath;

  /** サービスクラスパッケージ. */
  private final String svcClsPackage;

  /** メッセージマップ＜メッセージID、メッセージテキスト＞. */
  private final IoItems msgMap;

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>コンテキストパスとサービスクラスパッケージを設定します。</li>
   * </ul>
   *
   * @param contextPath コンテキストパス
   * @param svcClsPackage サービスクラスパッケージ
   */
  JsonServiceHandler(final String contextPath, final String svcClsPackage) {
    super();
    this.contextPath = contextPath;
    this.svcClsPackage = svcClsPackage;
    this.msgMap = ResourcesUtil.getJson(FwResourceName.MSG);
  }

  /**
   * {@inheritDoc}
   * <ul>
   * <li>リクエストURLからサービスクラスを動的に解決し実行します。</li>
   * <li>GET/POSTメソッドに応じてパラメーターを処理します。</li>
   * </ul>
   */
  @Override
  protected void doExecute(final HttpExchange exchange) throws Exception {
    // リクエストパス
    final String reqPath = exchange.getRequestURI().getPath();
    // クラス名構築
    final String clsName = buildClsNameByReq(reqPath);
    
    try {
      // サービスクラスの生成と検証
      final AbstractWebService serviceObj = createWebServiceClsInstance(clsName);
      
      // リクエストパラメーターの処理
      final Io io = reqToIoParams(exchange, clsName);
      
      // サービス処理実行
      serviceObj.execute(io);
      
      // レスポンス
      final String resJson = io.createJsonWithMsg(this.msgMap);
      ServerUtil.responseJson(exchange, resJson);
      
    } catch (final ClassNotFoundException e) {
      super.logger.error(e, "Web service class not found. " + LogUtil.joinKeyVal("class", clsName));
      ServerUtil.responseText(exchange, HttpURLConnection.HTTP_NOT_FOUND, "Json service class not found. ");
    } catch (final Exception | Error e) {
      super.logger.error(e, "An exception error occurred in web service execution. " + LogUtil.joinKeyVal("class", clsName));
      ServerUtil.responseText(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "Unexpected json service error. ");
    }
  }

  /**
   * クラス名の構築.<br>
   * <ul>
   * <li>リクエストパスからサービスクラス名を生成します。</li>
   * </ul>
   *
   * @param reqPath リクエストパス
   * @return クラス名
   */
  private String buildClsNameByReq(final String reqPath) {
    return this.svcClsPackage + "."
        + reqPath.replace("/" + this.contextPath + "/", "").replace("/", ".");
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
  private AbstractWebService createWebServiceClsInstance(final String clsName) throws Exception {
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
   * @throws ClassNotFoundException クラスが見つからない場合
   */
  private Class<?> getCls(final String clsName) {
    try {
      return Class.forName(clsName);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException("Web service class not found. " + LogUtil.joinKeyVal("class", clsName), e);
    }
  }
  
  /**
   * リクエストパラメーターをI/Oパラメーターに変換.<br>
   * <ul>
   * <li>HTTPメソッドに応じてパラメーターを解析しIoオブジェクトに設定します。</li>
   * </ul>
   *
   * @param exchange HTTP送受信データ
   * @param clsName クラス名（ログ用）
   * @return パラメーターを含むIoオブジェクト
   * @throws Exception パラメーター処理エラー
   */
  private Io reqToIoParams(final HttpExchange exchange, final String clsName) throws Exception {
    final String reqMethod = exchange.getRequestMethod();
    final Io io = new Io();

    if ("GET".equals(reqMethod)) {
      final String query = exchange.getRequestURI().getQuery();
      io.putAllByUrlParam(query);
    } else if ("POST".equals(reqMethod)) {
      final String body = ServerUtil.getRequestBody(exchange);
      io.putAllByJson(body);
    } else {
      throw new RuntimeException("Only GET or POST method is valid. " + LogUtil.joinKeyVal("method", reqMethod));
    }
    return io;
  }
}