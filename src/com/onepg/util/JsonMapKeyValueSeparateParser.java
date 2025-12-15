package com.onepg.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * JSONマップキー値分割パーサー.
 * @hidden
 */
final class JsonMapKeyValueSeparateParser extends AbstractStringSeparateParser {

  /**
   * キー値配列取得.
   *
   * @param jsonItem JSON項目文字列
   * @return 文字配列｛キー、値｝ 不正な値の場合は <code>null</code> を返す（<code>null</code> 有り）
   */
  static String[] getKeyValue(final String jsonItem) {
    // コロンでキーと値を分ける
    final Iterator<String> keyValIte = (new JsonMapKeyValueSeparateParser(jsonItem)).iterator();
    if (!keyValIte.hasNext()) {
      // 不正な値
      return null;
    }
    final String key = keyValIte.next();
    if (ValUtil.isBlank(key)) {
      // 不正な値
      return null;
    }
    if (!keyValIte.hasNext()) {
      // 不正な値
      return null;
    }
    final String val = keyValIte.next();
    return new String[] {key, val};
  }

  /**
   * コンストラクタ.
   *
   * @param jsonItem JSON項目文字列
   */
  private JsonMapKeyValueSeparateParser(final String jsonItem) {
    super(jsonItem);
  }

  /**
   * 分割始点終点検索.<br>
   * <ul>
   * <li>ダブルクォーテーション（エスケープ無し）で囲まれていないコロン箇所で分割する。</li>
   * <li>１つめのコロン箇所だけを返す。</li>
   * </ul>
   *
   * @param value 対象文字列
   * @return 始点終点リスト
   */
  @Override
  protected List<int[]> findBeginEnds(final String value) {
    final List<int[]> idxs = new ArrayList<>();
    if (ValUtil.isBlank(value)) {
      // 空の場合
      return idxs;
    }

    int beginPos = 0;
    int endPos = beginPos;

    boolean notBlank = false;
    boolean inDq = false;
    boolean readingValue = false;

    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);

      if (!notBlank && c != ' ' && c != ':') {
        notBlank = true;
        beginPos = i;
        endPos = i + 1;
      }
      if (readingValue) {
        if (notBlank && c != ' ') {
          endPos = i + 1;
        }
        continue;
      }
      if (notBlank && c != ' ' && c != ':') {
        endPos = i + 1;
      }

      if (c == '"') {
        if (isPreEsc(value, i)) {
          continue;
        }
        // エスケープされていない場合のみ
        // ダブルクォーテーション内のトグル
        inDq = !inDq;
        continue;
      }
      if (inDq) {
        // ダブルクォーテーション内は無視
        continue;
      }

      if (c == ':') {
        // コロンの場合はキーの終点として格納
        trimDqPosAdd(idxs, beginPos, endPos, value);
        // 次の始点
        beginPos = i + 1;
        endPos = beginPos;
        notBlank = false;
        readingValue = true;
      }
    }
    trimDqPosAdd(idxs, beginPos, endPos, value);
    return idxs;
  }

}
