package com.onepg.util;

import com.onepg.util.ValUtil.LineSep;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * プリントライターラッパークラス.<br>
 * <ul>
 * <li>OS依存ではなく改行コードが指定可能。</li>
 * <li>自動フラッシュ機能により、改行時の即座出力制御が可能。</li>
 * <li><code>null</code> 安全性を提供し、不正な引数を事前にチェック。</li>
 * </ul>
 * @hidden
 */
public final class CustomPrintWriter extends PrintWriter {

  /** 改行コード. */
  private final String lineSep;
  /** 自動（おもに改行時）フラッシュフラグ. */
  private final boolean autoFlush;

  /**
   * コンストラクタ.
   *
   * @param out ライター
   * @param autoFlush 自動（おもに改行時）フラッシュする場合は <code>true</code>
   * @param lineSep 改行コード
   */
  public CustomPrintWriter(final Writer out, final boolean autoFlush, final LineSep lineSep) {
    super(out, false);
    this.autoFlush = autoFlush;
    this.lineSep = lineSep.toString();
  }

  /**
   * コンストラクタ.
   *
   * @param out アウトプットストリーム
   * @param autoFlush 改行時フラッシュ
   * @param lineSep 改行コード
   */
  public CustomPrintWriter(final OutputStream out, final boolean autoFlush, final LineSep lineSep) {
    super(out, false);
    this.autoFlush = autoFlush;
    this.lineSep = lineSep.toString();
  }

  /**
   * 改行書出し.
   *
   * @param value 出力する値
   */
  private void writeValueAndLineSep(final String value) {
    super.write(value);
    super.write(this.lineSep);
    flushIfNeeded();
  }

  /**
   * 書出し.
   *
   * @param line 出力データ
   */
  private void writeAndFlush(final String line) {
    super.write(line);
    flushIfNeeded();
  }

  /**
   * フラッシュ実行.
   */
  private void flushIfNeeded() {
    if (this.autoFlush) {
      super.flush();
    }
  }

  /**
   * 改行書出し.
   */
  @Override
  public void println() {
    writeAndFlush(this.lineSep);
  }

  /**
   * 改行書出し.
   * @param x 出力データ
   */
  @Override
  public void println(boolean x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * 改行書出し.
   * @param x 出力データ
   */
  @Override
  public void println(char x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * 改行書出し.
   * @param x 出力データ
   */
  @Override
  public void println(int x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * 改行書出し.
   * @param x 出力データ
   */
  @Override
  public void println(long x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * 改行書出し.
   * @param x 出力データ
   */
  @Override
  public void println(float x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * 改行書出し.
   * @param x 出力データ
   */
  @Override
  public void println(double x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * 改行書出し.
   * @param x 出力データ
   */
  @Override
  public void println(char[] x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * 改行書出し.
   * @param x 出力データ
   */
  @Override
  public void println(String x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * 改行書出し.
   * @param x 出力データ
   */
  @Override
  public void println(Object x) {
    writeValueAndLineSep(String.valueOf(x));
  }

}
