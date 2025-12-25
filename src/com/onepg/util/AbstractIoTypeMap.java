package com.onepg.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 入出力可変型マップ 基底クラス.<br>
 * <ul>
 * <li>型ごとの値取得メソッドと値格納メソッドを持つマップクラス。</li>
 * <li>内部的には文字列で値を保持する。</li>
 * <li>値の格納順序を保持する。</li>
 * <li>Map&lt;String, String&gt; を継承することで汎用性を持たせている。</li>
 * <li>コンストラクタの引数指定で読み取り専用にできる。</li>
 * <li>基本ルール・制限
 * <ul>
 * <li>キーとして使用できる文字は英字小文字と数字、アンダースコア、ハイフン、ドットのみに限定される。<br>
 * （JSON で使用可能な文字のみ、かつ DBMS差異をなくすため英字は小文字統一とする）</li>
 * <li>原則として値取得メソッドは <code>null</code> を返さない。</li>
 * <li><code>null</code> を取得したい場合は意図的なメソッド［例］<code>#getStringNullable(String)</code>
 * で取得する。</li>
 * <li>原則として存在しないキーでの値取得は実行時エラーとなる。</li>
 * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKey(Object)</code> にて事前に存在確認するか、
 * 非存在時の戻値を指定する意図的なメソッド［例］<code>#getStringOrDefault(String, String)</code>
 * で取得する。</li>
 * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
 * <li>既に存在する可能性があるキーで値を格納する場合は意図的なメソッド［例］<code>#putForce(String, String)</code>
 * で格納する。</li>
 * <li><code>null</code> を明示的に格納したい場合も意図的なメソッド［例］<code>#putNull(String)</code> で格納する。</li>
 * <li>通常のマップとは異なり <code>#keySet()</code>、<code>#entrySet()</code>、<code>#values()</code>
 * の結果は読取専用となっている。<br>
 * （内部でキーを別管理しており、結果から削除されると整合性が崩れるため）</li>
 * <li>タイムスタンプは小数６桁までで保持される。</li>
 * </ul>
 * </ul>
 */
public abstract class AbstractIoTypeMap implements Map<String, String> {

  /** 値保持マップ. */
  private final Map<String, String> valMap;
  /** 全キーセット（値保持マップ以外の キーを併せ持ちチェックに使用する）. */
  private final Set<String> allKey;

  /** 日時フォーマッター：日付 入出力用（SQL:YYYYMMDD）. */
  static final DateTimeFormatter DTF_IO_DATE = DateTimeFormatter.ofPattern("uuuuMMdd")
      .withResolverStyle(ResolverStyle.STRICT);
  /** 日時フォーマッター：タイムスタンプ 入出力用 URLにも最適（SQL:YYYYMMDD"T"HH24MISSFF6）. */
  static final DateTimeFormatter DTF_IO_TIMESTAMP =
      DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmssSSSSSS").withResolverStyle(ResolverStyle.STRICT);

  /**
   * コンストラクタ.
   */
  AbstractIoTypeMap() {
    super();
    this.valMap = new LinkedHashMap<>();
    this.allKey = new LinkedHashSet<>();
  }

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>本クラスが保持している内容がイミュータブルオブジェクト（<code>String</code>）のため、実質ディープコピーとなる。</li>
   * </ul>
   *
   * @param srcMap ソースマップ
   */
  AbstractIoTypeMap(final Map<? extends String, ? extends String> srcMap) {
    this(srcMap, false);
  }

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>本クラスが保持している内容がイミュータブルオブジェクト（<code>String</code>）のため、実質ディープコピーとなる。</li>
   * </ul>
   *
   * @param srcMap ソースマップ
   * @param readOnly 読取専用マップを作成する場合は <code>true</code>
   */
  AbstractIoTypeMap(final Map<? extends String, ? extends String> srcMap, final boolean readOnly) {
    super();

    if (ValUtil.isNull(srcMap)) {
      throw new RuntimeException("Source map is required. ");
    }

    if (readOnly) {
      // 自クラスのインスタンスにコピーしてから読み取り専用にコピーする。
      final AbstractIoTypeMap tmp = new AbstractIoTypeMap() {};
      tmp.putAll(srcMap);
      this.valMap = Map.copyOf(tmp.valMap);
      this.allKey = Set.copyOf(tmp.allKey);
      return;
    }

    this.valMap = new LinkedHashMap<>();
    this.allKey = new LinkedHashSet<>();
    putAll(srcMap);
  }

