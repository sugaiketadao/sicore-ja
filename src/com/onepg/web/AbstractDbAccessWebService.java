package com.onepg.web;

import com.onepg.db.DbUtil;
import com.onepg.util.Io;
import com.onepg.util.ValUtil;

import java.sql.Connection;

/**
 * DBアクセス Webサービス 基底クラス.<br>
 * <ul>
 * <li>データベース接続を含む Webサービスの基底クラスです。</li>
 * <li>DB接続の取得とクローズを自動的に処理します。</li>
 * </ul>
 */
public abstract class AbstractDbAccessWebService extends AbstractWebService {

  /**
   * DB接続.
   */
  private Connection dbConn = null;

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>スーパークラスのコンストラクタを呼び出します。</li>
   * </ul>
   */
  public AbstractDbAccessWebService() {
    super();
  }

  /**
   * メイン処理の呼び出し.<br>
   * <ul>
   * <li>プーリングDB接続を取得し、スーパークラスの処理を実行します。</li>
   * <li>処理が正常終了した場合はコミットします。</li>
   * <li>例外エラーが発生した場合はロールバックします。（ロールバックは <code>DbConnPooled#close()</code> で行われる）</li>
   * <li>処理終了後、DB接続を必ずクローズします。</li>
   * </ul>
   *
   * @param io 引数かつ戻値（リクエストかつレスポンス）
   * @throws Exception 例外エラー
   */
  @Override
  void execute(final Io io) throws Exception {
    // プーリングDB接続取得
    try (final Connection conn = DbUtil.getConnPooled(super.traceCode)) {
      this.dbConn = conn;
      super.execute(io);
      if (!io.hasErrorMsg()) {
        this.dbConn.commit();
      }
    } finally {
      this.dbConn = null;
    }
  }

  /**
   * DB接続取得.<br>
   * <ul>
   * <li>現在のDB接続を返します。</li>
   * <li><code>execute</code>メソッド内でのみ有効な接続です。</li>
   * </ul>
   *
   * @return DB接続
   */
  protected Connection getDbConn() {
    if (ValUtil.isNull(this.dbConn)) {
      throw new RuntimeException("Database connection is valid only during main processing (execute method).");
    }
    return this.dbConn;
  }
}