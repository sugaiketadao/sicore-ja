package com.onepg.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 値ユーティリティクラス.
 */
public final class ValUtil {
  /**
   * コンストラクタ.
   */
  private ValUtil() {
    // 処理なし
  }

  /** ブランク値（ゼロバイト文字列）. */
  public static final String BLANK = "";

  /** 区分：ON値. */
  public static final String ON = "1";
  /** 区分：OFF値. */
  public static final String OFF = "0";

  /** シングルクォーテーション文字. */
  public static final String SQ = "'";
  /** ダブルクォーテーション文字. */
  public static final String DQ = "\"";

  /** JSON <code>null</code> 文字. */
  public static final String JSON_NULL = "null";

  /** 日時フォーマッター：日付. */
  private static final DateTimeFormatter DTF_DATE =
      DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

  /** 文字セット指定. */
  public enum CharSet {
    /** 文字セット指定 - UTF-8. */
    UTF8(StandardCharsets.UTF_8.name()),
    /** 文字セット指定 - Shift_JIS. */
    SJIS("Shift_JIS"),
    /** 文字セット指定 - MS932. */
    MS932("MS932");

    /** 文字セット. */
    private final String setName;

    /**
     * コンストラクタ.
     *
     * @param value 文字セット
     */
    private CharSet(final String value) {
      this.setName = value;
    }

    @Override
    public String toString() {
      return this.setName;
    }
  }

  /** 改行コード. */
  public enum LineSep {
    /** 改行コード LF. */
    LF("\n"),
    /** 改行コード CRLF. */
    CRLF("\r\n");

    /** 改行コード. */
    private final String sep;

    /**
     * コンストラクタ.
     *
     * @param value 改行コード
     */
    private LineSep(final String value) {
      this.sep = value;
    }

    @Override
    public String toString() {
      return this.sep;
    }
  }

  /**
   * <code>null</code> チェック.<br>
   * <ul>
   * <li><code>Object</code> が <code>null</code> かチェックする。</li>
   * </ul>
   *
   * @param obj チェック対象
   * @return <code>null</code> の場合は <code>true</code>
   */
  public static boolean isNull(final Object obj) {
    return null == obj;
  }

  /**
   * ブランクチェック.<br>
   * <ul>
   * <li>半角スペースのみで構成／ゼロバイト文字列／<code>null</code> のいずれの場合もブランクと判断する。</li>
   * </ul>
   *
   * @param value チェック対象
   * @return ブランクの場合は <code>true</code>
   */
  public static boolean isBlank(final String value) {
    if (isNull(value)) {
      return true;
    }
    return (value.trim().length() == 0);
  }

  /**
   * 空チェック.<br>
   * <ul>
   * <li>配列が長さゼロ 又は <code>null</code> かチェックする。</li>
   * </ul>
   *
   * @param values チェック対象
   * @return 空の場合は <code>true</code>
   */
  public static boolean isEmpty(final Object[] values) {
    return (values == null || values.length == 0);
  }

  /**
   * 空チェック.<br>
   * <ul>
   * <li>リストが長さゼロ 又は <code>null</code> かチェックする。</li>
   * </ul>
   *
   * @param list チェック対象
   * @return 空の場合は <code>true</code>
   */
  public static boolean isEmpty(final List<?> list) {
    if (isNull(list)) {
      return true;
    }
    return list.isEmpty();
  }

  /**
   * 空チェック.<br>
   * <ul>
   * <li>マップが長さゼロ 又は <code>null</code> かチェックする。</li>
   * </ul>
   *
   * @param map チェック対象
   * @return 空の場合は <code>true</code>
   */
  public static boolean isEmpty(final Map<?, ?> map) {
    if (isNull(map)) {
      return true;
    }
    return map.isEmpty();
  }

  /**
   * <code>null</code> ブランク置換.<br>
   * <ul>
   * <li>文字列が <code>null</code> の場合はブランクを返す。</li>
   * </ul>
   *
   * @param value チェック対象
   * @return <code>null</code> の場合は ブランク
   */
  public static String nvl(final String value) {
    return nvl(value, BLANK);
  }

