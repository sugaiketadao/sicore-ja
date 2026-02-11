package com.onepg.bat;

import com.onepg.util.IoItems;
import com.onepg.util.LogTxtHandler;
import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.onepg.util.ValUtil;

/**
 * バッチ処理 基底クラス.<br>
 * <ul>
 * <li>各バッチ処理の共通処理（ログ出力、例外処理等）を提供します。</li>
 * <li>サブクラスで <code>doExecute</code>メソッドを実装することで具体的なバッチ処理を定義します。</li>
 * <li>サブクラスは <code>main</code>メソッドから本クラスの <code>callMain</code>メソッドを呼び出すと <code>doExecute</code>メソッドが実行されます。</li>
 * <li>サブクラス <code>main</code>メソッドへの引数は URLパラメータ形式で <code>callMain</code>メソッドにそのまま渡される前提としています。</li>
 * <li>URLパラメータ形式の引数はマップ形式に変換され、 <code>IoItems</code>クラスとして <code>doExecute</code>メソッドに渡されます。</li>
 * <li>サブクラス <code>doExecute</code>メソッド内でのエラー時は Exception をスローする前提としています。</li>
 * <li>正常終了時の <code>callMain</code>メソッドの戻値は 0 となります。</li>
 * <li><code>doExecute</code>メソッド内で Exception がスローされた場合の <code>callMain</code>メソッドの戻値は 1 となります。</li>
 * </ul>
 * <pre>
 * ［実装例］<code>public class ExampleBatch extends AbstractBatch {
 *    public static void main(String[] args) {
 *      System.exit((new ExampleBatch()).callMain(args));
 *    }
 * 
 *    @Override
 *    public void doExecute(final IoItems io) throws Exception {
 *      // バッチ処理内容を実装
 *    }
 * }</code>
 * ［実行例］<code>java com.example.ExampleBatch "param1=value1&param2=value2"</code>
 * </pre> 
 */
public abstract class AbstractBatch {

  /** トレースコード. */
  protected final String traceCode;
  /** ログライター. */
  protected final LogWriter logger;

  /**
   * メイン処理.<br>
   * <ul>
   * <li>サブクラスで具体的な バッチ処理を実装します。</li>
   * </ul>
   *
   * @param args 引数
   * @throws Exception 例外エラー
   */
  protected abstract void doExecute(final IoItems args) throws Exception;

  /**
   * コンストラクタ.
   */
  public AbstractBatch() {
    this.traceCode = ValUtil.getSequenceCode();
    this.logger = LogUtil.newLogWriter(getClass(), this.traceCode);
  }

  /**
   * メイン処理の呼び出し.<br>
   * <ul>
   * <li>引数をURLパラメータ形式からマップ形式に変換し、ログ開始処理を実行後、<code>doExecute</code>メソッドを呼び出します。</li>
   * <li>コマンドライン引数1つあたりの長さ制限に対応するため、複数の引数を配列として受け取る。</li>
   * <li>変換された引数は <code>IoItems</code>クラスとして <code>doExecute</code>メソッドに渡されます。</li>
   * <li><code>doExecute</code>メソッド内で Exception がスローされた場合は、処理が異常終了したとみなします。</li>
   * </ul>
   *
   * @param args 引数
   * @return 正常終了時は 0、異常終了時は 1
   */
  protected int callMain(final String[] args) {
    final IoItems argsMap = new IoItems();
    argsMap.putAllByBatParam(args);
    if (this.logger.isDevelopMode()) {
      this.logger.develop(LogUtil.joinKeyVal("arguments", argsMap));
    }

    int status = 0;
    try {
      this.logger.begin();
      doExecute(argsMap);
    } catch (final Exception | Error e) {
      status = 1;
      this.logger.error(e, "An exception error occurred in batch processing. ");
    }
    this.logger.end(status);

    // リソースのクローズ処理
    closeResources();

    return status;
  }

  /**
   * リソースのクローズ処理.<br>
   * <ul>
   * <li>ログテキストファイルのクローズを行います。</li>
   * </ul>
   */
  private final void closeResources() {
    try {
      // ログテキストファイルを閉じる
      LogTxtHandler.closeAll();
    } catch (final Exception | Error e) {
      LogUtil.stdout(e, "An exception error occurred in log text file close.");
    }
  }
}

