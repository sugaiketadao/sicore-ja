package com.onepg.db;

import com.onepg.db.SqlUtil.ItemClsType;
import com.onepg.util.IoItems;
import com.onepg.util.LogUtil;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

/**
 * SQL結果セットラッパークラス.
 *
 * <ul>
 * <li>結果セット <code>ResultSet</code> のラッパークラスでイテレーターの提供とステートメント、結果セットのクローズを受け持つ。</li>
 * <li>このクラスを介することでイテレーターからしかデータを取り出せなくなる。</li>
 * <li>try 句（try-with-resources文）で宣言する。</li>
 * <li>本クラスのイテレーターから取得した行マップの項目物理名は英字小文字となる。（<code>IoItems</code> のキー）</li>
 * </ul>
 * 
 * @see SqlUtil#select(java.sql.Connection, SqlBuilder)
 */
public final class SqlResultSet implements Iterable<IoItems>, AutoCloseable {

  /** ステートメント. */
  private final PreparedStatement stmt;
  /** 結果セット. */
  private final ResultSet rset;
  /** DB項目名・クラスタイプマップ. */
  private final Map<String, ItemClsType> nameClsMap;
  /** 接続シリアルコード. */
  private final String serialCode;

  /** 読込済行数. */
  private int readedCount = 0;
  /** 最終行読込済判定. */
  private boolean readedEndRowFlag = false;

  /**
   * コンストラクタ.
   *
   * @param stmt ステートメント
   * @param rset 結果セット
   * @param nameClsMap DB項目名・クラスタイプマップ
   * @param serialCode 接続シリアルコード
   */
  SqlResultSet(final PreparedStatement stmt, final ResultSet rset,
      final Map<String, ItemClsType> nameClsMap, final String serialCode) throws SQLException {
    super();
    this.rset = rset;
    this.stmt = stmt;
    this.nameClsMap = nameClsMap;
    this.serialCode = serialCode;
  }

  /**
   * イテレーター作成.
   *
   * @return 結果行イテレーター
   */
  @Override
  public Iterator<IoItems> iterator() {
    return new SqlResultRowIterator();
  }

  /**
   * クローズ.<br>
   * <ul>
   * <li>結果セットとステートメントを閉じる。</li>
   * </ul>
   *
   */
  @Override
  public void close() {
    closeResultSet();
    closeStatement();
  }

  /**
   * 結果セットを閉じる.
   */
  private void closeResultSet() {
    try {
      if (this.rset.isClosed()) {
        return;
      }
    } catch (SQLException ignore) {
      // 処理なし
    }
    try {
      this.rset.close();
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred while closing the result set. "
                              + LogUtil.joinKeyVal("serialCode", serialCode), e);
    }
  }

  /**
   * ステートメントを閉じる.
   */
  private void closeStatement() {
    try {
      if (this.stmt.isClosed()) {
        return;
      }
    } catch (SQLException ignore) {
      // 処理なし
    }
    try {
      this.stmt.close();
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred while closing the statement. "
                              + LogUtil.joinKeyVal("serialCode", serialCode), e);
    }
  }

  /**
   * データ有り（DB抽出条件に合致有り）確認.<br>
   * <ul>
   * <li>DB2 では #isBeforeFirst（TYPE_FORWARD_ONLYで） はエラーとなる。</li>
   * </ul>
   *
   * @return データが取得できた場合は <code>true</code>
   */
  public boolean isExists() {
    try {
      return this.rset.isBeforeFirst();
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred while checking data existence in result set. "
                                + LogUtil.joinKeyVal("serialCode", serialCode), e);
    }
  }

  /**
   * 読込済行数（DB抽出条件に合致した件数ではない）取得.<br>
   * <ul>
   * <li>イテレーターで読み込んだ件数を返す。</li>
   * </ul>
   *
   * @return 読込済行数
   */
  public int getReadedCount() {
    return this.readedCount;
  }

  /**
   * 最終行読込済判定.
   *
   * @return 最終行読込済の場合は <code>true</code>
   */
  public boolean isReadedEndRow() {
    return this.readedEndRowFlag;
  }

  /**
   * DB項目名取得.
   *
   * @return DB項目名文字列配列
   */
  public String[] getItemNames() {
    final String[] itemNames = new String[this.nameClsMap.size()];
    int idx = 0;
    for (final String name : this.nameClsMap.keySet()) {
      itemNames[idx++] = name;
    }
    return itemNames;
  }

  /**
   * 結果行イテレータークラス.
   * <ul>
   * <li>結果セット <code>ResultSet</code> のイテレータークラス。</li>
   * </ul>
   */
  public final class SqlResultRowIterator implements Iterator<IoItems> {

    /** 次行有フラグ. */
    private boolean hasNextRow = false;
    /** 次行確認済フラグ. */
    private boolean hasNextChecked = false;

    /**
     * コンストラクタ.
     */
    private SqlResultRowIterator() {
        super();
    }

    /**
     * 次行確認.<br>
     * <ul>
     * <li>try 句が使用されなかった場合に備えて次行が存在しなかった場合は結果セットとステートメントを閉じる。</li>
     * <li>連続した hasNext() 呼び出しでは再確認しない。</li>
     * </ul>
     *
     * @return 次行が存在する場合は <code>true</code>
     */
    @Override
    public boolean hasNext() {
      // 既に確認済みの場合は再確認しない
      if (hasNextChecked) {
          return this.hasNextRow;
      }

      // 次行存在確認
      try {
          // TYPE_FORWARD_ONLY では <code>ResultSet#isLast()</code> は使用できない。
          this.hasNextRow = rset.next();
          this.hasNextChecked = true; // 確認完了フラグ
      } catch (SQLException e) {
          throw new RuntimeException("Exception error occurred while checking next record in result set. " + LogUtil.joinKeyVal("serialCode",
              serialCode, "readedCount", String.valueOf(readedCount)), e);
      }

      if (!this.hasNextRow) {
          // 最終行読込済ON
          readedEndRowFlag = true;
          // 結果セットとステートメントを閉じる
          close();
      }

      return this.hasNextRow;
    }

    /**
     * 次行取得.
     *
     * @return 行マップ
     */
    @Override
    public IoItems next() {
      if (!hasNext()) {
          throw new RuntimeException("Next record does not exist. " + LogUtil.joinKeyVal("serialCode",
              serialCode, "readedCount", String.valueOf(readedCount)));
      }

      // 結果セット行マップ取得
      final IoItems retMap = SqlUtil.createIoItemsFromResultSet(rset, nameClsMap);
      readedCount++;

      // 再度確認が必要
      this.hasNextChecked = false;

      return retMap;
    }
  }
}
