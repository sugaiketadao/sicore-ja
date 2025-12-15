package com.onepg.util;

import com.onepg.util.ValUtil.CharSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 設定ファイルユーティリティクラス.<br>
 * <ul>
 * <li>設定ファイル拡張子は .properties とする。（それ以外は無視される）</li>
 * <li>設定ファイル格納ディレクトリ（以下、設定ディレクトリ）はアプリケーション配備ディレクトリ直下の config
 * ディレクトリとし、アプリケーション配備ディレクトリから相対的に固定とする。 <br>
 * ［例］application/lib/program.jar の場合、application/config/ 配下となる。 <br>
 * アプリケーション配備ディレクトリの詳細は <code>#APPLICATION_DIR_PATH</code> 参照。</li>
 * <li>通常、設定ディレクトリのパスは上記のとおりだが、パスを変更したい場合は config/config.properties に設定キー
 * CONFIG_DIR でパス指定する。</li>
 * <li>設定ファイルの文字セットは UTF-8 を前提とする。</li>
 * <li>設定ファイルの内容は設定キーと設定値を列挙するカタチとし下記例のとおり。 <br>
 * KEY1=VAL1 <br>
 * KEY2=VAL2 <br>
 * KEY3=VAL3</li>
 * <li>設定ファイル名は自由だが下記ファイル名はフレームワーク部品専用とし使用不可とする。 <br>
 * web.properties <br>
 * bat.properties <br>
 * db.properties <br>
 * config.properties</li>
 * <li>設定ファイルの数は自由だが設定ファイルを跨いで設定キーは一意にする必要がある。（フレームワーク部品用設定ファイルとは重複しても問題ない）</li>
 * <li>設定値が ${ と } で囲われている場合は囲われている環境変数の値と置換する。（部分置換も可能） <br>
 * ただし、環境変数に存在しない場合はシステムエラーが発生する。</li>
 * <li>設定値内の $ApplicationDirPath はアプリケーション配備ディレクトリパスと置換する。（部分置換も可能）<br>
 * アプリケーション配備ディレクトリの詳細は <code>#APPLICATION_DIR_PATH</code> 参照。</li>
 * <li>設定値内の $TemporaryDirPath は OS の一時ディレクトリパスと置換する。（部分置換も可能）<br>
 * 具体的には Java の java.io.tmpdir システムプロパティのパスを置換する。</li>
 * <li>&lt;ConvertAbsolutePath&gt; 始まりの設定値は絶対パス変換する。</li>
 * <li>設定ファイル内で #
 * 始まりの行はコメントとして無視される。（<code>Properties#load(java.io.InputStream)</code> の仕様）</li>
 * <li>本クラスでは設定ファイルへの書込みは扱わない。</li>
 * </ul>
 */
public final class PropertiesUtil {

  /** 設定ファイル文字セット. */
  private static final String PROP_FILE_CHAR_SET = CharSet.UTF8.toString();
  /** デフォルト設定ディレクトリ名. */
  private static final String DEFAULT_PROP_DIRNAME = "config";
  /** 設定キー - 設定ディレクトリ指定. */
  private static final String PROPDIR_PKEY = "config.dir";
  /** プロパティファイル拡張子. */
  private static final String PROPERTIES_TYPEMARK = "properties";

  /** アプリケーション配備ディレクトリパス置換文字. */
  private static final String REPLACE_APPLICATION_DIR_PATH = "$ApplicationDirPath";
  /** 一時ディレクトリパス置換文字. */
  private static final String REPLACE_TEMPORARY_DIR_PATH = "$TemporaryDirPath";
  /** 絶対パス変換指示. */
  private static final String CONVERT_ABSOLUTE_PATH = "<ConvertAbsolutePath>";
  /** 環境変数置換パターン. */
  private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{(\\w+)\\}");

