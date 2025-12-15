package com.onepg.util;

import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.LineSep;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * テキストライター直列化ラッパークラス.<br>
 * <ul>
 * <li>並列スレッドから出力されても出力を直列化する。</li>
 * <li>内部で出力データのキャッシュも行う。</li>
 * </ul>
 * @hidden
 */
final class TxtSerializeWriter extends TxtWriter {

  /** 行キャッシュ. */
  private final ConcurrentLinkedQueue<String> lineCache = new ConcurrentLinkedQueue<>();
  /** 出力中フラグ. */
  private AtomicBoolean printNow = new AtomicBoolean(false);

  /**
   * コンストラクタ.
   *
   * @param filePath ファイルパス
   * @param canAppend 追記を許可する場合は <code>true</code>
   * @param lineFlush 改行時フラッシュする場合は <code>true</code>
   * @param lineSep 改行コード
   * @param charSet 文字セット
   * @param withBom BOM付きの場合は <code>true</code>
   */
  TxtSerializeWriter(final String filePath, final boolean canAppend, final boolean lineFlush,
      final LineSep lineSep, final CharSet charSet, final boolean withBom) {
    super(filePath, canAppend, lineFlush, lineSep, charSet, withBom);
  }

  /**
   * 行出力（直列）.
   */
  private synchronized void linePrint(final String line) {
    super.println(line);
  }

  /**
   * キャッシュ出力（直列）.
   *
   * @param fullFlush キャッシュしているデータをすべて出力する場合は <code>true</code> （プログラム終了時に使用する前提）
   */
  private synchronized void cachePrint(final boolean fullFlush) {
    final int olsSize = lineCache.size();
    if (olsSize <= 0) {
      return;
    }
    final int escSize;
    if (fullFlush) {
      escSize = Integer.MAX_VALUE;
    } else {
      escSize = olsSize;
    }

    int count = 0;
    String cache = null;
    while ((cache = this.lineCache.poll()) != null) {
      // キャッシュを取り出し出力する
      linePrint(cache);
      count++;
      if (escSize <= count) {
        // 処理中に追加されてもいったん終わらせる（長時間処理をさけるため）
        return;
      }
    }
  }

  @Override
  public void println(String line) {
    if (ValUtil.isNull(line)) {
      return;
    }

    // いったんキャッシュに格納してから出力する
    this.lineCache.offer(line);
    if (this.printNow.compareAndSet(false, true)) {
      try {
        // フラグが出力中でない場合は出力中に切り替え、キャッシュ出力する
        cachePrint(false);
      } catch (Exception e) {
        throw e; 
      }finally {
        // 例外発生時でもフラグを戻す
        this.printNow.set(false);
      }
    }
  }

  @Override
  public void flush() {
    cachePrint(true);
    super.flush();
  }

  @Override
  public void close() {
    flush();
    super.close();
  }
}
