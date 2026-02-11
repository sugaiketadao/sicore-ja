package com.onepg.util;

import java.util.Map;

import com.onepg.util.ValUtil.CsvType;

/**
 * 入出力項目群マップクラス.<br>
 * <ul>
 * <li>CSV を入出力することができる。</li>
 * <li>JSON を入出力することができる。</li>
 * <li>URLパラメーター を入出力することができる。</li>
 * <li>基本ルール・制限は <code>AbstractIoTypeMap</code> に準拠する。</li>
 * </ul>
 *
 * @see AbstractIoTypeMap
 */
public final class IoItems extends AbstractIoTypeMap {

  /**
   * コンストラクタ.
   */
  public IoItems() {
    super();
  }

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>内容がイミュータブルオブジェクト（<code>String</code>）のため、実質ディープコピーとなる。</li>
   * </ul>
   *
   * @param srcMap ソースマップ
   */
  public IoItems(final Map<? extends String, ? extends String> srcMap) {
    super(srcMap);
  }

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>内容がイミュータブルオブジェクト（<code>String</code>）のため、実質ディープコピーとなる。</li>
   * </ul>
   *
   * @param srcMap ソースマップ
   * @param readOnly 読取専用マップを作成する場合は <code>true</code>
   */
  public IoItems(final Map<? extends String, ? extends String> srcMap, final boolean readOnly) {
    super(srcMap, readOnly);
  }

  /**
   * CSV作成.<br>
   * <ul>
   * <li>マップへの値の追加順で CSV文字列を作成する。</li>
   * <li>CSVタイプがダブルクォーテーション付の場合、値内のダブルクォーテーション(")をダブルクォーテーション2つ("")に変換する。</li>
   * <li>CSVタイプが改行有りの場合、かつ値内に改行コードを含む場合、その改行コード（CRLF・CR）は LF に統一する。</li>
   * <li>CSVタイプが改行無し（改行有り以外）の場合、かつ値内に改行コードを含む場合、その改行コード（CRLF・CR・LF）は半角スペースに変換する。</li>
   * <li>文字列リスト、ネストマップ、複数行リスト、配列リストは出力されない。</li>
   * </ul>
   *
   * @param csvType CSVタイプ
   * @return CSV文字列
   */
  String createCsv(final CsvType csvType) {
    if (csvType == CsvType.NO_DQ) {
      return createCsvNoDq();
    } else if (csvType == CsvType.DQ_ALL) {
      return createCsvAllDq(false);
    } else if (csvType == CsvType.DQ_ALL_LF) {
      return createCsvAllDq(true);
    } else if (csvType == CsvType.DQ_STD) {
      return createCsvStdDq(false);
    } else {
      return createCsvStdDq(true);
    }
  }

