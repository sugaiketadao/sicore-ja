package com.onepg.db;

import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLパラメーター 基底クラス.<br>
 * <ul>
 * <li>DBアクセス時に必要な SQLとパラメーターリストを内包するクラスです。</li>
 * <li>SQL内の 2文字以上のブランクを1文字ブランクに置き換えして SQL追加します。</li>
 * </ul>
 */
public abstract class AbstractSqlWithParameters {

  /** １バイトブランク. */
  private static final String ONEBLANK = " ";

  /** SQL. */
  private final StringBuilder query = new StringBuilder();
  /** パラメーター. */
  private final List<Object> parameters = new ArrayList<>();
  
  /**
   * ORACLEプロトコル違反回避SQL.<br>
   * <a href="https://support.oracle.com/knowledge/Middleware/2707017_1.html">support.oracle.com（参考情報）</a>
   */
  public static final String ORACLE_PROTOCOL_ERR_AVOID_SQL =
      " /* protocol error avoidance */ FETCH FIRST 99999999 ROWS ONLY ";

  /**
   * コンストラクタ.
   */
  AbstractSqlWithParameters() {
    // 処理無し
  }

  /**
   * SQL文字列取得.
   *
   * @return SQL文字列
   */
  String getSql() {
    return this.query.toString();
  }

  /**
   * パラメーター取得.
   *
   * @return パラメーター
   */
  List<Object> getParameters() {
    return this.parameters;
  }

  /**
   * SQL文字列長さ取得.
   *
   * @return SQL文字列長さ
   */
  public int length() {
    return this.query.length();
  }

  /**
   * SQL追加.<br>
   * <ul>
   * <li>SQLを追加します。</li>
   * <li>２文字以上のブランクを1文字ブランクに置き換えして追加します。</li>
   * </ul>
   * 
   * @param sql    SQL
   */
  protected void addSql(final String sql) {
    if (ValUtil.isBlank(sql)) {
      return;
    }

    // 引数SQLの先頭がブランクの場合、先頭に１文字ブランク追加
    // ただし既存SQLが空または最後がブランクの場合は追加しない
    if (sql.startsWith(ONEBLANK) && this.query.length() > 0
        && this.query.charAt(this.query.length() - 1) != ' ') {
      this.query.append(ONEBLANK);
    }

    // 前後のブランクをトリム
    // ２文字以上のブランクを1文字ブランクに置き換え
    // ただしシングルクォーテーションに挟まれたブランクは置き換えない
    this.query.append(trimSpaces(sql));

    // 引数SQLの最後がブランクの場合、最後に１文字ブランク追加
    if (sql.endsWith(ONEBLANK)) {
      this.query.append(ONEBLANK);
    }
  }

  /**
   * パラメーター追加.<br>
   * <ul>
   * <li>複数渡すことも可能です。</li>
   * </ul>
   *
   * @param params パラメーター（複数可能）
   */
  protected void addParameters(final Object... params) {
    if (ValUtil.isEmpty(params)) {
      return;
    }
    for (final Object param : params) {
      this.parameters.add(param);
    }
  }

  /**
   * パラメーターリスト追加.
   *
   * @param params パラメーターリスト
   */
  protected void addParameters(final List<Object> params) {
    if (ValUtil.isEmpty(params)) {
      return;
    }
    this.parameters.addAll(params);
  }

  /**
   * 最終SQL削除.
   * <ul>
   * <li>SQLの最後から指定文字数を削除します。</li>
   * </ul>
   * <pre>［例］
   * <code>for (final String key : params.keySet()) {
   *   sb.addQuery(key).addQuery("=?", params.get(key)).addQuery(",");
   * }
   * // 最後のカンマ削除
   * sb.deleteLastChar(1);
   * </code></pre>
   * 
   * @param deleteCharCount 削除文字数
   */
  public void deleteLastChar(final int deleteCharCount) {
    if (this.query.length() <= deleteCharCount) {
      return;
    }
    this.query.setLength(this.query.length() - deleteCharCount);
  }

  /**
   * パラメータークリア.
   */
  public void clearParameters() {
    this.parameters.clear();
  }

  /**
   * ログ用文字列返却.
   */
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    try {
      sb.append("{\"").append(this.query.toString()).append("\"<-");
      sb.append(LogUtil.join(this.parameters)).append("}");
    } catch (Exception ignore) {
      // 処理なし
    }
    return sb.toString();
  }

  /**
   * ２文字以上のブランクを1文字ブランクに置き換え.<br>
   * <ul>
   * <li>前後のブランクをトリム。</li>
   * <li>２文字以上のブランクを1文字ブランクに置き換え。</li>
   * <li>シングルクォーテーションに挟まれたブランクは置き換えない。</li>
   * </ul>
   * 
   * @param sql SQL
   * @return 結果SQL
   */
  private static String trimSpaces(final String sql) {
      if (ValUtil.isBlank(sql)) {
          return ValUtil.BLANK;
      }
      
      final int length = sql.length();
      final char[] chars = sql.toCharArray(); // 配列アクセスで高速化
      final StringBuilder ret = new StringBuilder(length);
      
      boolean inSq = false;
      boolean prevSpace = false;
      int beginPos = 0;
      int endPos = length;
      
      // 前後のトリムを事前計算
      while (beginPos < endPos && Character.isWhitespace(chars[beginPos])) {
          beginPos++;
      }
      while (endPos > beginPos && Character.isWhitespace(chars[endPos - 1])) {
          endPos--;
      }
      
      for (int i = beginPos; i < endPos; i++) {
          final char c = chars[i];

          if (c == '\'' && (i == 0 || chars[i-1] != '\\')) {
              inSq = !inSq;
              ret.append(c);
              prevSpace = false;
          } else if (inSq) {
              ret.append(c);
              prevSpace = false;
          } else if (Character.isWhitespace(c)) {
              if (!prevSpace) {
                  ret.append(' ');
                  prevSpace = true;
              }
          } else {
              ret.append(c);
              prevSpace = false;
          }
      }
      
      return ret.toString();
  }
}