  /**
   * 値保持マップ取得.
   *
   * @return 値保持マップ
   */
  protected final Map<String, String> getValMap() {
    return this.valMap;
  }

  /**
   * 全キーセット取得.
   *
   * @return 全キーセット
   */
  protected final Set<String> allKeySet() {
    return this.allKey;
  }

  /**
   * キーバリデート.
   *
   * @param key キー
   */
  protected final void validateKey(final String key) {
    ValUtil.validateIoKey(key);
  }

  /**
   * 取得時キーバリデート.
   *
   * @param key キー
   */
  private final void validateKeyForGet(final String key) {
    // 値保持マップキー存在チェック
    if (!this.valMap.containsKey(key)) {
      throw new RuntimeException("Key does not exist. " + LogUtil.joinKeyVal("key", key));
    }
  }

  /**
   * 格納時キーバリデート.
   *
   * @param type 保持タイプ
   * @param key キー
   * @param canOverwrite 上書き許可
   */
  private final void validateKeyForPut(final String key, final boolean canOverwrite) {
    // キー妥当性チェック
    validateKey(key);

    final boolean isExists = this.valMap.containsKey(key);
    if (!canOverwrite) {
      // 値保持マップキー非存在チェック
      if (isExists) {
        throw new RuntimeException("Key already exists. " + LogUtil.joinKeyVal("key", key));
      }
    }

    // その他キー存在チェック
    if (!isExists && this.allKey.contains(key)) {
      throw new RuntimeException("Key already exists as a value in another format. "
                                + LogUtil.joinKeyVal("key", key));
    }
  }

  /**
   * キーバリデート＆値取得.
   *
   * @param key キー
   * @return 値
   */
  protected final String getVal(final String key) {
    // キーバリデート
    validateKeyForGet(key);
    return this.valMap.get(key);
  }

  /**
   * キーバリデート＆値格納.
   *
   * @param key キー
   * @param value 値
   * @param canOverwrite 上書き許可
   * @return 前回の格納値
   */
  protected final String putVal(final String key, final String value, final boolean canOverwrite) {
    // キーバリデート
    validateKeyForPut(key, canOverwrite);
    // 全キー格納
    this.allKey.add(key);
    return this.valMap.put(key, value);
  }

  /**
   * 文字列取得.<br>
   * <ul>
   * <li>型指定の値取得メソッド［例］<code>#getString(String)</code> を使用してください。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 文字列
   */
  @Override
  @Deprecated
  public final String get(final Object key) {
    return getString((String) key);
  }

  /**
   * 文字列取得.<br>
   * <ul>
   * <li>型指定の値取得メソッド［例］<code>#getStringNullableOrDefault(String, String)</code> を使用してください。</li>
   * <li>存在しないキーの場合は引数の非存在時戻値が返される。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param notExistsValue 非存在時戻値
   * @return 文字列
   */
  @Override
  @Deprecated
  public final String getOrDefault(final Object key, final String notExistsValue) {
    return getStringNullableOrDefault((String) key, notExistsValue);
  }

  /**
   * 文字列取得.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKey(Object)</code> にて事前に存在確認する。</li>
   * <li>格納されている値が <code>null</code> の場合はゼロバイト文字を返す。（<code>null</code> は返らない）</li>
   * <li><code>null</code> を取得したい場合は <code>#getStringNullable(String)</code> で取得する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 文字列
   */
  public final String getString(final String key) {
    return ValUtil.nvl(getVal(key));
  }

