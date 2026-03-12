package com.onepg.util;

import java.util.Iterator;

import com.onepg.util.ValUtil.CharSet;


/**
 * データ入出力用TSVリーダークラス.<br>
 * <ul>
 * <li>テキストリーダー <code>TxtReader</code> のラッパークラス。</li>
 * <li>try 句（try-with-resources文）で宣言する。</li>
 * <li>TSV の各行を <code>IoItems</code> として返すイテレーターを提供する。</li>
 * <li>ファイルの1行目を列名とし <code>IoItems</code> のキーに使用する。</li>
 * <li>キー名は <code>IoItems</code> オブジェクトキーとして有効な値である必要があります。（<code>AbstractIoTypeMap</code> のキールール）</li>
 * <li>ファイルの文字コードは UTF-8、改行コードは LF の前提とする。</li>
 * <li>値内にある改行コード（CRLF・CR・LF）とタブ文字はエスケープされている前提とする。</li>
 * </ul>
 * <pre>［例］
 * <code>try (final IoTsvReader tr = new IoTsvReader(filePath);) {
 *   for (final IoItems items : tr) {
 *     ：省略
 *   }
 * }</code>
 * </pre>
 */
public final class IoTsvReader implements Iterable<IoItems>, AutoCloseable {

  /** テキストリーダー */
  private final TxtReader txtReader;
  /** キー配列 */
  private final String[] keys;
  /** 読込済行数（ヘッダ行を除く）. */
  private int readedCount = 0;

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>ファイルの１行目をヘッダ行とし <code>IoItems</code> のキーに使用する。</li>
   * </ul>
   *
   * @param filePath ファイルパス
   */
  public IoTsvReader(final String filePath) {
    this.txtReader = new TxtReader(filePath, CharSet.UTF8);
    // ファイルの1行目をキーとして取得
    final String firstLine = this.txtReader.getFirstLine();
    if (ValUtil.isNull(firstLine)) {
      this.keys = new String[0];
      return;
    }
    this.keys = ValUtil.split(firstLine, ValUtil.TAB);
  }

  /**
   * イテレーター作成.
   *
   * @return TSV行イテレーター
   */
  @Override
  public Iterator<IoItems> iterator() {
    return new TsvReadIterator();
  }

  /**
   * ファイルクローズ.
   */
  @Override
  public void close() {
    this.txtReader.close();
  }

  /**
   * キー配列取得. <br>
   * <ul>
   * <li>ファイルの1行目から取得したキー配列を返す。</li>
   * <li>ファイルがゼロ行の場合は長さゼロの配列を返す。</li>
   * </ul>
   */
  public String[] getKeys() {
    return this.keys;
  }

  /**
   * 読込済行数（ヘッダ行を除く）取得.<br>
   * <ul>
   * <li>イテレーターで読み込んだ件数を返す。</li>
   * <li>ファイルの１行目をキー（ヘッダ行）として使用した場合、１行目はカウントされない。</li>
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
    return this.txtReader.isReadedEndRow();
  }

  /**
   * TSV読み込み行イテレータークラス.
   */
  public final class TsvReadIterator implements Iterator<IoItems> {

    /** TxtReader のイテレーター */
    private final Iterator<String> txtIterator;

    /**
     * コンストラクタ.
     */
    private TsvReadIterator() {
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
     * <li><code>IoItems</code> に TSV行を格納して返す。</li>
     * </ul>
     *
     * @return TSV行の <code>IoItems</code>
     */
    @Override
    public IoItems next() {
      final String line = this.txtIterator.next();
      // 読込済行数をカウントアップ
      readedCount++;

      final IoItems items = new IoItems();
      items.putAllByIoTsv(keys, line);
      return items;
    }
  }
}
