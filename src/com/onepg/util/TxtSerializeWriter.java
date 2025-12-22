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
 * <li>最後の書き込みが同時の場合、キャッシュに出力内容が残る場合があるため必ず <code>#close()</code> を呼び出すこと。（<code>#close()</code> 内で <code>#flush()</code> される）</li>
 * </ul>
 * @hidden
 */
final class TxtSerializeWriter extends TxtWriter {

  /** 行キャッシュ. */
  private final ConcurrentLinkedQueue<String> lineCache = new ConcurrentLinkedQueue<>();
  /** 出力中フラグ. */
  private AtomicBoolean isPrinting = new AtomicBoolean(false);

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
   * キャッシュ出力（直列）.
   *
   * @param fullFlush キャッシュしているデータをすべて出力する場合は <code>true</code> （プログラム終了時に使用する前提）
   */
  private synchronized void cachePrint(final boolean fullFlush) {
    if (fullFlush) {
      String cache = null;
      while ((cache = this.lineCache.poll()) != null) {
        super.println(cache);
      }
      return;
    }

    // 現時点でキャッシュされている分だけ出力する。
    // 長時間処理をさけるため処理中に追加されても終わらせる。
    final int cacheSize = lineCache.size();
    for (int i = 0; i < cacheSize; i++) {
      final String cache = this.lineCache.poll();
      if (cache == null) {
        break;
      }
      super.println(cache);
    }
  }

  @Override
  public void println(final String line) {
    // いったんキャッシュに格納してから出力する
    // TxtWriter#println でも null は置換しているが、キャッシュに null を入れるのは好ましくないためここでも置換する
    this.lineCache.offer(ValUtil.nvl(line));
    if (this.isPrinting.compareAndSet(false, true)) {
      try {
        // フラグが出力中でない場合は出力中に切り替え、キャッシュ出力する
        cachePrint(false);
      }finally {
        // 例外発生時でもフラグを戻す
        this.isPrinting.set(false);
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