  /**
   * 文字列取得.<br>
   * <ul>
   * <li>存在しないキーの場合は引数の非存在時戻値が返される。</li>
   * <li>格納されている値が <code>null</code> の場合はゼロバイト文字を返す。（<code>null</code> は返らない）</li>
   * <li><code>null</code> を取得したい場合は <code>#getStringNullableOrDefault(String, String)</code> で取得する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param notExistsValue 非存在時戻値
   * @return 文字列
   */
  public final String getStringOrDefault(final String key, final String notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getString(key);
  }

  /**
   * 文字列取得（<code>null</code> 有り）.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKey(Object)</code> にて事前に存在確認する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 文字列（<code>null</code> 有り）
   */
  public final String getStringNullable(final String key) {
    return getVal(key);
  }

  /**
   * 文字列取得（<code>null</code> 有り）.<br>
   * <ul>
   * <li>存在しないキーの場合は引数の非存在時戻値が返される。</li>
   * </ul>
   *
   * @param key            キー
   * @param notExistsValue 非存在時戻値
   * @return 文字列（<code>null</code> 有り）
   */
  public final String getStringNullableOrDefault(final String key, final String notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getStringNullable(key);
  }

  /**
   * 数値取得.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKey(Object)</code> にて事前に存在確認する。</li>
   * <li>格納されている値が <code>null</code> の場合はゼロを返す。（<code>null</code> は返らない）</li>
   * <li><code>null</code> を取得したい場合は <code>#getBigDecimalNullable(String)</code> で取得する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 数値
   */
  public final BigDecimal getBigDecimal(final String key) {
    final String val = getVal(key);
    if (ValUtil.isBlank(val)) {
      return BigDecimal.ZERO;
    }
    try {
      return new BigDecimal(val);
    } catch (final NumberFormatException e) {
      throw new RuntimeException("Invalid numeric value. " + LogUtil.joinKeyVal("key", key, "value", val), e);
    }
  }

  /**
   * 数値取得.<br>
   * <ul>
   * <li>存在しないキーの場合は引数の非存在時戻値が返される。</li>
   * <li>格納されている値が <code>null</code> の場合はゼロを返す。（<code>null</code> は返らない）</li>
   * <li><code>null</code> を取得したい場合は <code>#getBigDecimalNullableOrDefault(String)</code> で取得する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param notExistsValue 非存在時戻値
   * @return 数値
   */
  public final BigDecimal getBigDecimalOrDefault(final String key,
      final BigDecimal notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getBigDecimal(key);
  }

  /**
   * 数値取得（<code>null</code> 有り）.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKey(Object)</code> にて事前に存在確認する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 数値（<code>null</code> 有り）
   */
  public final BigDecimal getBigDecimalNullable(final String key) {
    final String val = getVal(key);
    if (ValUtil.isBlank(val)) {
      return null;
    }
    return new BigDecimal(val);
  }

  /**
   * 数値取得（<code>null</code> 有り）.<br>
   * <ul>
   * <li>存在しないキーの場合は引数の非存在時戻値が返される。</li>
   * </ul>
   *
   * @param key            キー
   * @param notExistsValue 非存在時戻値
   * @return 数値（<code>null</code> 有り）
   */
  public final BigDecimal getBigDecimalNullableOrDefault(final String key,
      final BigDecimal notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getBigDecimalNullable(key);
  }

  /**
   * int 値取得.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKey(Object)</code> にて事前に存在確認する。</li>
   * <li>格納されている値が <code>null</code> の場合はゼロを返す。</li>
   * <li>int 値許容範囲外の場合、例外エラーを投げる。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return int 値
   */
  public final int getInt(final String key) {
    final BigDecimal val = getBigDecimal(key);
    return val.intValueExact();
  }

  /**
   * int 値取得.<br>
   * <ul>
   * <li>存在しないキーの場合は引数の非存在時戻値が返される。</li>
   * <li>格納されている値が <code>null</code> の場合はゼロを返す。</li>
   * <li>int 値許容範囲外の場合、例外エラーを投げる。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param notExistsValue 非存在時戻値
   * @return int 値
   */
  public final int getIntOrDefault(final String key, final int notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getInt(key);
  }