  /**
   * <code>null</code> 置換.<br>
   * <ul>
   * <li>文字列が <code>null</code> の場合は置き換え文字を返す。</li>
   * </ul>
   *
   * @param value チェック対象
   * @param nullDefault 置き換え文字
   * @return <code>null</code> の場合は 置き換え文字
   */
  public static String nvl(final String value, final String nullDefault) {
    if (isNull(value)) {
      return nullDefault;
    }
    return value;
  }

  /**
   * <code>null</code> ゼロ置換.<br>
   * <ul>
   * <li>数値が <code>null</code> の場合はゼロを返す。</li>
   * </ul>
   *
   * @param value チェック対象
   * @return <code>null</code> の場合は ゼロ
   */
  public static BigDecimal nvl(final BigDecimal value) {
    if (isNull(value)) {
      return BigDecimal.ZERO;
    }
    return value;
  }

  /**
   * ブランク置換.<br>
   * <ul>
   * <li>文字列がブランクの場合は置き換え文字を返す。</li>
   * <li>ブランク判断には <code>#isBlank(String)</code> を使用。</li>
   * </ul>
   *
   * @param value チェック対象
   * @param blankDefault 置き換え文字
   * @return ブランクの場合は置き換え文字
   */
  public static String bvl(final String value, final String blankDefault) {
    if (isBlank(value)) {
      return blankDefault;
    }
    return value;
  }

  /**
   * 配列連結.<br>
   * <ul>
   * <li>配列が空の場合はブランクを返す。</li>
   * <li><code>null</code> はブランクとして連結される。</li>
   * </ul>
   *
   * @param joint つなぎ目文字
   * @param values 連結対象
   * @return 連結した文字列
   */
  public static String join(final String joint, final String... values) {
    if (isEmpty(values)) {
      return BLANK;
    }
    final String j = nvl(joint);
    final StringBuilder sb = new StringBuilder();
    for (final String val : values) {
      sb.append(nvl(val)).append(j);
    }
    ValUtil.deleteLastChar(sb, j.length());
    return sb.toString();
  }

  /**
   * リスト連結.<br>
   * <ul>
   * <li>リストが空の場合はブランクを返す。</li>
   * <li><code>null</code> はブランクとして連結される。</li>
   * </ul>
   *
   * @param joint つなぎ目文字
   * @param list 連結対象
   * @return 連結した文字列
   */
  public static String join(final String joint, final List<String> list) {
    if (list.isEmpty()) {
      return BLANK;
    }
    if (list.size() == 1) {
      return list.get(0);
    }
    final String[] values = list.toArray(new String[] {});
    return join(joint, values);
  }

  /**
   * リスト連結.<br>
   * <ul>
   * <li>リストが空の場合はブランクを返す。</li>
   * <li><code>null</code> はブランクとして連結される。</li>
   * </ul>
   *
   * @param joint つなぎ目文字
   * @param list 連結対象
   * @return 連結した文字列
   */
  public static String join(final String joint, final Set<String> list) {
    if (list.isEmpty()) {
      return BLANK;
    }
    if (list.size() == 1) {
      return list.toArray(new String[] {})[0];
    }
    final String[] values = list.toArray(new String[] {});
    return join(joint, values);
  }

  /**
   * 配列分割.<br>
   * <ul>
   * <li>文字が <code>null</code> の場合は長さゼロの配列を返す。</li>
   * </ul>
   *
   * @param value 分割対象文字
   * @param sep 分割文字
   * @return 分割した文字列配列
   */
  public static String[] split(final String value, final String sep) {
    if (isNull(value)) {
      return new String[] {};
    }
    final List<String> list = new ArrayList<>();
    for (final String val : new SimpleSeparateParser(value, sep)) {
      list.add(val);
    }
    return list.toArray(new String[0]);
  }

  /**
   * 正規表現配列分割.<br>
   * <ul>
   * <li>文字が <code>null</code> の場合は長さゼロの配列を返す。</li>
   * </ul>
   *
   * @param value 分割対象文字
   * @param sep 分割文字（正規表現）
   * @return 分割した文字列配列
   */
  public static String[] splitReg(final String value, final String sep) {
    return splitReg(value, sep, -1);
  }