  /** フレームワーク専用 設定ファイル名. */
  public enum FwPropertiesName {
    /** 設定ファイル名 - フレームワーク専用 Webサーバー設定ファイル名. */
    WEB("web.properties"),
    /** 設定ファイル名 - フレームワーク専用 バッチ処理設定ファイル名. */
    BAT("bat.properties"),
    /** 設定ファイル名 - フレームワーク専用 DB設定ファイル名. */
    DB("db.properties"),
    /** 設定ファイル名 - フレームワーク専用 ログ設定ファイル名. */
    LOG("log.properties"),
    /** 設定ファイル名 - フレームワーク専用 設定ディレクトリ指定ファイル. */
    PROPDIR("config.properties");

    /** ファイル名. */
    private final String name;

    /**
     * コンストラクタ.
     *
     * @param value ファイル名
     */
    private FwPropertiesName(final String value) {
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
      for (final FwPropertiesName fwName : values()) {
        if (fwName.toString().equalsIgnoreCase(name)) {
          return true;
        }
      }
      return false;
    }
  }

  /** ローカルホスト名. */
  public static final String LOCALHOST_NAME = getLocalHostName();

  /**
   * アプリケーション配備ディレクトリパス.<br>
   * <ul>
   * <li>アプリケーション配備ディレクトリとは下記のいずれかのディレクトリを指す。
   * <ul>
   * <li>Javaクラスファイル配備ルートディレクトリ（com や jp など）の２つ上のディレクトリ <br>
   * ［例］appdeploy/classes/com/onepg/Program.class の場合、appdeploy</li>
   * <li>Jarファイル配備ディレクトリの２つ上のディレクトリ <br>
   * ［例］appdeploy/lib/program.jar の場合、appdeploy</li>
   * </ul>
   * </ul>
   */
  public static final String APPLICATION_DIR_PATH;
  /** 一時ディレクトリパス. */
  private static final String TEMPORARY_DIR_PATH;

  /** 設定ディレクトリパス. */
  private static final String PROP_STORAGE_DIR_PATH;

  /**
   * 機能設定.<br>
   * <ul>
   * <li>設定ディレクトリ配下の設定ファイルの設定値をマップで返す。</li>
   * <li>フレームワーク専用 設定ファイルは除外する。</li>
   * <li>設定ファイルの数は自由だが設定ファイルを跨いで設定キーは一意にする必要がある。</li>
   * </ul>
   * <pre>［例］<code>final String value = PropertiesUtil.MODULE_PROP_MAP.getString("module.unique.key");</code></pre>
   */
  public static final IoItems MODULE_PROP_MAP;

  static {
    // 処理順を明示的にするため static ブロック内で初期化を行う
    APPLICATION_DIR_PATH = getJavaClassParentPath();
    TEMPORARY_DIR_PATH = FileUtil.getOsTemporaryPath();
    PROP_STORAGE_DIR_PATH = getPropDir();
    MODULE_PROP_MAP = getModulePorps();
  }

  /**
   * 設定ディレクトリ取得.
   */
  private static String getPropDir() {
    // デフォルト設定ディレクトリパス
    final String defaultDirPath = FileUtil.joinPath(APPLICATION_DIR_PATH, DEFAULT_PROP_DIRNAME);
    // 設定ディレクトリ指定ファイルパス
    final String propDirFilePath = FileUtil.joinPath(defaultDirPath, FwPropertiesName.PROPDIR.toString());
    
    if (FileUtil.exists(propDirFilePath)) {
      // デフォルト設定
      final IoItems propMap = getPropertiesMap(propDirFilePath);
      // 設定ディレクトリパス
      final String path = propMap.getString(PROPDIR_PKEY);
      if (!FileUtil.exists(path)) {
        throw new RuntimeException("Configuration directory does not exist. " + LogUtil.joinKeyVal("path", path));
      }
      return path;
    } else {
      // 設定ディレクトリ指定ファイルが存在しない場合はデフォルト設定ディレクトリを設定ディレクトリパスとする。
      return defaultDirPath;
    }
  }

