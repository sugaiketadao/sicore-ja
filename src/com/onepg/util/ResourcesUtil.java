package com.onepg.util;

import com.onepg.util.ValUtil.CharSet;

/**
 * リソースファイルユーティリティクラス.<br>
 * <ul>
 * <li>リソースファイルの読み込みを行う。</li>
 * <li>リソースファイル格納ディレクトリ（以下、リソースディレクトリ）はアプリケーション配備ディレクトリ直下の resources
 * ディレクトリとし、アプリケーション配備ディレクトリから相対的に固定とする。 <br>
 * ［例］application/lib/program.jar の場合、application/resources/ 配下となる。 <br>
 * アプリケーション配備ディレクトリの詳細は <code>PropertiesUtil#APPLICATION_DIR_PATH</code> 参照。</li>
 * <li>リソースファイルの文字セットは UTF-8 を前提とする。</li>
 * <li>リソースファイル名は自由だが下記ファイル名はフレームワーク部品専用とし使用不可とする。 <br>
 * msg.json</li>
 * </ul>
 */
public final class ResourcesUtil {

  /** リソースファイル文字セット. */
  private static final CharSet RESRC_FILE_CHAR_SET = CharSet.UTF8;
  /** リソースディレクトリ名. */
  private static final String RESRC_DIRNAME = "resources";

  /** リソースディレクトリパス. */
  private static final String RESRC_STORAGE_DIR_PATH = FileUtil.joinPath(PropertiesUtil.APPLICATION_DIR_PATH, RESRC_DIRNAME);

  /** フレームワーク専用 リソースファイル名. */
  public enum FwResourceName {
    /** リソースファイル名 - フレームワーク専用 メッセージリソースファイル名. */
    MSG("msg.json");

    /** ファイル名. */
    private final String name;

    /**
     * コンストラクタ.
     *
     * @param value ファイル名
     */
    private FwResourceName(final String value) {
      this.name = value;
    }

    @Override
    public String toString() {
      return this.name;
    }

    /**
     * 存在確認.
     *
     * @param name ファイル名
     * @return 存在する場合は <code>true</code>
     */
    private static boolean exists(final String name) {
      for (final FwResourceName fwName : values()) {
        if (fwName.toString().equalsIgnoreCase(name)) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * コンストラクタ.
   */
  private ResourcesUtil() {
    // 処理なし
  }

  /**
   * フレームワーク専用リソースJSON取得.<br>
   * <ul>
   * <li>リソースディレクトリ配下のJSONファイルを読み込み、マップで返す。</li>
   * <li>戻り値のマップは読取専用となる。</li>
   * </ul>
   *
   * @param resourceName フレームワーク専用リソース名
   * @return マップ
   */
  public static IoItems getJson(final FwResourceName resourceName) {
    return getJsonToIoItems(resourceName.toString());
  }

  /**
   * リソースJSON取得.<br>
   * <ul>
   * <li>リソースディレクトリ配下のJSONファイルを読み込み、マップで返す。</li>
   * <li>戻り値のマップは読取専用となる。</li>
   * </ul>
   * 
   * @param fileName JSONファイル名
   * @return マップ
   */
  public static IoItems getJson(final String fileName) {
    if (FwResourceName.exists(fileName)) {
      throw new RuntimeException("Framework-reserved resource files must be specified with FwResourceName class constants. " + LogUtil.joinKeyVal("file", fileName));
    }
    return getJsonToIoItems(fileName);
  }
  
  /**
   * JSONファイル読込マップ取得.
   * @param fileName ファイル名
   * @return マップ
   */
  private static IoItems getJsonToIoItems(final String fileName) {
    final String filePath = FileUtil.joinPath(RESRC_STORAGE_DIR_PATH, fileName);
    final StringBuilder sb = new StringBuilder();
    try (final TxtReader tr = new TxtReader(filePath, RESRC_FILE_CHAR_SET);) {
      for (final String line : tr) {
        sb.append(line);
      }
    }
    final IoItems ioMap = new IoItems();
    ioMap.putAllByJson(sb.toString());
    // 読取専用マップ
    return new IoItems(ioMap, true);
  }
}
