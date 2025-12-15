package com.onepg.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import com.onepg.util.ValUtil.CharSet;


/**
 * テキストリーダークラス.<br>
 * <ul>
 * <li>ファイルリーダー <code>BufferedReader</code> のラッパークラス。</li>
 * <li>try 句（try-with-resources文）で宣言する。</li>
 * </ul>
 * <pre>［例］
 * <code>try (final TxtReader tr = new TxtReader(filePath, ValUtil.UTF8);) {
 *   // ヘッダ行をスキップ
 *   tr.skip();
 *   for (final String line : tr) {
 *     ：省略
 *   }
 * }</code>
 * </pre>
 */
public final class TxtReader implements Iterable<String>, AutoCloseable {

  /** バッファリーダー */
  private final BufferedReader br;
  /** ファイルパス. */
  private final String filePath;
  /** 読み込んだ行 */
  private String nextLine = null;

  /** 読込済行数. */
  private int readedCount = 0;
  /** 最終行読込済判定. */
  private boolean readedEndRowFlag = false;
  /** 閉じた場合は <code>true</code> */
  private boolean isClosed = false;

  /**
   * コンストラクタ.
   *
   * @param filePath ファイルパス
   * @param charSet  文字セット
   */
  public TxtReader(final String filePath, final CharSet charSet) {
    this.filePath = FileUtil.convAbsolutePath(filePath);

    final File targetFile = new File(this.filePath);
    if (!targetFile.exists()) {
      throw new RuntimeException("File does not exist. " + LogUtil.joinKeyVal("path", this.filePath));
    }

    try {
      this.br = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile), charSet.toString()));
    } catch (UnsupportedEncodingException | FileNotFoundException e) {
      throw new RuntimeException("An exception error occurred while reading file. " + LogUtil.joinKeyVal("path", this.filePath), e);
    }
  }

  /**
   * イテレーター作成.
   *
   * @return 読み込み行イテレーター
   */
  @Override
  public Iterator<String> iterator() {
    return new TxtReadIterator();
  }

  /**
   * ファイルクローズ.
   */
  @Override
  public void close() {
    if (this.isClosed) {
      return;
    }
    this.isClosed = true;
    try {
      this.br.close();
    } catch (IOException e) {
      throw new RuntimeException("An exception error occurred while closing file. " + LogUtil.joinKeyVal("path", this.filePath), e);
    }
  }

  /**
   * 読込済行数取得.<br>
   * <ul>
   * <li>イテレーターで読み込んだ件数を返す。</li>
   * </ul>
   *
   * @return 読込済行数
   */
  public int getReadedCount() {
    return this.readedCount;
  }

  /**
   * 最終行読込済判定.
   *
   * @return 最終行読込済の場合は <code>true</code>
   */
  public boolean isReadedEndRow() {
    return this.readedEndRowFlag;
  }

  /**
   * 1行スキップ.
   * 
   * @see #skip(int)
   * @return 行数が不足していた場合は <code>false</code>
   */
  public boolean skip() {
    return skip(1);
  }

  /**
   * 行スキップ.<br>
   * <ul>
   * <li>ヘッダ行などをスキップする。</li>
   * <li>ファイル行よりスキップ行数が多くてもエラーとならず戻り値が <code>false</code> になる。</li>
   * <li>読み込み行数はカウントアップされない。</li>
   * </ul>
   *
   * @param count スキップ行数
   * @return 行数が不足していた場合は <code>false</code>
   */
  public boolean skip(final int count) {
    if (this.isClosed) {
      return false;
    }
    if (count <= 0) {
      return true;
    }
    
    try {
      for (int c = 1; c <= count; c++) {        
        // 1行読み飛ばし
        final String line = this.br.readLine();
        if (ValUtil.isNull(line)) {
          // 最終行読込済ON
          readedEndRowFlag = true;
          // ファイルを閉じる
          close();
          return false;
        }
      }
      return true;
    } catch (IOException e) {
      throw new RuntimeException("An exception error occurred while skipping rows. " + LogUtil.joinKeyVal("path", this.filePath) + LogUtil.joinKeyVal("skipCount", String.valueOf(count)), e);
    }
  }

  /**
   * 読み込み行イテレータークラス.
   */
  public final class TxtReadIterator implements Iterator<String> {

    /** 次行有フラグ. */
    private boolean hasNextRow = false;
    /** 次行確認済フラグ. */
    private boolean hasNextChecked = false;

    /**
     * コンストラクタ.
     */
    private TxtReadIterator() {
        super();
    }

    /**
     * 次行確認.<br>
     * <ul>
     * <li>try 句が使用されなかった場合に備えて次行が存在しなかった場合はファイルリーダーを閉じる。</li>
     * <li>連続した hasNext() 呼び出しでは再確認しない。</li>
     * </ul>
     *
     * @return 次行が存在する場合は <code>true</code>
     */
    @Override
    public boolean hasNext() {
      // 既に確認済みの場合は再確認しない
      if (hasNextChecked) {
        return this.hasNextRow;
      }

      // 次行存在確認
      try {
        nextLine = br.readLine();
        this.hasNextRow = !ValUtil.isNull(nextLine);
        this.hasNextChecked = true; // 確認完了フラグ
      } catch (IOException e) {
        throw new RuntimeException("An exception error occurred while checking for next row. " + LogUtil.joinKeyVal("readedCount", String.valueOf(readedCount)), e);
      }

      if (!this.hasNextRow) {
        // 最終行読込済ON
        readedEndRowFlag = true;
        // ファイルを閉じる
        close();
      }

      return this.hasNextRow;
    }

    /**
     * 次行取得.
     *
     * @return 行
     */
    @Override
    public String next() {
      if (!hasNext()) {
        throw new RuntimeException("Next row does not exist. " + LogUtil.joinKeyVal( "readedCount", String.valueOf(readedCount)));
      }

      final String line = nextLine;
      nextLine = null;
      // 読込済行数をカウントアップ
      readedCount++;

      // 再度確認が必要
      this.hasNextChecked = false;
      return line;
    }
  }
}
