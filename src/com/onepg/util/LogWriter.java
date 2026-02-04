package com.onepg.util;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;


/**
 * ログライタークラス.<br>
 * <ul>
 * <li>個別処理からのログ出力を受け付ける。</li>
 * <li>ログテキストの整形を受け持つ。</li>
 * <li>ログテキストハンドラー <code>LogTxtHandler</code> からテキストライターインスタンスを取得してログ出力する。</li>
 * <li>情報用とエラー用、２つのログテキストハンドラーを保持する。</li>
 * <li>開発モードの場合はコンソールにもログ出力する。</li>
 * <li>ログテキストのオープン・クローズはログテキストハンドラーが受け持ち、本クラスではオープン・クローズしない。</li>
 * </ul>
 */
public final class LogWriter {

  /** 生成クラス名. */
  private final String clsName;
  /** スレッド名. */
  private final String threadName;
  /** 情報ログテキストハンドラー. */
  private final LogTxtHandler infHdr;
  /** エラーログテキストハンドラー. */
  private final LogTxtHandler errHdr;
  /** コンソールライター（開発用）. */
  private final PrintWriter console;

  /** 追跡コード. */
  private final String traceCode;
  /** 情報ログプレフィックス. */
  private final String infPrefix;
  /** エラーログプレフィックス. */
  private final String errPrefix;
  /** 開発ログプレフィックス. */
  private final String devPrefix;
  /** 開始終了ログサフィックス. */
  private final String beginEndSuffix;
  /** 開発モードフラグ. */
  private final boolean isDevelopMode;
  /** 経過時間計測開始時刻. */
  private long watchStartTime = 0;

  /** 日時フォーマッター：タイムスタンプ ISO 8601完全準拠. */
  private static final DateTimeFormatter DTF_LOG_TIMESTAMP = DateTimeFormatter
      .ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSS").withResolverStyle(ResolverStyle.STRICT);

  /**
   * コンストラクタ.
   *
   * @param cls               ログ対象クラス
   * @param traceCode         トレースコード（省略可能）
   * @param isDevelopMode     開発モード
   * @param infLogFileHandler 情報ログテキストハンドラー
   * @param errLogFileHandler エラーログテキストハンドラー
   * @param consoleWriter     コンソールライター
   */
  LogWriter(final Class<?> cls, final String traceCode, final boolean isDevelopMode,
      final LogTxtHandler infLogTxtHandler, final LogTxtHandler errLogTxtHandler,
      final PrintWriter consoleWriter) {

    this.clsName = cls.getName();
    this.threadName = Thread.currentThread().getName();
    this.traceCode = ValUtil.nvl(traceCode);
    this.isDevelopMode = isDevelopMode;
    this.infHdr = infLogTxtHandler;
    this.errHdr = errLogTxtHandler;
    this.console = consoleWriter;
    final String prefixTraceCode;
    if (ValUtil.isBlank(this.traceCode)) {
      prefixTraceCode = ValUtil.BLANK;
    } else {
      prefixTraceCode = " #" + this.traceCode;
    }
    this.infPrefix = prefixTraceCode + " [INF] ";
    this.errPrefix = prefixTraceCode + " [ERR] ";
    this.devPrefix = prefixTraceCode + " [DEV] ";
    this.beginEndSuffix = LogUtil.joinKeyVal("class", this.clsName, "thread", this.threadName);
  }

  /**
   * フラッシュ.
   */
  public void flush() {
    try {
        this.infHdr.getWriter().flush();
    } catch (Exception e) {
        // ログ処理中のエラーは握りつぶす
        LogUtil.stdout(e, "An exception occurred while flushing the info log. ");
    }
    
    try {
        this.errHdr.getWriter().flush();
    } catch (Exception e) {
        // ログ処理中のエラーは握りつぶす
        LogUtil.stdout(e, "An exception occurred while flushing the error log. ");
    }
  }

  /**
   * ログ文言作成.
   *
   * @param prefix プレフィックス
   * @param msg ログ文言
   * @return ログ文言
   */
  private String createMsg(final String prefix, final String msg) {
    final String tm = LocalDateTime.now().format(DTF_LOG_TIMESTAMP);
    final String log = tm + prefix + ValUtil.nvl(msg);
    return log;
  }

