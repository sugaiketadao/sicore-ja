package com.onepg.web;

import com.onepg.util.FileUtil;
import com.onepg.util.LogUtil;
import com.onepg.util.PropertiesUtil;
import com.onepg.util.ValUtil.CharSet;
import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.net.HttpURLConnection;

/**
 * 静的ファイルハンドラークラス.
 * @hidden
 */
final class StaticFileHandler extends AbstractHttpHandler {

  /** 静的ファイルエンコーディング. */
  private static final CharSet STATIC_FILE_CHARSET = CharSet.UTF8;

  /** サーバー配備ディレクトリパス. */
  private final String serverDeployPath;
  
  /**
   * コンストラクタ.
   */
  StaticFileHandler() {
    super();
    this.serverDeployPath = PropertiesUtil.APPLICATION_DIR_PATH;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doExecute(final HttpExchange exchange) throws Exception {
    // リクエストファイル
    final String reqPath = exchange.getRequestURI().getPath();
    final String reqFilePath;

    if (reqPath.endsWith("/")) {
      // 最後がスラッシュ終わりの場合は index.html を表示
      reqFilePath = FileUtil.joinPath(this.serverDeployPath, reqPath, "index.html");
    } else {
      reqFilePath = FileUtil.joinPath(this.serverDeployPath, reqPath);
    }
    final File reqFile = new File(reqFilePath);

    // doExecute内でファイルアクセス前にパストラバーサルチェックを追加
    final String canonicalPath = reqFile.getCanonicalPath();
    if (!canonicalPath.startsWith(this.serverDeployPath)) {
      super.logger.error("Path traversal attack detected. " + LogUtil.joinKeyVal("request", reqPath));
      ServerUtil.responseText(exchange, HttpURLConnection.HTTP_FORBIDDEN,
          "Access is invalid. " + LogUtil.joinKeyVal("filename", reqFile.getName()));
      return;
    }

    // アクセスファイル妥当性チェック
    if (!checkAccessFile(reqFilePath)) {
      super.logger.error("File is not accessible. " + LogUtil.joinKeyVal("request", reqPath));
      ServerUtil.responseText(exchange, HttpURLConnection.HTTP_FORBIDDEN,
          "Access is invalid. " + LogUtil.joinKeyVal("filename", reqFile.getName()));
      return;
    }

    if (!reqFile.exists()) {
      // ファイルが見つからない場合はエラー
      ServerUtil.responseText(exchange, HttpURLConnection.HTTP_NOT_FOUND,
          "File does not exist. " + LogUtil.joinKeyVal("requestPath", reqPath));
      return;
    }

    if (reqFile.isDirectory()) {
      // ディレクトリの場合、元のURLにスラッシュを付与してリダイレクト
      final String reqlUrl = ServerUtil.getRequestFullUrl(exchange, "/", false);
      if (super.logger.isDevelopMode()) {
        super.logger.develop("Directory specified, redirecting. "
                + LogUtil.joinKeyVal("request", reqPath)
                + LogUtil.joinKeyVal("redirect", reqlUrl));
      }
      ServerUtil.responseRedirect(exchange, reqlUrl);
      return;
    }
    if (super.logger.isDevelopMode()) {
      super.logger.develop("Static file accessed. " + LogUtil.joinKeyVal("path", reqPath));
    }
    // ファイルをそのまま（静的に）返す
    ServerUtil.responseFile(exchange, reqFile, STATIC_FILE_CHARSET);
  }

  /**
   * アクセスファイル妥当性チェック.
   * 
   * @param filePath ファイルパス
   */
  private boolean checkAccessFile(final String filePath) {
    final String fileName = new File(filePath).getName().toLowerCase();
    // 隠しファイル・設定ファイルのアクセス禁止
    // 実行ファイルのアクセス禁止
    // バックアップファイルのアクセス禁止
    if (fileName.startsWith(".")
        || fileName.endsWith(".xml")
        || fileName.endsWith(".conf")
        || fileName.endsWith(".properties")
        || fileName.endsWith(".json")
        || fileName.endsWith(".log")
        || fileName.endsWith(".exe")
        || fileName.endsWith(".bat")
        || fileName.endsWith(".sh")
        || fileName.endsWith(".com")
        || fileName.endsWith(".class")
        || fileName.endsWith(".bak")
        || fileName.endsWith(".old")
        || fileName.endsWith(".org")
        || fileName.endsWith(".backup")
        || fileName.endsWith("~")
        || fileName.contains(".backup.")) {
      return false;
    }
    
    return true;
  }
}
