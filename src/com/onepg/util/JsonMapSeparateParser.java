package com.onepg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * JSONマップ分割パーサー.
 * @hidden
 */
final class JsonMapSeparateParser extends AbstractStringSeparateParser {

  /** JSONマップパターン. */
  static final Pattern JSON_MAP_PATTERN = Pattern.compile("^\\s*\\{\\s*.*\\s*\\}\\s*$");
  /** 空のJSONマップパターン. */
  static final Pattern BLANK_JSON_MAP_PATTERN = Pattern.compile("^\\s*\\{\\s*\\}\\s*$");

  /**
   * コンストラクタ.
   *
   * @param json JSON文字列
   */
  JsonMapSeparateParser(final String json) {
    super(json);
  }

  /**
   * 分割始点終点検索.<br>
   * <ul>
   * <li>ダブルクォーテーション（エスケープ無し）、角括弧、波括弧で囲まれていないカンマ箇所で分割する。</li>
   * </ul>
   *
   * @param value 対象文字列
   * @return 始点終点リスト
   */
  @Override
  protected List<int[]> findBeginEnds(final String value) {
    final List<int[]> idxs = new ArrayList<>();
    if (ValUtil.isBlank(value) || BLANK_JSON_MAP_PATTERN.matcher(value).find()) {
      // 空の場合
      return idxs;
    }

    if (!JSON_MAP_PATTERN.matcher(value).find()) {
      throw new RuntimeException("Must be enclosed in curly braces. " + LogUtil.joinKeyVal("json", value));
    }

    // 大外の波括弧の位置取得
    final int outerBegin = value.indexOf("{") + 1;
    final int outerEnd = value.lastIndexOf("}") - 1;

    // 最初の始点位置
    int beginPos = outerBegin;
    int endPos = beginPos;

    boolean notBlank = false;
    boolean inDq = false;
    int nestAryLvl = 0;
    int nestMapLvl = 0;

    int i = outerBegin;

    // 性能対応（1000文字を境界とした最適化）
    final char[] valChars;
    final boolean useCharAry = (value.length() > 1000);
    if (useCharAry) {
      // char[]配列使用
      valChars = value.toCharArray();
    } else {
      // charAt()使用
      valChars = null;
    }

    for (; i <= outerEnd; i++) {
      final char c;
      if (useCharAry) {
        c = valChars[i];
      } else {
        c = value.charAt(i);
      }
      
      if (c != ' ' && c != ',') {
        // ゼロバイトブランクを除いて始点終点を決定する
        if (!notBlank) {
          notBlank = true;
          beginPos = i;
        }
        endPos = i + 1;
      }

      if (c == '"') {
        if (useCharAry) {
          if (isPreEsc(valChars, i)) {
            continue;
          }
        } else {
          if (isPreEsc(value, i)) {
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

      if (c == '[') {
        // 開き角括弧
        nestAryLvl++;
        continue;
      }
      if (c == ']' && nestAryLvl > 0) {
        // 閉じ角括弧
        nestAryLvl--;
        continue;
      }
      if (c == '{') {
        // 開き波括弧
        nestMapLvl++;
        continue;
      }
      if (c == '}' && nestMapLvl > 0) {
        // 閉じ波括弧
        nestMapLvl--;
        continue;
      }
      if (nestAryLvl > 0 || nestMapLvl > 0) {
        // 角括弧内または波括弧内は無視（括弧が閉じられるまで）
        continue;
      }

      if (c == ',') {
        // カンマの場合は始点終点を追加
        idxs.add(new int[] {beginPos, endPos});
        // 次の始点
        beginPos = i + 1;
        endPos = beginPos;
        notBlank = false;
      }
    }

    if (notBlank) {
      // 最後の始点終点を追加
      idxs.add(new int[] {beginPos, endPos});
    }
    return idxs;
  }
}