  /**
   * 正規表現配列分割.<br>
   * <ul>
   * <li>文字が <code>null</code> の場合は長さゼロの配列を返す。</li>
   * </ul>
   *
   * @param value 分割対象文字
   * @param sep 分割文字（正規表現）
   * @param limitLength 最大長さ
   * @return 分割した文字列配列
   */
  public static String[] splitReg(final String value, final String sep, final int limitLength) {
    if (isNull(value)) {
      return new String[] {};
    }
    return value.split(sep, limitLength);
  }

  /**
   * 文字列比較.<br>
   * <ul>
   * <li><code>null</code> はゼロバイトブランク文字として比較する。</li>
   * </ul>
   *
   * @param str1 比較対象その1
   * @param str2 比較対象その2
   * @return 同値の場合は <code>true</code>
   */
  public static boolean equals(final String str1, final String str2) {
    return nvl(str1).equals(nvl(str2));
  }

  /**
   * 数値比較.<br>
   * <ul>
   * <li><code>null</code> はゼロとして比較する。</li>
   * </ul>
   *
   * @param dec1 比較対象その1
   * @param dec2 比較対象その2
   * @return 同値の場合は <code>true</code>
   */
  public static boolean equals(final BigDecimal dec1, final BigDecimal dec2) {
    // 精度違いの equals は <code>true</code> にならないので compareTo を使用する
    // 例：new BigDecimal("1").equals(new BigDecimal("1.0")) は <code>true</code> にならない
    return (nvl(dec1).compareTo(nvl(dec2)) == 0);
  }

  /**
   * 文字列安全切り取り（開始インデックスのみ指定）.<br>
   * <ul>
   * <li>開始インデックスから文字列の最後まで切り取る。</li>
   * </ul>
   *
   * @param value      対象文字列
   * @param beginIndex 開始インデックス
   * @return 切り取り文字列
   */
  public static String substring(final String value, final Integer beginIndex) {
    return substring(value, beginIndex, null);
  }

  /**
   * 文字列安全切り取り.<br>
   * <ul>
   * <li>文字列の指定範囲を安全に切り取る。</li>
   * <li>範囲外の指定や不正な値に対して適切に処理する。</li>
   * </ul>
   *
   * @param value      対象文字列
   * @param beginIndex 開始インデックス（省略可能）<code>null</code> を渡した（省略した）場合は0
   * @param endIndex   終了インデックス（省略可能）<code>null</code> を渡した（省略した）場合は文字列長
   * @return 切り取り文字列
   */
  public static String substring(final String value, final Integer beginIndex, final Integer endIndex) {
    if (isNull(value)) {
      return BLANK;
    }
    
    // 省略値の補完
    final int begin;
    if (isNull(beginIndex)) {
      begin = 0;
    } else {
      begin = beginIndex;
    }
    final int end;
    if (isNull(endIndex)) {
      end = value.length();
    } else {
    // 範囲外補正
      end = Math.min(endIndex, value.length());
    }
    
    // 開始位置が終了位置以降、または開始位置が文字列長以降の場合は空文字を返す
    if (begin < 0 || begin >= end || begin >= value.length()) {
      return BLANK;
    }

    return value.substring(begin, end);
  }

  /**
   * 英数字チェック.<br>
   * <ul>
   * <li>文字列が英数字として有効かチェックする。</li>
   * </ul>
   *
   * @param value チェック対象
   * @return 有効な場合は <code>true</code>
   */
  public static boolean isAlphabetNumber(final String value) {
    if (isBlank(value)) {
      // ブランクの場合
      return false;
    }
    return Pattern.matches("^[a-zA-Z0-9]+$", value);
  }

  /**
   * 数値チェック.<br>
   * <ul>
   * <li>文字列が数値として有効かチェックする。</li>
   * </ul>
   *
   * @param value チェック対象
   * @return 有効な場合は <code>true</code>
   */
  public static boolean isNumber(final String value) {
    return isNumber(value, false, false);
  }

