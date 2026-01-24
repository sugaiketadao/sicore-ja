package com.onepg.util;

import com.onepg.db.SqlBuilder;
import com.onepg.util.PropertiesUtil.FwPropertiesName;
import com.onepg.util.ValUtil.LineSep;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ログユーティリティクラス.
 */
public final class LogUtil {

  /** ログ出力時 <code>null</code> 置換文字. */
  private static final String NULL_REP = "<nul>";

  /** デフォルトログ設定キープレフィックス（設定キー <code>.inf.file</code> および <code>.err.file</code> より前の部分）. */
  private static final String DEFAULT_LOG_KEYPREFIX = "default";

  /** ログ設定（ファイルパスは絶対パスに変換済）. */
  static final IoItems PROP_MAP;
  /** 開発モード. */
  private static final boolean DEVELOP_MODE;
  /** コンソールライター. */
  private static final PrintWriter CONSOLE_WRITER;

  static {
    PROP_MAP = PropertiesUtil.getFrameworkProps(FwPropertiesName.LOG);
    DEVELOP_MODE = Boolean.parseBoolean(PROP_MAP.getString("develop.mode"));
    CONSOLE_WRITER = new CustomPrintWriter(System.out, true, LineSep.LF);

    // System.out を無効化する
    final PrintStream disablePs = new PrintStream(System.err) {
      @Override
      public void println(final String value) {
        throw new UnsupportedOperationException("System.out is disabled.");
      }

      @Override
      public void print(final String value) {
        throw new UnsupportedOperationException("System.out is disabled.");
      }
    };
    System.setOut(disablePs);
  }

  /**
   * コンストラクタ.
   */
  private LogUtil() {
    // 処理なし
  }

  /**
   * ログライターインスタンス生成.
   *
   * @param cls       ログ対象クラス
   * @return ログライターインスタンス
   */
  public static LogWriter newLogWriter(final Class<?> cls) {
    return newLogWriter(cls, null);
  }

  /**
   * ログライターインスタンス生成.
   *
   * @param cls ログ対象クラス
   * @param traceCode トレースコード
   * @return ログライターインスタンス
   */
  public static LogWriter newLogWriter(final Class<?> cls, final String traceCode) {
    final LogTxtHandler infHdr = LogTxtHandler.getInstance(DEFAULT_LOG_KEYPREFIX, false);
    final LogTxtHandler errHdr = LogTxtHandler.getInstance(DEFAULT_LOG_KEYPREFIX, true);
    try {
      return new LogWriter(cls, traceCode, DEVELOP_MODE, infHdr, errHdr, CONSOLE_WRITER);
    } catch (Exception | Error e) {
      throw new RuntimeException("An exception occurred while creating the log writer instance. ", e);
    }
  }

  /**
   * 標準出力.
   *
   * @param msgs メッセージ
   */
  public static void stdout(final String... msgs) {
    stdout(null, msgs);
  }

  /**
   * 標準出力.
   *
   * @param e エラーオブジェクト
   * @param msgs メッセージ
   */
  public static void stdout(final Throwable e, final String... msgs) {
    final String msg = ValUtil.join(System.lineSeparator(), msgs);
    CONSOLE_WRITER.println(msg);
    if (!ValUtil.isNull(e)) {
      e.printStackTrace(CONSOLE_WRITER);
    }
    CONSOLE_WRITER.flush();
  }

  /**
   * Java情報 標準出力.
   */
  public static void javaInfoStdout() {
    CONSOLE_WRITER.println("Java Information.");
    CONSOLE_WRITER.println(" version = " + System.getProperty("java.version"));
    CONSOLE_WRITER.println(" home    = " + System.getProperty("java.home"));
    CONSOLE_WRITER.println(" class   = " + System.getProperty("java.class.path"));
    CONSOLE_WRITER.println(" tmpdir  = " + System.getProperty("java.io.tmpdir"));
    CONSOLE_WRITER.println("");
    CONSOLE_WRITER.println("OS User Information.");
    CONSOLE_WRITER.println(" name = " + System.getProperty("user.name"));
    CONSOLE_WRITER.println(" home = " + System.getProperty("user.home"));
    CONSOLE_WRITER.println(" dir  = " + System.getProperty("user.dir"));
    CONSOLE_WRITER.println("");
  }

