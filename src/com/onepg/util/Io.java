package com.onepg.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 入出力マップクラス.<br>
 * <ul>
 * <li>文字列リストを保持することができる。</li>
 * <li>ネストされた可変型マップを保持することができる。</li>
 * <li>複数行の可変型マップをリストで保持することができる。</li>
 * <li>文字列配列（リスト）をリストで保持することができる。</li>
 * <li>上記のデータ構造は格納時・取得時ともにディープコピーされる。</li>
 * <li>JSON を入出力することができる。</li>
 * <li>URLパラメーター を入出力することができる。</li>
 * <li>基本ルール・制限は <code>AbstractIoTypeMap</code> に準拠する。</li>
 * 
 * <li>文字列リストについて
 * <ul>
 * <li><code>#getList(String)</code>、<code>#putList(String, List)</code>、<code>#containsKeyList(String)</code>、
 * <code>#removeList(String)</code> などの <code>**List</code>メソッドから扱う。</li>
 * <li>マップクラスが持つ <code>#size()</code> や <code>#containsKey(Object)</code>、
 * <code>#keySet()</code>
 * などの結果に文字列リストは含まれない。<br>
 * ただし <code>#clear()</code> では文字列リストの保持もクリアされる。</li>
 * <li><code>#getList(String)</code> から取得したリストに要素を追加しても本クラスで保持しているリストに影響しない。</li>
 * <li><code>#putList(String, List)</code>
 * で格納した元のリストに要素を追加しても本クラスで保持しているリストに影響しない。</li>
 * <li>文字列リストは CSV 出力の対象にはならない。</li>
 * <li>文字列リストを含む URLパラメーター は文字列長さに注意する。</li>
 * <li>ディープコピーを行うため、リストの格納・取得を繰り返す処理や、サイズの大きいリストを扱う処理では性能が低下する可能性がある。</li>
 * </ul>
 * </li>
 * 
 * <li>ネストマップについて
 * <ul>
 * <li><code>#getNest(String)</code>、<code>#putNest(String, Map)</code>、<code>#containsKeyNest(String)</code>、
 * <code>#removeNest(String)</code> などの <code>**Nest</code>メソッドから扱う。</li>
 * <li>マップクラスが持つ <code>#size()</code> や <code>#containsKey(Object)</code>、
 * <code>#keySet()</code>
 * などの結果にネストマップは含まれない。<br>
 * ただし <code>#clear()</code> ではネストマップの保持もクリアされる。</li>
 * <li><code>#getNest(String)</code> から取得したマップに要素を追加しても本クラスで保持しているマップに影響しない。</li>
 * <li><code>#putNest(String, List)</code>
 * で格納した元のマップに要素を追加しても本クラスで保持しているマップに影響しない。</li>
 * <li>ネストマップは CSV 出力、URLパラメーター出力の対象にはならない。</li>
 * <li>ディープコピーを行うため、リストの格納・取得を繰り返す処理や、サイズの大きいリストを扱う処理では性能が低下する可能性がある。</li>
 * </ul>
 * </li>
 * 
 * <li>複数行リストについて
 * <ul>
 * <li><code>#getRows(String)</code>、<code>#putRows(String, Collection)</code>、
 * <code>#containsKeyRows(String)</code>、<code>#removeRows(List)</code> などの <code>**Rows</code>メソッドから扱う。</li>
 * <li>マップクラスが持つ <code>#size()</code> や <code>#containsKey(Object)</code>、
 * <code>#keySet()</code>
 * などの結果に複数行リストは含まれない。<br>
 * ただし <code>#clear()</code> では複数行リストの保持もクリアされる。</li>
 * <li><code>#getRows(String)</code> から取得したリストに要素を追加しても本クラスで保持しているリストに影響しない。</li>
 * <li><code>#putRows(String, Collection)</code>
 * で格納した元のリストに要素を追加しても本クラスで保持しているリストに影響しない。</li>
 * <li>複数行リストは CSV 出力、URLパラメーター出力の対象にはならない。</li>
 * <li>ディープコピーを行うため、リストの格納・取得を繰り返す処理や、サイズの大きいリストを扱う処理では性能が低下する可能性がある。</li>
 * </ul>
 * </li>
 * 
 * <li>配列リストについて
 * <ul>
 * <li><code>#getArys(String)</code>、<code>#putArys(String, Collection)</code>、
 * <code>#containsKeyArys(String)</code>、<code>#removeArys(List)</code> などの <code>**Arys</code>メソッドから扱う。</li>
 * <li>マップクラスが持つ <code>#size()</code> や <code>#containsKey(Object)</code>、
 * <code>#keySet()</code>
 * などの結果に配列リストは含まれない。<br>
 * ただし <code>#clear()</code> では配列リストの保持もクリアされる。</li>
 * <li><code>#getArys(String)</code> から取得したリストに要素を追加しても本クラスで保持しているリストに影響しない。</li>
 * <li><code>#putArys(String, Collection)</code>
 * で格納した元のリストに要素を追加しても本クラスで保持しているリストに影響しない。</li>
 * <li>配列リストは CSV 出力、URLパラメーター出力の対象にはならない。</li>
 * <li>ディープコピーを行うため、リストの格納・取得を繰り返す処理や、サイズの大きいリストを扱う処理では性能が低下する可能性がある。</li>
 * </ul>
 * </li>
 * 
 * <li>JSON で４階層（次元）以上の配列には対応しない。
 * <ul>
 * <li>{ [ [ ] ] } は対応。→配列リスト</li>
 * <li>{ [ { } ] } は対応。→複数行リスト</li>
 * <li>{ [ [ [ ] ] ] } は非対応。</li>
 * <li>{ [ [ { } ] ] } は非対応。</li>
 * </ul>
 * </li>
 * 
 * <li>メッセージについて
 * <ul>
 * <li>メッセージIDと対象項目名等を保持することができ、メッセージは JSON 出力時に合わせて出力される。</li>
 * <li>JSON 出力時にメッセージテキストを引数渡しすることで保持しているメッセージ文言も合わせて出力される。</li>
 * <li>メッセージテキスト内の <code>{0}, {1}, ...</code> はメッセージ追加時の引数で渡された文字列で置換される。</li>
 * </ul>
 * </li>
 * 
 * </ul>
 *
 * @see AbstractIoTypeMap
 */
public final class Io extends AbstractIoTypeMap {

  /** 文字列リスト保持マップ. */
  private final Map<String, List<String>> listMap = new LinkedHashMap<>();
  /** ネストマップ保持マップ. */
  private final Map<String, Io> nestMap = new LinkedHashMap<>();
  /** 複数行リスト保持マップ. */
  private final Map<String, IoRows> rowsMap = new LinkedHashMap<>();
  /** 配列リスト保持マップ. */
  private final Map<String, IoArrays> arysMap = new LinkedHashMap<>();

