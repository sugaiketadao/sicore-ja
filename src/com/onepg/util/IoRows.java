package com.onepg.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * 複数行リスト.<br>
 * <ul>
 * <li>複数個のマップデータを保持する。</li>
 * </ul>
 */
public final class IoRows extends ArrayList<IoItems> {

  /** 次行存在フラグ. */
  private boolean hasNextRow = false;

  /**
   * コンストラクタ.
   */
  public IoRows() {
    super();
  }

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>内容をディープコピーするため、ソースリストとの参照は切れる。</li>
   * </ul>
   *
   * @param srcList ソースリスト
   */
  public IoRows(final Collection<? extends Map<? extends String, ? extends String>> srcList) {
    if (ValUtil.isNull(srcList)) {
      throw new RuntimeException("Source list is required. ");
    }
    for (final Map<? extends String, ? extends String> row : srcList) {
      if (ValUtil.isNull(row)) {
        add(null);
        continue;
      }
      add(new IoItems(row));
    }
  }

  /**
   * コンストラクタ.
   */
  public IoRows(final int initialCapacity) {
    super(initialCapacity);
  }

  /**
   * 次行存在判定.
   *
   * @return 次行が存在する場合は <code>true</code>
   */
  public boolean hasNextRow() {
    return hasNextRow;
  }

  /**
   * 次行存在フラグ格納.
   *
   * @param hasNextRow 次行が存在する場合は <code>true</code>
   */
  public void setHasNextRow(final boolean hasNextRow) {
    this.hasNextRow = hasNextRow;
  }
}
