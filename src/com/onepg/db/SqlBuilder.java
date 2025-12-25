package com.onepg.db;

import com.onepg.util.ValUtil;

import java.util.List;

/**
 * SQLビルダー.<br>
 * <ul>
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
 * @see AbstractSqlWithParameters
 */
public final class SqlBuilder extends AbstractSqlWithParameters {

  /**
   * コンストラクタ.
   */
  public SqlBuilder() {
    super();
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
    super.addSql(sb.getSql());
    // パラメーター追加
    super.addParametersList(sb.getParameters());
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
    super.addSql(sql);
    // パラメーター追加
    super.addParameters(params);
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
    super.addParameters(params);
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
    for (int i = 0; i < params.size(); i++) {
      sb.append("?,");
    }
    ValUtil.deleteLastChar(sb);

    super.addSql(sb.toString());
    super.addParametersList(params);
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

}
