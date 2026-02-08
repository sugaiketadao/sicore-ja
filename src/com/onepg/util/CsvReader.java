package com.onepg.util;

import java.util.Iterator;

import com.onepg.util.ValUtil.CsvType;
import com.onepg.util.ValUtil.CharSet;


/**
 * CSVリーダークラス.<br>
 * <ul>
 * <li>テキストリーダー <code>TxtReader</code> のラッパークラス。</li>
 * <li>try 句（try-with-resources文）で宣言する。</li>
 * <li>CSV の各行を <code>IoItems</code> として返すイテレーターを提供する。</li>
 * <li>ファイルの1行目を列名とし <code>IoItems</code> のキーに使用する。</li>
 * <li>ファイルの1行目に列名が無い場合は <code>IoItems</code> のキーをコンストラクタに渡す必要がある。</li>
 * <li>引数のキー配列でキーがブランクの項目は <code>IoItems</code> に格納されない。（読み飛ばしたい列にはブランクキーを指定する）</li>
 * <li>CSV項目数がキー数より多い場合、余剰分の項目は格納されない。</li>
 * <li>キー数がCSV項目数より多い場合、そのキーの値は常にブランクとなります。</li>
 * <li>CSVタイプがダブルクォーテーション付の場合、値内の２つ連続したダブルクォーテーション（""）は１つのダブルクォーテーション（"）に変換される。</li>
 * <li>CSVタイプが改行有りの場合、かつ値（ダブルクォーテーション間）に改行コードを含む場合、その改行コード（CRLF・CR）は LF に統一される。</li>
 * <li>CSVタイプが改行無し（改行有り以外）の場合、値（ダブルクォーテーション間）に改行コードを含むと、改行箇所が列区切りとして誤認識され列数不足のエラー状態となる。</li>
 * </ul>
 * <pre>［例］
 * <code>try (final CsvReader cr = new CsvReader(filePath, ValUtil.UTF8, CsvType.DQ_ALL);) {
 *   for (final IoItems items : cr) {
 *     ：省略
 *   }
 * }</code>
 * </pre>
 */
public final class CsvReader implements Iterable<IoItems>, AutoCloseable {

  /** テキストリーダー */
  private final TxtReader txtReader;
  /** キー配列 */
  private final String[] keys;
  /** CSVタイプ */
  private final CsvType csvType;

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>ファイルの1行目を列名とし <code>IoItems</code> のキーに使用する。</li>
   * </ul>
   *
   * @param filePath ファイルパス
   * @param charSet  文字セット
   * @param csvType CSVタイプ
   */
  public CsvReader(final String filePath, final CharSet charSet, final CsvType csvType) {
    this.txtReader = new TxtReader(filePath, charSet);
    this.csvType = csvType;
    // ファイルの1行目をキーとして取得
    final String firstLine = this.txtReader.getFirstLine();
    if (ValUtil.isNull(firstLine)) {
      this.keys = new String[0];
      return;
    }
    this.keys = ValUtil.splitCsv(firstLine, csvType);
  }

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>引数のキー配列を使用し、ファイルの1行目からCSVデータとして読み込む。</li>
   * </ul>
   *
   * @param filePath ファイルパス
   * @param charSet  文字セット
   * @param keys キー配列
   * @param csvType CSVタイプ
   */
  public CsvReader(final String filePath, final CharSet charSet, final String[] keys, final CsvType csvType) {
    this.txtReader = new TxtReader(filePath, charSet);
    this.csvType = csvType;
    if (ValUtil.isEmpty(keys)) {
      throw new RuntimeException("Keys array is empty or null. " + LogUtil.joinKeyVal("path", filePath));
    }
    this.keys = keys;
  }

  /**
   * イテレーター作成.
   *
   * @return CSV行イテレーター
   */
  @Override
  public Iterator<IoItems> iterator() {
    return new CsvReadIterator();
  }

  /**
   * ファイルクローズ.
   */
  @Override
  public void close() {
    this.txtReader.close();
  }

  /**
   * 読込済行数取得.<br>
   * <ul>
   * <li>イテレーターで読み込んだ件数を返す。</li>
   * <li>ファイルの1行目をキーとして使用した場合、ヘッダ行はカウントされない。</li>
   * </ul>
   *
   * @return 読込済行数
   */
  public int getReadedCount() {
    return this.txtReader.getReadedCount();
  }

  /**
   * 最終行読込済判定.
   *
   * @return 最終行読込済の場合は <code>true</code>
   */
  public boolean isReadedEndRow() {
    return this.txtReader.isReadedEndRow();
  }

  /**
   * CSV読み込み行イテレータークラス.
   */
  public final class CsvReadIterator implements Iterator<IoItems> {

    /** TxtReader のイテレーター */
    private final Iterator<String> txtIterator;

    /**
     * コンストラクタ.
     */
    private CsvReadIterator() {
      super();
      this.txtIterator = txtReader.iterator();
    }

    /**
     * 次行確認.
     *
     * @return 次行が存在する場合は <code>true</code>
     */
    @Override
    public boolean hasNext() {
      if (ValUtil.isEmpty(keys)) {
        // キー配列が無い場合は次行無しとする（txtIterator.hasNext()もfalseになるはずだが念のため）
        return false;
      }
      return this.txtIterator.hasNext();
    }

    /**
     * 次行取得.<br>
     * <ul>
     * <li><code>IoItems</code> に CSV行を格納して返す。</li>
     * </ul>
     *
     * @return CSV行の <code>IoItems</code>
     */
    @Override
    public IoItems next() {
      final String line = this.txtIterator.next();
      
      if (csvType == CsvType.NO_DQ) {
        // ダブルクォーテーション無し
        final IoItems items = new IoItems();
        items.putAllByCsvNoDq(keys, line);
        return items;
      }
      
      if (csvType == CsvType.DQ_ALL || csvType == CsvType.DQ_STD) {
        // ダブルクォーテーション付き改行無し
        final IoItems items = new IoItems();
        items.putAllByCsvDq(keys, line);
        return items;
      }

      // ダブルクォーテーション付き改行有り
      String mergedLine = line;
      CsvDqParser dqParser = new CsvDqParser(mergedLine);
      while (dqParser.isUnclosedDq() && this.txtIterator.hasNext()) {
        // ダブルクォーテーション閉じてない場合は次行を連結する
        final String nextLine = this.txtIterator.next();
        mergedLine = mergedLine + ValUtil.LF + nextLine;
        dqParser = new CsvDqParser(mergedLine);
      }
      final IoItems items = new IoItems();
      items.putAllByCsvDq(keys, mergedLine, dqParser);
      return items;
    }
  }
}