  /**
   * 機能設定取得.
   */
  private static IoItems getModulePorps() {
    final IoItems allPropMap = new IoItems();
    final List<String> fileList =
        FileUtil.getFileList(PROP_STORAGE_DIR_PATH, PROPERTIES_TYPEMARK, null, null, null);
    for (final String filePath : fileList) {
      if (FwPropertiesName.exists(FileUtil.getFileName(filePath))) {
        // フレームワーク専用 設定ファイルは除外
        continue;
      }
      final IoItems propMap = getPropertiesMap(filePath);
      allPropMap.putAll(propMap);
    }
    // 読取専用マップ
    return new IoItems(allPropMap, true);
  }

  /**
   * コンストラクタ.
   */
  private PropertiesUtil() {
    // 処理なし
  }

  /**
   * ローカルホスト名取得.
   *
   * @return ホスト名
   */
  private static String getLocalHostName() {
    try {
      final String hostName = InetAddress.getLocalHost().getHostName();
      return hostName;
    } catch (UnknownHostException e) {
      throw new RuntimeException("An exception occurred while getting the local host name. ", e);
    }
  }

  /**
   * フレームワーク設定取得.
   *
   * @param propFileName 設定ファイル名
   * @return 設定値マップ
   */
  public static IoItems getFrameworkProps(final FwPropertiesName propFileName) {
    if (!FwPropertiesName.exists(propFileName.toString())) {
      throw new RuntimeException("Not a framework-reserved properties file name. " + LogUtil.joinKeyVal("file", propFileName.toString()));
    }
    final String propFilePath = FileUtil.joinPath(PROP_STORAGE_DIR_PATH, propFileName.toString());
    if (!FileUtil.exists(propFilePath)) {
      throw new RuntimeException("Properties file does not exist. " + LogUtil.joinKeyVal("file", propFilePath));
    }
    final IoItems propMap = getPropertiesMap(propFilePath);
    return propMap;
  }

  /**
   * 設定値マップ取得.<br>
   * <ul>
   * <li>戻り値のマップは読取専用となっている。</li>
   * </ul>
   *
   * @param propFilePath 設定ファイルパス
   * @return 設定値マップ（読取専用）
   */
  private static IoItems getPropertiesMap(final String propFilePath) {
    // Properties インスタンス
    final Properties props = getPropertiesObj(propFilePath);
    final IoItems retMap = new IoItems();
    for (Map.Entry<Object, Object> ent : props.entrySet()) {
      final String key = (String) ent.getKey();
      final String val = (String) ent.getValue();
      if (retMap.containsKey(key)) {
        final String val2 = retMap.getString(key);
        // 設定キーの重複があればエラー
        throw new RuntimeException("Property key is duplicated. "
            + LogUtil.joinKeyVal("file", propFilePath, "key", key, "value1", val, "value2", val2));
      }
      final String convVal = convDirPath(convEnv(val, propFilePath, key));
      retMap.put(key, convVal);
    }
    return new IoItems(retMap, true);
  }

  /**
   * Properties インスタンス取得.
   *
   * @param propFilePath 設定ファイルパス
   * @return Properties インスタンス
   */
  private static Properties getPropertiesObj(final String propFilePath) {
    final File propFile = new File(propFilePath);
    if (!propFile.exists()) {
      throw new RuntimeException("Properties file does not exist. " + LogUtil.joinKeyVal("path", propFilePath));
    }
    final Properties props = new Properties();
    try (final FileInputStream fis = new FileInputStream(propFile);
        final InputStreamReader isr = new InputStreamReader(fis, PROP_FILE_CHAR_SET);) {
      props.load(isr);
    } catch (IOException e) {
      throw new RuntimeException("An exception occurred while reading the properties file. " + LogUtil.joinKeyVal("file", propFilePath), e);
    }
    return props;
  }

