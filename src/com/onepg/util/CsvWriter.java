package com.onepg.util;

import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.CsvType;
import com.onepg.util.ValUtil.LineSep;

/**
 * CSVライタークラス.<br>
 * <ul>
 * <li>TxtWriterをラッピングし、CSV出力に特化した機能を提供する。</li>
 * <li>CSV形式での出力を簡易化する。</li>
 * <li>文字セット、改行コード、CSV形式を指定可能。</li>
 * </ul>
 */
public class CsvWriter implements AutoCloseable {

  /** テキストライター. */
  private final TxtWriter txtWriter;
  /** CSV形式. */
  private final CsvType csvType;

  /**
   * コンストラクタ.
   *
   * @param filePath ファイルパス
   * @param lineSep 改行コード
   * @param charSet 文字セット
   * @param csvType CSV形式
   */
  public CsvWriter(final String filePath, final LineSep lineSep, final CharSet charSet, final CsvType csvType) {
    this.txtWriter = new TxtWriter(filePath, lineSep, charSet);
    this.csvType = csvType;
  }

  /**
   * ファイルクローズ.
   */
  public void close() {
    this.txtWriter.close();
  }

  /**
   * CSV行出力（文字列配列）.
   *
   * @param values 値配列
   */
  public void println(final String[] values) {
    this.txtWriter.println(ValUtil.joinCsv(values, this.csvType));
  }

  /**
   * CSV行出力（IoItems）.
   *
   * @param row 行データ
   */
  public void println(final IoItems row) {
    this.txtWriter.println(row.createCsv(this.csvType));
  }

  /**
   * フラッシュ.
   */
  public void flush() {
    this.txtWriter.flush();
  }

  /**
   * ファイルパス取得.
   *
   * @return ファイルパス
   */
  public String getFilePath() {
    return this.txtWriter.getFilePath();
  }

  /**
   * 出力行数取得.
   *
   * @return 出力行数
   */
  public long getLineCount() {
    return this.txtWriter.getLineCount();
  }

  /**
   * 文字列化.
   *
   * @return ファイルパスと出力行数
   */
  public String toString() {
    return this.txtWriter.toString();
  }
}
