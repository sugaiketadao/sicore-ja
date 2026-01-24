package com.onepg.db;

import com.onepg.util.LogUtil;
import com.onepg.util.PropertiesUtil;
import com.onepg.util.PropertiesUtil.FwPropertiesName;
import com.onepg.util.IoItems;
import com.onepg.util.ValUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * DBユーティリティクラス.
 */
public final class DbUtil {

  /** DBMS名. */
  enum DbmsName {
    /** PostgreSQL. */
    POSTGRESQL("PostgreSQL"),
    /** Oracle. */
    ORACLE("Oracle"),
    /** MS-SqlServer. */
    MSSQL("Microsoft SQL Server"),
    /** SQLite. */
    SQLITE("SQLite"),
    /** DB2. */
    DB2("DB2"),
    /** その他. */
    ETC("-");

    /** 製品名. */
    private final String productName;

    /**
     * コンストラクタ.
     *
     * @param value 製品名
     */
    private DbmsName(final String value) {
      this.productName = value;
    }

    @Override
    public String toString() {
      return this.productName;
    }
  }


  /** デフォルトDB接続名（.dbcon.url より前の部分）. */
  private static final String DEFAULT_CONN_NAME = "default";

  /** DB接続 - URL 接尾語. */
  private static final String PKEY_SUFFIX_URL = ".conn.url";
  /** DB接続 - ユーザー 接尾語. */
  private static final String PKEY_SUFFIX_USER = ".conn.user";
  /** DB接続 - パスワード 接尾語. */
  private static final String PKEY_SUFFIX_PASS = ".conn.pass";
  /** DB接続 - 最大接続数（プール数） 接尾語. */
  private static final String PKEY_SUFFIX_MAX = ".conn.max";

  /** DB設定. */
  private static final IoItems PROP_MAP;
  /** 警告出力SQL実行経過時間. */
  static final long SQL_EXEC_WARN_TIME_MSEC;

  static {
    // DB設定取得
    PROP_MAP = PropertiesUtil.getFrameworkProps(FwPropertiesName.DB);
    SQL_EXEC_WARN_TIME_MSEC = PROP_MAP.getLongOrDefault("sqlexec.warn.time", -1);
  }

  /**
   * 接続プール管理マップ＜DB接続名、接続プールマップ＜接続シリアルコード、DB接続＞＞（シングルトン）.<br>
   * <ul>
   * <li>複数の接続を同時に扱うことを考慮してDB接続名ごとにマップを保持する。</li>
   * <li>内部の接続プールマップはスレッドセーフなクラスを使用する。</li>
   * <li>接続シリアルコードは接続確立時に <code>DbConn</code> クラス内で発番される。</li>
   * </ul>
   */
  private static final Map<String, ConcurrentMap<String, Connection>> connPoolMaps_ =
      new HashMap<>();

  /**
   * 使用中接続管理マップ＜DB接続名、使用中接続リスト＜接続シリアルコード＞＞（シングルトン）.<br>
   * <ul>
   * <li>複数の接続を同時に扱うことを考慮してDB接続名ごとにリストを保持する。</li>
   * <li>内部の使用中接続リストはスレッドセーフなクラスを使用する。</li>
   * <li>接続シリアルコードは接続確立時に <code>DbConn</code> クラス内で発番される。</li>
   * </ul>
   */
  private static final Map<String, ConcurrentLinkedQueue<String>> connBusyLists_ = new HashMap<>();

  /**
   * コンストラクタ.
   */
  private DbUtil() {
    // 処理なし
  }

  /**
   * デフォルト DB接続取得.<br>
   * <ul>
   * <li>try 句（try-with-resources文）で宣言する。</li>
   * </ul>
   *
   * @return DB接続
   */
  public static Connection getConn() {
    return getConnByConfigName(DEFAULT_CONN_NAME);
  }

  /**
   * デフォルト DB接続取得.<br>
   * <ul>
   * <li>try 句（try-with-resources文）で宣言する。</li>
   * </ul>
   * 
   * @param traceCode トレースコード
   * @return DB接続
   */
  public static Connection getConn(final String traceCode) {
    return getConnByConfigName(DEFAULT_CONN_NAME, traceCode);
  }