  /**
   * long 値取得.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKey(Object)</code> にて事前に存在確認する。</li>
   * <li>格納されている値が <code>null</code> の場合はゼロを返す。</li>
   * <li>long 値許容範囲外の場合、例外エラーを投げる。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return long 値
   */
  public final long getLong(final String key) {
    final BigDecimal val = getBigDecimal(key);
    return val.longValueExact();
  }

  /**
   * long 値取得.<br>
   * <ul>
   * <li>存在しないキーの場合は引数の非存在時戻値が返される。</li>
   * <li>格納されている値が <code>null</code> の場合はゼロを返す。</li>
   * <li>long 値許容範囲外の場合、例外エラーを投げる。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param notExistsValue 非存在時戻値
   * @return long 値
   */
  public final long getLongOrDefault(final String key, final long notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getLong(key);
  }

  /**
   * 日付取得（<code>null</code> 有り）.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKey(Object)</code> にて事前に存在確認する。</li>
   * <li>日付変換できない場合、例外エラーを投げる。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 日付（<code>null</code> 有り）
   */
  public final LocalDate getDateNullable(final String key) {
    final String val = getVal(key);
    if (ValUtil.isBlank(val)) {
      return null;
    }
    final LocalDate ld = LocalDate.parse(val, DTF_IO_DATE);
    return ld;
  }

  /**
   * 日付取得（<code>null</code> 有り）.<br>
   * <ul>
   * <li>存在しないキーの場合は引数の非存在時戻値が返される。</li>
   * <li>日付変換できない場合、例外エラーを投げる。</li>
   * </ul>
   *
   * @param key            キー
   * @param notExistsValue 非存在時戻値
   * @return 日付（<code>null</code> 有り）
   */
  public final LocalDate getDateNullableOrDefault(final String key,
      final LocalDate notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getDateNullable(key);
  }

  /**
   * 日時取得（<code>null</code> 有り）.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKey(Object)</code> にて事前に存在確認する。</li>
   * <li>日時変換できない場合、例外エラーを投げる。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 日時（<code>null</code> 有り）
   */
  public final LocalDateTime getDateTimeNullable(final String key) {
    final String val = getVal(key);
    if (ValUtil.isBlank(val)) {
      return null;
    }
    final LocalDateTime ldt = LocalDateTime.parse(val, DTF_IO_TIMESTAMP);
    return ldt;
  }

  /**
   * 日時取得（<code>null</code> 有り）.<br>
   * <ul>
   * <li>存在しないキーの場合は引数の非存在時戻値が返される。</li>
   * <li>日時変換できない場合、例外エラーを投げる。</li>
   * </ul>
   *
   * @param key            キー
   * @param notExistsValue 非存在時戻値
   * @return 日時（<code>null</code> 有り）
   */
  public final LocalDateTime getDateTimeNullableOrDefault(final String key,
      final LocalDateTime notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getDateTimeNullable(key);
  }

  /**
   * SQL日付取得（<code>null</code> 有り）.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKey(Object)</code> にて事前に存在確認する。</li>
   * <li>日付変換できない場合、例外エラーを投げる。</li>
   * <li>SQLのバインド値として使用する想定。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return SQL日付（<code>null</code> 有り）
   */
  public final java.sql.Date getSqlDateNullable(final String key) {
    final LocalDate ld = getDateNullable(key);
    if (ValUtil.isNull(ld)) {
      return null;
    }
    return java.sql.Date.valueOf(ld);
  }

