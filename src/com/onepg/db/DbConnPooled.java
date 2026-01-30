package com.onepg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.onepg.util.BreakException;
import com.onepg.util.LogUtil;

/**
 * プーリングDB接続クラス.
 * @hidden
 */
public final class DbConnPooled extends DbConn {

  /**
   * 使用中接続リスト&lt;接続シリアルコード&gt;（スレッドセーフ） .<br>
   * <ul>
   * <li><code>DbUtil</code> で管理されているリストのコピー</li>
   * </ul>
   */
  private final ConcurrentLinkedQueue<String> connBusyList;

  /**
   * コンストラクタ.
   *
   * @param conn DB接続
   * @param serialCode 接続シリアルコード
   * @param connBusyList 使用中接続リスト
   */
  DbConnPooled(final Connection conn, final String serialCode, final ConcurrentLinkedQueue<String> connBusyList) {
    this(conn, serialCode, connBusyList, null);
  }

  /**
   * コンストラクタ.
   *
   * @param conn DB接続
   * @param serialCode 接続シリアルコード
   * @param connBusyList 使用中接続リスト
   * @param traceCode トレースコード
   */
  DbConnPooled(final Connection conn, final String serialCode, final ConcurrentLinkedQueue<String> connBusyList, final String traceCode) {
    super(conn, serialCode, traceCode);
    this.connBusyList = connBusyList;
    if (super.logger.isDevelopMode()) {
      super.logger.develop("Database connection is now busy. " + LogUtil.joinKeyVal("busyConnSize", this.connBusyList.size()));
    }
  }

  /**
   * DB切断.<br>
   * <ul>
   * <li>実際にはDB切断せず使用中接続リストから削除のみ行う。（プーリングに返すイメージ）</li>
   * <li>実際にDB切断したい場合は <code>#rollbackCloseForce()</code> を使用する。</li>
   * <li>トランザクションをリセットするためにロールバックする。</li>
   * <li>なんらかの理由で接続が閉じられている場合は使用中接続リストからの削除のみ行う。</li>
   * <li>切断された接続は <code>DbUtil#getConnPooled()</code> でチェックして破棄される。</li>
   * <li>本メソッド内でエラーが発生した場合は実際にDB切断するが、DB切断処理もエラーとなった場合は例外を投げる。<br>
   * （異常な接続の発生の検知を早くするため）</li>
   * </ul>
   */
  @Override
  public void close() throws SQLException {
    try {
      if (!super.isClosed()) {
        super.rollback();
      }
    } catch (SQLException re) {
      super.logger.error(re, "Exception error occurred during database rollback. ");
      // ロールバックエラーが発生した場合は強制的にDB切断する
      try {
        super.close();
      } catch (SQLException ce) {
        super.logger.error(ce, "Exception error occurred during database close. ");
        throw new BreakException();
      }
    } finally {
      // 使用中接続リストから削除
      this.connBusyList.remove(super.serialCode);
      if (super.logger.isDevelopMode()) {
        super.logger.develop("Released busy database connection. " + LogUtil.joinKeyVal("busyConnSize", this.connBusyList.size()));
      }
    }
  }
}
