package com.onepg.util;

/**
 * ブレーク例外クラス.<br>
 * <ul>
 * <li>処理を中止するために使用する例外クラスです。</li>
 * <li>本クラスをスローするとスタックトレースの出力が中断されます。（<code>LogUtil.getStackTrace()</code> メソッドで考慮されています。）</li>
 * <li>既にログ出力しており、エラーの伝播先で再度トレースログを出力されたくない場合に使用します。</li>
 * </ul>
 */
public final class BreakException extends RuntimeException {

  /**
   * コンストラクタ.
   */
  public BreakException() {
    super();
  }

}
