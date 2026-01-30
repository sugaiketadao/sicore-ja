package com.onepg.util;

import com.onepg.util.PropertiesUtil.FwPropertiesName;
import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.LineSep;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * ログテキストハンドラークラス.<br>
 * <ul>
 * <li>内部で自インスタンスをプーリングし、同じファイルパスの場合はプーリングから自インスタンスを返す。</li>
 * <li>内部でテキストライターインスタンスを保持しログライター <code>LogWriter</code> に返す。</li>
 * <li>ログテキストのオープン・クローズを受け持つ。</li>
 * <li>日付をまたいだ場合はファイルをローリングする。</li>
 * <li>テキストライタークラスは並列スレッドから呼び出されても出力を直列化する。</li>
 * </ul>
 * @hidden
 */
public final class LogTxtHandler implements AutoCloseable {

  /** 情報ログファイル設定キー接尾語. */
  private static final String INF_FILE_PROP_KEY_SUFFIX = ".inf.file";
  /** エラーログファイル設定キー接尾語. */
  private static final String ERR_FILE_PROP_KEY_SUFFIX = ".err.file";

  /** ログテキストハンドラープールマップ&lt;ファイルパス、ログテキストハンドラー&gt;（シングルトン）. */
  private static final Map<String, LogTxtHandler> logTxtPoolMaps_ = new HashMap<>();

  /** 基本ファイルパス（拡張子抜き）. */
  private final String baseFilePath;
  /** 拡張子（ドット有り）. */
  private final String fileTypeMark;
  /** ファイルパス（基本ファイルパス＋拡張子）. */
  private final String filePath;
  /** 前回出力日付（YYYYMMDD）. */
  private String beforePrintDate = null;

  /** テキストライター（直列出力）. */
  private TxtSerializeWriter tw = null;

  /** 日時フォーマッター：日付. */
  private static final DateTimeFormatter DTF_DATE =
      DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

  /**
   * ログテキストハンドラー取得.<br>
   * <ul>
   * <li>同じファイルパスがプーリングに有る場合はプーリングから自インスタンスを返す。</li>
   * <li>プーリングに無い場合は生成して自インスタンスを返す。</li>
   * </ul>
   *
   * @param keyPrefix 設定キープレフィックス
   * @param isErr エラーログの場合は <code>true</code>
   * @return ログテキストハンドラー
   */
  static LogTxtHandler getInstance(final String keyPrefix, final boolean isErr) {
    final String key;
    if (isErr) {
      key = keyPrefix + ERR_FILE_PROP_KEY_SUFFIX;
    } else {
      key = keyPrefix + INF_FILE_PROP_KEY_SUFFIX;
    }
    // 設定取得
    if (!LogUtil.PROP_MAP.containsKey(key)) {
      throw new RuntimeException("Property does not exist. "
          + LogUtil.joinKeyVal("file", FwPropertiesName.LOG.toString(), "key", key));
    }
    // ログファイルパス（絶対パスに変換済）
    final String logPath = LogUtil.PROP_MAP.getString(key);

    // 既に生成されていればプーリングされているハンドラーを返す
    if (logTxtPoolMaps_.containsKey(logPath)) {
      return logTxtPoolMaps_.get(logPath);
    }

    // ログテキストハンドラー生成
    synchronized (logTxtPoolMaps_) {
      // 再度確認し、既に生成されていれば返す（ココだけだと性能悪化するので上でも確認している）
      if (logTxtPoolMaps_.containsKey(logPath)) {
        return logTxtPoolMaps_.get(logPath);
      }

      LogUtil.stdout("Creates a log text handler. " + LogUtil.joinKeyVal("path", logPath));
      // ログテキストハンドラー
      final LogTxtHandler lfh = new LogTxtHandler(logPath);
      // ログテキストハンドラーをマップに格納
      logTxtPoolMaps_.put(logPath, lfh);
      return lfh;
    }
  }