  /** 数値チェック用パターン. */
  private static final Pattern VALID_NUMBER_PATTERN = Pattern.compile("^([1-9]\\d*|0)$");
  /** 数値チェック用パターン - 小数許容. */
  private static final Pattern VALID_NUMBER_PATTERN_DEC =
      Pattern.compile("^([1-9]\\d*|0)(\\.\\d+)?$");
  /** 数値チェック用パターン - マイナス値許容. */
  private static final Pattern VALID_NUMBER_PATTERN_MINUS = Pattern.compile("^[-]?([1-9]\\d*|0)$");
  /** 数値チェック用パターン - 小数許容、マイナス値許容. */
  private static final Pattern VALID_NUMBER_PATTERN_DEC_MINUS =
      Pattern.compile("^[-]?([1-9]\\d*|0)(\\.\\d+)?$");

  /**
   * 数値チェック.<br>
   * <ul>
   * <li>文字列が数値として有効かチェックする。</li>
   * </ul>
   *
   * @param value チェック対象
   * @param minusNg マイナス値を無効とする場合は <code>true</code>
   * @param decNg 小数を無効とする場合は <code>true</code>
   * @return 有効な場合は <code>true</code>
   */
  public static boolean isNumber(final String value, final boolean minusNg, final boolean decNg) {
    if (isBlank(value)) {
      // ブランクの場合
      return false;
    }
    final String checkVal = trimLeftZeroByIsNumber(value);
    if (minusNg && decNg) {
      return VALID_NUMBER_PATTERN.matcher(checkVal).find();
    } else if (minusNg) {
      return VALID_NUMBER_PATTERN_DEC.matcher(checkVal).find();
    } else if (decNg) {
      return VALID_NUMBER_PATTERN_MINUS.matcher(checkVal).find();
    }
    return VALID_NUMBER_PATTERN_DEC_MINUS.matcher(checkVal).find();
  }

  /**
   * 桁数チェック.<br>
   * <ul>
   * <li><code>#isBlank(String)</code> が <code>true</code> の前提とし、チェック対象がブランクの場合は <code>false</code> を返す。</li>
   * </ul>
   * 
   * @param value      チェック対象
   * @param len 有効桁数
   * @return 有効な場合は <code>true</code>
   */
  public static boolean checkLength(final String value, final int len) {
    if (isBlank(value)) {
      // ブランクの場合
      return false;
    }
    return value.length() <= len;
  }

  /**
   * 数値桁数（精度）チェック.<br>
   * <ul>
   * <li>整数部分と小数点以下の桁数が指定された範囲内であることを確認する。</li>
   * <li>引数はDB項目定義と同じで整数部と小数部を足した桁数と小数部だけの桁数で指定する。</li>
   * <li><code>#isNumber(String)</code> が <code>true</code> の前提とし、チェック対象がブランクの場合は
   * <code>false</code> を返す。</li>
   * </ul>
   * 
   * @param value      チェック対象
   * @param intPartLen 整数部と小数部を足した有効桁数
   * @param decPartLen 小数部の有効桁数
   * @return 有効な場合は <code>true</code>
   */
  public static boolean checkLengthNumber(final String value, final int intPartLen,
      final int decPartLen) {
    if (isBlank(value)) {
      // ブランクの場合
      return false;
    }
    final String checkVal = trimLeftZeroByIsNumber(value);
    final String patternStr;
    if (decPartLen > 0) {
      patternStr = "^[-]?\\d{1," + intPartLen + "}(\\.\\d{1," + decPartLen + "})?$";
    } else {
      patternStr = "^[-]?\\d{1," + intPartLen + "}$";
    }
    final Pattern pattern = Pattern.compile(patternStr);
    return pattern.matcher(checkVal).find();
  }

  /**
   * 数値チェック用左ゼロ除去.
   *
   * @param value 文字列
   * @return 処理後の文字
   */
  private static String trimLeftZeroByIsNumber(final String value) {
    final String tmp;
    if (value.startsWith("-")) {
      tmp = value.substring(1);
    } else {
      tmp = value;
    }
    final String ret = tmp.replaceAll("^0+", "");
    if (isBlank(ret)) {
      return "0";
    }
    if (value.startsWith("-")) {
      return "-" + ret;
    } else {
      return ret;
    }
  }

