package com.onepg.util;

import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.LineSep;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * テキストライタークラス.<br>
 * <ul>
 * <li>テキストファイル専用のプリントライターラッパークラス。</li>
 * <li>文字セット、改行コード指定可能。</li>
 * <li><code>null</code>の書き込みは改行のみ出力される。</li>
 * </ul>
 */
public class TxtWriter implements AutoCloseable {

  /** プリントライター. */
  private final CustomPrintWriter pw;
  /** ファイルパス. */
  private final String filePath;
  /** 出力行数. */
  private long lineCount = 0;

  /**
   * コンストラクタ.
   *
   * @param filePath ファイルパス
   * @param lineSep 改行コード
   * @param charSet 文字セット
   */
  public TxtWriter(final String filePath, final LineSep lineSep, final CharSet charSet) {
    this(filePath, lineSep, charSet, false, false, false);
  }
  
  /**
   * コンストラクタ.
   *
   * @param filePath ファイルパス
   * @param lineSep 改行コード
   * @param charSet 文字セット
   * @param withBom BOM付きの場合は <code>true</code>
   */
  public TxtWriter(final String filePath, final LineSep lineSep, final CharSet charSet, final boolean withBom) {
    this(filePath, lineSep, charSet, withBom, false, false);
  }

  /**
   * コンストラクタ.
   *
   * @param filePath ファイルパス
   * @param lineSep 改行コード
   * @param charSet 文字セット
   * @param withBom BOM付きの場合は <code>true</code>
   * @param canAppend 追記を許可する場合は <code>true</code>
   * @param lineFlush 改行時フラッシュする場合は <code>true</code>
   */
  public TxtWriter(final String filePath, final LineSep lineSep, final CharSet charSet, final boolean withBom, 
    final boolean canAppend, final boolean lineFlush) {
    this.filePath = FileUtil.convAbsolutePath(filePath);

    // 追記無しで既存ファイルが有ればエラー
    if (!canAppend && FileUtil.exists(this.filePath)) {
      throw new RuntimeException("File already exists. " + LogUtil.joinKeyVal("path", this.filePath));
    }
    // 親ディレクトリが無ければエラー
    if (!FileUtil.existsParent(this.filePath)) {
      throw new RuntimeException("File creation target directory does not exist. " + LogUtil.joinKeyVal("path", this.filePath));
    }

    try {
      final FileOutputStream fos = new FileOutputStream(this.filePath, canAppend);
      if (withBom && CharSet.UTF8 == charSet) {
        // BOM
        fos.write(0xef);
        fos.write(0xbb);
        fos.write(0xbf);
      }
      final OutputStreamWriter os = new OutputStreamWriter(fos, charSet.toString());
      final BufferedWriter bw = new BufferedWriter(os);
      final CustomPrintWriter pw = new CustomPrintWriter(bw, lineFlush, lineSep);
      this.pw = pw;
    } catch (IOException e) {
      throw new RuntimeException("An exception error occurred while creating output stream. " 
          + LogUtil.joinKeyVal("path", this.filePath), e);
    }
  }

  /**
   * ファイルクローズ.
   */
  public void close() {
    this.pw.flush();
    this.pw.close();
  }

  /**
   * 行出力.
   *
   * @param line 行データ
   */
  public void println(final String line) {
    // nullの場合はブランクに置き換え（結果改行のみ出力される）
    this.pw.println(ValUtil.nvl(line));
    this.lineCount++;
  }

  /**
   * フラッシュ.
   */
  public void flush() {
    this.pw.flush();
  }

  /**
   * ファイルパス取得.
   *
   * @return ファイルパス
   */
  public String getFilePath() {
    return this.filePath;
  }

  /**
   * 出力行数.
   *
   * @return 出力行数
   */
  public long getLineCount() {
    return this.lineCount;
  }

  /**
   * 文字列化.
   *
   * @return ファイルパス
   */
  public String toString() {
    return LogUtil.joinKeyVal("path", this.filePath, "lineCount", String.valueOf(this.lineCount));
  }
}