  /**
   * SQL日付取得（<code>null</code> 有り）.<br>
   * <ul>
   * <li>存在しないキーの場合は引数の非存在時戻値が返される。</li>
   * <li>日付変換できない場合、例外エラーを投げる。</li>
   * <li>SQLのバインド値として使用する想定。</li>
   * </ul>
   *
   * @param key            キー
   * @param notExistsValue 非存在時戻値
   * @return SQL日付（<code>null</code> 有り）
   */
  public final java.sql.Date getSqlDateNullableOrDefault(final String key,
      final java.sql.Date notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getSqlDateNullable(key);
  }

  /**
   * SQLタイムスタンプ取得（<code>null</code> 有り）.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKey(Object)</code> にて事前に存在確認する。</li>
   * <li>タイムスタンプ変換できない場合、例外エラーを投げる。</li>
   * <li>SQLのバインド値として使用する想定。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return SQLタイムスタンプ（<code>null</code> 有り）
   */
  public final java.sql.Timestamp getSqlTimestampNullable(final String key) {
    final LocalDateTime ldt = getDateTimeNullable(key);
    if (ValUtil.isNull(ldt)) {
      return null;
    }
    return java.sql.Timestamp.valueOf(ldt);
  }

  /**
   * SQLタイムスタンプ取得（<code>null</code> 有り）.<br>
   * <ul>
   * <li>存在しないキーの場合は引数の非存在時戻値が返される。</li>
   * <li>タイムスタンプ変換できない場合、例外エラーを投げる。</li>
   * <li>SQLのバインド値として使用する想定。</li>
   * </ul>
   *
   * @param key            キー
   * @param notExistsValue 非存在時戻値
   * @return SQLタイムスタンプ（<code>null</code> 有り）
   */
  public final java.sql.Timestamp getSqlTimestampNullableOrDefault(final String key,
      final java.sql.Timestamp notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getSqlTimestampNullable(key);
  }

  /**
   * 真偽値取得.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKey(Object)</code> にて事前に存在確認する。</li>
   * <li>真偽値の評価は <code>ValUtil.isTrue(String)</code> に準拠する。</li>
   * </ul>
   *
   * @see ValUtil#isTrue(String)
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 真偽値
   */
  public final boolean getBoolean(final String key) {
    final String val = getStringNullable(key);
    final boolean ret = ValUtil.isTrue(val);
    return ret;
  }

  /**
   * 真偽値取得.<br>
   * <ul>
   * <li>存在しないキーの場合は引数の非存在時戻値が返される。</li>
   * <li>値の評価は <code>ValUtil.isTrue(String)</code> に準拠する。</li>
   * </ul>
   *
   * @param key            キー
   * @param notExistsValue 非存在時戻値
   * @return 真偽値
   */
  public final boolean getBooleanOrDefault(final String key, final boolean notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getBoolean(key);
  }

  /**
   * <code>null</code> 格納.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 前回の格納文字列
   */
  public final String putNull(final String key) {
    return putVal(key, (String) null, false);
  }

  /**
   * <code>null</code> 格納（上書き許可）.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 前回の格納文字列
   */
  public final String putNullForce(final String key) {
    return putVal(key, (String) null, true);
  }

  /**
   * 文字列格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーで値格納する場合は <code>#putForce(String, String)</code> で格納する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value 値
   * @return 前回の格納文字列
   */
  @Override
  public final String put(final String key, final String value) {
    return putVal(key, value, false);
  }

  /**
   * 数値格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーで値格納する場合は <code>#putForce(String, BigDecimal)</code> で格納する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value 数値
   * @return 前回の格納文字列
   */
  public final String put(final String key, final BigDecimal value) {
    if (ValUtil.isNull(value)) {
      return putNull(key);
    }
    return putVal(key, value.toPlainString(), false);
  }

  /**
   * int 値格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーで値格納する場合は <code>#putForce(String, int)</code> で格納する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value int 値
   * @return 前回の格納文字列
   */
  public final String put(final String key, final int value) {
    return putVal(key, String.valueOf(value), false);
  }

  /**
   * long 値格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーで値格納する場合は <code>#putForce(String, long)</code> で格納する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value long 値
   * @return 前回の格納文字列
   */
  public final String put(final String key, final long value) {
    return putVal(key, String.valueOf(value), false);
  }

