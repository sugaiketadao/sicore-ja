package com.onepg.web;

import com.onepg.db.DbUtil;
import com.onepg.util.LogTxtHandler;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Webサーバークラス.
 * @hidden
 */
public final class StandaloneServer {

  /** Singletonインスタンス. */
  private static StandaloneServer instance = null;
  /** HTTPサーバー（Webサーバー）. */
  private HttpServer server = null;
  /** 停止処理実行済みフラグ. */
  private boolean terminated = false;

  /**
   * コンストラクタ.
   */
  private StandaloneServer() {
    // 処理なし
  }
  
  /**
   * インスタンス取得.
   * @return JsonHttpServerインスタンス
   */
  static synchronized StandaloneServer getInstance() {
    if (instance == null) {
      instance = new StandaloneServer();
    }
    return instance;
  }

  /**
   * メイン処理.<br>
   * <ul>
   * <li>Webサーバーを起動します。</li>
   * </ul>
   *
   * @param args 引数
   * @throws IOException I/O例外エラー
   */
  public static void main(final String[] args) {
    LogUtil.javaInfoStdout();
    LogUtil.stdout("Starting web server main processing. arguments=" + LogUtil.join(args));

    // シングルトンインスタンス取得
    final StandaloneServer myObj = getInstance();

    // シャットダウンフックを追加
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        myObj.terminate();
      }
    });

    // 自インスタンス実行
    try {
      myObj.start(args);
    } catch (Exception e) {
      // ここは先にログ出力する
      LogUtil.stdout(e, "An exception error occurred in web server startup. ");
      System.exit(1);
      return;
    }
    LogUtil.stdout("Ending web server main processing. ");
  }

  /**
   * Webサーバー起動.
   *
   * @param args 引数
   * @throws IOException I/O例外エラー
   */
  private void start(final String[] args) throws IOException {
    LogUtil.stdout("Starting web server startup processing. ");

    // Webサーバー生成
    final int portNo = ServerUtil.PROP_MAP.getInt("port.no");
    final int waitingProcessesCount = ServerUtil.PROP_MAP.getInt("waiting.processes.count");
    final int parallelProcessesCount = ServerUtil.PROP_MAP.getInt("parallel.processes.count");
    this.server = HttpServer.create(new InetSocketAddress(portNo), waitingProcessesCount);
    this.server.setExecutor(Executors.newFixedThreadPool(parallelProcessesCount));

    // ルートURLハンドラー
    LogUtil.stdout("Creating context. '/'");
    this.server.createContext("/", new RootHandler());

    // サーバー停止URLハンドラー
    final String serverStopContext = ServerUtil.PROP_MAP.getString("server.stop.context");
    LogUtil.stdout("Creating context. '/" + serverStopContext + "'");
    this.server.createContext("/" + serverStopContext, new StopHandler());

    // 静的ファイルハンドラー
    if (ServerUtil.PROP_MAP.containsKey("static.file.context")) {
      final String staticFileContext = ServerUtil.PROP_MAP.getString("static.file.context");
      LogUtil.stdout("Creating context. '/" + staticFileContext + "'");
      this.server.createContext("/" + staticFileContext, new StaticFileHandler());
    }
    
    // JSONサービスハンドラー
    if (ServerUtil.PROP_MAP.containsKey("json.service.context")) {
      final String jsonServiceContext = ServerUtil.PROP_MAP.getString("json.service.context");
      final String jsonServicePackage = ServerUtil.PROP_MAP.getString("json.service.package");
      LogUtil.stdout("Creating context. '/" + jsonServiceContext + "'" + " (Java package '" + jsonServicePackage + "')");
      this.server.createContext("/" + jsonServiceContext, new JsonServiceHandler(jsonServiceContext, jsonServicePackage));
    }

    // サインインサービスハンドラー
    if (ServerUtil.PROP_MAP.containsKey("signin.service.context")) {
      final String signinServiceContext = ServerUtil.PROP_MAP.getString("signin.service.context");
      LogUtil.stdout("Creating context. '/" + signinServiceContext + "'");
      this.server.createContext("/" + signinServiceContext, new SigninServiceHandler());
    }

    // 開始
    this.server.start();
    LogUtil.stdout("Web server started. " + LogUtil.joinKeyVal("port", String.valueOf(portNo), "parallel",
            String.valueOf(parallelProcessesCount), "stopUrl", String.valueOf(serverStopContext)));
  }

  /**
   * ルートURLハンドラー.
   */
  private class RootHandler implements HttpHandler {
    @Override
    public void handle(final HttpExchange exchange) throws IOException {
      ServerUtil.responseText(exchange, "HTTP server is running.");
    }
  }

  /**
   * サーバー停止ハンドラー.
   */
  private class StopHandler implements HttpHandler {
    @Override
    public void handle(final HttpExchange exchange) throws IOException {
      try {
        // レスポンスを先に送信
        ServerUtil.responseText(exchange, "HTTP server shutdown...", "Reload to confirm shutdown.");
        // terminate メソッドを呼び出して完全なクリーンアップを実行
        StandaloneServer.this.terminate();
        
        // レスポンス送信完了を待つため100ms待機
        TimeUnit.MILLISECONDS.sleep(100);
        // JVMプロセスを確実に終了
        System.exit(0);
        
      } catch (final Exception | Error e) {
        LogUtil.stdout(e, "An exception error occurred in web server stop handler. ");
        // エラーが発生した場合でもプロセス終了
        System.exit(1);
      }
    }
  }

  /**
   * 停止時処理.
   */
  synchronized void terminate() {
    // 既に停止処理が実行済みの場合は何もしない（重複実行防止）
    if (this.terminated) {
      return;
    }
    this.terminated = true;
    
    LogUtil.stdout("Starting web server stop processing. ");
    try {
      // Webサーバー停止
      if (!ValUtil.isNull(this.server)) {
        this.server.stop(0);
        this.server = null;
        LogUtil.stdout("Web server stopped.");
      }
    } catch (final Exception | Error e) {
      LogUtil.stdout(e, "An exception error occurred in web server stop.");
    }
    try {
      // プーリングDB切断
      DbUtil.closePooledConn();
    } catch (final Exception | Error e) {
      LogUtil.stdout(e, "An exception error occurred in disconnecting pooled DB connections. ");
    }
    try {
      // ログテキストファイルを閉じる
      LogTxtHandler.closeAll();
    } catch (final Exception | Error e) {
      LogUtil.stdout(e, "An exception error occurred in log text file close.");
    }
    // System.exit() は呼ばない
    // シャットダウンフック経由の場合は JVMが既に終了プロセス中のため
    // HTTP経由の場合は StopHandler側でSystem.exit()を実行のため
  }
}