  /** 保持タイプ. */
  private enum StorageType {
    /** 文字列リスト保持. */
    LIST,
    /** ネストマップ保持. */
    NEST,
    /** 複数行リスト保持. */
    ROWS,
    /** 配列リスト保持. */
    ARYS;
  }

  /**
   * コンストラクタ.
   */
  public Io() {
    super();
  }

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>内容をディープコピーするため、ソースマップとの参照は切れる。</li>
   * </ul>
   *
   * @param srcMap ソースマップ
   */
  public Io(final Map<? extends String, ? extends String> srcMap) {
    super(srcMap);
  }

  /**
   * コンストラクタ.<br>
   * <ul>
   * <li>内容をディープコピーするため、ソースマップとの参照は切れる。</li>
   * <li>メッセージもコピーされる。</li>
   * </ul>
   *
   * @param srcMap ソースマップ
   */
  public Io(final Io srcMap) {
    super();

    if (ValUtil.isNull(srcMap)) {
      throw new RuntimeException("Source map is required. ");
    }

    // ディープコピーするため、ソースマップとの参照は切れる。
    putAllByIoMap(srcMap, false);
  }

  /**
   * 取得時キーバリデート（保持タイプ別）.
   *
   * @param type 保持タイプ
   * @param key キー
   */
  private void validateKeyByTypeForGet(final StorageType type, final String key) {

    if (StorageType.LIST == type) {
      // 文字列リスト保持マップキー存在チェック
      if (!this.listMap.containsKey(key)) {
        throw new RuntimeException("Key does not exist in string list. "
                                + LogUtil.joinKeyVal("key", key));
      }
    }

    if (StorageType.NEST == type) {
      // ネストマップ保持マップキー存在チェック
      if (!this.nestMap.containsKey(key)) {
        throw new RuntimeException("Key does not exist in nested map. "
                                + LogUtil.joinKeyVal("key", key));
      }
    }

    if (StorageType.ROWS == type) {
      // 複数行リスト保持マップキー存在チェック
      if (!this.rowsMap.containsKey(key)) {
        throw new RuntimeException("Key does not exist in multiple rows list. "
                                + LogUtil.joinKeyVal("key", key));
      }
    }

    if (StorageType.ARYS == type) {
      // 配列リスト保持マップキー存在チェック
      if (!this.arysMap.containsKey(key)) {
        throw new RuntimeException("Key does not exist in array list. "
                                + LogUtil.joinKeyVal("key", key));
      }
    }
  }

  /**
   * 格納時キーバリデート（保持タイプ別）.
   *
   * @param type 保持タイプ
   * @param key キー
   * @param canOverwrite 上書き許可
   */
  private void validateKeyByTypeForPut(final StorageType type, final String key,
      final boolean canOverwrite) {

    // キー妥当性チェック
    validateKey(key);

    // 値保持マップキー非存在チェック
    if (super.getValMap().containsKey(key)) {
      throw new RuntimeException("Key is for value storage map. " + LogUtil.joinKeyVal("key", key));
    }

    if ((StorageType.LIST == type && !canOverwrite) || StorageType.LIST != type) {
      // 文字列リスト保持マップキー非存在チェック
      if (this.listMap.containsKey(key)) {
        throw new RuntimeException("Already a string list key (cannot overwrite). " + LogUtil.joinKeyVal("key", key));
      }
    }

    if ((StorageType.NEST == type && !canOverwrite) || StorageType.NEST != type) {
      // ネストマップ保持マップキー非存在チェック
      if (this.nestMap.containsKey(key)) {
        throw new RuntimeException("Already a nested map key (cannot overwrite). " + LogUtil.joinKeyVal("key", key));
      }
    }

    if ((StorageType.ROWS == type && !canOverwrite) || StorageType.ROWS != type) {
      // 複数行リスト保持マップキー非存在チェック
      if (this.rowsMap.containsKey(key)) {
        throw new RuntimeException("Already a multiple rows list key (cannot overwrite). " + LogUtil.joinKeyVal("key", key));
      }
    }

    if ((StorageType.ARYS == type && !canOverwrite) || StorageType.ARYS != type) {
      // 配列リスト保持マップキー非存在チェック
      if (this.arysMap.containsKey(key)) {
        throw new RuntimeException("Already an array list key (cannot overwrite). " + LogUtil.joinKeyVal("key", key));
      }
    }
  }

  /**
   * キーバリデート＆文字列リストコピー取得.<br>
   * <ul>
   * <li><code>ArrayList</code> のコンストラクタはシャロ―コピーとなるが内容がイミュータブルオブジェクト（<code>String</code>）
   * のため、実質ディープコピーとなり格納リストとの参照は切れる。</li>
   * </ul>
   *
   * @param key キー
   * @return 文字列リスト（<code>null</code> 有り）
   */
  private List<String> getCopyList(final String key) {
    // キーバリデート
    validateKeyByTypeForGet(StorageType.LIST, key);

    final List<String> list = this.listMap.get(key);
    if (ValUtil.isNull(list)) {
      return null;
    }
    // コピーを返す
    final List<String> copyList = new ArrayList<>(list);
    return copyList;
  }

  /**
   * キーバリデート＆ネストマップコピー取得.<br>
   * <ul>
   * <li><code>Io</code> のコンストラクタは内容をディープコピーするため、格納マップとの参照は切れる。</li>
   * </ul>
   *
   * @param key キー
   * @return ネストマップ（<code>null</code> 有り）
   */
  private Io getCopyNest(final String key) {
    // キーバリデート
    validateKeyByTypeForGet(StorageType.NEST, key);

    final Io nest = this.nestMap.get(key);
    if (ValUtil.isNull(nest)) {
      return null;
    }
    // コピーを返す
    final Io copyNest = new Io(nest);
    return copyNest;
  }

  /**
   * キーバリデート＆複数行リストコピー取得.<br>
   * <ul>
   * <li><code>IoRows</code> のコンストラクタは内容をディープコピーするため、格納リストとの参照は切れる。</li>
   * </ul>
   *
   * @param key キー
   * @return 複数行リスト（<code>null</code> 有り）
   */
  private IoRows getCopyRows(final String key) {
    // キーバリデート
    validateKeyByTypeForGet(StorageType.ROWS, key);

    final IoRows rows = this.rowsMap.get(key);
    if (ValUtil.isNull(rows)) {
      return null;
    }
    // コピーを返す
    final IoRows copyRows = new IoRows(rows);
    return copyRows;
  }

  /**
   * キーバリデート＆配列リストコピー取得.<br>
   * <ul>
   * <li><code>IoArrays</code> のコンストラクタは内容をディープコピーするため、格納リストとの参照は切れる。</li>
   * </ul>
   *
   * @param key キー
   * @return 配列リスト（<code>null</code> 有り）
   */
  private IoArrays getCopyArys(final String key) {
    // キーバリデート
    validateKeyByTypeForGet(StorageType.ARYS, key);

    final IoArrays arys = this.arysMap.get(key);
    if (ValUtil.isNull(arys)) {
      return null;
    }
    // コピーを返す
    final IoArrays copyArys = new IoArrays(arys);
    return copyArys;
  }