  /**
   * DB接続名指定 DB接続取得.<br>
   * <ul>
   * <li>try 句（try-with-resources文）で宣言する。</li>
   * </ul>
   *
   * @param connName 設定ファイル上のDB接続名（.dbcon.url より前の部分）
   * @return DB接続
   */
  public static Connection getConnByConfigName(final String connName) {
    return getConnByConfigName(connName, null);
  }

  /**
   * DB接続名指定 DB接続取得.<br>
   * <ul>
   * <li>try 句（try-with-resources文）で宣言する。</li>
   * </ul>
   *
   * @param connName  設定ファイル上のDB接続名（.dbcon.url より前の部分）
   * @param traceCode トレースコード
   * @return DB接続
   */
  public static Connection getConnByConfigName(final String connName, final String traceCode) {
    // 接続シリアルコードを発番
    final String serialCode = createSerialCode(connName);
    // 新規接続
    final Connection conn = createConn(connName);
    // ラッピングして返す
    final DbConn dbConn = new DbConn(conn, serialCode, traceCode);
    return dbConn;
  }

  /**
   * デフォルト DB接続プーリング取得.<br>
   * <ul>
   * <li>プーリングされているDB接続を取得する。</li>
   * <li>try 句（try-with-resources文）で宣言する。</li>
   * </ul>
   *
   * @return DB接続
   */
  public static Connection getConnPooled() {
    return getConnPooledByConfigName(DEFAULT_CONN_NAME);
  }

  /**
   * デフォルト DB接続プーリング取得.<br>
   * <ul>
   * <li>プーリングされているDB接続を取得する。</li>
   * <li>try 句（try-with-resources文）で宣言する。</li>
   * </ul>
   *
   * @param traceCode トレースコード
   * @return DB接続
   */
  public static Connection getConnPooled(final String traceCode) {
    return getConnPooledByConfigName(DEFAULT_CONN_NAME, traceCode);
  }

  /**
   * DB接続名指定 DB接続プーリング取得.<br>
   * <ul>
   * <li>プーリングされているDB接続を取得する。</li>
   * <li>try 句（try-with-resources文）で宣言する。</li>
   * </ul>
   *
   * @param connName  設定ファイル上のDB接続名（.dbcon.url より前の部分）
   * @return DB接続
   */
  public static synchronized Connection getConnPooledByConfigName(final String connName) {
    return getConnPooledByConfigName(connName,  null);
  }

  /**
   * DB接続名指定 DB接続プーリング取得.<br>
   * <ul>
   * <li>プーリングされているDB接続を取得する。</li>
   * <li>try 句（try-with-resources文）で宣言する。</li>
   * </ul>
   *
   * @param connName  設定ファイル上のDB接続名（.dbcon.url より前の部分）
   * @param traceCode トレースコード
   * @return DB接続
   */
  public static synchronized Connection getConnPooledByConfigName(final String connName, final String traceCode) {

    // DB接続名の管理データがなければ作成する
    if (!connPoolMaps_.containsKey(connName)) {
      connPoolMaps_.put(connName, new ConcurrentHashMap<String, Connection>());
      connBusyLists_.put(connName, new ConcurrentLinkedQueue<String>());
    }

    // 接続プール（スレッドセーフ）
    final ConcurrentMap<String, Connection> connPoolMap = connPoolMaps_.get(connName);
    // 使用中接続（スレッドセーフ）
    final ConcurrentLinkedQueue<String> connBusyList = connBusyLists_.get(connName);

    // 使用中ではない接続を探して返す
    // 削除する可能性があるのでイテレーターを使う
    final Iterator<Map.Entry<String, Connection>> connIte = connPoolMap.entrySet().iterator();
    // 接続プールのループ
    while (connIte.hasNext()) {
      final Map.Entry<String, Connection> connEnt = connIte.next();
      // 接続シリアルコード
      final String serialCode = connEnt.getKey();
      if (!connBusyList.contains(serialCode)) {
        // 使用中接続リストに無い
        final Connection conn = connEnt.getValue();
        if (isClosedQuietly(conn)) {
          // 何らかの理由でDB切断されている場合はマップから削除して次を探す
          connPoolMap.remove(serialCode);
          continue;
        }
        // 接続シリアルコードを使用中接続リストに追加（削除はDB切断時に行われる）
        // インスタンス生成より前に追加することで、同一接続が複数のスレッドで使用されることを防止する
        connBusyList.add(serialCode);
        // ラッピングして返す
        final DbConnPooled dbConn = new DbConnPooled(conn, serialCode, connBusyList, traceCode);
        return dbConn;
      }
    }
    // 不使用の接続が無い場合かつ最大接続数に達していない場合は新規接続して返す。

    // 最大接続数（プール数）
    final int maxSize = PROP_MAP.getInt(connName + PKEY_SUFFIX_MAX);

    if (connPoolMap.size() >= maxSize) {
      throw new RuntimeException("Database connection limit reached. " + LogUtil.joinKeyVal("maxSize", maxSize));
    }

    // 接続シリアルコードを発番
    final String newSerialCode = createSerialCode(connName);
    // 新規接続確立
    final Connection conn = createConn(connName);
    // 接続プールに追加
    connPoolMap.put(newSerialCode, conn);
    // 接続シリアルコードを使用中接続リストに追加（削除はDB切断時に行われる）
    connBusyList.add(newSerialCode);
    // ラッピングして返す
    final DbConnPooled dbConn = new DbConnPooled(conn, newSerialCode, connBusyList, traceCode);
    return dbConn;
  }

