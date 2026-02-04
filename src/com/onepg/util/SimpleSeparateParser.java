package com.onepg.util;

import java.util.ArrayList;
import java.util.List;

/**
 * シンプル分割パーサー.<br>
 * <ul>
 * <li>指定された区切り文字で文字列を分割する</li>
 * <li>可変長の区切り文字に対応</li>
 * <li>連続する区切り文字は空文字列として扱われる</li>
 * </ul>
 * <pre>［例］
 * <code>for (final String item : (new SimpleSeparateParser("a,b,c", ","))) {
 *   System.out.println(item);
 * }</code></pre>
 * @hidden
 */
final class SimpleSeparateParser extends AbstractStringSeparateParser {

  /** 分割文字列. */
  private final String sep;

  /**
   * コンストラクタ.
   *
   * @param line 文字列
   * @param sep 分割文字列
   */
  SimpleSeparateParser(final String line, final String sep) {
    super(line, true);
    this.sep = sep;
  }

  /**
   * 分割始点終点検索.
   *
   * @param value 対象文字列
   * @return 始点終点リスト
   */
  @Override
  protected List<int[]> findBeginEnds(final String value) {
    final List<int[]> idxs = new ArrayList<>();
    final int sepLen = this.sep.length();
    int beginPos = 0;
    int endPos = beginPos;

    while ((endPos = value.indexOf(this.sep, beginPos)) != -1) {
      // 区切り文字手前まで
      idxs.add(new int[] {beginPos, endPos});
      // 区切り文字直後
      beginPos = endPos + sepLen;
    }

    // 最後の始点終点を追加
    idxs.add(new int[] {beginPos, value.length()});
    return idxs;
  }
}
