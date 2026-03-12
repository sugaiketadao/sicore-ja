package com.onepg.util;

import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.LineSep;

/**
 * データ入出力用TSVライタークラス.<br>
 * <ul>
 * <li>TxtWriterをラッピングし、データ入出力用TSV出力に特化した機能を提供する。</li>
 * <li>TSV形式での出力を簡易化する。</li>
 * <li>ファイルの1行目を列名を出力する。</li>
 * <li>ファイルの文字コードは UTF-8、改行コードは LF 固定とする。</li>
 * <li><code>null</code> はエスケープする。</li>
 * <li>値内にある改行コード（CRLF・CR・LF）とタブ文字はエスケープする。</li>
 * </ul>
 */
public final class IoTsvWriter implements AutoCloseable {

  /** テキストライター. */
  private final TxtWriter txtWriter;

  /**
   * コンストラクタ.
   *
   * @param filePath ファイルパス
   */
  public IoTsvWriter(final String filePath) {
    this.txtWriter = new TxtWriter(filePath, LineSep.LF, CharSet.UTF8);
  }

  /**
   * ファイルクローズ.
   */
  public void close() {
    this.txtWriter.close();
  }

  /**
   * TSV行出力（文字列配列）.
   *
   * @param values 値配列
   */
  public void println(final String[] values) {
    this.txtWriter.println(ValUtil.joinIoTsv(values));
  }

  /**
   * TSV行出力（IoItems）.
   *
   * @param row 行データ
   */
  public void println(final IoItems row) {
    this.txtWriter.println(row.createIoTsv());
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