  /**
   * 日付格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーで値格納する場合は <code>#putForce(String, LocalDate)</code> で格納する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value 日付
   * @return 前回の格納文字列
   */
  public final String put(final String key, final LocalDate value) {
    if (ValUtil.isNull(value)) {
      return putNull(key);
    }
    final String s = value.format(DTF_IO_DATE);
    return putVal(key, s, false);
  }

  /**
   * 日時格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーで値格納する場合は <code>#putForce(String, LocalDateTime)</code> で格納する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value 日時
   * @return 前回の格納文字列
   */
  public final String put(final String key, final LocalDateTime value) {
    if (ValUtil.isNull(value)) {
      return putNull(key);
    }
    final String s = value.format(DTF_IO_TIMESTAMP);
    return putVal(key, s, false);
  }

  /**
   * UTIL日付格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーで値格納する場合は <code>#putForce(String, java.util.Date)</code> で格納する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value 日付（<code>java.sql.Date</code> 含む）
   * @return 前回の格納文字列
   */
  public final String put(final String key, final java.util.Date value) {
    if (ValUtil.isNull(value)) {
      return putNull(key);
    }
    final LocalDate ld = ValUtil.dateToLocalDate(value);
    return put(key, ld);
  }

  /**
   * SQLタイムスタンプ格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーで値格納する場合は <code>#putForce(String, java.sql.Timestamp)</code> で格納する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value タイムスタンプ
   * @return 前回の格納文字列
   */
  public final String put(final String key, final java.sql.Timestamp value) {
    if (ValUtil.isNull(value)) {
      return putNull(key);
    }
    final LocalDateTime ldt = value.toLocalDateTime();
    return put(key, ldt);
  }

  /**
   * 真偽値格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーで値格納する場合は <code>#putForce(String, boolean)</code> で格納する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value 真偽値
   * @return 前回の格納文字列
   */
  public final String put(final String key, final boolean value) {
    return put(key, Boolean.toString(value));
  }

  /**
   * 文字列格納（上書き許可）.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value 値
   * @return 前回の格納文字列
   */
  public final String putForce(final String key, final String value) {
    return putVal(key, value, true);
  }

  /**
   * 数値格納（上書き許可）.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value 数値
   * @return 前回の格納文字列
   */
  public final String putForce(final String key, final BigDecimal value) {
    if (ValUtil.isNull(value)) {
      return putNullForce(key);
    }
    return putVal(key, value.toPlainString(), true);
  }

  /**
   * int 値格納（上書き許可）.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value int 値
   * @return 前回の格納文字列
   */
  public final String putForce(final String key, final int value) {
    return putVal(key, String.valueOf(value), true);
  }

  /**
   * long 値格納（上書き許可）.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value long 値
   * @return 前回の格納文字列
   */
  public final String putForce(final String key, final long value) {
    return putVal(key, String.valueOf(value), true);
  }

  /**
   * 日付格納（上書き許可）.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value 日付
   * @return 前回の格納文字列
   */
  public final String putForce(final String key, final LocalDate value) {
    if (ValUtil.isNull(value)) {
      return putNullForce(key);
    }
    final String s = value.format(DTF_IO_DATE);
    return putVal(key, s, true);
  }


  /**
   * 日時格納（上書き許可）.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value 日時
   * @return 前回の格納文字列
   */
  public final String putForce(final String key, final LocalDateTime value) {
    if (ValUtil.isNull(value)) {
      return putNullForce(key);
    }
    final String s = value.format(DTF_IO_TIMESTAMP);
    return putVal(key, s, true);
  }

  /**
   * UTIL日付格納（上書き許可）.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value 日付（<code>java.sql.Date</code> 含む）
   * @return 前回の格納文字列
   */
  public final String putForce(final String key, final java.util.Date value) {
    if (ValUtil.isNull(value)) {
      return putNullForce(key);
    }
    final LocalDate ld = ValUtil.dateToLocalDate(value);
    return putForce(key, ld);
  }