  /**
   * CSV作成.
   *
   * @return CSV文字列
   */
  private String createCsvNoDq() {
    final StringBuilder sb = new StringBuilder();
    for (final Entry<String, String> ent : super.getValMap().entrySet()) {
      final String val = ValUtil.nvl(ent.getValue());
      sb.append(val);
      sb.append(',');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * CSV作成 ダブルクォーテーション付.
   *
   * @param hasLf 改行有りフラグ
   * @return CSV文字列
   */
  private String createCsvAllDq(final boolean hasLf) {
    final StringBuilder sb = new StringBuilder();
    for (final Entry<String, String> ent : super.getValMap().entrySet()) {
      final String val = ValUtil.nvl(ent.getValue());
      sb.append('"').append(ValUtil.convCsvDqWrap(val, hasLf)).append('"');
      sb.append(',');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * CSV作成 CSV仕様準拠ダブルクォーテーション付.
   *
   * @param hasLf 改行有りフラグ
   * @return CSV文字列
   */
  private String createCsvStdDq(final boolean hasLf) {
    final StringBuilder sb = new StringBuilder();
    for (final Entry<String, String> ent : super.getValMap().entrySet()) {
      final String val = ValUtil.nvl(ent.getValue());
      // カンマ、改行、ダブルクォートが含まれる場合のみクォート
      if (val.contains(",") || val.contains("\"") || val.contains(ValUtil.LF) || val.contains(ValUtil.CR)) {
        sb.append('"').append(ValUtil.convCsvDqWrap(val, hasLf)).append('"');
      } else {
        sb.append(val);
      }
      sb.append(',');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * URLパラメーター（URLの?より後ろの部分）作成.
   *
   * @return URLエンコードされたGETパラメーター
   */
  String createUrlParam() {
    final Map<String, String> valMap = super.getValMap();
    final StringBuilder sb = new StringBuilder();
    for (final String key : super.allKeySet()) {
      final String val = valMap.get(key);
      final String encVal = ValUtil.urlEncode(val);
      sb.append(key).append('=').append(encVal).append('&');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * JSON作成.
   *
   * @return JSON文字列
   */
  String createJson() {
    final Map<String, String> valMap = super.getValMap();
    final StringBuilder sb = new StringBuilder();
    for (final String key : super.allKeySet()) {
      sb.append('"').append(key).append('"').append(':');
      final String val = valMap.get(key);
      if (ValUtil.isNull(val)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String escVal = ValUtil.jsonEscape(val);
      sb.append('"').append(escVal).append('"').append(',');
    }
    ValUtil.deleteLastChar(sb);
    sb.insert(0, '{');
    sb.append('}');
    return sb.toString();
  }

  /**
   * ログ出力文字列作成.
   *
   * @return ログ出力文字列
   */
  final String createLogString() {
    final StringBuilder sb = new StringBuilder();
    try {
      for (final Entry<String, String> ent : super.getValMap().entrySet()) {
        final String key = ent.getKey();
        final String val = ent.getValue();
        final String sval = LogUtil.convOutput(val);
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
   * CSV格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>引数のキー配列でキーがブランクの項目は格納されない。（読み飛ばしたい列にはブランクキーを指定する）</li>
   * <li>CSV項目数がキー数より多い場合、余剰分の項目は格納されない。</li>
   * <li>キー数がCSV項目数より多い場合、そのキーの値は常にブランクとなります。</li>
   * <li>ダブルクォーテーション付 CSVの場合、値内の２つ連続したダブルクォーテーション("")は１つのダブルクォーテーション(")に変換されて格納される。</li>
   * </ul>
   *
   * @param keys キー配列
   * @param csv CSV文字列
   * @param csvType CSVタイプ
   * @return 格納項目数
   */
  int putAllByCsv(final String[] keys, final String csv, final CsvType csvType) {
    if (csvType == CsvType.NO_DQ) {
      return putAllByCsvNoDq(keys, csv);
    } else {
      return putAllByCsvDq(keys, csv);
    }
  }

  /**
   * CSV格納.
   *
   * @param keys キー配列
   * @param csv CSV文字列
   * @return 格納項目数
   */
  int putAllByCsvNoDq(final String[] keys, final String csv) {
    // 最大インデックス
    final int keyMaxIdx = keys.length - 1;

    int keyIdx = -1;
    int count = 0;

    for (final String value : new SimpleSeparateParser(csv, ",")) {
      keyIdx++;
      if (keyMaxIdx < keyIdx) {
        // CSV列がキー列より多い場合は終了する
        break;
      }

      // キー名
      final String key = keys[keyIdx];
      if (ValUtil.isBlank(key)) {
        // キー名がブランクの場合は不要項目としスキップする
        continue;
      }

      // 値を格納
      count++;
      put(key, value);
    }
    return count;
  }

  /**
   * ダブルクォーテーション付 CSV格納.
   *
   * @see #putAllByCsv(String[], String)
   * @param keys キー配列
   * @param csv CSV文字列
   * @return 格納項目数
   */
  int putAllByCsvDq(final String[] keys, final String csv) {
    return putAllByCsvDq(keys, csv, new CsvDqParser(csv));
  }

  /**
   * ダブルクォーテーション付 CSV格納 性能対応.
   *
   * @see #putAllByCsv(String[], String)
   * @param keys キー配列
   * @param csv CSV文字列
   * @param dqParser CSVパーサー
   * @return 格納項目数
   */
  int putAllByCsvDq(final String[] keys, final String csv, final CsvDqParser dqParser) {
    // キー最大インデックス
    final int keyMaxIdx = keys.length - 1;

    int keyIdx = -1;
    int count = 0;

    for (final String value : dqParser) {
      keyIdx++;
      if (keyMaxIdx < keyIdx) {
        // CSV列がキー列より多い場合は終了する
        break;
      }
      // キー名
      final String key = keys[keyIdx];
      if (ValUtil.isBlank(key)) {
        // キー名がブランクの場合は不要項目としスキップする
        continue;
      }

      // 値を格納
      count++;
      put(key, value.replace("\"\"", "\""));
    }
    return count;
  }

  /**
   * URLパラメーター（URLの?より後ろの部分）値格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * </ul>
   *
   * @param url URL全体 または URLパラメーター
   * @return 格納パラメーター数
   */
  int putAllByUrlParam(final String url) {
    if (ValUtil.isBlank(url)) {
      return 0;
    }

    final String params;
    if (url.indexOf('?') > 0) {
      params = url.substring(url.indexOf('?') + 1);
    } else {
      params = url;
    }

    int count = 0;
    for (final String param : new SimpleSeparateParser(params, "&")) {
      if (ValUtil.isBlank(param)) {
        continue;
      }
      final String[] keyVal = ValUtil.splitReg(param, "=", 2);
      final String key = keyVal[0];
      final String val;
      if (keyVal.length == 1) {
        val = ValUtil.BLANK;
      } else {
        val = ValUtil.urlDecode(keyVal[1]);
      }

      if (key.endsWith("[]")) {
        // 配列キーはエラー
        throw new RuntimeException("Keys representing arrays cannot be used. " + LogUtil.joinKeyVal("key", key));
      }

      // 値を格納
      count++;
      put(key, val);
    }
    return count;
  }

  /**
   * バッチパラメーター値を格納（内容はURLパラメーターと同じ形式）.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>コマンドライン引数1つあたりの長さ制限に対応するため、複数の引数を配列として受け取る。</li>
   * </ul>
   *
   * @param args バッチパラメーター配列（各要素はURLパラメーター形式の文字列）
   * @return 格納パラメーター数
   */
  public int putAllByBatParam(final String[] args) {
    if (ValUtil.isEmpty(args)) {
      return 0;
    }
    int count = 0;
    for (final String arg : args) {
      count += putAllByUrlParam(arg);
    }
    return count;
  }

  /**
   * JSON格納.
   *
   * @param json JSON文字列
   * @return 格納項目数
   */
  int putAllByJson(final String json) {
    if (ValUtil.isBlank(json)) {
      return 0;
    }

    int count = 0;

    // JSON項目のループ
    for (final String item : new JsonMapSeparateParser(json)) {
      final String[] keyVal = JsonMapKeyValueSeparateParser.getKeyValue(item);
      if (ValUtil.isNull(keyVal)) {
        continue;
      }
      final String key = keyVal[0];
      final String val = keyVal[1];

      if (JsonMapSeparateParser.JSON_MAP_PATTERN.matcher(val).find()
          || JsonArraySeparateParser.JSON_ARRAY_PATTERN.matcher(val).find()) {
        throw new RuntimeException("Associative arrays and arrays are not supported as values. " + LogUtil.joinKeyVal("json", val));
      }

      count++;
      if (ValUtil.JSON_NULL.equals(val)) {
        putNull(key);
        continue;
      }
      final String unEscVal = ValUtil.jsonUnEscape(ValUtil.trimDq(val));
      put(key, unEscVal);
    }
    return count;
  }

  /**
   * ログ用文字列返却.
   */
  public final String toString() {
    return createLogString();
  }

}
