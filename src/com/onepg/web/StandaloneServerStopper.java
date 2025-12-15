package com.onepg.web;

import com.onepg.util.LogUtil;
import com.onepg.util.PropertiesUtil;
import com.onepg.util.PropertiesUtil.FwPropertiesName;
import com.onepg.util.IoItems;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * StandaloneHttpServer停止クラス.
 * @hidden
 */
public final class StandaloneServerStopper {

  /** HTTP接続タイムアウト（ミリ秒）. */
  private static final int HTTP_TIMEOUT_MS = 5000;

  /**
   * メイン処理.
   * @param args コマンドライン引数
   */
  public static void main(final String[] args) {
    try {
      // Webサーバー設定
      final IoItems propMap = PropertiesUtil.getFrameworkProps(FwPropertiesName.WEB);
      final int portNo = propMap.getInt("port.no");
      final String serverStopContext = propMap.getString("server.stop.context");
      // 停止リクエスト送信
      stopHttpServer(portNo, serverStopContext);
      // 停止確認
      confirmServerStop(portNo);

      LogUtil.stdout("Web server stop processing completed.");
      System.exit(0);

    } catch (final Exception e) {
      LogUtil.stdout(e, "An exception error occurred in web server stop. ");
      System.exit(1);
    }
  }

  /**
   * Webサーバー停止リクエスト送信.<br>
   *
   * @param port 対象ポート
   * @param stopContext 停止コンテキスト
   * @throws IOException HTTP通信エラー
   */
  private static void stopHttpServer(final int port, final String stopContext) throws IOException {
    final String stopUrl = "http://localhost:" + String.valueOf(port) + "/" + stopContext;
    LogUtil.stdout("Sending stop request. " + LogUtil.joinKeyVal("url", stopUrl));

    HttpURLConnection connection = null;
    try {
      final URI uri = new URI(stopUrl);
      final URL url = uri.toURL();
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(HTTP_TIMEOUT_MS);
      connection.setReadTimeout(HTTP_TIMEOUT_MS);

      final int responseCode = connection.getResponseCode();
      LogUtil.stdout("Stop request response received. " + LogUtil.joinKeyVal("responseCode", responseCode));

      if (responseCode == HttpURLConnection.HTTP_OK) {
        // レスポンス内容を読み取り
        try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
          final String response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
          LogUtil.stdout("Stop request response content: " + response);
        }
      } else {
        LogUtil.stdout("Stop request received error response. " + LogUtil.joinKeyVal("responseCode", responseCode));
      }
    } catch (final java.net.URISyntaxException e) {
      throw new RuntimeException("Invalid URL format. " + LogUtil.joinKeyVal("url", stopUrl), e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  /**
   * サーバー停止確認.<br>
   *
   * @param port 対象ポート
   */
  private static void confirmServerStop(final int port) {
    final String checkUrl = "http://localhost:" + String.valueOf(port) + "/";
    LogUtil.stdout("Starting server stop confirmation.");

    // 最大10回、1秒間隔で確認
    for (int i = 0; i < 10; i++) {
      try {
        Thread.sleep(1000); // 1秒待機

        final URI uri = new URI(checkUrl);
        final URL url = uri.toURL();
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(1000);

        final int responseCode = connection.getResponseCode();
        connection.disconnect();

        LogUtil.stdout("Server is still running. " + LogUtil.joinKeyVal("attempt", i + 1, "responseCode", responseCode));
      } catch (final java.net.URISyntaxException e) {
        throw new RuntimeException("Invalid URL format. " + LogUtil.joinKeyVal("url", checkUrl), e);
      } catch (final Exception e) {
        // 接続エラー = サーバーが停止した
        LogUtil.stdout("Server has stopped. " + LogUtil.joinKeyVal("attempt", i + 1));
        return;
      }
    }

    LogUtil.stdout("Warning: Server stop confirmation timed out. Please check server status manually.");
  }

}