  /**
   * 開発モード
   * @return 開発モードの場合は <code>true</code>
   */
  public static boolean isDevelopMode() {
    return DEVELOP_MODE;
  } 

  /**
   * エラーオブジェクトのスタックトレース取得.<br>
   * <ul>
   * <li><code>BreakException</code>の場合はスタックトレースの取得を中断する。</li>
   * </ul>
   *
   * @param lineSep 改行文字
   * @param e エラーオブジェクト
   * @return スタックトレース
   */
  public static String getStackTrace(final String lineSep, final Throwable e) {
    final StringBuilder sb = new StringBuilder();
    try {
      Throwable current = e;
      boolean isFirst = true;

      while (!ValUtil.isNull(current) && !(current instanceof BreakException)) {
        if (!isFirst) {
          sb.append(lineSep).append("Caused by: ");
        }
        sb.append(current.toString());
        
        for (final StackTraceElement element : current.getStackTrace()) {
          sb.append(lineSep);
          sb.append(" at ");
          sb.append(element.toString());
        }
        
        current = current.getCause();
        isFirst = false;
      }
    } catch (Exception ignore) {
      // 処理なし
    }
    return sb.toString();
  }

  /**
   * キー値文字列連結.<br>
   * <ul>
   * <li>「キー=値, キー=値, キー=値,...」形式で連結してログ用文字列を作成する。</li>
   * <li>値が配列、リスト、マップの場合はそれぞれの連結メソッドを呼び出して連結する。</li>
   * </ul>
   *
   * @param keyVal キーバリュー（キー,値,キー,値,キー,値,...）
   * @return キー=値, キー=値, キー=値 形式の連結文字列
   */
  public static String joinKeyVal(final Object... keyVal) {
    if (ValUtil.isNull(keyVal)) {
      return NULL_REP;
    }
    if (ValUtil.isEmpty(keyVal)) {
      return "";
    }
    final StringBuilder sb = new StringBuilder();
    try {
      for (int i = 0; i < keyVal.length; i++) {
        if (i % 2 == 0) {
          if (ValUtil.isNull(keyVal[i])) {
            sb.append(NULL_REP);
          } else {
            // キーは文字列である前提
            sb.append(keyVal[i].toString());
          }
          sb.append('=');
        } else {
          final String sval = convOutput(keyVal[i]);
          sb.append(sval);
          sb.append(',');
        }
      }
      ValUtil.deleteLastChar(sb);
    } catch (Exception ignore) {
      // 処理なし
    }
    return sb.toString();
  }

  /**
   * 値連結.<br>
   * <ul>
   * <li>「値, 値,... 」形式で連結してログ用文字列を作成する。</li>
   * </ul>
   *
   * @param values 連結対象値
   * @return 値, 値, 値, 値 形式の文字列
   */
  public static String joinValues(final String... values) {
    if (ValUtil.isNull(values)) {
      return NULL_REP;
    }
    final StringBuilder sb = new StringBuilder();
    try {
      for (final String val : values) {
        final String sval = convOutput(val);
        sb.append(sval);
        sb.append(',');
      }
      ValUtil.deleteLastChar(sb);
    } catch (Exception ignore) {
      // 処理なし
    }
    return sb.toString();
  }

  /**
   * 配列値連結.<br>
   * <ul>
   * <li>「 [値, 値,...] 」形式で連結してログ用文字列を作成する。</li>
   * </ul>
   *
   * @param values 連結対象値
   * @return [値, 値, 値, 値] 形式の文字列
   */
  public static String join(final String[] values) {
    if (ValUtil.isNull(values)) {
      return NULL_REP;
    }
    final StringBuilder sb = new StringBuilder();
    try {
      for (final String val : values) {
        final String sval = convOutput(val);
        sb.append(sval);
        sb.append(',');
      }
      ValUtil.deleteLastChar(sb);
      sb.insert(0, '[');
      sb.append(']');
    } catch (Exception ignore) {
      // 処理なし
    }
    return sb.toString();
  }

