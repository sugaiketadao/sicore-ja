package com.onepg.web;

import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.onepg.util.PropertiesUtil;
import com.onepg.util.ValUtil;
import com.onepg.util.ValUtil.CharSet;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Webサーバーユーティリティクラス.<br>
 * <ul>
 * <li>HTTPレスポンスの共通処理を提供します。</li>
 * <li>テキスト、ファイル、リダイレクトなど各種レスポンス形式をサポートします。</li>
 * </ul>
 * @hidden
 */
final class ServerUtil {

  /** ログライター. */
  private static final LogWriter logger = LogUtil.newLogWriter(ServerUtil.class);

  /** 最適バッファサイズ（バイト）. */
  private static final int OPTIMAL_BUFFER_SIZE = calcBufferSize();
  /** テキスト圧縮対象ファイルサイズ下限（1KB）. */
  private static final long TXT_TO_COMPRESS_MIN_SIZE = 1024;
  /** テキスト圧縮対象ファイルサイズ上限（1MB）. */
  private static final long TXT_TO_COMPRESS_MAX_SIZE = 1024 * 1024;
  
  /** HTTP日付フォーマット (RFC 1123). */
  private static final DateTimeFormatter DTF_HTTP_DATE = 
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)
                       .withZone(ZoneId.of("GMT"));

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>ユーティリティクラスのためインスタンス化を禁止します。</li>
   * </ul>
   */
  private ServerUtil() {
    // 処理なし
  }

  /**
   * 最適バッファサイズ計算.
   * @return バッファサイズ（バイト）
   */
  private static int calcBufferSize() {
    // 利用可能メモリ
    final long maxMemory = Runtime.getRuntime().maxMemory();
    // 4GB以上
    if (maxMemory > 4L * 1024 * 1024 * 1024) {
      // 64KB
      return 65536;
    }
    // 1GB以上
    if (maxMemory > 1024 * 1024 * 1024) {
      // 32KB
      return 32768;
    }
    // 基本サイズ 8KB
    return 8192;
  }

  /**
   * テキストレスポンス表示.<br>
   * <ul>
   * <li>OK(200) ステータスで返します。</li>
   * </ul>
   *
   * @param exchange HTTP送受信データ
   * @param txts 表示テキスト（複数可能）
   * @throws IOException I/O例外エラー
   */
  static void responseText(final HttpExchange exchange, final String... txts) throws IOException {
    responseText(exchange, HttpURLConnection.HTTP_OK, txts);
  }

  /**
   * エラーテキストレスポンス表示.<br>
   * <ul>
   * <li>Internal Server Error(500) ステータスで返します。</li>
   * <li>エラーの詳細情報（スタックトレース）も含めてレスポンスします。</li>
   * </ul>
   *
   * @param exchange HTTP送受信データ
   * @param e        エラーオブジェクト
   * @param txts     表示テキスト（複数可能）
   * @throws IOException I/O例外エラー
   */
  static void responseText(final HttpExchange exchange, final Throwable e, final String... txts)
      throws IOException {
    // スタックトレースをレスポンスに含める
    final String[] errTxts = new String[txts.length + 1];
    System.arraycopy(txts, 0, errTxts, 0, txts.length);
    errTxts[txts.length] = LogUtil.getStackTrace(ValUtil.LF, e);
    responseText(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, errTxts);
  }

  /**
   * テキストレスポンス表示.<br>
   * <ul>
   * <li>指定されたHTTPステータスコードでテキストレスポンスを返します。</li>
   * </ul>
   *
   * @param exchange   HTTP送受信データ
   * @param httpStatus HTTPステータスコード（HttpURLConnection.HTTP_*）
   * @param txts       表示テキスト（複数可能）
   * @throws IOException I/O例外エラー
   */
  static void responseText(final HttpExchange exchange, final int httpStatus, final String... txts)
      throws IOException {
    final String txt = ValUtil.join(ValUtil.LF, txts);
    final Headers headers = exchange.getResponseHeaders();
    setSecurityHeaders(headers);
    headers.set("Content-Type", "text/plain; charset=UTF-8");
    headers.set("Cache-Control", "no-cache");
    responseCompressed(exchange, httpStatus, txt);
  }


  /**
   * JSON レスポンス表示.<br>
   * <ul>
   * <li>OK(200) ステータスで返します。</li>
   * </ul>
   *
   * @param exchange HTTP送受信データ
   * @param json     JSON文字列
   * @throws IOException I/O例外エラー
   */
  static void responseJson(final HttpExchange exchange, final String json)
      throws IOException {
    if (ValUtil.isBlank(json)) {
      throw new RuntimeException("Response JSON string is empty. ");
    }
    final Headers headers = exchange.getResponseHeaders();
    setSecurityHeaders(headers);
    headers.set("Content-Type", "application/json; charset=UTF-8");
    headers.set("Cache-Control", "no-cache");
    responseCompressed(exchange, HttpURLConnection.HTTP_OK, json);
  }

  /**
   * ファイル表示.<br>
   * <ul>
   * <li>指定されたファイルの内容をレスポンスとして返します。</li>
   * <li>ファイルタイプに応じて適切なContent-Typeを設定します。</li>
   * <li>ファイル更新日時をチェックして適切にキャッシュを破棄します。</li>
   * </ul>
   *
   * @param exchange HTTP送受信データ
   * @param resFile 表示ファイル
   * @param charSet 文字セット
   * @return キャッシュを使用した場合は <code>true</code>、何らかのエラーも含めそれ以外は <code>false</code>
   * @throws IOException I/O例外エラー
   */
  static boolean responseFile(final HttpExchange exchange, final File resFile, final CharSet charSet)
      throws IOException {
    // ファイル存在チェック
    if (!resFile.exists() || !resFile.isFile()) {
      responseText(exchange, HttpURLConnection.HTTP_NOT_FOUND, "File not found. " +  LogUtil.joinKeyVal("filename", resFile.getName()));
      return false;
    }

    // パストラバーサル攻撃対策
    final String canonicalPath = resFile.getCanonicalPath();
    if (!canonicalPath.startsWith(PropertiesUtil.APPLICATION_DIR_PATH)) {
      responseText(exchange, HttpURLConnection.HTTP_FORBIDDEN, "Access denied. " +  LogUtil.joinKeyVal("filename", resFile.getName()));
      return false;
    }

    // サーバー側ファイル更新日時シリアル値（ミリ秒）
    final long serverModMsec = resFile.lastModified();
    // サーバー側ファイル更新日時文字列
    final String serverModVal = DTF_HTTP_DATE.format(Instant.ofEpochMilli(serverModMsec));
    // クライアント側ファイル更新日時文字列
    final String clientModVal = exchange.getRequestHeaders().getFirst("If-Modified-Since");

    if (isUseCache(serverModMsec, serverModVal, clientModVal)) {
      // 未更新の場合は 304 を返し、キャッシュの利用を促す
      final Headers headers = exchange.getResponseHeaders();
      setSecurityHeaders(headers);
      headers.set("Last-Modified", serverModVal);
      headers.set("Cache-Control", "max-age=0, must-revalidate");
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, -1);
      if (logger.isDevelopMode()) {
        logger.develop("Using client-side cache. " + LogUtil.joinKeyVal("filename", resFile.getName(),
            "lastModified", serverModVal));
      }
      return true;
    }

    if (logger.isDevelopMode()) {
      logger.develop("Returning latest file. " + LogUtil.joinKeyVal("filename", resFile.getName(), "lastModified", serverModVal));
    }

    // コンテンツタイプ確認
    final String checkCtype;
    
    // Files#probeContentType()が WindowsOSで不正確な場合があるため、先に拡張子ベースで決定
    final String fileName = resFile.getName().toLowerCase();
    if (fileName.endsWith(".css")) {
      checkCtype = "text/css";
    } else if (fileName.endsWith(".js")) {
      checkCtype = "application/javascript";
    } else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
      checkCtype = "text/html";
    } else if (fileName.endsWith(".svg")) {
      checkCtype = "image/svg+xml";
    } else if (fileName.endsWith(".woff") || fileName.endsWith(".woff2")) {
      checkCtype = "font/woff";
    } else if (fileName.endsWith(".ttf")) {
      checkCtype = "font/ttf";
    } else if (fileName.endsWith(".eot")) {
      checkCtype = "application/vnd.ms-fontobject";
    } else {
      // その他はJava標準のMIMEタイプを使用
      checkCtype = Files.probeContentType(resFile.toPath());
    }
    
    // テキスト判定
    final boolean isText = (checkCtype.startsWith("text/")
        || checkCtype.endsWith("/javascript")
        || checkCtype.endsWith("/json")
        || checkCtype.endsWith("/xml")
        || checkCtype.endsWith("+xml"));
    
    // ヘッダ設定コンテンツタイプ
    final String headCtype;
    if (ValUtil.isBlank(checkCtype)) {
      // 不明な場合はバイナリとして扱う
      headCtype = "application/octet-stream";
    } else {
      if (isText && !checkCtype.contains("charset=")) {
        // 文字セット指定を付与
        headCtype = checkCtype + "; charset=" + charSet.toString();
      } else {
        headCtype = checkCtype;
      }
    }
    
    final Headers headers = exchange.getResponseHeaders();
    setSecurityHeaders(headers);
    headers.set("Content-Type", headCtype);
    // キャッシュ制御
    headers.set("Last-Modified", serverModVal);
    headers.set("Cache-Control", "max-age=0, must-revalidate");
    headers.set("ETag", "\"" + serverModMsec + "-" + resFile.length() + "\"");

    final long fileSize = resFile.length();
    if (isText && TXT_TO_COMPRESS_MIN_SIZE < fileSize && fileSize <= TXT_TO_COMPRESS_MAX_SIZE) {
      // テキストファイルで圧縮対象サイズの場合は圧縮してレスポンス
      final String fileContent = Files.readString(resFile.toPath(),
          java.nio.charset.Charset.forName(charSet.toString()));
      responseCompressed(exchange, HttpURLConnection.HTTP_OK, fileContent);
      return false;
    }

    // 圧縮対象外はそのままレスポンス
    responseCopyOrStream(exchange, resFile);

    return false;
  }

  /**
   * キャッシュ使用判定.<br>
   * <ul>
   * <li>サーバー側のファイル更新日時とクライアント側のIf-Modified-Sinceヘッダーを比較します。</li>
   * <li>クライアント側の日時がサーバー側の日時以降であればキャッシュを使用可能と判定します。</li>
   * <li>日時の比較は1秒の誤差を考慮して行います。</li>
   * <li>日時パースに失敗した場合は文字列の完全一致で判定します。</li>
   * </ul>
   * 
   * @param serverModMsec サーバー側ファイル更新日時シリアル値（ミリ秒）
   * @param serverModVal  サーバー側ファイル更新日時（RFC 1123形式文字列）
   * @param clientModVal  クライアント側If-Modified-Sinceヘッダー（RFC 1123形式文字列、<code>null</code>可）
   * @return キャッシュ使用可の場合は <code>true</code>
   */
  private static boolean isUseCache(final long serverModMsec, final String serverModVal, final String clientModVal) {
    if (ValUtil.isNull(clientModVal)) {
      return false;
    }
    try {
      // クライアント側ファイル更新日時
      final ZonedDateTime clientModTime = ZonedDateTime.parse(clientModVal, DTF_HTTP_DATE);
      // シリアル値（ミリ秒）
      final long clientModMsec = clientModTime.toInstant().toEpochMilli();
      // ファイルの更新日時がクライアント側の日時以降でない場合（1秒の誤差を考慮して比較）
      if (serverModMsec <= clientModMsec + 1000) {
        return true;
      }
    } catch (Exception e) {
      // パースエラーの場合は文字列比較
      if (serverModVal.equals(clientModVal)) {
        return true;
      }
    }
    return false;
  }

  /**
   * リダイレクト表示.<br>
   * <ul>
   * <li>指定されたURLにリダイレクトします。</li>
   * <li>301 Moved Permanentlyステータスを使用します。</li>
   * </ul>
   *
   * @param exchange HTTP送受信データ
   * @param url リダイレクトURL
   * @throws IOException I/O例外エラー
   */
  static void responseRedirect(final HttpExchange exchange, final String url) throws IOException {
    final Headers headers = exchange.getResponseHeaders();
    setSecurityHeaders(headers);
    headers.set("Location", url);
    exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM, -1);
    // レスポンスボディは空
    exchange.getResponseBody().close();
  }

  /**
   * リクエストフルURL取得.<br>
   * <ul>
   * <li>現在のリクエストから完全なURLを構築します。</li>
   * <li>オプションでパス追加やクエリ除去が可能です。</li>
   * </ul>
   *
   * @param exchange HTTP送受信データ
   * @param addPath 追加パス
   * @param trimQuery クエリー除去する場合は <code>true</code>
   * @return フルURL
   */
  static String getRequestFullUrl(final HttpExchange exchange, final String addPath,
      final boolean trimQuery) {
    final String protocol = ValUtil.splitReg(exchange.getProtocol().toLowerCase(), "/")[0];
    final String host = exchange.getRequestHeaders().getFirst("Host");
    final URI uri = exchange.getRequestURI();
    final String path = uri.getPath();
    final String query = uri.getQuery();
    
    final StringBuilder urlSb = new StringBuilder();
    urlSb.append(protocol).append("://").append(host).append(path);
    if (!ValUtil.isBlank(addPath)) {
      urlSb.append(addPath);
    }
    if (!ValUtil.isBlank(query) && !trimQuery) {
      urlSb.append("?").append(query);
    }
    return urlSb.toString();
  }

  /**
   * リクエストボディ取得（POSTデータ取得）.<br>
   * <ul>
   * <li>HTTPリクエストのボディ部分を文字列として取得します。</li>
   * <li>リソースの適切なクローズを保証します。</li>
   * </ul>
   *
   * @param exchange HTTP送受信データ
   * @return リクエスト文字列
   * @throws IOException I/O例外エラー
   */
  static String getRequestBody(final HttpExchange exchange) throws IOException {
    final StringBuilder sb = new StringBuilder();
    try (final InputStream is = exchange.getRequestBody();
         final BufferedReader br = new BufferedReader(
             new InputStreamReader(is, ValUtil.UTF8))) {
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
    }
    return sb.toString();
  }

  /**
   * セキュリティヘッダー設定.
   * 
   * @param headers HTTP送受信ヘッダー
   */
  private static void setSecurityHeaders(final Headers headers) {
    // XSS保護
    headers.set("X-Content-Type-Options", "nosniff");
    headers.set("X-Frame-Options", "DENY");
    headers.set("X-XSS-Protection", "1; mode=block");
        
    // CSP（Content Security Policy）
    headers.set("Content-Security-Policy", 
        "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");
    
    // リファラー制御
    headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
  }

  /**
   * ファイルレスポンスコピーまたはストリームレスポンス（通常処理）.
   * 
   * @param exchange HTTP送受信データ
   * @param resFile  表示ファイル
   */
  private static void responseCopyOrStream(final HttpExchange exchange, final File resFile)
      throws IOException, FileNotFoundException {
    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, resFile.length());
    try (final OutputStream os = exchange.getResponseBody()) {
      if (resFile.length() <= OPTIMAL_BUFFER_SIZE) {
        Files.copy(resFile.toPath(), os);
      } else {
        try (final FileInputStream fis = new FileInputStream(resFile)) {
          final byte[] buffer = new byte[OPTIMAL_BUFFER_SIZE];
          int bytesRead;
          while ((bytesRead = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
          }
        }
      }
    }
  }

  /**
   * 圧縮対応レスポンス.
   * @param exchange HTTP送受信データ
   * @param httpStatus HTTPステータスコード（HttpURLConnection.HTTP_*）
   * @param resTxt レスポンステキスト
   */
  private static void responseCompressed(final HttpExchange exchange, final int httpStatus, 
        final String resTxt) throws IOException {

    final Headers headers = exchange.getResponseHeaders();
    final String acceptEncoding = ValUtil.nvl(exchange.getRequestHeaders().getFirst("Accept-Encoding"));
    
    byte[] resBytes;
    if (acceptEncoding.contains("gzip") && resTxt.length() > 1024) {
        // GZIP圧縮
        resBytes = compresseGzip(resTxt.getBytes(ValUtil.UTF8));
        headers.set("Content-Encoding", "gzip");
    } else {
        resBytes = resTxt.getBytes(ValUtil.UTF8);
    }
    
    exchange.sendResponseHeaders(httpStatus, resBytes.length);
    try (final OutputStream os = exchange.getResponseBody()) {
        os.write(resBytes);
    }
  }

  /**
   * GZIP圧縮処理.
   * @param data 圧縮対象データ
   * @return 圧縮データ
   */
  private static byte[] compresseGzip(final byte[] data) throws IOException {
    try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
         final GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
        gzos.write(data);
        gzos.finish();
        return baos.toByteArray();
    }
  }
}
