package com.onepg.db;

import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLビルダー.<br>
 * <ul>
 * <li>DBアクセス時に必要な SQLとパラメーターリストを内包するクラスです。</li>
 * <li>SQLの組み立てとパラメーターのセットを同時に行えるメソッドを持ちます。</li>
 * <li>add* のメソッドは自インスタンスを返すのでメソッドチェーンで使えます。</li>
 * <li>SQL内の 2文字以上のブランクを1文字ブランクに置き換えして SQL追加します。</li>
 * </ul>
 * <pre>
 * ［例１］ <code>sqlBuilder.addQuery("AND a.user_id IS NOT NULL ");</code>
 * ［例２］ <code>sqlBuilder.addQuery("AND a.user_id = ? ", userId);</code>
 * ［例３］ <code>sqlBuilder.addQuery("AND ? <= a.birth_dt AND a.birth_dt <= ?", birthDtFrom, birthDtTo);</code>
 * ［例４］ <code>sqlBuilder.addQnotB("AND a.user_id = ? ", userId);</code>
 * ［例５］ <code>sqlBuilder.addQnotB("AND a.user_id = ? ", userId).addQnotB("AND a.user_nm LIKE ? ", '%' + name + '%');</code>
 * </pre>
 */
public final class SqlBuilder {

  /** １バイトブランク. */
  private static final String ONEBLANK = " ";

  /** SQL. */
  private final StringBuilder query = new StringBuilder();
  /** パラメーター. */
  private final List<Object> parameters = new ArrayList<>();
  
  /**
   * プロトコル違反回避SQL.<br>
   * <a href="https://support.oracle.com/knowledge/Middleware/2707017_1.html">support.oracle.com（参考情報）</a>
   */
  public static final String PROTOCOL_ERR_AVOID_SQL =
      " /* protocol error avoidance */ FETCH FIRST 99999999 ROWS ONLY ";