  /**
   * リスト値連結.<br>
   * <ul>
   * <li>リスト内の値を「 [値, 値,...] 」形式で連結してログ用文字列を作成する。</li>
   * </ul>
   *
   * @param values 連結対象リスト
   * @return [値, 値, 値, 値] 形式の文字列
   */
  public static String join(final List<?> values) {
    if (ValUtil.isNull(values)) {
      return NULL_REP;
    }
    final StringBuilder sb = new StringBuilder();
    try {
      for (final Object val : values) {
        final String sval = convOutput(val);
        sb.append(sval);
        sb.append(',');
      }
      ValUtil.deleteLastChar(sb);
      sb.insert(0, '[');
      sb.append(']');
    } catch (Exception ignore) {
      // 処理なし
    }
    return sb.toString();
  }

  /**
   * マップキー値連結.<br>
   * <ul>
   * <li>マップ内のキーと値を「 {キー=値, キー=値, キー=値,...} 」形式で連結してログ用文字列を作成する。</li>
   * </ul>
   *
   * @param map 連結対象マップ
   * @return {キー=値, キー=値, キー=値} 形式の文字列
   */
  public static <T> String join(final Map<String, T> map) {
    if (ValUtil.isNull(map)) {
      return NULL_REP;
    }
    final StringBuilder sb = new StringBuilder();
    try {
      for (final Map.Entry<String, ?> ent : map.entrySet()) {
        final String key = ent.getKey();
        final Object val = ent.getValue();
        final String sval = convOutput(val);
        sb.append(key).append('=').append(sval);
        sb.append(',');
      }
      ValUtil.deleteLastChar(sb);
      sb.insert(0, '{');
      sb.append('}');
    } catch (Exception ignore) {
      // 処理なし
    }
    return sb.toString();
  }

  /**
   * ログ用文字列変換.<br>
   * <ul>
   * <li>値が <code>null</code> の場合、ログ用の置換文字を返す。</li>
   * <li>ダブルクォートで囲み、文字列内のダブルクォートはエスケープする。</li>
   * <li>引数が Object のメソッドを回避するためのメソッド。</li>
   * </ul>
   *
   * @param val 文字列
   * @return ログ用文字列
   */
  static String convOutput(final String val) {
    if (ValUtil.isNull(val)) {
      return NULL_REP;
    }
    return '"' + ((String) val).replace("\"", "\\\"") + '"';
  }

  /**
   * ログ用文字列変換.<br>
   * <ul>
   * <li>値が <code>null</code> の場合、ログ用の置換文字を返す。</li>
   * <li>値が文字列の場合、ダブルクォートで囲み、文字列内のダブルクォートはエスケープする。</li>
   * <li>値が日付、日時の場合、標準フォーマットで文字列化する。</li>
   * <li>値が配列、リスト、マップの場合はそれぞれの連結メソッドを呼び出して連結する。</li>
   * <li>その他のオブジェクトは <code>toString()</code> メソッドの結果を返す。</li>
   * </ul>
   * @param obj オブジェクト
   * @return ログ用文字列
   */
  static String convOutput(final Object obj) {
    try {
      if (ValUtil.isNull(obj)) {
        return NULL_REP;
      } else if (obj instanceof String) {
        return '"' + ((String) obj).replace("\"", "\\\"") + '"';
      } else if (obj instanceof Io) {
        // Io は Map のサブクラスなので Map より先にチェックする
        return ((Io) obj).toString();
      } else if (obj instanceof IoItems) {
        // IoItems は Map のサブクラスなので Map より先にチェックする
        return ((IoItems) obj).toString();
      } else if (obj instanceof SqlBuilder) {
        return ((SqlBuilder) obj).toString();
      } else if (obj instanceof String[]) {
        return join((String[]) obj);
      } else if (obj instanceof List) {
        return join((List<?>) obj);
      } else if (obj instanceof Map) {
        try {
          @SuppressWarnings("unchecked")
          final Map<String, ?> smap = (Map<String, ?>) obj;
          return join(smap);
        } catch (Exception e) {
          return "<MAP_CAST_ERROR>";
        }
      } else if (obj instanceof BigDecimal) {
        return ((BigDecimal) obj).toPlainString();
      } else if (obj instanceof LocalDate) {
        return ((LocalDate) obj).format(AbstractIoTypeMap.DTF_IO_DATE);
      } else if (obj instanceof LocalDateTime) {
        return ((LocalDateTime) obj).format(AbstractIoTypeMap.DTF_IO_TIMESTAMP);
      } else if (obj instanceof java.sql.Timestamp) {
        // java.sql.Timestamp は java.util.Date のサブクラスなので java.util.Date より先にチェックする
        return ((java.sql.Timestamp) obj).toLocalDateTime().format(AbstractIoTypeMap.DTF_IO_TIMESTAMP);
      } else if (obj instanceof java.sql.Date) {
        // java.sql.Date は java.util.Date のサブクラスなので java.util.Date より先にチェックする
        return ValUtil.dateToLocalDate((java.sql.Date) obj).format(AbstractIoTypeMap.DTF_IO_DATE);
      } else if (obj instanceof java.util.Date) {
        return ValUtil.dateToLocalDate((java.util.Date) obj).format(AbstractIoTypeMap.DTF_IO_DATE);
      }
      return String.valueOf(obj);
    } catch (Exception e) {
      return "<CONVERT_ERROR>";
    }
  }

