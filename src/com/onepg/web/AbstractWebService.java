package com.onepg.web;

import com.onepg.util.BreakException;
import com.onepg.util.Io;
import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.onepg.util.ValUtil;

/**
 * Webサービス 基底クラス.<br>
 * <ul>
 * <li>各Webサービスの共通処理（ログ出力、例外処理等）を提供します。</li>
 * <li>サブクラスで <code>doExecute</code>メソッドを実装することで具体的な Webサービス処理を定義します。</li>
 * </ul>
 */
public abstract class AbstractWebService {

  /** トレースコード. */
  protected final String traceCode;
  /** ログライター. */
  protected final LogWriter logger;

  /**
   * メイン処理.<br>
   * <ul>
   * <li>サブクラスで具体的な Webサービス処理を実装します。</li>
   * </ul>
   *
   * @param io 引数かつ戻値（リクエストかつレスポンス）
   * @throws Exception 例外エラー
   */
  public abstract void doExecute(final Io io) throws Exception;

  /**
   * コンストラクタ.
   */
  public AbstractWebService() {
    this.traceCode = ValUtil.getSequenceCode();
    this.logger = LogUtil.newLogWriter(getClass(), this.traceCode);
  }

  /**
   * メイン処理の呼び出し.<br>
   * <ul>
   * <li>ログ開始処理を実行後、<code>doExecute</code>メソッドを呼び出し、最後にログ終了処理を実行します。</li>
   * <li>外部からの直接呼び出しを防ぐためパッケージプライベートとしています。</li>
   * </ul>
   *
   * @param io 引数かつ戻値（リクエストかつレスポンス）
   * @throws Exception 例外エラー
   */
  void execute(final Io io) throws Exception {
    try {
      this.logger.begin();
      doExecute(io);
    } catch (final Exception | Error e) {
      this.logger.error(e, "An exception error occurred in web service processing. ");
      throw new BreakException();
    } finally {
      this.logger.end();
    }
  }
}
