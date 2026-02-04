package com.onepg.util;

import java.util.ArrayList;
import java.util.List;

/**
 * CSV（ダブルクォーテーション付）パーサー.
 * @hidden
 */
final class CsvDqParser extends AbstractStringSeparateParser {
  
  /** 
   * ダブルクォーテーション未閉鎖判定フラグ.
   * （フィールド宣言時に初期値を設定すると findBeginEnds() で算出した値が上書きされるため、初期値は設定せず findBeginEnds() 内でのみ値を設定する）
   */
  private boolean unclosedDq;

  /**
   * コンストラクタ.
   *
   * @param csv CSV文字列
   */
  CsvDqParser(final String csv) {
    super(csv);
  }

  /**
   * 分割始点終点検索.<br>
   * <ul>
   * <li>エスケープされていないダブルクォーテーションで囲まれていないカンマの位置で分割する。</li>
   * <li>ダブルクォーテーション内のカンマは区切り文字として扱わない。</li>
   * <li>エスケープされたダブルクォーテーション（\"）は通常の文字として扱う。</li>
   * <li>２つ連続したダブルクォーテーション（""）は１つのダブルクォーテーション文字（"）として扱う。</li>
   * <li>各項目の先頭と末尾の空白文字は除去される。</li>
   * <li>ダブルクォーテーションが閉じているかの判定も同時に行う。</li>
   * </ul>
   *
   * @param value 対象文字列
   * @return 始点終点リスト
   */
  @Override
  protected List<int[]> findBeginEnds(final String value) {
    this.unclosedDq = false;
    final List<int[]> idxs = new ArrayList<>();
    if (ValUtil.isBlank(value)) {
      // 空の場合
      return idxs;
    }

    int beginPos = 0;
    int endPos = beginPos;

    boolean notBlank = false;
    boolean inDq = false;

    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);

      if (!notBlank && c != ' ' && c != ',') {
        notBlank = true;
        beginPos = i;
        endPos = i + 1;
      }
      if (notBlank && c != ' ' && c != ',') {
        endPos = i + 1;
      }

      if (c == '"') {
        if (isPreEsc(value, i)) {
          continue;
        }
        // ダブルクォーテーションの次の文字もダブルクォーテーションの場合はエスケープシーケンス（""→"）として扱う
        if (i + 1 < value.length() && value.charAt(i + 1) == '"') {
          if (inDq) {
            // ダブルクォーテーション内のみ有効
            // 次のダブルクォーテーションもスキップ
            i++; 
            continue;
          }
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
      if (c == ',') {
        // カンマの場合は始点終点を追加
        trimDqPosAdd(idxs, beginPos, endPos, value);
        // 次の始点
        beginPos = i + 1;
        endPos = beginPos;
        notBlank = false;
      }
    }

    // 最後の始点終点を追加
    trimDqPosAdd(idxs, beginPos, endPos, value);

    // ダブルクォーテーション閉じてない場合はフラグON
    if (inDq) {
      this.unclosedDq = true;
    }
    return idxs;
  }

  /**
   * ダブルクォーテーション未閉鎖判定フラグ取得.
   *
   * @return ダブルクォーテーションが閉じてない場合は <code>true</code>
   */  
  boolean isUnclosedDq() {
    return this.unclosedDq; 
  }
}