  /**
   * キーバリデート＆文字列リストコピー格納.<br>
   * <ul>
   * <li><code>ArrayList</code> のコンストラクタはシャロ―コピーとなるが内容がイミュータブルオブジェクト（<code>String</code>）
   * のため、実質ディープコピーとなりソースリストとの参照は切れる。</li>
   * </ul>
   *
   * @param key キー
   * @param srcList ソースリスト
   * @param canOverwrite 上書き許可
   * @return 前回の格納文字列リスト
   */
  private List<String> putCopyList(final String key, final List<String> srcList,
      final boolean canOverwrite) {
    // キーバリデート
    validateKeyByTypeForPut(StorageType.LIST, key, canOverwrite);
    // 全キー格納
    super.allKeySet().add(key);

    if (ValUtil.isNull(srcList)) {
      return this.listMap.put(key, null);
    }
    // コピーを格納
    final List<String> copyList = new ArrayList<>(srcList);
    return this.listMap.put(key, copyList);
  }

  /**
   * キーバリデート＆ネストマップコピー格納.<br>
   * <ul>
   * <li><code>Io</code> のコンストラクタは内容をディープコピーするため、ソースマップとの参照は切れる。</li>
   * </ul>
   *
   * @param key キー
   * @param srcMap ソースマップ
   * @param canOverwrite 上書き許可
   * @return 前回の格納ネストマップ
   */
  private Io putCopyNest(final String key, final Io srcMap, final boolean canOverwrite) {
    // キーバリデート
    validateKeyByTypeForPut(StorageType.NEST, key, canOverwrite);
    // 全キー格納
    super.allKeySet().add(key);

    if (ValUtil.isNull(srcMap)) {
      return this.nestMap.put(key, null);
    }
    // コピーを格納
    final Io copyNest = new Io(srcMap);
    return this.nestMap.put(key, copyNest);
  }

  /**
   * キーバリデート＆複数行リストコピー格納.<br>
   * <ul>
   * <li><code>IoRows</code> のコンストラクタは内容をディープコピーするため、ソースリストとの参照は切れる。</li>
   * </ul>
   *
   * @param key キー
   * @param srcList ソースリスト
   * @param canOverwrite 上書き許可
   * @return 前回の格納複数行リスト
   */
  private IoRows putCopyRows(final String key,
      final Collection<? extends Map<? extends String, ? extends String>> srcList,
      final boolean canOverwrite) {
    // キーバリデート
    validateKeyByTypeForPut(StorageType.ROWS, key, canOverwrite);
    // 全キー格納
    super.allKeySet().add(key);

    if (ValUtil.isNull(srcList)) {
      return this.rowsMap.put(key, null);
    }
    // コピーを格納
    final IoRows copyRows = new IoRows(srcList);
    return this.rowsMap.put(key, copyRows);
  }

  /**
   * キーバリデート＆配列リストコピー格納.<br>
   * <ul>
   * <li><code>IoArrays</code> のコンストラクタは内容をディープコピーするため、ソースリストとの参照は切れる。</li>
   * </ul>
   *
   * @param key キー
   * @param srcList ソースリスト
   * @param canOverwrite 上書き許可
   * @return 前回の格納配列リスト
   */
  private IoArrays putCopyArys(final String key,
      final Collection<? extends Collection<? extends String>> srcList,
      final boolean canOverwrite) {
    // キーバリデート
    validateKeyByTypeForPut(StorageType.ARYS, key, canOverwrite);
    // 全キー格納
    super.allKeySet().add(key);

    if (ValUtil.isNull(srcList)) {
      return this.arysMap.put(key, null);
    }
    // コピーを格納
    final IoArrays copyArys = new IoArrays(srcList);
    return this.arysMap.put(key, copyArys);
  }

  /**
   * 文字列リスト取得.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKeyList(String)</code> にて事前に存在確認する。</li>
   * <li>格納されている値が <code>null</code> の場合はサイズゼロのリストを返す。（<code>null</code> は返らない）</li>
   * <li>取得したリストに要素を追加しても本クラスで保持しているリストに影響しない。（ディープコピーとなる）</li>
   * <li>サイズが大きいリストを扱ったり、リストの格納と取得を繰り返したりすると性能が悪化する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 文字列リスト
   */
  public List<String> getList(final String key) {
    final List<String> list = getCopyList(key);
    if (ValUtil.isNull(list)) {
      return Collections.emptyList();
    }
    return list;
  }

  /**
   * 文字列リスト格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーで値格納する場合は <code>#putListForce(String, List)</code> で格納する。</li>
   * <li>格納した元のリストに要素を追加しても本クラスで保持しているリストに影響しない。（ディープコピーとなる）</li>
   * <li>サイズが大きいリストを扱ったり、リストの格納と取得を繰り返したりすると性能が悪化する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param list 文字列リスト
   * @return 前回の格納文字列リスト
   */
  public List<String> putList(final String key, final List<String> list) {
    return putCopyList(key, list, false);
  }

  /**
   * 文字列リスト格納（上書き許可）.<br>
   * <ul>
   * <li>格納した元のリストに要素を追加しても本クラスで保持しているリストに影響しない。（ディープコピーとなる）</li>
   * <li>サイズが大きいリストを扱ったり、リストの格納と取得を繰り返したりすると性能が悪化する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param list 文字列リスト
   * @return 前回の格納文字列リスト
   */
  public List<String> putListForce(final String key, final List<String> list) {
    return putCopyList(key, list, true);
  }

  /**
   * ネストマップ取得.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKeyNest(String)</code> にて事前に存在確認する。</li>
   * <li>格納されている値が <code>null</code> の場合はサイズゼロのマップを返す。（<code>null</code> は返らない）</li>
   * <li>取得したマップに要素を追加しても本クラスで保持しているマップに影響しない。（ディープコピーとなる）</li>
   * <li>サイズが大きいマップを扱ったり、マップの格納と取得を繰り返したりすると性能が悪化する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return ネストマップ
   */
  public Io getNest(final String key) {
    final Io nest = getCopyNest(key);
    if (ValUtil.isNull(nest)) {
      return new Io();
    }
    return nest;
  }

  /**
   * ネストマップ格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーで値格納する場合は <code>#putNestForce(String, Nest)</code> で格納する。</li>
   * <li>格納した元のマップに要素を追加しても本クラスで保持しているマップに影響しない。（ディープコピーとなる）</li>
   * <li>サイズが大きいマップを扱ったり、マップの格納と取得を繰り返したりすると性能が悪化する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param nest ネストマップ
   * @return 前回の格納ネストマップ
   */
  public Io putNest(final String key, final Io nest) {
    return putCopyNest(key, nest, false);
  }