  /**
   * プーリングDB切断.<br>
   * <ul>
   * <li>プーリングされているDB接続をすべて切断する。</li>
   * <li>使用中の接続も切断する。</li>
   * </ul>
   * 
   * @return 切断した場合は <code>true</code>
   */
  public static synchronized boolean closePooledConn() {
    boolean ret = false;
    // 削除するのでイテレーターを使う
    final Iterator<String> connNameIte = connPoolMaps_.keySet().iterator();
    // 接続プール管理マップのループ
    while (connNameIte.hasNext()) {
      final String connName = connNameIte.next();
      // 接続プール（スレッドセーフ）
      final ConcurrentMap<String, Connection> connPoolMap = connPoolMaps_.get(connName);
      // 使用中接続（スレッドセーフ）
      final ConcurrentLinkedQueue<String> connBusyList = connBusyLists_.get(connName);

      // 接続プールのループ
      for (final Map.Entry<String, Connection> connEnt : connPoolMap.entrySet()) {
        // 接続シリアルコード
        final String serialCode = connEnt.getKey();
        if (connBusyList.contains(serialCode)) {
          // 使用中接続の場合
          LogUtil.stdout("Warninng! Database connection is currently busy during close pooled connections. "
              + LogUtil.joinKeyVal("serialCode", serialCode));
        }
        // DB接続
        final Connection conn = connEnt.getValue();
        // ラッピングしてからDB切断（ログ出力を統一するため）
        @SuppressWarnings("resource")
        final DbConn dbConn = new DbConn(conn, serialCode);
        dbConn.rollbackCloseForce();
        ret = true;
      }
      // すべてDB切断できたら接続プール管理マップと使用中接続管理マップから削除
      connPoolMaps_.remove(connName);
      connBusyLists_.remove(connName);
    }
    return ret;
  }