  /**
   * コンストラクタ.
   */
  public SqlBuilder() {
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
   * SQLビルダー追加.<br>
   * <ul>
   * <li>SQLとパラメーターを引き継ぐ。</li>
   * </ul>
   *
   * @param sb SQLビルダー
   */
  public void addSqlBuilder(final SqlBuilder sb) {

    addQuery(sb.getSql());

    final List<Object> params = sb.getParameters();
    for (final Object param : params) {
      addParam(param);
    }
  }

  /**
   * SQL＆パラメーター追加.<br>
   * <ul>
   * <li>パラメーター引数は省略可能で単一または複数で渡すことも可能です。</li>
   * </ul>
   * <pre>
   * ［例１］ <code>sqlBuilder.addQuery("AND a.user_id IS NOT NULL ");</code>
   * ［例２］ <code>sqlBuilder.addQuery("AND a.user_id = ? ", userId);</code>
   * ［例３］ <code>sqlBuilder.addQuery("AND ? <= a.birth_dt AND a.birth_dt <= ?", birthDtFrom, birthDtTo);</code>
   * </pre>
   * @param sql    SQL
   * @param params パラメーター（複数可能）（省略可能）
   * @return 自インスタンス
   */
  public SqlBuilder addQuery(final String sql, final Object... params) {
    if (ValUtil.isBlank(sql)) {
      return this;
    }

    // 引数SQLの先頭がブランクの場合、先頭に１文字ブランク追加
    // ただし既存SQLが空または最後がブランクの場合は追加しない
    if (sql.startsWith(ONEBLANK) && this.query.length() > 1
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

    // パラメーター追加
    addParam(params);

    return this;
  }

  /**
   * パラメーター追加.<br>
   * <ul>
   * <li>複数渡すことも可能です。</li>
   * </ul>
   * <pre>
   * ［例１］ <code>sqlBuilder.addParam(userId);</code>
   * ［例２］ <code>sqlBuilder.addParam(birthDtFrom, birthDtTo);</code>
   * </pre>
   *
   * @param params パラメーター（複数可能）
   * @return 自インスタンス
   */
  public SqlBuilder addParam(final Object... params) {
    if (ValUtil.isEmpty(params)) {
      return this;
    }
    for (final Object param : params) {
      this.parameters.add(param);
    }
    return this;
  }


  /**
   * カンマ区切りバインド文字SQL追加.<br>
   * <ul>
   * <li>リスト内の要素数だけカンマ区切りでバインド文字 "?" をSQLに追加します。</li>
   * <li>リスト内の要素数が 3 の場合は "?,?,?" がSQLに追加されます。</li>
   * <li>リスト内の値はバインド文字パラメーターとして追加されます。</li>
   * <li>バインド文字数が可変の IN句で使用する想定です。</li>
   * </ul>
   * <pre>
   * ［例］<code>sqlBuilder.addQuery("AND type_cs IN (").addListInBind(list).addQuery(")");</code>
   * </pre>
   *
   * @param params パラメーターリスト
   * @return 自インスタンス
   */
  public SqlBuilder addListInBind(final List<Object> params) {
    if (ValUtil.isEmpty(params)) {
      return this;
    }
    final StringBuilder sb = new StringBuilder();
    for (final Object param : params) {
      this.parameters.add(param);
      sb.append("?,");
    }
    ValUtil.deleteLastChar(sb);
    addQuery(sb.toString());
    return this;
  }

  /**
   * パラメーターが <code>null</code>以外かつブランク以外 の場合のみ SQLとパラメーターを追加.<br>
   * <ul>
   * <li>パラメーターが <code>null</code>・ブランク以外の場合のみ SQLとパラメーターを追加します。</li>
   * <li>それ以外の仕様は <code>#addQuery(String, Object...)</code>と同じです。</li>
   * <li>基本的には本メソッドのショートカット <code>#addQnotB(String, Object)</code> を使用してください。</li>
   * </ul>
   * <pre>下記例では <code>userId</code> が <code>null</code>・ブランク以外の場合のみ SQLが追加されます。
   * ［例］ <code>sqlBuilder.addQueryWithParamNotBlank("AND user_id = ? ", userId);</code>
   * </pre>
   * @see #addQuery(String, Object...)
   * @see #addQnotB(String, Object)
   * @param sql SQL
   * @param param パラメーター（単一のみ）
   * @return 自インスタンス
   */
  public SqlBuilder addQueryWithParamNotBlank(final String sql, final Object param) {

    if (ValUtil.isNull(param)) {
      return this;
    }

    if (param instanceof String && ValUtil.isBlank((String) param)) {
      return this;
    }

    addQuery(sql, param);
    return this;
  }

  /**
   * パラメーターが <code>null</code>以外かつブランク以外 の場合のみ SQLとパラメーターを追加.<br>
   * <ul>
   * <li><code>#addQueryWithParamNotBlank(String, Object)</code> のショートカット。</li>
   * <li>パラメーターが <code>null</code>・ブランク以外の場合のみ SQLとパラメーターを追加します。</li>
   * <li>それ以外の仕様は <code>#addQuery(String, Object...)</code>と同じです。</li>
   * </ul>
   * <pre>下記例では <code>userId</code> が <code>null</code>・ブランク以外の場合のみ SQLが追加されます。
   * ［例］ <code>sqlBuilder.addQnotB("AND user_id = ? ", userId);</code>
   * </pre>
   *
   * @see #addQueryWithParamNotBlank(String, Object)
   * @param sql SQL
   * @param param パラメーター（単一のみ）
   * @return 自インスタンス
   */
  @SuppressWarnings("all")
  public SqlBuilder addQnotB(final String sql, final Object param) {
    return addQueryWithParamNotBlank(sql, param);
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
   * <li>２文字以上のブランクを1文字ブランクに置き換え。<br>
   * ただしシングルクォーテーションに挟まれたブランクは置き換えない。</li>
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