  /**
   * ネストマップ格納（上書き許可）.<br>
   * <ul>
   * <li>格納した元のマップに要素を追加しても本クラスで保持しているマップに影響しない。（ディープコピーとなる）</li>
   * <li>サイズが大きいマップを扱ったり、マップの格納と取得を繰り返したりすると性能が悪化する。</li>
   * </ul>
   *
   * @see #putNest(String, Nest)
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param nest ネストマップ
   * @return 前回の格納ネストマップ
   */
  public Io putNestForce(final String key, final Io nest) {
    return putCopyNest(key, nest, true);
  }

  /**
   * 複数行リスト取得.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKeyRows(String)</code> にて事前に存在確認する。</li>
   * <li>格納されている値が <code>null</code> の場合はサイズゼロのリストを返す。（<code>null</code> は返らない）</li>
   * <li>取得したリストに要素を追加しても本クラスで保持しているリストに影響しない。（ディープコピーとなる）</li>
   * <li>サイズが大きいリストを扱ったり、リストの格納と取得を繰り返したりすると性能が悪化する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 複数行リスト
   */
  public IoRows getRows(final String key) {
    final IoRows rows = getCopyRows(key);
    if (ValUtil.isNull(rows)) {
      return new IoRows();
    }
    return rows;
  }

  /**
   * 複数行リスト格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーで値格納する場合は <code>#putRowsForce(String, List)</code> で格納する。</li>
   * <li>格納した元のリストに要素を追加しても本クラスで保持しているリストに影響しない。（ディープコピーとなる）</li>
   * <li>サイズが大きいリストを扱ったり、リストの格納と取得を繰り返したりすると性能が悪化する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param rows 複数行リスト
   * @return 前回の格納複数行リスト
   */
  public IoRows putRows(final String key,
      final Collection<? extends Map<? extends String, ? extends String>> rows) {
    return putCopyRows(key, rows, false);
  }

  /**
   * 複数行リスト格納（上書き許可）.<br>
   * <ul>
   * <li>格納した元のリストに要素を追加しても本クラスで保持しているリストに影響しない。（ディープコピーとなる）</li>
   * <li>サイズが大きいリストを扱ったり、リストの格納と取得を繰り返したりすると性能が悪化する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param rows 複数行リスト
   * @return 前回の格納複数行リスト
   */
  public IoRows putRowsForce(final String key, final Collection<? extends IoItems> rows) {
    return putCopyRows(key, rows, true);
  }

  /**
   * 配列リスト取得.<br>
   * <ul>
   * <li>存在しないキーでの値取得は実行時エラーとなる。</li>
   * <li>存在しない可能性があるキーで値取得する場合は <code>#containsKeyArys(String)</code> にて事前に存在確認する。</li>
   * <li>格納されている値が <code>null</code> の場合はサイズゼロのリストを返す。（<code>null</code> は返らない）</li>
   * <li>取得したリストに要素を追加しても本クラスで保持しているリストに影響しない。（ディープコピーとなる）</li>
   * <li>サイズが大きいリストを扱ったり、リストの格納と取得を繰り返したりすると性能が悪化する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 配列リスト
   */
  public IoArrays getArys(final String key) {
    final IoArrays arys = getCopyArys(key);
    if (ValUtil.isNull(arys)) {
      return new IoArrays();
    }
    return arys;
  }

  /**
   * 配列リスト格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーで値格納する場合は <code>#putArysForce(String, List)</code> で格納する。</li>
   * <li>格納した元のリストに要素を追加しても本クラスで保持しているリストに影響しない。（ディープコピーとなる）</li>
   * <li>サイズが大きいリストを扱ったり、リストの格納と取得を繰り返したりすると性能が悪化する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param arys 配列リスト
   * @return 前回の格納配列リスト
   */
  public IoArrays putArys(final String key, final Collection<? extends List<String>> arys) {
    return putCopyArys(key, arys, false);
  }

  /**
   * 配列リスト格納（上書き許可）.<br>
   * <ul>
   * <li>格納した元のリストに要素を追加しても本クラスで保持しているリストに影響しない。（ディープコピーとなる）</li>
   * <li>サイズが大きいリストを扱ったり、リストの格納と取得を繰り返したりすると性能が悪化する。</li>
   * </ul>
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @param arys 配列リスト
   * @return 前回の格納配列リスト
   */
  public IoArrays putArysForce(final String key, final Collection<? extends List<String>> arys) {
    return putCopyArys(key, arys, true);
  }

