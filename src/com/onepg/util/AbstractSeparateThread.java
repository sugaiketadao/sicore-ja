package com.onepg.util;

/**
 * 別スレッド処理 基底クラス.
 * @hidden
 */
abstract class AbstractSeparateThread implements Runnable {

  /** ログライター. */
  protected LogWriter logger = null;
  /** 処理中フラグ. */
  private volatile boolean running = false;
  /** スレッドインスタンス. */
  private Thread thread = null;

  /**
   * メイン処理.
   */
  protected abstract void doExecute();

  /**
   * コンストラクタ.
   */
  AbstractSeparateThread() {
    super();
  }

  @Override
  public final void run() {
    this.running = true;
    this.logger = LogUtil.newLogWriter(this.getClass(), this.thread.getName());
    this.logger.begin();
    try {
      doExecute();
    } catch (Exception | Error e) {
      this.logger.error(e, "An exception error occurred during separate thread execution. ");
    } finally {
      this.running = false;
      this.logger.end();
    }
  }

  /**
   * スレッド実行.
   */
  public synchronized void execute() {
    if (isRunning()) {
        throw new RuntimeException("The thread is already running. ");
    }
    
    try {
      this.thread = new Thread(this, getClass().getSimpleName() + "-" + ValUtil.getSequenceCode());
      this.thread.setDaemon(false); // 明示的にデーモンスレッドではないことを設定
      this.thread.start();
    } catch (IllegalArgumentException | SecurityException e) {
        throw new RuntimeException("An exception error occurred during thread creation. ", e);
    }
  }

  /**
   * 実行中フラグ取得.
   * <ul>
   * <li>runningフラグとスレッドの生存状態の両方をチェック</li>
   * <li>スレッド起動直後や終了直前は <code>false</code> を返す可能性がある</li>
   * </ul>
   *
   * @return 実行中の場合は <code>true</code>
   */
  public boolean isRunning() {
    return this.running && this.thread != null && this.thread.isAlive();
  }
}