  /**
   * ログテキストハンドラーを閉じる.<br>
   * <ul>
   * <li>プーリングされているログテキストハンドラーをすべて閉じる。</li>
   * </ul>
   */
  public static synchronized boolean closeAll() {
    boolean ret = false;
    // LogTxtHandlerのクローズ処理でプーリングから削除されるためキーのコピーを作成してイテレート
    for (final String key : new ArrayList<>(logTxtPoolMaps_.keySet())) {
      final LogTxtHandler handler = logTxtPoolMaps_.get(key);
      if (ValUtil.isNull(handler)) {
        continue;
      } 
      try {
        LogUtil.stdout("Closes the log text handler. " + LogUtil.joinKeyVal("path", key));
        handler.close();
        ret = true;
      } catch (final Exception e) {
        // ログクローズ時のエラーは握り潰す（ログ出力中にエラーが発生する可能性があるため）
        LogUtil.stdout(e, "An exception occurred while closing the log text handler. " + LogUtil.joinKeyVal("path", key));
      }
    }
    logTxtPoolMaps_.clear();
    return ret;
  }


  /**
   * コンストラクタ.
   *
   * @param baseFilePath 基本ファイルパス
   */
  private LogTxtHandler(final String baseFilePath) {

    final String[] tmp = FileUtil.splitFileTypeMark(baseFilePath);
    this.baseFilePath = tmp[0];
    this.fileTypeMark = "." + tmp[1];
    this.filePath = this.baseFilePath + this.fileTypeMark;

    // 前回起動時ファイルが残っている場合はファイル更新日を前回出力日付とする
    if (FileUtil.exists(this.filePath)) {
      final String modDt = FileUtil.getFileModifiedDateTime(this.filePath);
      this.beforePrintDate = modDt.substring(0, 8);
      if (!ValUtil.isDate(this.beforePrintDate)) {
        throw new RuntimeException("File modified date is invalid. "
            + LogUtil.joinKeyVal("path", this.filePath, "modDate", modDt, "beforePrintDate", this.beforePrintDate));
      }
    } else {
      final String nowDate = LocalDateTime.now().format(DTF_DATE);
      this.beforePrintDate = nowDate;
    }
    // ファイルオープン
    open();
  }

  /**
   * ファイルオープン.
   */
  private final void open() {
    this.tw = new TxtSerializeWriter(this.filePath, LineSep.LF, CharSet.UTF8, false, true, false);
  }

  /**
   * ファイルクローズ.
   */
  @Override
  public void close() {
    if (this.tw != null) {
      try {
        this.tw.close();
      } catch (final Exception e) {
        // ログクローズ時のエラーは握り潰すが、デバッグ用に出力
        LogUtil.stdout(e, "An exception occurred while closing the text writer. " + LogUtil.joinKeyVal("path", this.tw.getFilePath()));
      } finally {
        this.tw = null;
        // プールからこのインスタンスを削除
        logTxtPoolMaps_.remove(this.filePath);
      }
    }
  }

  /**
   * テキストライター取得.<br>
   * <ul>
   * <li>日付が前回と変わっていればファイルローリング実行。</li>
   * </ul>
   */
  TxtSerializeWriter getWriter() {
    final String nowDate = LocalDateTime.now().format(DTF_DATE);
    if (!nowDate.equals(this.beforePrintDate)) {
      rolling(nowDate);
    }
    return this.tw;
  }

  /**
   * ファイルローリング実行.<br>
   * <ul>
   * <li>ファイルを閉じ、リネームして新しいファイルを開く。</li>
   * </ul>
   *
   * @param newDate ローリング後の日付
   */
  private synchronized void rolling(final String newDate) {
    // 他スレッドから処理されているか再度確認
    if (newDate.equals(this.beforePrintDate)) {
      return;
    }

    final String destPath = this.baseFilePath + "_" + this.beforePrintDate + this.fileTypeMark;
    if (FileUtil.exists(destPath)) {
      // 基本的にありえないが既に日付の付いているファイルが存在する場合はリネームしない
      // ローリング失敗時も継続するため、エラーログを出力して処理継続
      LogUtil.stdout("Dated file already exists. " + LogUtil.joinKeyVal("path", this.filePath));
      this.beforePrintDate = newDate;
      return;
    }
    
    try {
      close();
      if (FileUtil.exists(this.filePath)) {
        FileUtil.move(this.filePath, destPath);
      }
      open();
      this.beforePrintDate = newDate;
    } catch (final Exception e) {
      // ローリング失敗時も継続するため、エラーログを出力して処理継続
      LogUtil.stdout(e, "An exception occurred during file rolling. " + LogUtil.joinKeyVal("path", this.filePath));
      // ファイルを再オープンして処理継続
      if (ValUtil.isNull(this.tw)) {
        open();
      }
      this.beforePrintDate = newDate;
    }
  }
}