  /**
   * 設定値環境変数置換.<br>
   * <ul>
   * <li>設定値が ${ と } で囲われている場合は囲われている環境変数の値と置換する。（部分置換も可能） <br>
   * ただし、環境変数に存在しない場合はシステムエラーが発生する。</li>
   * </ul>
   *
   * @param value 設定値
   * @param filePath 設定ファイルパス（エラーログ用）
   * @param key 設定キー（エラーログ用）
   * @return 設定値
   */
  private static String convEnv(final String value, final String filePath, final String key) {
    if (!value.contains("${") || !value.contains("}")) {
      return value;
    }

    final Matcher mt = ENV_VAR_PATTERN.matcher(value);

    final StringBuilder sb = new StringBuilder();
    int end = 0;
    while (mt.find()) {
      // グループ1指定で ${} 括弧内の環境変数名のみ取得
      final String envKey = mt.group(1);  
      String envVal = System.getenv(envKey);
      if (ValUtil.isNull(envVal)) {
        throw new RuntimeException("Environment variable does not exist. " + LogUtil.joinKeyVal("file", filePath, "key", key, "envKey", envKey));
      }
      // Windowsはゼロバイトブランクで環境変数を宣言できず「""」で宣言するため、末端のダブルクォーテーションを除去する
      envVal = ValUtil.trimDq(envVal);
      final int start = mt.start();
      sb.append(value.substring(end, start));
      sb.append(envVal);
      end = mt.end();
    }
    sb.append(value.substring(end));
    return sb.toString();
  }

  /**
   * 設定値ディレクトリパス置換.<br>
   * <ul>
   * <li>設定値内の $ApplicationDirPath はアプリケーション配備ディレクトリパスと置換する。（部分置換も可能）<br>
   * アプリケーション配備ディレクトリの詳細は <code>#APPLICATION_DIR_PATH</code> 参照。</li>
   * <li>設定値内の $TemporaryDirPath は OS の一時ディレクトリパスと置換する。（部分置換も可能）<br>
   * 具体的には Java の java.io.tmpdir システムプロパティのパスを置換する。</li>
   * <li>&lt;ConvertAbsolutePath&gt; 始まりの設定値は絶対パス変換する。</li>
   * </ul>
   *
   * @param value 設定値
   * @return 置換設定値
   */
  private static String convDirPath(final String value) {
    String retVal = value;
    retVal = retVal.replace(REPLACE_APPLICATION_DIR_PATH, APPLICATION_DIR_PATH);
    retVal = retVal.replace(REPLACE_TEMPORARY_DIR_PATH, TEMPORARY_DIR_PATH);
    if (value.startsWith(CONVERT_ABSOLUTE_PATH)) {
      retVal = retVal.substring(CONVERT_ABSOLUTE_PATH.length());
      retVal = FileUtil.convAbsolutePath(retVal);
    }
    return retVal;
  }
  
  /**
   * Javaクラスファイル親ディレクトリパス取得.<br>
   * <ul>
   * <li>Javaクラスファイル配備ディレクトリの１つ上のディレクトリパスを返す。<br>
   * またはJarファイル配備ディレクトリの１つ上のディレクトリパスを返す。</li>
   * </ul>
   *
   * @return バイナリファイル親ディレクトリパス
   * @throws IllegalStateException クラスファイルのパス取得に失敗した場合
   */
  private static String getJavaClassParentPath() {
    final ProtectionDomain pd = FileUtil.class.getProtectionDomain();
    final CodeSource cs = pd.getCodeSource();
    final URL location = cs.getLocation();
    File file;
    try {
      file = new File(location.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException("An exception occurred while getting the parent directory path of the running Java class file. "
          + LogUtil.joinKeyVal("location", location.toString()), e);
    }

    final String rootPath;
    if (file.isDirectory()) {
      // クラスファイルの場合
      rootPath = file.getParent();
    } else {
      // jarファイルの場合
      rootPath = file.getParentFile().getParent();
    }
    return rootPath;
  }

  /**
   * MS-WindowsOS判断.
   *
   * @return WindowsOSの場合は <code>true</code>
   */
  public static boolean isWindowsOs() {
    return ValUtil.nvl(System.getProperty("os.name")).toLowerCase().startsWith("windows");
  }

}