  /**
   * SQLタイムスタンプ格納（上書き許可）.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value タイムスタンプ
   * @return 前回の格納文字列
   */
  public final String putForce(final String key, final java.sql.Timestamp value) {
    if (ValUtil.isNull(value)) {
      return putNullForce(key);
    }
    final LocalDateTime ldt = value.toLocalDateTime();
    return putForce(key, ldt);
  }

  /**
   * 真偽値格納（上書き許可）.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param value 真偽値
   * @return 前回の格納文字列
   */
  public final String putForce(final String key, final boolean value) {
    return putForce(key, Boolean.toString(value));
  }

  /**
   * 全値格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーが含まれる場合は <code>#putAllForce(Map)</code> で格納する。</li>
   * </ul>
   *
   * @param map マップ
   */
  @Override
  public final void putAll(final Map<? extends String, ? extends String> map) {
    putAllByMap(map, false);
  }

  /**
   * 全値格納（上書き許可）.
   *
   * @param map マップ
   */
  public final void putAllForce(final Map<? extends String, ? extends String> map) {
    putAllByMap(map, true);
  }

  /**
   * マップ格納.<br>
   * <ul>
   * <li>内容がイミュータブルオブジェクト（<code>String</code>）のため、実質ディープコピーとなる。</li>
   * </ul>
   *
   * @param srcMmap ソースマップ
   * @param canOverwrite 上書き許可
   */
  private final void putAllByMap(final Map<? extends String, ? extends String> srcMap,
      final boolean canOverwrite) {
    if (ValUtil.isNull(srcMap)) {
      return;
    }

    for (final Entry<? extends String, ? extends String> ent : srcMap.entrySet()) {
      putVal(ent.getKey(), ent.getValue(), canOverwrite);
    }
  }

  /**
   * マップサイズ取得.
   *
   * @return マップサイズ
   */
  @Override
  public final int size() {
    return this.valMap.size();
  }

  /**
   * マップゼロ件判断.
   *
   * @return ゼロ件の場合は <code>true</code>
   */
  @Override
  public final boolean isEmpty() {
    return this.valMap.isEmpty();
  }

  /**
   * マップキー存在確認.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 存在する場合は <code>true</code>
   */
  @Override
  public final boolean containsKey(final Object key) {
    return this.valMap.containsKey((String) key);
  }

  /**
   * マップ値存在確認.
   *
   * @param value 値
   * @return 存在する場合は <code>true</code>
   */
  @Override
  public final boolean containsValue(final Object value) {
    return this.valMap.containsValue((String) value);
  }

  /**
   * マップ値削除.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 削除された値
   */
  @Override
  public final String remove(final Object key) {
    this.allKey.remove((String) key);
    return this.valMap.remove((String) key);
  }

  /**
   * クリア.
   */
  @Override
  public final void clear() {
    throw new UnsupportedOperationException("Clear method is not available for this class. Create a new instance instead.");
  }

  /**
   * キーセット取得.<br>
   * <ul>
   * <li>削除されると内部の整合性が崩れるため読取専用としている。</li>
   * </ul>
   *
   * @return キーセット
   */
  @Override
  public final Set<String> keySet() {
    return Collections.unmodifiableSet(this.valMap.keySet());
  }

  /**
   * 値セット取得.<br>
   * <ul>
   * <li>削除されると内部の整合性が崩れるため読取専用としている。</li>
   * </ul>
   *
   * @return 値セット
   */
  @Override
  public final Collection<String> values() {
    return Collections.unmodifiableCollection(this.valMap.values());
  }

  /**
   * エントリーセット取得.<br>
   * <ul>
   * <li>削除されると内部の整合性が崩れるため読取専用としている。</li>
   * </ul>
   *
   * @return エントリーセット
   */
  @Override
  public final Set<Entry<String, String>> entrySet() {
    return Collections.unmodifiableSet(this.valMap.entrySet());
  }
}