  /**
   * DB接続確立.
   *
   * @param connName 設定ファイル上のDB名（.dbcon.url より前の部分）
   * @return DB接続
   */
  private static Connection createConn(final String connName) {
    if (!PROP_MAP.containsKey(connName + PKEY_SUFFIX_URL)) {
      throw new RuntimeException("Configuration does not exist. "  + LogUtil.joinKeyVal("ConnName", connName));
    }

    // DB URL
    final String url = PROP_MAP.getString(connName + PKEY_SUFFIX_URL);

    // DBユーザー
    final String user;
    // DBパスワード
    final String pass;
    if (PROP_MAP.containsKey(connName + PKEY_SUFFIX_USER)) {
      user = PROP_MAP.getString(connName + PKEY_SUFFIX_USER);
      pass = PROP_MAP.getString(connName + PKEY_SUFFIX_PASS);
    } else {
      user = null;
      pass = null;
    }

    try {
      final Connection conn = DriverManager.getConnection(url, user, pass);
      return conn;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during database connection. " 
                              + LogUtil.joinKeyVal("url", url, "user", user), e);
    }
  }

  /**
   * エラー無視DB切断確認.<br>
   * <ul>
   * <li>部品からは <code>#isClosed()</code> ではなくエラーをスローしない本メソッドを使用する。</li>
   * </ul>
   *
   * @param conn DB接続
   * @return DB切断されている場合は <code>true</code>（エラーの場合も <code>true</code>）
   */
  private static boolean isClosedQuietly(final Connection conn) {
    try {
      if (conn.isClosed()) {
        return true;
      }
    } catch (SQLException ignore) {
      // 例外エラー時は閉じられていると判断
      return true;
    }
    return false;
  }

  /**
   * シリアルコード発番.
   *
   * @param connName 設定ファイル上のDB接続名（.dbcon.url より前の部分）
   * @return シリアルコード
   */
  private static String createSerialCode(final String connName) {
    final String serialCode = ValUtil.getSequenceCode();
    return serialCode + "-" + connName;
  }

  /**
   * DB接続からシリアルコード取得.
   *
   * @param conn DB接続
   * @return シリアルコード
   */
  static String getSerialCode(final Connection conn) {
    if (conn instanceof DbConn) {
      return ((DbConn) conn).getSerialCode();
    }
    return "-";
  }

  /**
   * DB接続からDBMS名取得.
   *
   * @param conn DB接続
   * @return DBMS名
   */
  static DbmsName getDbmsName(final Connection conn) {
    try {
      // DB接続の製品名
      final String productName =
          ValUtil.nvl(conn.getMetaData().getDatabaseProductName()).toLowerCase();
      for (final DbmsName dbmsName : DbmsName.values()) {
        if (dbmsName.toString().toLowerCase().contains(productName)) {
          return dbmsName;
        }
      }
      return DbmsName.ETC;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred while getting DBMS name. ", e);
    }
  }

  /**
   * DBステートメントからDBMS名取得.
   *
   * @param stmt DBステートメント
   * @return DBMS名
   */
  static DbmsName getDbmsName(final Statement stmt) {
    try {
      return getDbmsName(stmt.getConnection());
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred while getting DBMS name. ", e);
    }
  }

  /**
   * DB結果セットからDBMS名取得.
   *
   * @param rset DB結果セット
   * @return DBMS名
   */
  static DbmsName getDbmsName(final ResultSet rset) {
    try {
      return getDbmsName(rset.getStatement());
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred while getting DBMS name. ", e);
    }
  }

  /**
   * DB接続設定名取得.
   * @return URLが設定されている接続名のリスト（デフォルト接続名も含まれる）
   */
  public static List<String> getConnNames() {
    final List<String> ret = new ArrayList<>();
    for (final String key : PROP_MAP.keySet()) {
      if (key.endsWith(PKEY_SUFFIX_URL)) {
        final String connName = key.substring(0, key.length() - PKEY_SUFFIX_URL.length());
        ret.add(connName);
      }
    }
    return ret;
  }

  /**
   * エラー無視プリペアードステートメントクローズ.
   *
   * @param stmt プリペアードステートメント
   */
  static void closeQuietly(final PreparedStatement stmt) {
    if (ValUtil.isNull(stmt)) {
      return;
    }
    try {
      if (stmt.isClosed()) {
        return;
      }
      stmt.close();
    } catch (SQLException ignore) {
      // 処理なし
    }
  }
  
  /**
   * エラー無視プリペアードステートメントクローズ.
   *
   * @param rset 結果セット
   */
  static void closeQuietly(final ResultSet rset) {
    if (ValUtil.isNull(rset)) {
      return;
    }
    try {
      if (rset.isClosed()) {
        return;
      }
      rset.close();
    } catch (SQLException ignore) {
      // 処理なし
    }
  }
}

