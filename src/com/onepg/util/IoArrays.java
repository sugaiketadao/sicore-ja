package com.onepg.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 配列リスト.<br>
 * <ul>
 * <li>テーブルイメージのデータを保持する。</li>
 * </ul>
 */
public final class IoArrays extends ArrayList<List<String>> {

 /** 配列項目名. */
  // private List<String> names = new ArrayList<String>();

  /**
   * コンストラクタ.
   */
  public IoArrays() {
    super();
  }

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>内容をディープコピーするため、ソースリストとの参照は切れる。</li>
   * <li></li>
   * </ul>
   *
   * @param srcList ソースリスト
   */
  public IoArrays(final Collection<? extends Collection<? extends String>> srcList) {
    if (ValUtil.isNull(srcList)) {
      throw new RuntimeException("Source list is required. ");
    }
    for (final Collection<? extends String> row : srcList) {
      if (ValUtil.isNull(row)) {
        add(null);
        continue;
      }
      // <code>ArrayList</code> のコンストラクタはシャロ―コピーとなるが内容がイミュータブルオブジェクト（<code>String</code>）のため、実質ディープコピーとなる。
      add(new ArrayList<String>(row));
    }
  }

  /**
   * コンストラクタ.
   */
  public IoArrays(final int initialCapacity) {
    super(initialCapacity);
  }
}
