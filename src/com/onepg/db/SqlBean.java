package com.onepg.db;

import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;

import java.util.List;

/**
 * SQL Beanクラス.<br>
 * <ul>
 * <li>SQL文字列とバインド値を格納します。</li>
 * <li>このクラスのインスタンスは <code>SqlUtil</code> の SQL実行メソッドに引数として渡されます。</li>
 * </ul>
 */
public class SqlBean {

  /** SQL-ID.  */
  protected final String id;
  /** SQL文字列. */
  protected final String query;
  /** SQL文字列ビルダー. */
  protected final StringBuilder queryBuilder;
  /** バインド値リスト. */
  protected final List<Object> bindValues;
  
  /**
   * コンストラクタ.
   * @param id SQL-ID
   * @param query SQL文字列
   * @param queryBuilder SQL文字列ビルダー
   * @param bindValues バインド値リスト
   */
  protected SqlBean(final String id, final String query, final StringBuilder queryBuilder, final List<Object> bindValues) {
    this.id = id;
    this.query = query;
    this.queryBuilder = queryBuilder;
    this.bindValues = bindValues;
  }
  
  /**
   * SQL-ID取得.
   *
   * @return SQL-ID（null有り）
   */
  String getId() {
    return this.id;
  }

  /**
   * SQL文字列取得.
   *
   * @return SQL文字列
   */
  String getQuery() {
    if (!ValUtil.isNull(this.queryBuilder)) {
      return this.queryBuilder.toString();
    } else {
      // 固定SQL
      return this.query;
    }
  }
  
  /**
   * バインド値取得.
   *
   * @return バインド値リスト
   */
  List<Object> getBindValues() {
    return this.bindValues;
  }
  
  /**
   * ログ用文字列返却.
   */
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    try {
      sb.append("{");
      sb.append("\"").append(getQuery()).append("\"");
      sb.append("<-").append(LogUtil.join(getBindValues()));
      sb.append("}");
    } catch (Exception ignore) {
      // 処理なし
    }
    return sb.toString();
  }
}
