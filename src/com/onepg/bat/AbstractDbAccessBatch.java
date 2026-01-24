package com.onepg.bat;

import com.onepg.db.DbUtil;
import com.onepg.util.IoItems;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;

import java.sql.Connection;

/**
 * DBアクセス バッチ処理 基底クラス.<br>
 * <ul>
 * <li>データベース接続を含む バッチ処理の基底クラスです。</li>
 * <li>DB接続の取得とクローズを自動的に処理します。</li>
 * </ul>
 */
public abstract class AbstractDbAccessBatch extends AbstractBatch {

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
  public AbstractDbAccessBatch() {
    super();
  }

  /**
   * メイン処理の呼び出し.<br>
   * <ul>
   * <li>引数をURLパラメータ形式からマップ形式に変換し、ログ開始処理を実行後、DB接続を取得して <code>doExecute</code>メソッドを呼び出します。</li>
   * <li>変換された引数は <code>IoItems</code>クラスとして <code>doExecute</code>メソッドに渡されます。</li>
   * <li><code>doExecute</code>メソッドの戻値が 0 の場合、処理が正常終了したとみなしコミットします。</li>
   * <li><code>doExecute</code>メソッドの戻値が 0 以外の場合または例外エラーが発生した場合、処理が異常終了したとみなしロールバックします。（ロールバックは <code>Connection#close()</code> で行われる）</li>
   * <li>処理終了後、DB接続を必ずクローズします。</li>
   * </ul>
   *
   * @param args 引数
   */
  @Override
  protected void callMain(final String[] args) {    
    final IoItems argsMap = new IoItems();
    if (!ValUtil.isEmpty(args)) {
      argsMap.putAllByUrlParam(args[0]);
    }
    if (this.logger.isDevelopMode()) {
      this.logger.develop(LogUtil.joinKeyVal("arguments", argsMap));
    }

    int status = 0;    
    try {
      this.logger.begin();
      // プーリングDB接続取得
      try (final Connection conn = DbUtil.getConn(super.traceCode)) {
        this.dbConn = conn;
        status = doExecute(argsMap);
        if (status == 0) {
          this.dbConn.commit();
        }
      } finally {
        this.dbConn = null;
      }
    } catch (final Exception | Error e) {
      status = 1;
      this.logger.error(e, "An exception error occurred in batch processing. ");
    }
    this.logger.end(status);
    System.exit(status);
  }

  /**
   * DB接続取得.<br>
   * <ul>
   * <li>現在のDB接続を返します。</li>
   * <li><code>callMain</code>メソッド内でのみ有効な接続です。</li>
   * </ul>
   *
   * @return DB接続
   */
  protected Connection getDbConn() {
    if (ValUtil.isNull(this.dbConn)) {
      throw new RuntimeException("Database connection is valid only during main processing (callMain method).");
    }
    return this.dbConn;
  }
}