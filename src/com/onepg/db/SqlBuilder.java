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
 * </ul>
 * <pre>
 * ［例１］ <code>sqlBuilder.addQuery("AND a.user_id IS NOT NULL ");</code>
 * ［例２］ <code>sqlBuilder.addQuery("AND a.user_id = ? ", userId);</code>
 * ［例３］ <code>sqlBuilder.addQuery("AND ? <= a.birth_dt AND a.birth_dt <= ?", birthDtFrom, birthDtTo);</code>
 * ［例４］ <code>sqlBuilder.addQnotB("AND a.user_id = ? ", userId);</code>
 * ［例５］ <code>sqlBuilder.addQnotB("AND a.user_id = ? ", userId).addQnotB("AND a.user_nm LIKE ? ", '%' + name + '%');</code>
 * </pre>
 * 
 */
public final class SqlBuilder {

  /** SQL文字列. */
  private final StringBuilder query = new StringBuilder();
  /** パラメーター. */
  private final List<Object> parameters = new ArrayList<>();
  
  /**
   * コンストラクタ.
   */
  public SqlBuilder() {
    super();
  }

  /**
   * SQL文字列追加.<br>
   * <ul>
   * <li>SQL文字列を追加します。</li>
   * <li>２文字以上のブランクを1文字ブランクに置き換えして追加します。</li>
   * </ul>
   * 
   * @param sql SQL文字列
   */
  private void appendQuery(final String sql) {
    SqlUtil.appendQuery(this.query, sql);
  }

  /**
   * パラメーター追加.<br>
   * <ul>
   * <li>複数渡すことも可能です。</li>
   * </ul>
   *
   * @param params パラメーター（複数可能）
   */
  private void addAllParameters(final Object... params) {
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
  private void addAllParameters(final List<Object> params) {
    if (ValUtil.isEmpty(params)) {
      return;
    }
    this.parameters.addAll(params);
  }

  /**
   * SQL文字列取得.
   *
   * @return SQL文字列
   */
  String getQuery() {
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
   * SQLビルダー追加.<br>
   * <ul>
   * <li>SQLとパラメーターを引き継ぐ。</li>
   * </ul>
   *
   * @param sb SQLビルダー
   */
  public void addSqlBuilder(final SqlBuilder sb) {
    // SQL追加
    appendQuery(sb.getQuery());
    // パラメーター追加
    addAllParameters(sb.getParameters());
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
    // SQL追加
    appendQuery(sql);
    // パラメーター追加
    addAllParameters(params);
    return this;
  }

  /**
   * パラメーター追加.<br>
   * <ul>
   * <li>複数渡すことも可能です。</li>
   * </ul>
   * <pre>
   * ［例１］ <code>sqlBuilder.addParams(userId);</code>
   * ［例２］ <code>sqlBuilder.addParams(birthDtFrom, birthDtTo);</code>
   * </pre>
   *
   * @param params パラメーター（複数可能）
   * @return 自インスタンス
   */
  public SqlBuilder addParams(final Object... params) {
    addAllParameters(params);
    return this;
  }

  /**
   * カンマ区切りSQLバインド文字追加.<br>
   * <ul>
   * <li>リスト内の要素数だけカンマ区切りでSQLバインド文字 "?" をSQLに追加します。</li>
   * <li>リスト内の要素数が 3 の場合は "?,?,?" がSQLに追加されます。</li>
   * <li>リスト内の値はSQLバインド文字パラメーターとして追加されます。</li>
   * <li>SQLバインド文字数が可変の IN句で使用する想定です。</li>
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
    for (int i = 0; i < params.size(); i++) {
      sb.append("?,");
    }
    ValUtil.deleteLastChar(sb);

    appendQuery(sb.toString());
    addAllParameters(params);
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
   * ［例］ <code>sqlBuilder.addQueryIfNotBlankParameter("AND user_id = ? ", userId);</code>
   * </pre>
   * @see #addQuery(String, Object...)
   * @see #addQnotB(String, Object)
   * @param sql SQL
   * @param param パラメーター（単一のみ）
   * @return 自インスタンス
   */
  public SqlBuilder addQueryIfNotBlankParameter(final String sql, final Object param) {

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
   * <li><code>#addQueryIfNotBlankParameter(String, Object)</code> のショートカット。</li>
   * <li>パラメーターが <code>null</code>・ブランク以外の場合のみ SQLとパラメーターを追加します。</li>
   * <li>それ以外の仕様は <code>#addQuery(String, Object...)</code>と同じです。</li>
   * </ul>
   * <pre>下記例では <code>userId</code> が <code>null</code>・ブランク以外の場合のみ SQLが追加されます。
   * ［例］ <code>sqlBuilder.addQnotB("AND user_id = ? ", userId);</code>
   * </pre>
   *
   * @see #addQueryIfNotBlankParameter(String, Object)
   * @param sql SQL
   * @param param パラメーター（単一のみ）
   * @return 自インスタンス
   */
  @SuppressWarnings("all")
  public SqlBuilder addQnotB(final String sql, final Object param) {
    return addQueryIfNotBlankParameter(sql, param);
  }

  /**
   * 最終SQL文字列削除.
   * <ul>
   * <li>SQL文字列の最終文字（１文字）を削除します。</li>
   * </ul>
   * <pre>［例］
   * <code>for (final String key : params.keySet()) {
   *   sb.addQuery(key).addQuery("=?", params.get(key)).addQuery(",");
   * }
   * // 最後のカンマ削除
   * sb.deleteLastChar();
   * </code></pre>
   * 
   * @return 自インスタンス
   */
  public SqlBuilder delLastChar() {
    ValUtil.deleteLastChar(this.query);
    return this;
  }
  
  /**
   * 最終SQL文字列削除.
   * <ul>
   * <li>SQL文字列の最後から指定文字数を削除します。</li>
   * </ul>
   * <pre>［例］
   * <code>for (final String key : params.keySet()) {
   *   sb.addQuery(key).addQuery("=?", params.get(key)).addQuery(" AND ");
   * }
   * // 最後の AND 削除
   * sb.delLastChar(4);
   * </code></pre>
   * 
   * @param deleteCharCount 削除文字数
   * @return 自インスタンス
   */
  public SqlBuilder delLastChar(final int deleteCharCount) {
    ValUtil.deleteLastChar(this.query, deleteCharCount);
    return this;
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
}