  /**
   * 実在日チェック.<br>
   * <ul>
   * <li>文字列が日付として有効かチェックする。</li>
   * </ul>
   *
   * @param value チェック対象（YYYYMMDD）
   * @return 有効な場合は <code>true</code>
   */
  public static boolean isDate(final String value) {
    if (isBlank(value)) {
      // ブランクの場合
      return false;
    }

    if (value.length() != 8 || !isNumber(value)) {
      // 8桁以外または数値以外の場合
      return false;
    }
    try {
      LocalDate.parse(value, DTF_DATE);
    } catch (Exception e) {
      // パースしてエラーが発生した場合
      return false;
    }
    return true;
  }

  /** 真偽値「真」とみなす文字列のセット */
  private static final Set<String> TRUE_VALUES = Set.of("1", "true", "yes", "on");

  /**
   * 真偽値チェック.<br>
   * <ul>
   * <li>文字列が真偽値「真」とみなす値かチェックする。</li>
   * <li>下記の評価を行う。
   * <ol>
   * <li>"1", "true", "yes", "on"（すべて半角）は <code>true</code>。</li>
   * <li><code>null</code> またはブランクは <code>false</code> を含み、上記以外は <code>false</code>。</li>
   * <li>大文字小文字を区別しない。</li>
   * <li>左右の半角ブランクは無視する。</li>
   * </ol>
   * </li>
   * </ul>
   *
   * @param val チェック対象
   * @return 真偽値「真」とみなす場合は <code>true</code>
   */
  public static boolean isTrue(final String val) {
    if (isBlank(val)) {
      return false;
    }
    final String lowVal = val.trim().toLowerCase();
    if (TRUE_VALUES.contains(lowVal)) {
      return true;
    }
    return false;
  }

  /**
   * Date型 to LocalDate型変換.
   *
   * @param date Date型（java.sql.Date 含む）
   * @return LocalDate型
   */
  public static LocalDate dateToLocalDate(final java.util.Date date) {
    final java.sql.Date sd;
    if (date instanceof java.sql.Date) {
      sd = (java.sql.Date) date;
    } else {
      sd = new java.sql.Date(date.getTime());
    }
    final LocalDate ld = sd.toLocalDate();
    return ld;
  }

  /**
   * ダブルクォーテーション除去.<br>
   * <ul>
   * <li>文字列の開始と終了のダブルクォーテーションを除去する。</li>
   * </ul>
   *
   * @see #trimBothEnds(String, char, char)
   * @param value 対象文字列
   * @return 除去後文字列
   */
  public static String trimDq(final String value) {
    return trimBothEnds(value, '"', '"');
  }

  /**
   * 文字列両端切り捨て.<br>
   * <ul>
   * <li>トリムして両端文字ともに見つかった場合のみ切り捨てる。</li>
   * </ul>
   *
   * @param value 対象文字列
   * @param prefix 前の端の文字
   * @param suffix 後ろの端の文字
   * @return 両端を切り捨てた文字列
   */
  public static String trimBothEnds(final String value, final char prefix, final char suffix) {
    return trimBothEnds(value, String.valueOf(prefix), String.valueOf(suffix));
  }

  /**
   * 文字列両端切り捨て.<br>
   * <ul>
   * <li>トリムして両端文字ともに見つかった場合のみ切り捨てる。</li>
   * </ul>
   *
   * @param value 対象文字列
   * @param prefix 前の端の文字
   * @param suffix 後ろの端の文字
   * @return 両端を切り捨てた文字列
   */
  public static String trimBothEnds(final String value, final String prefix, final String suffix) {
    if (ValUtil.isNull(value) || value.length() < 2) {
      // 最低２文字以上はあるはず
      return value;
    }
    if (value.startsWith(prefix) && value.endsWith(suffix)) {
      return value.substring(prefix.length(), value.length() - suffix.length());
    }
    return value;
  }