  /**
   * <code>null</code>置換.<br>
   * 値が <code>null</code> の場合、ログ用の置換文字を返す。
   *
   * @param value <code>null</code> の場合、置換文字
   * @return <code>null</code> でない場合はそのまま、<code>null</code> の場合は置換文字
   */
  public static String replaceNullValue(final String value) {
    return ValUtil.nvl(value, NULL_REP);
  }

  /**
   * 日時フォーマット.<br>
   * <ul>
   * <li>ミリ秒を読みやすい形式（日時分秒ミリ秒）に変換します。</li>
   * <li>例：「11T03:15:30.123」「0T01:00:00.000」</li>
   * </ul>
   *
   * @param msec ミリ秒
   * @return フォーマット済み稼働時間
   */
  public static String formatDaysTime(final long msec) {
    try {
      if (msec <= 0) {
        return "0T00:00:00.000";
      }
      // オーバーフロー対策
      if (Long.MAX_VALUE < (msec / 1000)) {
        return ValUtil.BLANK;
      }

      final long sec = msec / 1000;
      final long min = sec / 60;
      final long hur = min / 60;
      long day = hur / 24;
      
      // 非現実的な値のチェック
      if (day > 999999) { // 約2700年以上
        day = -1;
      }

      final long sepHur = hur % 24;     // 0-23の範囲
      final long sepMin = min % 60;     // 0-59の範囲
      final long sepSec = sec % 60;     // 0-59の範囲
      final long sepMsec = msec % 1000; // 0-999の範囲（ミリ秒）
      
      final StringBuilder sb = new StringBuilder();
      sb.append(day).append("T");
      sb.append(String.format("%02d", sepHur)).append(":");
      sb.append(String.format("%02d", sepMin)).append(":");
      sb.append(String.format("%02d", sepSec)).append(".");
      sb.append(String.format("%03d", sepMsec));

      return sb.toString();
    } catch (Exception ignore) {
      // 処理なし
      return ValUtil.BLANK;
    }
  }

  /**
   * 呼び出し元情報取得.
   * <ul>
   * <li>スタックトレースから呼び出し元の クラスパッケージ＋クラス名＋行番号 を取得する。</li>
   * </ul>
   * @param callerClass 呼び出し元クラス
   * @return クラスパッケージ＋クラス名＋行番号
   */
  public static String getClassNameAndLineNo(final Class<?> callerClass) {
    final String callerClassName = callerClass.getName();
    final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    for (final StackTraceElement element : stackTrace) {
      final String className = element.getClassName();
      if (!className.equals(callerClassName)
          && !className.equals(Thread.class.getName())) {
        return className + "[" + element.getLineNumber() + "]";
      }
    }
    return "UnknownSource";
  }
  
}