  /**
   * 共通ログ出力処理.
   */
  private void writeLog(final String prefix, final String msg, final boolean toErrorLog, 
                     final String stackTrace) {
    final String log = createMsg(prefix, msg);
    
    // 情報ログに出力
    this.infHdr.getWriter().println(log);
    
    // エラーログにも出力する場合
    if (toErrorLog) {
        this.errHdr.getWriter().println(log);
        if (stackTrace != null) {
            this.errHdr.getWriter().println(stackTrace);
        }
    }
    
    // 開発モード時のコンソール出力
    if (this.isDevelopMode) {
        this.console.println(log);
        if (stackTrace != null) {
            this.console.println(stackTrace);
        }
    }
  }

  /**
   * エラー出力.
   *
   * @param e   エラーインスタンス
   * @param msg ログ出力文言
   */
  public void error(final Throwable e, final String msg) {
    final String etrace;
    if (ValUtil.isNull(e)) {
      etrace = null;
    } else {
      etrace = LogUtil.getStackTrace(ValUtil.LF, e);
    }
    writeLog(this.errPrefix, msg, true, etrace);
  }

  /**
   * エラー出力.
   *
   * @param e   エラーインスタンス
   */
  public void error(final Throwable e) {
    error(e, ValUtil.BLANK);
  }

  /**
   * エラー出力.
   *
   * @param msg ログ出力文言
   */
  public void error(final String msg) {
    error(null, msg);
  }

  /**
   * 情報出力.
   *
   * @param msg ログ出力文言
   */
  public void info(final String msg) {
    writeLog(this.infPrefix, msg, false, null);
  }

  /**
   * 開始情報出力.
   *
   */
  public void begin() {
    final String log = createMsg(this.infPrefix, "<begin> " + this.beginEndSuffix);

    this.infHdr.getWriter().println(log);
    if (this.isDevelopMode) {
      this.console.println(log);
    }
  }

  /**
   * 終了情報出力.
   *
   */
  public void end() {
    final String log = createMsg(this.infPrefix, "< end > " + this.beginEndSuffix);

    this.infHdr.getWriter().println(log);
    if (this.isDevelopMode) {
      this.console.println(log);
    }
  }

  /**
   * 終了情報出力.
   * @param exitStatus 終了ステータス
   */
  public void end(final int exitStatus) {
    final String log = createMsg(this.infPrefix, "< end > " + this.beginEndSuffix + " " + LogUtil.joinKeyVal("status", exitStatus));

    this.infHdr.getWriter().println(log);
    if (this.isDevelopMode) {
      this.console.println(log);
    }
  }

  /**
   * 開発用出力.
   *
   * @param msg ログ出力文言
   */
  public void develop(final String msg) {
    if (!this.isDevelopMode) {
      return;
    }
    final String log = createMsg(this.devPrefix, msg);

    this.infHdr.getWriter().println(log);
    this.console.println(log);
  }

  /**
   * 開発モード判断.
   *
   * @return 開発ログ有効の場合は <code>true</code>
   */
  public boolean isDevelopMode() {
    return this.isDevelopMode;
  }

  /**
   * 経過時間計測開始.
   */
  public void startWatch() {
    this.watchStartTime = System.currentTimeMillis();
    this.info("<stopwatch> start");
  }

  /**
   * 経過時間計測終了.
   */
  public void stopWatch() {
    if (this.watchStartTime == 0) {
      this.error("<stopwatch> Not started.");
      return;
    }
    
    final long elapsedMillis = System.currentTimeMillis() - this.watchStartTime;
    final String formattedTime = milliSecToHmsSss(elapsedMillis);
    this.info("<stopwatch> stop time=" + formattedTime);
    
    // ストップウォッチをリセット
    this.watchStartTime = 0;
  }

  // 定数の追加
  private static final long MILLIS_PER_SECOND = 1000L;
  private static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
  private static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;

  /**
   * 経過時間をフォーマット.
   */
  private String milliSecToHmsSss(final long millis) {
    final long hours = millis / MILLIS_PER_HOUR;
    final long minutes = (millis / MILLIS_PER_MINUTE) % 60;
    final long seconds = (millis / MILLIS_PER_SECOND) % 60;
    final long milliseconds = millis % MILLIS_PER_SECOND;
    
    return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
  }
}