  /** 正規表現 - 始端全角文字列. */
  private static final String REGEX_ZENKAKU_SPACE_START = "^[" + '\u3000' + "]+";
  /** 正規表現 - 終端全角文字列. */
  private static final String REGEX_ZENKAKU_SPACE_END = "[" + '\u3000' + "]+$";

  /**
   * 文字列両端全角スペース切り捨て.<br>
   * <ul>
   * <li>文字列の開始と終了の全角スペースを除去する。</li>
   * </ul>
   *
   * @param value 対象文字列
   * @return 除去後文字列
   */
  public static String trimZenkakuSpace(final String value) {
    if (isNull(value)) {
      return value;
    }
    return value.replaceFirst(REGEX_ZENKAKU_SPACE_START, BLANK)
        .replaceFirst(REGEX_ZENKAKU_SPACE_END, BLANK);
  }

  /**
   * 左ゼロ除去.<br>
   * <ul>
   * <li>文字列の左の <code>"0"</code> を除去する。</li>
   * </ul>
   *
   * @param value 文字列
   * @return 処理後の文字
   */
  public static String trimLeftZero(final String value) {
    if (isNull(value)) {
      return value;
    }
    final String ret = value.replaceAll("^0+", "");
    return ret;
  }

  /**
   * 左ゼロ詰め.<br>
   * <ul>
   * <li>文字列の左に <code>"0"</code> を詰める。</li>
   * </ul>
   *
   * @param value 文字列
   * @param digit 詰めた後の文字桁数
   * @return 処理後の文字
   */
  public static String paddingLeftZero(final String value, final int digit) {
    if (ValUtil.isNull(value)) {
      return String.format("%0" + digit + "d", 0);
    }
    if (value.length() >= digit) {
      // 長さが超えている場合はそのまま返す
      return value;
    }
    final StringBuilder sb = new StringBuilder(value);
    while (sb.length() < digit) {
      sb.insert(0, "0");
    }
    return sb.toString();
  }

  /**
   * 左ゼロ詰め.<br>
   * <ul>
   * <li>数値を文字列に変換して左に <code>"0"</code> を詰める。</li>
   * </ul>
   *
   * @param value 数値
   * @param digit 詰めた後の文字桁数
   * @return 処理後の文字
   */
  public static String paddingLeftZero(final int value, final int digit) {
    return String.format("%0" + String.valueOf(digit) + "d", value);
  }


  /**
   * 最終１文字削除.
   *
   * @param sb StringBuilder
   */
  public static void deleteLastChar(final StringBuilder sb) {
    if (sb.length() >= 1) {
      sb.deleteCharAt(sb.length() - 1);
    }
  }

  /**
   * 最終文字削除.
   *
   * @param sb StringBuilder
   * @param length 最終文字長さ
   */
  public static void deleteLastChar(final StringBuilder sb, final int length) {
    if (length <= 0) {
      return;
    }
    if (sb.length() >= length) {
      sb.delete(sb.length() - length, sb.length());
    }
  }

  /**
   * URLエンコード変換.<br>
   * <ul>
   * <li>JavaScript の encodeURIComponent と合わせるため１バイトブランクを "+" から "%20" に変換しなおす。</li>
   * <li>ワイルドカードの "*" を "%2A" に変換する。</li>
   * </ul>
   *
   * @param url 変換文字列
   * @return 変換後文字列
   */
  public static String urlEncode(final String url) {
    final String value = ValUtil.nvl(url);
    try {
      return URLEncoder.encode(value, CharSet.UTF8.toString()).replace("+", "%20").replace("*",
          "%2A");
    } catch (UnsupportedEncodingException e) {
      return value;
    }
  }

  /**
   * URLデコード変換.
   *
   * @param url 変換文字列
   * @return 変換後文字列
   */
  public static String urlDecode(final String url) {
    final String value = ValUtil.nvl(url);
    try {
      return URLDecoder.decode(value, CharSet.UTF8.toString());
    } catch (UnsupportedEncodingException e) {
      return value;
    }
  }