  /**
   * URLパラメーター（URLの?より後ろの部分）作成.<br>
   * <ul>
   * <li>文字列リストはカンマ区切りのパラメーターとなる。</li>
   * <li>ネストマップと複数行リスト、配列リストは出力されない。</li>
   * </ul>
   *
   * @return URLエンコードされたGETパラメーター
   */
  String createUrlParam() {
    final Map<String, String> valMap = super.getValMap();
    final StringBuilder sb = new StringBuilder();
    for (final String key : super.allKeySet()) {
      if (this.listMap.containsKey(key)) {
        final List<String> list = this.listMap.get(key);
        final String paramAry = listToUrlParamArray(key, list);
        sb.append(paramAry).append('&');
        continue;
      }
      final String val = valMap.get(key);
      final String encVal = ValUtil.urlEncode(val);
      sb.append(key).append('=').append(encVal).append('&');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * 文字列リスト to URLパラメーター配列文字列.
   *
   * @param key キー
   * @param list 文字列リスト
   * @return URLパラメーター配列文字列.
   */
  private String listToUrlParamArray(final String key, final List<String> list) {
    final String pKey = key + "[]";

    final StringBuilder sb = new StringBuilder();
    if (ValUtil.isEmpty(list)) {
      sb.append(pKey).append('=');
      return sb.toString();
    }
    for (final String val : list) {
      sb.append(pKey).append('=');
      final String encVal = ValUtil.urlEncode(val);
      sb.append(encVal);
      sb.append('&');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * JSON作成.
   *
   * @return JSON文字列
   */
  public String createJson() {
    return createJsonWithMsg(null);
  }

  /**
   * メッセージ付き JSON作成.
   * @param msgTextMap メッセージテキストマップ&lt;メッセージID、メッセージテキスト&gt;
   * @return JSON文字列
   */
  public String createJsonWithMsg(final Map<String, String> msgTextMap) {
    final Map<String, String> valMap = super.getValMap();
    final StringBuilder sb = new StringBuilder();
    for (final String key : super.allKeySet()) {
      sb.append('"').append(key).append('"').append(':');

      if (this.listMap.containsKey(key)) {
        final List<String> list = this.listMap.get(key);
        final String json = listToJsonArray(list);
        sb.append(json).append(',');
        continue;
      }
      if (this.nestMap.containsKey(key)) {
        final Io nest = this.nestMap.get(key);
        final String json = nest.createJson();
        sb.append(json).append(',');
        continue;
      }
      if (this.rowsMap.containsKey(key)) {
        final IoRows rows = this.rowsMap.get(key);
        final String json = rowsToJsonArray(rows);
        sb.append(json).append(',');
        continue;
      }
      if (this.arysMap.containsKey(key)) {
        final IoArrays arys = this.arysMap.get(key);
        final String json = arysToJsonArray(arys);
        sb.append(json).append(',');
        continue;
      }

      final String val = valMap.get(key);
      if (ValUtil.isNull(val)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String escVal = ValUtil.jsonEscape(val);
      sb.append('"').append(escVal).append('"').append(',');
    }

    // メッセージを追加（メッセージが存在する場合のみ）
    if (hasMsg()) {
      final String msg = createMsgJsoAry(msgTextMap);
      sb.append('"').append("_msg").append('"').append(':').append(msg).append(',');
      // エラーフラグを追加
      sb.append('"').append("_has_err").append('"').append(':').append(hasErrorMsg()).append(',');
    }

    ValUtil.deleteLastChar(sb);
    sb.insert(0, '{');
    sb.append('}');
    return sb.toString();
  }

  /**
   * 文字列リスト to JSON配列文字列.
   *
   * @param list 文字列リスト
   * @return JSON配列文字列
   */
  private String listToJsonArray(final List<String> list) {
    if (ValUtil.isEmpty(list)) {
      return "[]";
    }
    final StringBuilder sb = new StringBuilder();
    for (final String val : list) {
      if (ValUtil.isNull(val)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String escVal = ValUtil.jsonEscape(val);
      sb.append('"').append(escVal).append('"').append(',');
    }
    ValUtil.deleteLastChar(sb);
    sb.insert(0, '[');
    sb.append(']');
    return sb.toString();
  }

  /**
   * 複数行リスト to JSON配列文字列.
   *
   * @param rows 複数行リスト
   * @return JSON配列文字列
   */
  private String rowsToJsonArray(final IoRows rows) {
    if (ValUtil.isEmpty(rows)) {
      return "[]";
    }
    final StringBuilder sb = new StringBuilder();
    for (final IoItems row : rows) {
      if (ValUtil.isNull(row)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String json = row.createJson();
      sb.append(json).append(',');
    }
    ValUtil.deleteLastChar(sb);
    sb.insert(0, '[');
    sb.append(']');
    return sb.toString();
  }

  /**
   * 配列リスト to JSON配列文字列.
   *
   * @param arys 配列リスト
   * @return JSON配列文字列
   */
  private String arysToJsonArray(final IoArrays arys) {
    if (ValUtil.isEmpty(arys)) {
      return "[]";
    }
    final StringBuilder sb = new StringBuilder();
    for (final List<String> ary : arys) {
      if (ValUtil.isNull(ary)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String json = listToJsonArray(ary);
      sb.append(json).append(',');
    }
    ValUtil.deleteLastChar(sb);
    sb.insert(0, '[');
    sb.append(']');
    return sb.toString();
  }

  /**
   * ログ出力文字列作成.
   *
   * @return ログ出力文字列
   */
  private final String createLogString() {
    final Map<String, String> valMap = super.getValMap();
    final StringBuilder sb = new StringBuilder();
    try {
      for (final String key : super.allKeySet()) {
        sb.append(key).append('=');
        if (this.listMap.containsKey(key)) {
          final List<String> list = this.listMap.get(key);
          final String log = LogUtil.join(list);
          sb.append(log).append(',');
          continue;
        }
        if (this.nestMap.containsKey(key)) {
          final Io nest = this.nestMap.get(key);
          final String log = nest.createLogString();
          sb.append(log).append(',');
          continue;
        }
        if (this.rowsMap.containsKey(key)) {
          final IoRows rows = this.rowsMap.get(key);
          final String log = rowsToLog(rows);
          sb.append(log).append(',');
          continue;
        }
        if (this.arysMap.containsKey(key)) {
          final IoArrays arys = this.arysMap.get(key);
          final String log = arysToLog(arys);
          sb.append(log).append(',');
          continue;
        }

        final String val = valMap.get(key);
        final String sval = LogUtil.convOutput(val);
        sb.append(sval);
        sb.append(',');
      }
      ValUtil.deleteLastChar(sb);
      sb.insert(0, '{');
      sb.append('}');
    } catch (Exception ignore) {
      // 処理なし
    }
    return sb.toString();
  }

  /**
   * 複数行リスト to ログ出力文字列.
   *
   * @param rows 複数行リスト
   * @return ログ出力文字列
   */
  private String rowsToLog(final IoRows rows) {
    if (ValUtil.isEmpty(rows)) {
      return "[]";
    }
    final StringBuilder sb = new StringBuilder();
    for (final IoItems row : rows) {
      if (ValUtil.isNull(row)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String log = row.createLogString();
      sb.append(log).append(',');
    }
    ValUtil.deleteLastChar(sb);
    sb.insert(0, '[');
    sb.append(']');
    return sb.toString();
  }

  /**
   * 配列リスト to ログ出力文字列.
   *
   * @param arys 配列リスト
   * @return ログ出力文字列
   */
  private String arysToLog(final IoArrays arys) {
    if (ValUtil.isEmpty(arys)) {
      return "[]";
    }
    final StringBuilder sb = new StringBuilder();
    for (final List<String> ary : arys) {
      if (ValUtil.isNull(ary)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String log = LogUtil.join(ary);
      sb.append(log).append(',');
    }
    ValUtil.deleteLastChar(sb);
    sb.insert(0, '[');
    sb.append(']');
    return sb.toString();
  }


  /**
   * 全値格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * <li>既に存在する可能性があるキーが含まれる場合は <code>#putAllForce(Map)</code> で格納する。</li>
   * <li>メッセージもコピーされる。</li>
   * </ul>
   *
   * @param map 入出力マップ
   */
  public void putAll(final Io map) {
    putAllByIoMap(map, false);
  }

  /**
   * 全値格納（上書き許可）.
   * @see #putAll(Io)
   * @param map 入出力マップ
   */
  public void putAllForce(final Io map) {
    putAllByIoMap(map, true);
  }

  /**
   * 入出力マップ格納.<br>
   * <ul>
   * <li>内容をディープコピーするため、ソースマップとの参照は切れる。</li>
   * <li>メッセージもコピーされる。</li>
   * </ul>
   *
   * @param srcMap       ソースマップ
   * @param canOverwrite 上書き許可
   */
  private void putAllByIoMap(final Io srcMap, final boolean canOverwrite) {

    if (ValUtil.isNull(srcMap)) {
      return;
    }

    final Map<String, String> valMap = srcMap.getValMap();

    // 全キーのループ
    for (final String key : srcMap.allKeySet()) {
      // 値格納
      if (valMap.containsKey(key)) {
        putVal(key, valMap.get(key), canOverwrite);
      }
      // 文字列リスト格納
      if (srcMap.listMap.containsKey(key)) {
        putCopyList(key, srcMap.listMap.get(key), canOverwrite);
      }
      // ネストマップ格納
      if (srcMap.nestMap.containsKey(key)) {
        putCopyNest(key, srcMap.nestMap.get(key), canOverwrite);
      }
      // 複数行リスト格納
      if (srcMap.rowsMap.containsKey(key)) {
        putCopyRows(key, srcMap.rowsMap.get(key), canOverwrite);
      }
      // 配列リスト格納
      if (srcMap.arysMap.containsKey(key)) {
        putCopyArys(key, srcMap.arysMap.get(key), canOverwrite);
      }
    }
    // メッセージもコピー
    copyMsg(srcMap);
  }

  /**
   * URLパラメーター（URLの?より後ろの部分）値格納.<br>
   * <ul>
   * <li>既に存在するキーでの格納は実行時エラーとなる。</li>
   * </ul>
   *
   * @param url URL全体 または URLパラメーター
   * @return 格納パラメーター数
   */
  public int putAllByUrlParam(final String url) {
    if (ValUtil.isBlank(url)) {
      return 0;
    }

    final String params;
    if (url.indexOf('?') > 0) {
      params = url.substring(url.indexOf('?') + 1);
    } else {
      params = url;
    }

    final Map<String, List<String>> aryList = new LinkedHashMap<>();
    int count = 0;
    for (final String param : new SimpleSeparateParser(params, "&")) {
      final String[] keyVal = ValUtil.splitReg(param, "=", 2);
      final String key = keyVal[0];
      final String val;
      if (keyVal.length == 1) {
        val = ValUtil.BLANK;
      } else {
        val = ValUtil.urlDecode(keyVal[1]);
      }

      if (key.endsWith("[]")) {
        final String lsKey = key.substring(key.length() - 2);
        final List<String> list;
        if (aryList.containsKey(lsKey)) {
          list = aryList.get(lsKey);
        } else {
          list = new ArrayList<>();
          aryList.put(lsKey, list);
        }
        list.add(val);
        continue;
      }

      count++;
      put(key, val);
    }

    for (final Entry<String, List<String>> ent : aryList.entrySet()) {
      count++;
      putList(ent.getKey(), ent.getValue());
    }

    return count;
  }

  /**
   * JSON格納.
   *
   * @param json JSON文字列
   * @return 格納項目数
   */
  public int putAllByJson(final String json) {
    if (ValUtil.isBlank(json)) {
      return 0;
    }

    int count = 0;

    // JSON項目のループ
    for (final String item : new JsonMapSeparateParser(json)) {
      final String[] keyVal = JsonMapKeyValueSeparateParser.getKeyValue(item);
      if (ValUtil.isNull(keyVal)) {
        continue;
      }
      final String key = keyVal[0];
      final String val = keyVal[1];

      count++;
      if (JsonMapSeparateParser.JSON_MAP_PATTERN.matcher(val).find()) {
        // ネストマップ追加
        final Io nest = new Io();
        nest.putAllByJson(val);
        putNest(key, nest);
        continue;
      }
      if (JsonArraySeparateParser.JSON_ARRAY_PATTERN.matcher(val).find()) {
        // 文字列リスト追加
        final List<String> list = jsonArrayToList(val);
        // ただし、値の中に連想配列があれば複数行リスト追加
        // 値の中に配列があれば配列リスト追加
        boolean isRows = false;
        boolean isArys = false;
        for (final String listVal : list) {
          if (ValUtil.isBlank(listVal)) {
            continue;
          }
          if (JsonMapSeparateParser.JSON_MAP_PATTERN.matcher(listVal).find()) {
            isRows = true;
            break;
          }
          if (JsonArraySeparateParser.JSON_ARRAY_PATTERN.matcher(listVal).find()) {
            isArys = true;
            break;
          }
        }
        if (isRows) {
          // 複数行リスト追加
          final IoRows rows = jsonArrayToRows(val);
          putRows(key, rows);
          continue;
        }
        if (isArys) {
          // 複数行リスト追加
          final IoArrays arys = jsonArrayToArys(val);
          putArys(key, arys);
          continue;
        }
        putList(key, list);
        continue;
      }
      if (ValUtil.JSON_NULL.equals(val)) {
        putNull(key);
        continue;
      }
      final String unEscVal = ValUtil.jsonUnEscape(ValUtil.trimDq(val));
      put(key, unEscVal);
    }
    return count;
  }

  /**
   * JSON配列文字列 to 文字列リスト.
   *
   * @param json JSON配列文字列
   * @return 文字列リスト
   */
  private List<String> jsonArrayToList(final String json) {
    final List<String> list = new ArrayList<>();
    for (final String value : new JsonArraySeparateParser(json)) {
      if (ValUtil.JSON_NULL.equals(value)) {
        list.add(null);
        continue;
      }
      final String val = ValUtil.jsonUnEscape(value);
      list.add(val);
    }
    return list;
  }

  /**
   * JSON配列文字列 to 複数行リスト.
   *
   * @param jsonAry JSON配列文字列
   * @return 複数行リスト
   */
  private IoRows jsonArrayToRows(final String jsonAry) {
    final IoRows rows = new IoRows();
    for (final String json : new JsonArraySeparateParser(jsonAry)) {
      if (ValUtil.JSON_NULL.equals(json)) {
        rows.add(null);
        continue;
      }

      final IoItems row = new IoItems();
      row.putAllByJson(json);

      // ４階層以上が無いかチェック
      for (final String value : row.values()) {
        if (JsonMapSeparateParser.JSON_MAP_PATTERN.matcher(value).find()
            || JsonArraySeparateParser.JSON_ARRAY_PATTERN.matcher(value).find()) {
          throw new RuntimeException("Arrays of 4 or more layers are not supported. " + LogUtil.joinKeyVal("json", jsonAry));
        }
      }

      rows.add(row);
    }
    return rows;
  }

  /**
   * JSON配列文字列 to 配列リスト.
   *
   * @param jsonAry JSON配列文字列
   * @return 配列リスト
   */
  private IoArrays jsonArrayToArys(final String jsonAry) {
    final IoArrays arys = new IoArrays();

    for (final String json : new JsonArraySeparateParser(jsonAry)) {
      if (ValUtil.JSON_NULL.equals(json)) {
        arys.add(null);
        continue;
      }

      final List<String> ary = new ArrayList<>();
      for (final String aryVal : new JsonArraySeparateParser(json)) {

        if (ValUtil.JSON_NULL.equals(aryVal)) {
          ary.add(null);
          continue;
        }

        // ４階層以上が無いかチェック
        if (JsonMapSeparateParser.JSON_MAP_PATTERN.matcher(aryVal).find()
            || JsonArraySeparateParser.JSON_ARRAY_PATTERN.matcher(aryVal).find()) {
          throw new RuntimeException("Arrays of 4 or more layers are not supported. " + LogUtil.joinKeyVal("json", jsonAry));
        }

        final String val = ValUtil.jsonUnEscape(aryVal);
        ary.add(val);
      }

      arys.add(ary);
    }
    return arys;
  }

  /**
   * ログ用文字列返却.
   */
  public final String toString() {
    return createLogString();
  }

  /**
   * 文字列リストキー存在確認.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 存在する場合は <code>true</code>
   */
  public boolean containsKeyList(final String key) {
    return this.listMap.containsKey(key);
  }

  /**
   * 文字列リスト削除.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 削除したリスト
   */
  public List<String> removeList(final String key) {
    super.allKeySet().remove(key);
    return this.listMap.remove(key);
  }

  /**
   * ネストマップキー存在確認.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 存在する場合は <code>true</code>
   */
  public boolean containsKeyNest(final String key) {
    return this.nestMap.containsKey(key);
  }

  /**
   * ネストマップ削除.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 削除したネストマップ
   */
  public Io removeNest(final String key) {
    super.allKeySet().remove(key);
    return this.nestMap.remove(key);
  }

  /**
   * 複数行リストキー存在確認.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 存在する場合は <code>true</code>
   */
  public boolean containsKeyRows(final String key) {
    return this.rowsMap.containsKey(key);
  }

  /**
   * 複数行リスト削除.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 削除した複数行リスト
   */
  public IoRows removeRows(final String key) {
    super.allKeySet().remove(key);
    return this.rowsMap.remove(key);
  }

  /**
   * 配列リストキー存在確認.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 存在する場合は <code>true</code>
   */
  public boolean containsKeyArys(final String key) {
    return this.arysMap.containsKey(key);
  }

  /**
   * 配列リスト削除.
   *
   * @param key キー（英字小文字、数字、アンダースコア、ハイフン、ドットのみ）
   * @return 削除した配列リスト
   */
  public IoArrays removeArys(final String key) {
    super.allKeySet().remove(key);
    return this.arysMap.remove(key);
  }

  /** メッセージ保持マップ. */
  private final Map<String, MsgBean> msgMap = new LinkedHashMap<>();
  /** エラーメッセージ保持判定. */
  private boolean errMsgExists = false;

  /** メッセージタイプ. */
  public enum MsgType {
    /** エラーメッセージ. */
    ERROR,
    /** 警告メッセージ. */
    WARN,
    /** 情報メッセージ. */
    INFO;
  }

  /** メッセージ保持クラス. */
  private class MsgBean {

    /** メッセージタイプ. */
    private MsgType type;
    /** メッセージID. */
    private String msgId;
    /** メッセージ内置換文字列. */
    private String[] replaceVals;
    /** 対象項目ID. */
    private String itemId;
    /** 行リストID. */
    private String rowListId;
    /** 行インデックス. */
    private int rowIndex;

    /**
     * コンストラクタ.
     * 
     * @param type        メッセージタイプ
     * @param msgId       メッセージID
     * @param replaceVals メッセージ内置換文字列 メッセージテキスト内の <code>{0</code>, {1}, ...} を置換える。
     * @param itemId      対象項目ID
     * @param rowListId   行リストID
     * @param rowIndex    行インデックス
     */
    private MsgBean(final MsgType type, final String msgId, final String[] replaceVals, final String itemId,
        final String rowListId, final int rowIndex) {
      this.type = type;
      this.msgId = msgId;
      this.replaceVals = replaceVals;
      this.itemId = itemId;
      this.rowListId = rowListId;
      this.rowIndex = rowIndex;
    }

    /**
     * コピーコンストラクタ.<br>
     * <ul>
     * <li>ディープコピーを実行し、元オブジェクトとの参照を切る。</li>
     * </ul>
     * 
     * @param srcMsg コピー元メッセージBean
     */
    private MsgBean(final MsgBean srcMsg) {
      this.type = srcMsg.type;
      this.msgId = srcMsg.msgId;
      // 配列のディープコピー
      if (ValUtil.isNull(srcMsg.replaceVals)) {
        this.replaceVals = null;
      } else {
        this.replaceVals = new String[srcMsg.replaceVals.length];
        System.arraycopy(srcMsg.replaceVals, 0, this.replaceVals, 0, srcMsg.replaceVals.length);
      }
      this.itemId = srcMsg.itemId;
      this.rowListId = srcMsg.rowListId;
      this.rowIndex = srcMsg.rowIndex;
    }

    /**
     * メッセージタイプ取得.
     * 
     * @return メッセージタイプ
     */
    private MsgType getType() {
      return type;
    }

    /**
     * キー生成.<br>
     * <ul>
     * <li>同じ内容のメッセージが重複して登録されるのを防止するために使用する。</li>
     * <li>メッセージの表示順を決定する。</li>
     * </ul>
     *
     * @return キー文字列
     */
    private String createKey() {
      final StringBuilder sb = new StringBuilder();
      sb.append(this.type.ordinal()).append("_").append(this.msgId);
      if (!ValUtil.isEmpty(this.replaceVals)) {
        sb.append("_").append(String.join("&", this.replaceVals));
      }
      if (!ValUtil.isBlank(this.itemId)) {
        if (ValUtil.isBlank(this.rowListId)) {
          sb.append(this.itemId);
        } else {
          sb.append(this.rowListId).append("[").append(ValUtil.paddingLeftZero(this.rowIndex, 4)).append("].")
              .append(this.itemId);
        }
      }
      return sb.toString();
    }

    /**
     * JSON作成.
     * 
     * @param msgTextMap メッセージテキストマップ&lt;メッセージID、メッセージテキスト&gt;（省略可能）
     * @return JSON文字列
     */
    protected String createJson(final Map<String, String> msgTextMap) {
      final StringBuilder sb = new StringBuilder();
      sb.append('{');
      sb.append('"').append("type").append('"').append(':').append('"').append(this.type.name()).append('"').append(',');
      sb.append('"').append("id").append('"').append(':').append('"').append(this.msgId).append('"').append(',');

      final String text;
      if (!ValUtil.isNull(msgTextMap) && msgTextMap.containsKey(this.msgId)) {
        final String msgText = msgTextMap.get(this.msgId);
        text = replaceText(msgText, this.replaceVals);
      } else {
        if (!ValUtil.isEmpty(this.replaceVals)) {
          text = "エラー情報：" + ValUtil.join(",", this.replaceVals);
        } else {
          text = ValUtil.BLANK;
        }
      }
      sb.append('"').append("text").append('"').append(':').append('"').append(ValUtil.jsonEscape(text)).append('"')
          .append(',');

      if (!ValUtil.isBlank(this.itemId)) {
        sb.append('"').append("item").append('"').append(':');
        if (!ValUtil.isBlank(this.rowListId)) {
          sb.append('"').append(this.rowListId).append('.').append(this.itemId).append('"').append(',');
          sb.append('"').append("row").append('"').append(':').append(this.rowIndex).append(',');
        } else {
          sb.append('"').append(this.itemId).append('"').append(',');
        }
      }
      ValUtil.deleteLastChar(sb);
      sb.append('}');
      return sb.toString();
    }

    /**
     * テキスト置換.<br>
     * <ul>
     * <li>メッセージテキスト内の  <code>{0}, {1}, ...</code> を置換える。</li>
     * </ul>
     * 
     * @param text 対象テキスト
     * @param replaceVals 置換値配列
     * @return 置換後テキスト
     */
    private String replaceText(final String text, final String[] replaceVals) {
      String ret = text;
      if (!ValUtil.isEmpty(replaceVals)) {
        for (int i = 0; i < replaceVals.length; i++) {
          final String key = "{" + i + "}";
          final String val = ValUtil.nvl(replaceVals[i]);
          ret = ret.replace(key, val);
        }
      }
      // {0}, {1}, ... が残っている場合は空文字に置換
      final String regex = "\\{[0-9]+\\}";
      ret = ret.replaceAll(regex, ValUtil.BLANK);
      return ret;
    }
  }

  /**
   * メッセージ追加.
   * 
   * @param type メッセージタイプ
   * @param msgId メッセージID
   */
  public void putMsg(final MsgType type, final String msgId) {
    putMsg(type, msgId, null, null, null, -1);
  }

  /**
   * メッセージ追加.
   * 
   * @param type メッセージタイプ
   * @param msgId メッセージID
   * @param replaceVals メッセージ内置換文字列（省略可能）
   */
  public void putMsg(final MsgType type, final String msgId, final String[] replaceVals) {
    putMsg(type, msgId, replaceVals, null, null, -1);
  }

  /**
   * メッセージ追加.
   * 
   * @param type メッセージタイプ
   * @param msgId メッセージID
   * @param itemId 対象項目ID（省略可能）
   */
  public void putMsg(final MsgType type, final String msgId, final String itemId) {
    putMsg(type, msgId, null, itemId, null, -1);
  }

  /**
   * メッセージ追加.
   * 
   * @param type メッセージタイプ
   * @param msgId メッセージID
   * @param replaceVals メッセージ内置換文字列（省略可能）
   * @param itemId 対象項目ID（省略可能）
   */
  public void putMsg(final MsgType type, final String msgId, final String[] replaceVals, final String itemId) {
    putMsg(type, msgId, replaceVals, itemId, null, -1);
  }

  /**
   * メッセージ追加.
   * 
   * @param type メッセージタイプ
   * @param msgId メッセージID
   * @param itemId 対象項目ID（省略可能）
   * @param rowListId 行リストID（省略可能）
   * @param rowIndex 行インデックス（省略可能）
   */
  public void putMsg(final MsgType type, final String msgId, final String itemId, final String rowListId,
      final int rowIndex) {
    putMsg(type, msgId, null, itemId, rowListId, rowIndex);
  }

  /**
   * メッセージ追加.
   * 
   * @param type メッセージタイプ
   * @param msgId メッセージID
   * @param replaceVals メッセージ内置換文字列（省略可能）
   * @param itemId 対象項目ID（省略可能）
   * @param rowListId 行リストID（省略可能）
   * @param rowIndex 行インデックス（省略可能）
   */
  public void putMsg(final MsgType type, final String msgId, final String[] replaceVals, final String itemId,
      final String rowListId, final int rowIndex) {
    if (ValUtil.isBlank(msgId)) {
      throw new RuntimeException("msgId is blank.");
    }
    if (rowIndex >= 0 && ValUtil.isBlank(rowListId)) {
      throw new RuntimeException("rowListId is blank.");
    }
    if (!ValUtil.isBlank(rowListId) && rowIndex < 0) {
      throw new RuntimeException("rowIndex is invalid.");
    }
    if (!ValUtil.isBlank(rowListId) && ValUtil.isBlank(itemId)) {
      throw new RuntimeException("itemId is blank.");
    }
    final MsgBean msg = new MsgBean(type, msgId, replaceVals, itemId, rowListId, rowIndex);
    final String key = msg.createKey();
    if (this.msgMap.containsKey(key)) {
      return;
    }
    this.msgMap.put(key, msg);
    
    // エラーメッセージフラグ更新
    if (MsgType.ERROR == type) {
      this.errMsgExists = true;
    }
  }

  /**
   * メッセージ保持判定.
   * 
   * @return メッセージが保持されている場合は <code>true</code>
   */
  public boolean hasMsg() {
    return !ValUtil.isEmpty(this.msgMap);
  }

  /**
   * エラーメッセージ存在判定.
   * 
   * @return エラーメッセージが存在する場合は <code>true</code>
   */
  public boolean hasErrorMsg() {
    return this.errMsgExists;
  }

  /**
   * メッセージクリア.
   */
  public void clearMsg() {
    this.msgMap.clear();
    this.errMsgExists = false;
  }

  /**
   * メッセージコピー.<br>
   * <ul>
   * <li>メッセージをディープコピーするため、元オブジェクトとの参照は切れる。</li>
   * </ul>
   * 
   * @param srcMap コピー元マップ
   */
  private void copyMsg(final Io srcMap) {
    if (ValUtil.isNull(srcMap)) {
      return;
    }

    for (final MsgBean bean : srcMap.msgMap.values()) {
      // ディープコピーで格納
      final MsgBean msg = new MsgBean(bean);
      final String key = msg.createKey();
      if (this.msgMap.containsKey(key)) {
        return;
      }
      this.msgMap.put(key, msg);

      // エラーメッセージフラグ更新
      if (MsgType.ERROR == msg.getType()) {
        this.errMsgExists = true;
      }
    }
  }

  /**
   * メッセージJSON配列作成.<br>
   * <ul>
   * <li>メッセージテキストマップが指定されている
   * 場合は、メッセージIDに対応するメッセージテキストをセットする。</li>
   * <li>メッセージテキストマップが指定されていない場合は、メッセージテキストは空文字列とする。</li>
   * </ul>
   * 
   * @param msgTextMap メッセージテキストマップ&lt;メッセージID、メッセージテキスト&gt;
   * @return JSON配列 "[ {...}, {...}, ... ]"
   */
  private String createMsgJsoAry(final Map<String, String> msgTextMap) {
    final StringBuilder sb = new StringBuilder();
    sb.append('[');
    if (!ValUtil.isEmpty(this.msgMap)) {
      for (final MsgBean msgBean : this.msgMap.values()) {
        final String msgJson = msgBean.createJson(msgTextMap);
        sb.append(msgJson).append(',');
      }
      ValUtil.deleteLastChar(sb);
    }
    sb.append(']');
    return sb.toString();
  }
}
