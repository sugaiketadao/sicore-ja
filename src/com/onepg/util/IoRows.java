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

  /** 始端行番号. */
  private int beginRowNo = -1;
  /** 終端行番号. */
  private int endRowNo = -1;
  /** 制限超え判定. */
  private boolean limitOverFlag = false;

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

    if (srcList instanceof IoRows) {
      // ソースマップが本クラスの場合はクラス変数値をコピー
      final IoRows tlist = (IoRows) srcList;
      setBeginRowNo(tlist.getBeginRowNo());
      setEndRowNo(tlist.getEndRowNo());
      setLimitOver(tlist.isLimitOver());
    }
  }

  /**
   * コンストラクタ.
   */
  public IoRows(final int initialCapacity) {
    super(initialCapacity);
  }

  /**
   * 始端行番号取得.
   *
   * @return 始端行番号
   */
  public int getBeginRowNo() {
    return beginRowNo;
  }

  /**
   * 始端行番号格納.
   *
   * @param beginRowNo 始端行番号
   */
  public void setBeginRowNo(final int beginRowNo) {
    this.beginRowNo = beginRowNo;
  }

  /**
   * 終端行番号取得.
   *
   * @return 終端行番号
   */
  public int getEndRowNo() {
    return endRowNo;
  }

  /**
   * 終端行番号格納.
   *
   * @param endRowNo 終端行番号
   */
  public void setEndRowNo(final int endRowNo) {
    this.endRowNo = endRowNo;
  }

  /**
   * 制限超え判定.
   *
   * @return 制限超えの場合は <code>true</code>
   */
  public boolean isLimitOver() {
    return limitOverFlag;
  }

  /**
   * 制限超え判定格納.
   *
   * @param limitOver 制限超えの場合は <code>true</code>
   */
  public void setLimitOver(final boolean limitOver) {
    this.limitOverFlag = limitOver;
  }
}