  /**
   * JSONエスケープ変換.<br>
   * <ul>
   * <li>JSONでエスケープが必要な文字をエスケープする。</li>
   * <li><code>null</code> も制御文字としてエスケープする。</li>
   * </ul>
   *
   * @param value 文字列
   * @return 変換後文字列
   */
  public static String jsonEscape(final String value) {
    if (isNull(value)) {
      return "\\u0000";
    }
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);
      switch (c) {
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '/':
          sb.append("\\/");
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          if (c < ' ') {
            String t = "000" + Integer.toHexString(c);
            sb.append("\\u" + t.substring(t.length() - 4));
          } else {
            sb.append(c);
          }
      }
    }
    return sb.toString();
  }

  /**
   * JSONエスケープ変換除去.<br>
   * <ul>
   * <li>JSONでエスケープが必要な文字のエスケープを除去する。</li>
   * </ul>
   *
   * @param value 文字列
   * @return 除去後文字列
   */
  public static String jsonUnEscape(final String value) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      final char ch = value.charAt(i);
      if (ch == '\\' && i + 1 < value.length()) {
        char nextChar = value.charAt(i + 1);
        switch (nextChar) {
          case '"':
            sb.append('"');
            i++;
            break;
          case '\\':
            sb.append('\\');
            i++;
            break;
          case 'b':
            sb.append('\b');
            i++;
            break;
          case 'f':
            sb.append('\f');
            i++;
            break;
          case 'n':
            sb.append('\n');
            i++;
            break;
          case 'r':
            sb.append('\r');
            i++;
            break;
          case 't':
            sb.append('\t');
            i++;
            break;
          case 'u':
            if (i + 5 < value.length()) {
              try {
                final String ifHex = value.substring(i + 2, i + 6);
                final int hex = Integer.parseInt(ifHex, 16);
                sb.append((char) hex);
                i += 5;
              } catch (NumberFormatException e) {
                sb.append(ch);
              }
            } else {
              sb.append(ch);
            }
            break;
          default:
            sb.append(ch);
            break;
        }
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  /** シーケンスコード用 フォーマッター：タイムスタンプ（SQL:YYYYMMDDHH24MISSFF9）. */
  private static final DateTimeFormatter SEQCODE_DTF = DateTimeFormatter
      .ofPattern("uuuuMMddHHmmssSSSSSSSSS").withResolverStyle(ResolverStyle.STRICT);
  /** シーケンスコード用 タイムスタンプ分割桁指定. */
  private static final int[][] SEQCODE_TM_SPLIT_KETA = {{2, 5}, // 年:月1 最大 "991"
      {5, 8}, // 月2日 最大 "930"
      {8, 11}, // 時:分1 最大 "235"
      {11, 14}, // 分2:秒 最大 "959"
      {14, 17}, // ミリ秒 最大 "999"
      {17, 20}, // マイクロ秒 最大 "999"
      {20, 23} // ナノ秒 最大 "999"
  };

  /**
   * シーケンスコード値取得.<br>
   * <ul>
   * <li>ナノ秒まで含むタイムスタンプを36進数に変換した値を返す。</li>
   * <li>システム内で常にユニークな値を返すため１ナノ秒スリープしてから返す。</li>
   * <li>複数のシステムで使用する場合、ユニークな値を返すためにはホスト名やシステム名を付加するなどの考慮が必要となる。</li>
   * </ul>
   *
   * @return シーケンスコード値
   */
  public static synchronized String getSequenceCode() {
    // 1nsスリープ
    try {
      TimeUnit.NANOSECONDS.sleep(1);
    } catch (InterruptedException ignore) {
      // 処理なし
    }
    // タイムスタンプを分割
    final String tm = LocalDateTime.now().format(SEQCODE_DTF);

    final StringBuilder sb = new StringBuilder();
    for (final int[] keta : SEQCODE_TM_SPLIT_KETA) {
      final String tmPart = tm.substring(keta[0], keta[1]);
      final int tmPartInt = Integer.parseInt(tmPart);
      final String tmPart36 = Integer.toString(tmPartInt, Character.MAX_RADIX);
      sb.append(String.format("%2s", tmPart36).replace(' ', '0').toUpperCase());
    }
    return sb.toString();
  }



}
