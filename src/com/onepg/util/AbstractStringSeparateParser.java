package com.onepg.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 文字列分割処理 基底クラス.<br>
 * <ul>
 * <li>文字列を指定のルールで分割し、各項目をイテレートする機能を提供する。</li>
 * <li>サブクラスで分割ルールを実装する。</li>
 * </ul>
 * @hidden
 */
abstract class AbstractStringSeparateParser implements Iterable<String> {

  /** 対象文字列. */
  private final String value;
  /** 始点終点リスト. */
  private List<int[]> beginEnds = null;


  /**
   * 分割始点終点検索.<br>
   * <ul>
   * <li>分割した１項目の始点を配列のインデックス0、終点をインデックス1として返す。</li>
   * <li>複数項目を想定しリストで返す。</li>
   * </ul>
   *
   * @param value 対象文字列
   * @return 始点終点リスト
   */
  protected abstract List<int[]> findBeginEnds(final String value);

  /**
   * コンストラクタ.
   *
   * @param value 対象文字列
   */
  AbstractStringSeparateParser(final String value) {
    this(value, false);
  }

  /**
   * コンストラクタ.
   * <ul>
   * <li>子クラスコンストラクタで <code>super()</code> 呼び出し後にメンバー変数初期化などの処理がある場合は初期化処理を遅延させ <code>iterator()</code> 実行時に行う必要がある。</li>
   * </ul>
   * <pre>［要遅延 コンストラクタ例］<code>
   *   SubParser(final String line, final String sep) {
   *     super(line, true);
   *     this.sep = sep;
   *   }</code></pre>
   * 
   * @param value 対象文字列
   * @param delayInit 初期化遅延実行フラグ <code>true</code> の場合は初期化を遅延する
   */
  AbstractStringSeparateParser(final String value, final boolean delayInit) {
    this.value = value; // nullも許可する設計なのでそのまま
    if (!delayInit) {
      init();
    }
  }

  @Override
  public Iterator<String> iterator() {
    init();
    return new StringSeparateIterator();
  }

  /**
   * 初期化処理.<br>
   * <ul>
   * <li>分割始点終点検索呼び出し。</li>
   * </ul>
   */
  private void init() {
    if (ValUtil.isNull(this.beginEnds)) {
      if (ValUtil.isNull(this.value)) {
        this.beginEnds = new ArrayList<>();
      } else {
        this.beginEnds = findBeginEnds(this.value);
      }
    }
  }

  /**
   * エスケープ判定.<br>
   * <ul>
   * <li>指定位置の文字がエスケープされているか判定 （直前に奇数個のバックスラッシュがあればエスケープ）</li>
   * </ul>
   *
   * @param target 判定対象
   * @param targetPos 判定対象位置
   * @return エスケープされていれば <code>true</code>
   */
  protected boolean isPreEsc(final String target, final int targetPos) {
    if (targetPos <= 0 || target == null || targetPos > target.length()) {
        return false;
    }
    
    int bsCount = 0;
    for (int i = targetPos - 1; i >= 0; i--) {
      if (target.charAt(i) != '\\') {
        break;
      }
      bsCount++;
    }
    return (bsCount % 2 == 1);
  }

  /**
   * エスケープ判定 (char[]版).<br>
   * <ul>
   * <li>指定位置の文字がエスケープされているか判定（直前に奇数個のバックスラッシュがあればエスケープ）</li>
   * </ul>
   *
   * @param target 判定対象char配列
   * @param targetPos 判定対象位置
   * @return エスケープされていれば <code>true</code>
   */
  protected static boolean isPreEsc(final char[] target, final int targetPos) {
    if (targetPos <= 0 || ValUtil.isNull(target) || targetPos >= target.length) {
      return false;
    }

    int bsCount = 0;
    for (int i = targetPos - 1; i >= 0 && target[i] == '\\'; i--) {
      bsCount++;
    }
    // 奇数個なら true
    return (bsCount % 2 == 1);
  }

  /**
   * 両端がダブルクォーテーションの場合は内側をリストに格納.
   *
   * @param retList  結果リスト
   * @param beginPos 始点
   * @param endPos   終点
   * @param value    元の文字列
   */
  protected void trimDqPosAdd(final List<int[]> retList, final int beginPos, final int endPos,
      final String value) {
    if (beginPos < value.length() && endPos > 0 && beginPos + 1 < endPos
        && value.charAt(beginPos) == '"'
        && value.charAt(endPos - 1) == '"') {
        retList.add(new int[] {beginPos + 1, endPos - 1});
    } else {
        retList.add(new int[] {beginPos, endPos});
    }
  }

  /**
   * 文字列分割イテレータークラス.
   */
  private final class StringSeparateIterator implements Iterator<String> {
    /** 現在位置. */
    private int beginEndsIndex = 0;
    /** 最大位置. */
    private final int maxIndex;

    /**
     * コンストラクタ.
     */
    private StringSeparateIterator() {
      this.maxIndex = beginEnds.size();
    }

    /**
     * 次文字列確認.
     *
     * @return 次の文字列が存在する場合は <code>true</code>
     */
    @Override
    public boolean hasNext() {
      return this.beginEndsIndex < this.maxIndex;
    }

    /**
     * 次文字列取得.
     *
     * @return 次文字列
     */
    @Override
    public String next() {
      if (!hasNext()) {
        throw new RuntimeException("No next element exists. " +
            LogUtil.joinKeyVal("currentIndex", this.beginEndsIndex, "maxIndex", this.maxIndex));
      }

      final int[] pos = beginEnds.get(this.beginEndsIndex);
      this.beginEndsIndex++;

      // バリデーション
      if (pos[0] < 0 || pos[1] > value.length() || pos[0] > pos[1]) {
          throw new RuntimeException("Invalid string index. " +
              LogUtil.joinKeyVal("begin", pos[0], "end", pos[1], 
                                "valueLength", value.length()));
      }

      try {
          return value.substring(pos[0], pos[1]);
      } catch (StringIndexOutOfBoundsException e) {
          throw new RuntimeException("String index out of bounds. " +
              LogUtil.joinKeyVal("begin", pos[0], "end", pos[1]), e);
      }
    }
  }

}
