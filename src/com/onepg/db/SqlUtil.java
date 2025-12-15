package com.onepg.db;

import com.onepg.db.DbUtil.DbmsName;
import com.onepg.util.AbstractIoTypeMap;
import com.onepg.util.IoItems;
import com.onepg.util.IoRows;
import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.onepg.util.ValUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL実行ユーティリティクラス.
 */
public final class SqlUtil {

  /** ログライター. */
  private static final LogWriter logger = LogUtil.newLogWriter(SqlUtil.class);

  /** デフォルトフェッチサイズ. */
  private static final int DEFAULT_FETCH_SIZE = 500;

  /** 日時フォーマッター：日付 SQL標準. */
  private static final DateTimeFormatter DTF_SQL_DATE =
      DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
  /** 日時フォーマッター：タイムスタンプ SQL標準. */
  private static final DateTimeFormatter DTF_SQL_TIMESTAMP =
      DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSSSS").withResolverStyle(ResolverStyle.STRICT);

  /**
   * DB項目クラスタイプ.<br>
   * <ul>
   * <li>DB項目の型に対応する Java の変数クラスを示す。</li>
   * <li>数値の型は BigDecimal に統一する。</li>
   * <li>StringToDateCls と StringToTsCls は SQLLite 用で文字列から日付/タイムスタンプへの変換を行う。</li>
   * </ul>
   */
  enum ItemClsType {
    StringCls, BigDecCls, DateCls, TsCls, StringToDateCls, StringToTsCls
  }

  /**
   * コンストラクタ.
   */
  private SqlUtil() {
    // 処理なし
  }

  /**
   * １件取得（ゼロ件エラー）.<br>
   * <ul>
   * <li>対象データが１件のみ存在することが前提で使用する。</li>
   * <li>結果がゼロ件の場合は例外エラーを投げる。</li>
   * <li>結果が複数件の場合は例外エラーを投げる。</li>
   * </ul>
   *
   * @param conn DB接続
   * @param sqlBuilder SQLビルダー
   * @return 行データマップ
   */
  public static IoItems selectOneExists(final Connection conn, final SqlBuilder sqlBuilder) {
    final IoItems retMap = selectFirstRec(conn, sqlBuilder, false);
    if (ValUtil.isNull(retMap)) {
      throw new RuntimeException("No matching data exists. " + sqlBuilder.toString());
    }
    return retMap;
  }

  /**
   * １件取得.<br>
   * <ul>
   * <li>対象データが１件のみ存在することが前提で使用する。</li>
   * <li>結果がゼロ件の場合は <code>null</code> を返す。</li>
   * <li>結果が複数件の場合は例外エラーを投げる。</li>
   * <li>項目物理名は英字小文字となる。（<code>AbstractIoTypeMap</code> のキールール）</li>
   * </ul>
   *
   * @param conn       DB接続
   * @param sqlBuilder SQLビルダー
   * @return 行データマップ
   */
  public static IoItems selectOne(final Connection conn, final SqlBuilder sqlBuilder) {
    return selectFirstRec(conn, sqlBuilder, false);
  }

  /**
   * １件取得（複数存在OK）.<br>
   * <ul>
   * <li>結果がゼロ件の場合は <code>null</code> を返す。</li>
   * <li>複数件取得できた場合でもエラーとせず最初の１件を返す。</li>
   * <li>項目物理名は英字小文字となる。（<code>AbstractIoTypeMap</code> のキールール）</li>
   * </ul>
   *
   * @param conn       DB接続
   * @param sqlBuilder SQLビルダー
   * @return 行データマップ
   */
  public static IoItems selectOneMultiIgnore(final Connection conn, final SqlBuilder sqlBuilder) {
    return selectFirstRec(conn, sqlBuilder, true);
  }

  /**
   * 最初の１件取得.<br>
   * <ul>
   * <li>結果がゼロ件の場合は <code>null</code> を返す。</li>
   * <li>複数件取得できた場合でもエラーとしない場合は multiDataIgnore 引数に <code>true</code> を渡す。<br>
   * 複数件取得できた場合は最初の１件を返す。</li>
   * <li>multiDataIgnore 引数が <code>false</code> を渡し、かつ複数件取得できた場合は例外エラーを投げる。</li>
   * <li>項目物理名は英字小文字となる。（<code>AbstractIoTypeMap</code> のキールール）</li>
   * </ul>
   *
   * @param conn            DB接続
   * @param sqlBuilder      SQLビルダー
   * @param multiDataIgnore 複数件取得できた場合でもエラーとしない場合は <code>true</code>
   * @return 行データマップ（<code>null</code> 有り）
   */
  private static IoItems selectFirstRec(final Connection conn, final SqlBuilder sqlBuilder,
      final boolean multiDataIgnore) {

    // 一括取得
    final IoRows rows = selectBulkByLimitCount(conn, sqlBuilder, 1);
    if (rows.size() <= 0) {
      // データなし
      return null;
    }

    if (rows.isLimitOver()) {
      // １行以上取得
      if (!multiDataIgnore) {
        throw new RuntimeException("Multiple records were retrieved. " + sqlBuilder.toString());
      }
    }
    return rows.get(0);
  }

  /**
   * 複数件取得.<br>
   * <ul>
   * <li><code>SqlResultSet</code> で返す。</li>
   * <li><code>SqlResultSet</code>
   * のイテレーターから取得した行マップの項目物理名は英字小文字となる。（<code>AbstractIoTypeMap</code> のキールール）</li>
   * <li>try 句（try-with-resources文）で使用する。</li>
   * <li>本クラスではデフォルトフェッチサイズを 500 とし、全件フェッチしたい場合は <code>SqlBuilder#fetchAll()</code>
   * を実行しておく。</li>
   * <li>DBMSごとのフェッチサイズについて
   * <ul>
   * <li>Oralce はデフォルト 10 件となっており小さいため、フェッチサイズを指定する。</li>
   * <li>PostgreSQL はデフォルト 全件フェッチとなっており OutOfMemory になる可能性があるためフェッチサイズを指定する。</li>
   * <li>PostgreSQL
   * はフェッチサイズ指定し（全件フェッチせず）、かつ取得データを更新し、かつ中間コミットするとカーソル無効エラー（SQLSTATE
   * 34000）が発生するので、その場合は中間コミットをやめるか全件フェッチする必要がある。（<code>SqlUtil#selectFetchAll(Connection, SqlBuilder)</code> 参照）<br>
   * また処理が複雑になるが SQL の LIMIT句でデータを分割取得しても解決できる。</li>
   * <li>MS-SqlServer ではフェッチサイズを指定してもその通りにはならない場合があるため、OutOfMemory になる可能性がある場合は
   * SQL の LIMIT句でデータを分割取得する必要がある。</li>
   * </ul>
   * </li>
   * </ul>
   * <pre>［例］
   * <code>try (final SqlResultSet rSet = SqlUtil.select(getDbConn(), sqlBuilder);) {
   *   for (final IoItems row : rSet) {
   *     ：省略
   *   }
   *   if (rSet.getReadedCount() <= 0) {
   *     // ゼロ件の場合
   *   }
   * }</code>
   * </pre>
   *
   * @param conn       DB接続
   * @param sqlBuilder SQLビルダー
   * @return SQL結果セット
   */
  public static SqlResultSet select(final Connection conn, final SqlBuilder sqlBuilder) {
    return selectByFetchSize(conn, sqlBuilder, DEFAULT_FETCH_SIZE);
  }

  /**
   * 複数件取得（全件フェッチ）.<br>
   * <ul>
   * <li>基本的に本メソッドは使用しない。</li>
   * <li>全件フェッチしないと不具合が発生する場合のみ使用する。</li>
   * <li>本メソッドで大量件数取得するとメモリエラーが発生する可能性がある。</li>
   * </ul>
   * 
   * @see #select(Connection, SqlBuilder)
   * @param conn       DB接続
   * @param sqlBuilder SQLビルダー
   * @return SQL結果セット
   */
  public static SqlResultSet selectFetchAll(final Connection conn, final SqlBuilder sqlBuilder) {
    return selectByFetchSize(conn, sqlBuilder, 0);
  }

  /**
   * 複数件一括取得.<br>
   * <ul>
   * <li>複数行リストを返す。</li>
   * <li>結果がゼロ件の場合はサイズゼロのリストを返す。</li>
   * <li>１行のマップの項目物理名は英字小文字となる。（<code>AbstractIoTypeMap</code> のキールール）</li>
   * <li>本メソッドはメモリを消費するのでループ処理する場合は <code>#select(Connection, SqlBuilder)</code>
   * を使用する。</li>
   * <li>本メソッドで大量件数取得するとメモリエラーが発生する可能性がある。</li>
   * </ul>
   *
   * @param conn       DB接続
   * @param sqlBuilder SQLビルダー
   * @param limitCount 取得件数上限
   * @return 複数行リスト
   */
  public static IoRows selectBulk(final Connection conn, final SqlBuilder sqlBuilder,
      final int limitCount) {
    return selectBulkByLimitCount(conn, sqlBuilder, limitCount);
  }

  /**
   * 複数件一括取得（全件取得）.<br>
   * <ul>
   * <li>複数行リストを返す。</li>
   * <li>結果がゼロ件の場合はサイズゼロのリストを返す。</li>
   * <li>１行のマップの項目物理名は英字小文字となる。（<code>AbstractIoTypeMap</code> のキールール）</li>
   * <li>本メソッドはメモリを消費するのでループ処理する場合は <code>#select(Connection, SqlBuilder)</code>
   * を使用する。</li>
   * <li>本メソッドで大量件数取得するとメモリエラーが発生する可能性がある。</li>
   * </ul>
   *
   * @param conn       DB接続
   * @param sqlBuilder SQLビルダー
   * @return 複数行リスト
   */
  public static IoRows selectBulkAll(final Connection conn, final SqlBuilder sqlBuilder) {
    return selectBulkByLimitCount(conn, sqlBuilder, 0);
  }

  /**
   * 複数件一括取得.
   * 
   * @param conn DB接続
   * @param sqlBuilder SQLビルダー
   * @param limitCount 取得件数上限（ゼロ以下の場合は全件取得）
   * @return 複数行リスト
   */
  private static IoRows selectBulkByLimitCount(final Connection conn, final SqlBuilder sqlBuilder,
      final int limitCount) {
    // フェッチサイズ
    final int fetchSize;
    if (limitCount <= 0 || DEFAULT_FETCH_SIZE < limitCount) {
      // 少しでもメモリ使用量を減らすため
      // 全件やデフォルトフェッチサイズ超えの場合はデフォルトフェッチサイズを使用
      fetchSize = DEFAULT_FETCH_SIZE;
    } else {
      // 制限超え判定用に +1 する
      fetchSize = limitCount + 1;
    }

    final IoRows rows = new IoRows();
    try (final SqlResultSet rSet = SqlUtil.selectByFetchSize(conn, sqlBuilder, fetchSize);) {
      final Iterator<IoItems> ite = rSet.iterator();
      while (ite.hasNext()) {
        final IoItems row = ite.next();
        rows.add(row);
        if (limitCount > 0 && rows.size() >= limitCount) {
          // 制限件数に達したため終了
          if (ite.hasNext()) {
            // まだデータがある = 制限超え
            rows.setLimitOver(true);
          }
          break;
        }
      }
      if (rSet.getReadedCount() > 0) {
        // ゼロ件以外の場合は始端行番号と終端行番号をセット
        rows.setBeginRowNo(1);
        rows.setEndRowNo(rSet.getReadedCount());
      }
    }
    return rows;
  }

  /**
   * フェッチサイズ指定複数件取得.
   *
   * @param conn DB接続
   * @param sqlBuilder SQLビルダー
   * @param fetchSize フェッチサイズ
   * @return SQL結果セット
   */
  private static SqlResultSet selectByFetchSize(final Connection conn, final SqlBuilder sqlBuilder,
      final int fetchSize) {
        
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final String sql = sqlBuilder.getSql();
    final List<Object> params = sqlBuilder.getParameters();
    if (logger.isDevelopMode()) {
      // SQLログ出力
      logger.develop("SQL#SELECT execution. " + LogUtil.joinKeyVal("sql", sqlBuilder, "fetchSize", fetchSize));
    }

    try {
      // ステートメント生成
      final PreparedStatement stmt = conn.prepareStatement(sql);
      // ステートメントにパラメーターセット
      setStmtParameters(stmt, params, dbmsName);
      // ステートメントにフェッチ関連プロパティをセット
      setStmtFetchProperty(stmt, fetchSize);

      // SQL実行
      final ResultSet rset = stmt.executeQuery();
      // DB項目名・クラスタイプマップ
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMap(rset);

      // 接続シリアルコード
      final String serialCode = DbUtil.getSerialCode(conn);

      // SQL結果セット
      final SqlResultSet retSet = new SqlResultSet(stmt, rset, itemClsMap, serialCode);
      return retSet;

    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data retrieval. " + LogUtil.joinKeyVal("sql",
          sqlBuilder, "fetchSize", fetchSize), e);
    }
  }

  /**
   * テーブル指定１件登録.<br>
   * <ul>
   * <li>テーブル名を指定して１件登録します。</li>
   * <li>テーブルに存在しないパラメーターは無視されます。</li>
   * <li>実装完了後にテーブルに項目が追加され、その項目名が元からパラメーターに存在する場合は<br>
   * 実装修正無しで追加項目に値が登録されるので注意が必要です。</li>
   * <li>一意制約違反以外で反映件数がゼロ件の場合は例外エラーを投げる。</li>
   * </ul>
   *
   * @param conn      DB接続
   * @param tableName テーブル名
   * @param params    パラメーター値
   * @return 一意制約違反の場合は <code>false</code>、正常に１件登録された場合は <code>true</code>
   */
  public static boolean insertOne(final Connection conn, final String tableName, final AbstractIoTypeMap params) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final SqlBuilder sbInto = new SqlBuilder();
    final SqlBuilder sbVals = new SqlBuilder();
    try {
      // DB項目名・クラスタイプマップ
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sbInto.addQuery("INSERT INTO ").addQuery(tableName);

      sbInto.addQuery(" ( ");
      sbVals.addQuery(" ( ");
      for (final String itemName : params.keySet()) {
        if (!itemClsMap.containsKey(itemName)) {
          // テーブルに存在しないパラメーターはスキップ
          continue;
        }

        // 項目クラスタイプ
        final ItemClsType itemCls = itemClsMap.get(itemName);
        // 項目クラスタイプごとの値取得
        final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);

        // SQL追加
        sbInto.addQuery(itemName).addQuery(",");
        sbVals.addQuery("?,", param);
      }

      // SQL組み立て
      sbInto.deleteLastChar(1);
      sbVals.deleteLastChar(1);
      sbInto.addQuery(" ) VALUES ");
      sbVals.addQuery(" ) ");
      sbInto.addSqlBuilder(sbVals);

    } catch (final SQLException e) {
      throw new RuntimeException("Exception error occurred during data insert SQL generation. "
          + LogUtil.joinKeyVal("tableName", tableName, "params", params), e);
    }

    try{
      // SQL実行
      final int ret = executeSql(conn, sbInto);
      if (ret != 1) {
        throw new RuntimeException("Failed to insert data. " + LogUtil.joinKeyVal("sql",
            sbInto));
      }
      return true;

    } catch (final SQLException e) {
      if (isUniqueKeyErr(e, dbmsName)) {
        // 一意制約違反エラー
        return false;
      }
      throw new RuntimeException("Exception error occurred during data insert. " + LogUtil.joinKeyVal("sql",
          sbInto), e);
    }
  }

  /**
   * テーブル指定１件登録（タイムスタンプ自動セット）.<br>
   * <ul>
   * <li>テーブル名を指定して１件登録します。</li>
   * <li>楽観排他制御用のタイムスタンプをセットします。</li>
   * <li>テーブルに存在しないパラメーターは無視されます。</li>
   * <li>実装完了後にテーブルに項目が追加され、その項目名が元からパラメーターに存在する場合は<br>
   * 実装修正無しで追加項目に値が登録されるので注意が必要です。</li>
   * <li>一意制約違反以外で反映件数がゼロ件の場合は例外エラーを投げる。</li>
   * </ul>
   *
   * @param conn      DB接続
   * @param tableName テーブル名
   * @param params    パラメーター値
   * @param tsItem    タイムスタンプ項目名（楽観排他制御用）
   * @return 一意制約違反の場合は <code>false</code>、正常に１件登録された場合は <code>true</code>
   */
  public static boolean insertOne(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String tsItem) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final String curTs = getCurrentTimestampSql(dbmsName);

    final SqlBuilder sbInto = new SqlBuilder();
    final SqlBuilder sbVals = new SqlBuilder();
    try {
      // DB項目名・クラスタイプマップ
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sbInto.addQuery("INSERT INTO ").addQuery(tableName);

      sbInto.addQuery(" ( ");
      sbVals.addQuery(" ( ");
      for (final String itemName : params.keySet()) {
        if (!itemClsMap.containsKey(itemName)) {
          // テーブルに存在しないパラメーターはスキップ
          continue;
        }
        if (tsItem.equals(itemName)) {
          // タイムスタンプはスキップ
          continue;
        }

        // 項目クラスタイプ
        final ItemClsType itemCls = itemClsMap.get(itemName);
        // 項目クラスタイプごとの値取得
        final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);

        // SQL追加
        sbInto.addQuery(itemName).addQuery(",");
        sbVals.addQuery("?,", param);
      }
      // タイムスタンプSQL追加
      sbInto.addQuery(tsItem);
      sbVals.addQuery(curTs);

      // SQL組み立て
      sbInto.addQuery(" ) VALUES ");
      sbVals.addQuery(" ) ");
      sbInto.addSqlBuilder(sbVals);

    } catch (final SQLException e) {
      throw new RuntimeException("Exception error occurred during data insert SQL generation. "
          + LogUtil.joinKeyVal("tableName", tableName, "params", params), e);
    }

    try {
      // SQL実行
      final int ret = executeSql(conn, sbInto);
      if (ret != 1) {
        throw new RuntimeException("Failed to insert data. " + LogUtil.joinKeyVal("sql",
            sbInto));
      }
      return true;

    } catch (final SQLException e) {
      if (isUniqueKeyErr(e, dbmsName)) {
        // 一意制約違反エラー
        return false;
      }
      throw new RuntimeException("Exception error occurred during data insert. " + LogUtil.joinKeyVal("sql",
          sbInto), e);
    }
  }

  /**
   * テーブル指定1件更新.<br>
   * <ul>
   * <li>テーブル名を指定して1件更新します。</li>
   * <li>複数件更新した場合は例外エラーとする。</li>
   * <li>テーブルに存在しないパラメーターは無視されます。</li>
   * <li>実装完了後にテーブルに項目が追加され、その項目名が元からパラメーターに存在する場合は<br>
   * 実装修正無しで追加項目に値が更新されるので注意が必要です。</li>
   * <li>キー項目で WHERE句が作成される。</li>
   * <li>キー項目はパラメーター値に含まれている必要があります。</li>
   * <li>キー項目名の英字は小文字で指定する必要があります。（<code>AbstractIoTypeMap</code> のキールール）</li>
   * </ul>
   *
   * @param conn      DB接続
   * @param tableName テーブル名
   * @param params    パラメーター値（キー項目名を含む）
   * @param keyItems  キー項目名
   *
   * @return １件更新された場合は <code>true</code>、０件の場合は <code>false</code>
   */
  public static boolean updateOne(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] keyItems) {
    if (ValUtil.isEmpty(keyItems)) {
      throw new RuntimeException("Key column names are required. ");
    }
    final int ret = update(conn, tableName, params, keyItems);
    if (ret > 1) {
      throw new RuntimeException("Multiple records were updated. " + LogUtil.joinKeyVal("tableName", tableName,
          "keyItems", keyItems, "params", params));
    }
    return (ret == 1);
  }

  /**
   * テーブル指定１件更新（タイムスタンプ排他制御更新）.<br>
   * <ul>
   * <li>テーブル名を指定して１件更新します。</li>
   * <li>複数件更新した場合は例外エラーとする。</li>
   * <li>タイムスタンプで楽観排他制御を行います。</li>
   * <li>テーブルに存在しないパラメーターは無視されます。</li>
   * <li>実装完了後にテーブルに項目が追加され、その項目名が元からパラメーターに存在する場合は<br>
   * 実装修正無しで追加項目に値が更新されるので注意が必要です。</li>
   * <li>キー項目とタイムスタンプ項目（排他制御）で WHERE句が作成される。</li>
   * <li>キー項目とタイムスタンプ項目はパラメーター値に含まれている必要があります。</li>
   * <li>キー項目名とタイムスタンプ項目名の英字は小文字で指定する必要があります。（<code>AbstractIoTypeMap</code>
   * のキールール）</li>
   * <li>タイムスタンプ項目は現在日時で更新されます。</li>
   * <li>タイムスタンプ排他制御不要な場合は <code>#updateOne(Connection, String, AbstractIoTypeMap, String[])</code> を使用してください。</li>
   * </ul>
   *
   * @param conn      DB接続
   * @param tableName テーブル名
   * @param params    パラメーター値（キー項目名を含む）
   * @param keyItems  キー項目名
   * @param tsItem    タイムスタンプ項目名（楽観排他制御用）
   *
   * @return １件更新された場合は <code>true</code>、０件の場合は <code>false</code>
   */
  public static boolean updateOne(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] keyItems, final String tsItem) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }
    if (ValUtil.isEmpty(keyItems)) {
      throw new RuntimeException("Key column names are required. ");
    }
    if (ValUtil.isBlank(tsItem)) {
      throw new RuntimeException("Timestamp column name is required. ");
    }
    
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final String curTs = getCurrentTimestampSql(dbmsName);
    
    final SqlBuilder sb = new SqlBuilder();
    try {
      // DB項目名・クラスタイプマップ
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      final String[] whereItems = Arrays.copyOf(keyItems, keyItems.length + 1);
      whereItems[keyItems.length] = tsItem;

      sb.addQuery("UPDATE ").addQuery(tableName);
      // SET句追加
      addSetQuery(sb, params, whereItems, itemClsMap);
      // タイムスタンプ項目は現在日時で更新
      sb.addQuery(",").addQuery(tsItem).addQuery("=").addQuery(curTs);
      // WHERE句追加
      addWhereQuery(sb, tableName, params, whereItems, itemClsMap);

    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update SQL generation. " + LogUtil.joinKeyVal("tableName", tableName,
          "keyItems", keyItems, "tsItem", tsItem, "params", params), e);
    }

    try {
      // SQL実行
      final int ret = executeSql(conn, sb);
      if (ret > 1) {
        throw new RuntimeException("Multiple records were updated. " + LogUtil.joinKeyVal("sql", sb));
      }
      return (ret == 1);
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * テーブル指定更新.<br>
   * <ul>
   * <li>テーブル名を指定して複数件更新します。</li>
   * <li>テーブルに存在しないパラメーターは無視されます。</li>
   * <li>実装完了後にテーブルに項目が追加され、その項目名が元からパラメーターに存在する場合は<br>
   * 実装修正無しで追加項目に値が更新されるので注意が必要です。</li>
   * <li>抽出条件項目で WHERE句が作成される。</li>
   * <li>抽出条件項目はパラメーター値に含まれている必要があります。</li>
   * <li>抽出条件項目名の英字は小文字で指定する必要があります。（<code>AbstractIoTypeMap</code> のキールール）</li>
   * </ul>
   *
   * @param conn       DB接続
   * @param tableName  テーブル名
   * @param params     パラメーター値（抽出条件項目名を含む）
   * @param whereItems 抽出条件項目名（省略可能）省略した場合は <code>null</code>
   *
   * @return 更新件数
   */
  public static int update(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] whereItems) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }

    final SqlBuilder sb = new SqlBuilder();
    try {
      // DB項目名・クラスタイプマップ
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sb.addQuery("UPDATE ").addQuery(tableName);
      // SET句追加
      addSetQuery(sb, params, whereItems, itemClsMap);
      // WHERE句追加
      addWhereQuery(sb, tableName, params, whereItems, itemClsMap);

    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update SQL generation. " + LogUtil.joinKeyVal("tableName", tableName,
          "whereItems", whereItems, "params", params), e);
    }

    try {
      // SQL実行
      final int ret = executeSql(conn, sb);
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * テーブル指定１件削除.<br>
   * <ul>
   * <li>テーブル名を指定して１件削除します。</li>
   * <li>複数件削除した場合は例外エラーとする。</li>
   * <li>キー項目で WHERE句が作成される。</li>
   * <li>キー項目はパラメーター値に含まれる必要があります。</li>
   * <li>キー項目名の英字は小文字で指定する必要があります。（<code>AbstractIoTypeMap</code> のキールール）</li>
   * </ul>
   *
   * @param conn      DB接続
   * @param tableName テーブル名
   * @param params    パラメーター値（キー項目名を含む）
   * @param keyItems  キー項目名
   *
   * @return １件削除された場合は <code>true</code>、０件の場合は <code>false</code>
   */
  public static boolean deleteOne(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] keyItems) {
    if (ValUtil.isEmpty(keyItems)) {
      throw new RuntimeException("Key column names are required. ");
    }
    final int ret = delete(conn, tableName, params, keyItems);
    if (ret > 1) {
      throw new RuntimeException("Multiple records were deleted. " + LogUtil.joinKeyVal("tableName", tableName,
          "keyItems", keyItems, "params", params));
    }
    return (ret == 1);
  }

  /**
   * テーブル指定１件削除（タイムスタンプ排他制御削除）.<br>
   * <ul>
   * <li>テーブル名を指定して１件削除します。</li>
   * <li>複数件削除した場合は例外エラーとする。</li>
   * <li>タイムスタンプで楽観排他制御を行います。</li>
   * <li>キー項目とタイムスタンプ項目（排他制御）で WHERE句が作成される。</li>
   * <li>キー項目とタイムスタンプ項目はパラメーター値に含まれている必要があります。</li>
   * <li>キー項目名とタイムスタンプ項目名の英字は小文字で指定する必要があります。（<code>AbstractIoTypeMap</code>
   * のキールール）</li>
   * <li>タイムスタンプ排他制御不要な場合は
   * <code>#deleteOne(Connection, String, AbstractIoTypeMap, String[])</code>
   * を使用してください。</li>
   * </ul>
   *
   * @param conn      DB接続
   * @param tableName テーブル名
   * @param params    パラメーター値（キー項目名を含む）
   * @param keyItems  キー項目名
   * @param tsItem    タイムスタンプ項目名（楽観排他制御用）
   *
   * @return １件削除された場合は <code>true</code>、０件の場合は <code>false</code>
   */
  public static boolean deleteOne(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] keyItems, final String tsItem) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }
    if (ValUtil.isEmpty(keyItems)) {
      throw new RuntimeException("Key column names are required. ");
    }
    if (ValUtil.isBlank(tsItem)) {
      throw new RuntimeException("Timestamp column name is required. ");
    }
    
    final String[] whereItems = Arrays.copyOf(keyItems, keyItems.length + 1);
    whereItems[keyItems.length] = tsItem;
    final int ret = delete(conn, tableName, params, whereItems);
    if (ret > 1) {
      throw new RuntimeException("Multiple records were deleted. " + LogUtil.joinKeyVal("tableName", tableName,
          "keyItems", keyItems, "tsItem", tsItem, "params", params));
    }
    return (ret == 1);
  }

  /**
   * テーブル指定削除.<br>
   * <ul>
   * <li>テーブル名を指定して複数件削除します。</li>
   * <li>抽出条件項目で WHERE句が作成される。</li>
   * <li>抽出条件項目はパラメーター値に含まれる必要があります。</li>
   * <li>抽出条件項目名の英字は小文字で指定する必要があります。（<code>AbstractIoTypeMap</code> のキールール）</li>
   * </ul>
   *
   * @param conn DB接続
   * @param tableName テーブル名
   * @param params パラメーター値（抽出条件項目名を含む）
   * @param whereItems 抽出条件項目名
   *
   * @return 削除件数
   */
  public static int delete(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] whereItems) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }

    final SqlBuilder sb = new SqlBuilder();
    try {
      // DB項目名・クラスタイプマップ
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sb.addQuery("DELETE FROM ").addQuery(tableName);
      // WHERE句追加
      addWhereQuery(sb, tableName, params, whereItems, itemClsMap);
      
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data delete SQL generation. " + LogUtil.joinKeyVal("tableName", tableName,
          "whereItems", whereItems, "params", params), e);
    }

    try {
      // SQL実行
      final int ret = executeSql(conn, sb);
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data delete. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * SET句追加.
   *
   * @param sb         SQLビルダー
   * @param params     パラメーター値（抽出条件項目名を含む）
   * @param whereItems 抽出条件項目名（省略可能）省略した場合は <code>null</code>
   * @param itemClsMap DB項目名・クラスタイプマップ
   */
  private static void addSetQuery(final SqlBuilder sb, final AbstractIoTypeMap params,
      final String[] whereItems, final Map<String, ItemClsType> itemClsMap) {

    // SET句の作成
    sb.addQuery(" SET ");

    // 存在確認用 抽出条件項目リスト
    final List<String> whereItemList;
    if (ValUtil.isEmpty(whereItems)) {
      whereItemList = Arrays.asList(new String[] {});
    } else {
      whereItemList = Arrays.asList(whereItems);
    }

    for (final String itemName : params.keySet()) {
      if (!itemClsMap.containsKey(itemName)) {
        // テーブルに存在しないパラメーターはスキップ
        continue;
      }
      if (whereItemList.contains(itemName)) {
        // 抽出条件項目に存在する場合はスキップ
        continue;
      }

      // 項目クラスタイプ
      final ItemClsType itemCls = itemClsMap.get(itemName);
      // 項目クラスタイプごとの値取得
      final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);
      // SQL追加
      sb.addQuery(itemName).addQuery("=?", param).addQuery(",");
    }
    sb.deleteLastChar(1);
  }

  /**
   * WHERE句追加.
   *
   * @param sb         SQLビルダー
   * @param tableName  テーブル名
   * @param params     パラメーター値（抽出条件項目名を含む）
   * @param whereItems 抽出条件項目名（省略可能）省略した場合は <code>null</code>
   * @param itemClsMap DB項目名・クラスタイプマップ
   */
  private static void addWhereQuery(final SqlBuilder sb, final String tableName,
      final AbstractIoTypeMap params, final String[] whereItems, final Map<String, ItemClsType> itemClsMap) {

    if (ValUtil.isEmpty(whereItems)) {
      return;
    }

    // WHERE句の作成
    sb.addQuery(" WHERE ");
    for (final String itemName : whereItems) {
      if (!itemClsMap.containsKey(itemName)) {
        // テーブルに存在しないパラメーターはスキップ
        continue;
      }
      if (!params.containsKey(itemName)) {
        // 抽出条件項目がパラメーターに存在しない場合はエラー
        throw new RuntimeException("Extraction condition field does not exist in parameters. " + LogUtil.joinKeyVal("tableName",
            tableName, "whereItemName", itemName, "params", params));
      }

      // 項目クラスタイプ
      final ItemClsType itemCls = itemClsMap.get(itemName);
      // 項目クラスタイプごとの値取得
      final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);
      // SQL追加
      sb.addQuery(itemName).addQuery("=?", param).addQuery(" AND ");
    }
    sb.deleteLastChar(4);
  }

  /**
   * SQL １件登録・更新・削除.<br>
   * <ul>
   * <li>反映件数が複数件の場合は例外エラーとする。</li>
   * </ul>
   *
   * @param conn       DB接続
   * @param sqlBuilder SQLビルダー
   * @return 反映件数が１件の場合は <code>true</code>、０件の場合は <code>false</code>
   */
  public static boolean executeOne(final Connection conn, final SqlBuilder sqlBuilder) {
    final int ret = execute(conn, sqlBuilder);
    if (ret > 1) {
      throw new RuntimeException("Multiple records were affected. " + LogUtil.joinKeyVal("sql", sqlBuilder));
    }
    return (ret == 1);
  }

  /**
   * SQL 登録・更新・削除.
   *
   * @param conn DB接続
   * @param sqlBuilder SQLビルダー
   * @return 反映件数
   */
  public static int execute(final Connection conn, final SqlBuilder sqlBuilder) {
    try {
      return executeSql(conn, sqlBuilder);
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during SQL execution. " + LogUtil.joinKeyVal("sql", sqlBuilder), e);
    }
  }

  /**
   * SQLビルダー実行.
   *
   * @param conn DB接続
   * @param sqlBuilder SQLビルダー
   * @return 反映件数
   * @throws SQLException SQL例外エラー
   */
  private static int executeSql(final Connection conn, final SqlBuilder sqlBuilder)
      throws SQLException {
        
    if (logger.isDevelopMode()) {
      // SQLログ出力
      logger.develop("SQL#EXECUTE execution. " + LogUtil.joinKeyVal("sql", sqlBuilder));
    }
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    // ステートメント生成
    try (final PreparedStatement stmt = conn.prepareStatement(sqlBuilder.getSql());) {
      // ステートメントにパラメーターセット
      setStmtParameters(stmt, sqlBuilder.getParameters(), dbmsName);
      // SQL実行
      final int ret = stmt.executeUpdate();
      return ret;
    }
  }

  /**
   * ステートメントにパラメーターセット.
   *
   * @param stmt     ステートメント
   * @param params   パラメーター
   * @param dbmsName DBMS名
   * @throws SQLException SQL例外エラー
   */
  private static void setStmtParameters(final PreparedStatement stmt, final List<Object> params,
      final DbmsName dbmsName) throws SQLException {
    int bindNo = 0;
    for (final Object param : params) {
      ++bindNo;
      if (dbmsName == DbmsName.SQLITE) {
        if (ValUtil.isNull(param)) {
          stmt.setObject(bindNo, param);
        } else if (param instanceof java.sql.Timestamp) {
          // java.sql.Timestamp の場合は String に変換してセット
          final java.sql.Timestamp ts = (java.sql.Timestamp) param;
          final LocalDateTime ldt = ts.toLocalDateTime();
          final String s = DTF_SQL_TIMESTAMP.format(ldt);
          stmt.setString(bindNo, s);
        } else if (param instanceof java.sql.Date) {
          // java.sql.Date の場合は String に変換してセット
          final java.sql.Date dt = (java.sql.Date) param;
          final LocalDate ld = dt.toLocalDate();
          final String s = DTF_SQL_DATE.format(ld);
          stmt.setString(bindNo, s);
        } else {
          stmt.setObject(bindNo, param);
        }
      } else {
        stmt.setObject(bindNo, param);
      }
    }
  }
  /**
   * ステートメントにフェッチ関連プロパティをセット.
   *
   * @param stmt ステートメント
   * @param fetchSize フェッチサイズ
   * @throws SQLException SQL例外エラー
   */
  private static void setStmtFetchProperty(final PreparedStatement stmt, final int fetchSize)
      throws SQLException {
    // フェッチ方向とフェッチサイズをセット
    stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
    stmt.setFetchSize(fetchSize);
  }

  /**
   * 結果セットからDB項目名・クラスタイプマップ作成.<br>
   * <ul>
   * <li>結果セットから項目名とクラスタイプのマップを作成する。</li>
   * <li>マップは項目順を保持する。</li>
   * <li>項目物理名は英字小文字に変換する。（<code>AbstractIoTypeMap</code> のキールールとあわせる）</li>
   * </ul>
   *
   * @param rset 結果セット
   * @return DB項目名・クラスタイプマップ
   * @throws SQLException SQL例外エラー
   */
  private static Map<String, ItemClsType> createItemNameClsMap(final ResultSet rset)
      throws SQLException {

    // DB項目名・クラスタイプマップ
    final Map<String, ItemClsType> itemClsMap = new LinkedHashMap<>();

    // DBMS名
    final DbmsName dbmsName = DbUtil.getDbmsName(rset);
    // 結果セットメタ情報
    final ResultSetMetaData rmeta = rset.getMetaData();
    // 列数
    final int itemCount = rmeta.getColumnCount();
    // 結果セット列のループ
    for (int c = 1; c <= itemCount; c++) {
      // 列名
      final String itemName;
      if (DbmsName.DB2 == dbmsName) {
        // DB2 の #getColumnName は別名でなく元の項目名を返すため
        itemName = rmeta.getColumnLabel(c).toLowerCase();
      } else {
        itemName = rmeta.getColumnName(c).toLowerCase();
      }
      // 型No
      final int typeNo = rmeta.getColumnType(c);
      // 型名
      // Oracle は DATE型も時刻を持っており TIMESTAMP と判断されるので型名で判断する必要がある。
      final String typeName = rmeta.getColumnTypeName(c).toUpperCase();
      // 項目クラスタイプ
      final ItemClsType itemCls = convItemClsType(typeNo, typeName, dbmsName);

      itemClsMap.put(itemName, itemCls);
    }
    return itemClsMap;
  }

  /**
   * テーブル指定でDB項目名・クラスタイプマップ作成.<br>
   * <ul>
   * <li>DBメタ情報から項目名とクラスタイプのマップを作成する。</li>
   * <li>マップは項目順を保持する。</li>
   * <li>項目物理名は英字小文字に変換する。（<code>AbstractIoTypeMap</code> のキールールとあわせる）</li>
   * </ul>
   *
   * @param conn      DB接続
   * @param tableName テーブル名
   * @throws SQLException SQL例外エラー
   */
  private static Map<String, ItemClsType> createItemNameClsMapByMeta(final Connection conn,
      final String tableName) throws SQLException {

    // DB項目名・クラスタイプマップ
    final Map<String, ItemClsType> itemClsMap = new LinkedHashMap<>();

    // DBMS名
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    // DBメタ情報
    final DatabaseMetaData cmeta = conn.getMetaData();
    // 列情報結果セット
    final ResultSet rset = cmeta.getColumns(null, null, tableName, null);

    while (rset.next()) {
      // 列名
      final String itemName = rset.getString("COLUMN_NAME").toLowerCase();
      // 型No
      final int typeNo = rset.getInt("DATA_TYPE");
      // 型名
      // Oracle は DATE型も時刻を持っており TIMESTAMP と判断されるので型名で判断する必要がある。
      final String typeName = rset.getString("TYPE_NAME");
      // 項目クラスタイプ
      final ItemClsType itemCls = convItemClsType(typeNo, typeName, dbmsName);

      itemClsMap.put(itemName, itemCls);
    }
    return itemClsMap;
  }

  /**
   * DB項目クラスタイプ変換.
   *
   * @param typeNo   型No
   * @param typeName 型名大文字
   * @param dbmsName DBMS名
   * @return クラスタイプ
   */
  private static ItemClsType convItemClsType(final int typeNo, final String typeName, final DbmsName dbmsName) {
    final ItemClsType itemCls;
    if (/* BigDecimal にマッピングされる JDBC型 */ Types.DECIMAL == typeNo || Types.NUMERIC == typeNo
        || /* Integer にマッピングされる JDBC型 */ Types.TINYINT == typeNo || Types.SMALLINT == typeNo
        || Types.INTEGER == typeNo || /* Long にマッピングされる JDBC型 */ Types.BIGINT == typeNo
        || /* Float にマッピングされる JDBC型 */ Types.FLOAT == typeNo || Types.REAL == typeNo
        || /* Double にマッピングされる JDBC型 */ Types.DOUBLE == typeNo) {
      // 数値の型は BigDecimal に統一する
      itemCls = ItemClsType.BigDecCls;
    } else if (Types.DATE == typeNo) {
      if ("DATETIME".equals(typeName) && DbmsName.MSSQL == dbmsName) {
        itemCls = ItemClsType.TsCls;
      } else if (DbmsName.SQLITE == dbmsName) {
        // SQLLite で Types.DATE が結果セットから返された場合は実際は文字列なので変換する必要がある（#createIoItemsFromResultSet 参照）
        itemCls = ItemClsType.StringToDateCls;
      } else {
        itemCls = ItemClsType.DateCls;
      }
    } else if (Types.TIMESTAMP == typeNo || Types.TIMESTAMP_WITH_TIMEZONE == typeNo) {
      if ("DATE".equals(typeName) && DbmsName.ORACLE == dbmsName) {
        itemCls = ItemClsType.DateCls;
      } else if (DbmsName.SQLITE == dbmsName) {
        // SQLLite で Types.TIMESTAMP が結果セットから返された場合は実際は文字列なので変換する必要がある（#createIoItemsFromResultSet 参照）
        itemCls = ItemClsType.StringToTsCls;
      } else {
        itemCls = ItemClsType.TsCls;
      }
    } else {
      if (DbmsName.SQLITE == dbmsName) {
        if ("DATE".equals(typeName)) {
          // SQLLite のテーブルメタ情報は Types.VARCHAR でタイプ名が "DATE" となる
          itemCls = ItemClsType.StringToDateCls;
        } else if ("TIMESTAMP".equals(typeName)) {
          // SQLLite のテーブルメタ情報は Types.VARCHAR でタイプ名が "TIMESTAMP" となる
          itemCls = ItemClsType.StringToTsCls;
        } else {
          itemCls = ItemClsType.StringCls;
        }
      } else {
        // 文字列の型は String に統一する
        itemCls = ItemClsType.StringCls;
      }
    }
    return itemCls;
  }

  /**
   * 結果セット行マップ作成.<br>
   * <ul>
   * <li>結果セットの現在行の値をマップで返す。</li>
   * </ul>
   *
   * @param rset 結果セット
   * @param itemClsMap DB項目名・クラスタイプマップ
   * @return 行マップ
   */
  static IoItems createIoItemsFromResultSet(final ResultSet rset,
      final Map<String, ItemClsType> itemClsMap) {

    // 行マップ
    final IoItems rowMap = new IoItems();

    // DB項目のループ
    for (final Map.Entry<String, ItemClsType> ent : itemClsMap.entrySet()) {
      // 項目名
      final String itemName = ent.getKey();
      try {
        // 項目クラスタイプ
        final ItemClsType itemCls = ent.getValue();
        // 項目クラスタイプごとの値セット
        if (ItemClsType.StringCls == itemCls) {
          final String value = rset.getString(itemName);
          rowMap.put(itemName, value);
        } else if (ItemClsType.BigDecCls == itemCls) {
          final BigDecimal value = rset.getBigDecimal(itemName);
          rowMap.put(itemName, value);
        } else if (ItemClsType.DateCls == itemCls) {
          final java.sql.Date value = rset.getDate(itemName);
          rowMap.put(itemName, value);
        } else if (ItemClsType.TsCls == itemCls) {
          final java.sql.Timestamp value = rset.getTimestamp(itemName);
          rowMap.put(itemName, value);
        } else if (ItemClsType.StringToDateCls == itemCls) {
          final String value = rset.getString(itemName);
          if (ValUtil.isBlank(value) || value.length() != 10) {
            rowMap.putNull(itemName);
            continue;
          }
          final LocalDate ld = LocalDate.parse(value, DTF_SQL_DATE);
          // 本来は Date でセットする必要があるが、IoItems 内で LocalDate に変換されてセットされるためそのままセット
          rowMap.put(itemName, ld);
        } else if (ItemClsType.StringToTsCls == itemCls) {
          final String value = rset.getString(itemName);
          if (ValUtil.isBlank(value)) {
            rowMap.putNull(itemName);
            continue;
          }
          final String ajustVal;
          final int len = value.length();
          if (len == 19) {
            // 小数秒が無い場合は .000000 を付与する（"uuuu-MM-dd HH:mm:ss"＝19文字）
            ajustVal = value + ".000000";
          } else if (19 < len && len < 26) {
            // 小数秒の桁数不足は 000000 を付与する（"uuuu-MM-dd HH:mm:ss.SSSSSS"＝26文字）
            ajustVal = ValUtil.substring(value + "000000", 0, 26);
          } else if (len == 26) {
            ajustVal = value;
          } else if (len > 26) {
            // 小数秒の桁数は 6桁で切る（"uuuu-MM-dd HH:mm:ss.SSSSSS"＝26文字）
            ajustVal = ValUtil.substring(value, 0, 26);
          } else {
            rowMap.putNull(itemName);
            continue;
          }
          final LocalDateTime ldt = LocalDateTime.parse(ajustVal, DTF_SQL_TIMESTAMP);
          // 本来は Timestamp でセットする必要があるが、IoItems 内で LocalDateTime に変換されてセットされるためそのままセット
          rowMap.put(itemName, ldt);
        } else {
          throw new RuntimeException("Item class type is invalid. "
              + LogUtil.joinKeyVal("itemName", itemName, "itemCls", itemCls.toString()));
        }
      } catch (final Exception e) {
        throw new RuntimeException("Exception error occurred while getting value from result set. "
                                + LogUtil.joinKeyVal("itemName", itemName), e);
      }
    }
    return rowMap;
  }

  /**
   * パラメーター値取得.<br>
   * <ul>
   * <li>DB項目クラスタイプによってパラメーターの getter を使い分けて値を取得して返す。</li>
   * </ul>
   *
   * @param params パラメーター
   * @param itemName 項目名
   * @param itemCls DB項目クラスタイプ
   * @return パラメーター値
   */
  private static Object getValueFromIoItemsByItemCls(final AbstractIoTypeMap params, final String itemName,
      final ItemClsType itemCls) {
    final Object param;
    if (ItemClsType.StringCls == itemCls) {
      param = params.getStringNullable(itemName);
    } else if (ItemClsType.BigDecCls == itemCls) {
      param = params.getBigDecimalNullable(itemName);
    } else if (ItemClsType.DateCls == itemCls) {
      param = params.getSqlDateNullable(itemName);
    } else if (ItemClsType.TsCls == itemCls) {
      param = params.getSqlTimestampNullable(itemName);
    } else if (ItemClsType.StringToDateCls == itemCls) {
      final LocalDate ld = params.getDateNullable(itemName);
      if (ValUtil.isNull(ld)) {
        param = null;
      } else {
        param = ld.format(DTF_SQL_DATE);
      }
    } else if (ItemClsType.StringToTsCls == itemCls) {
      final LocalDateTime ldt = params.getDateTimeNullable(itemName);
      if (ValUtil.isNull(ldt)) {
        param = null;
      } else {
        param = ldt.format(DTF_SQL_TIMESTAMP);
      }
    } else {
      throw new RuntimeException("Item class type is invalid. "
          + LogUtil.joinKeyVal("itemName", itemName, "itemCls", itemCls.toString()));
    }
    return param;
  }

  /**
   * 一意制約違反エラー判定.<br>
   * <ul>
   * <li>DBMS別に一意制約違反エラーかどうかを判定する。</li>
   * <li>一意制約違反の場合は <code>true</code> を返す。</li>
   * </ul>
   *
   * @param e SQL例外
   * @param dbmsName DBMS名
   * @return 一意制約違反エラーの場合は <code>true</code>
   */
  private static boolean isUniqueKeyErr(final SQLException e, final DbmsName dbmsName) {
    // Oracle 一意制約違反エラー判定
    if (dbmsName == DbmsName.ORACLE && e.getErrorCode() == 1) {
      return true;
    }
    // PostgreSQL 一意制約違反エラー判定
    if (dbmsName == DbmsName.POSTGRESQL && "23505".equals(e.getSQLState())) {
      return true;
    } 
    // MS-SqlServer 一意制約違反エラー判定
    if (dbmsName == DbmsName.MSSQL && "23000".equals(e.getSQLState())
        && e.getMessage().contains("Violation of UNIQUE KEY constraint")) {
      return true;
    }
    // SQLite 一意制約違反エラー判定
    if (dbmsName == DbmsName.SQLITE && e.getErrorCode() == 19
        && e.getMessage().contains("UNIQUE constraint failed")) {
      return true;
    }
    // DB2 一意制約違反エラー判定
    if (dbmsName == DbmsName.DB2 && "23505".equals(e.getSQLState())) {
      return true;
    }

    return false;
  }

  /** タイムスタンプ取得SQL（小数秒6桁） マップ. */
  private static final Map<DbmsName, String> SQL_CUR_TS = new HashMap<>();
  /** タイムスタンプ取得SQL（小数秒6桁） その他. */
  private static final String SQL_CUR_TS_OTHER;
  static {
    // SQLLite は小数秒3桁しか取得できないため、000を付与して6桁にする
    SQL_CUR_TS.put(DbmsName.SQLITE, "strftime('%Y-%m-%d %H:%M:%f000', 'now', 'localtime')");
    SQL_CUR_TS.put(DbmsName.MSSQL, "SYSDATETIME()");
    SQL_CUR_TS_OTHER = "CURRENT_TIMESTAMP(6)";
  }

  /**
   * DBMS別 現在タイムスタンプ値取得SQL取得.
   *
   * @param dbmsName DBMS名
   * @return 現在タイムスタンプ値取得SQL
   */
  private static String getCurrentTimestampSql(final DbmsName dbmsName) {
    return SQL_CUR_TS.getOrDefault(dbmsName, SQL_CUR_TS_OTHER);
  }

  /** 日付取得SELECT文 マップ. */
  private static final Map<DbmsName, String> SQL_SELECT_TODAY = new HashMap<>();
  static {
    SQL_SELECT_TODAY.put(DbmsName.POSTGRESQL, "SELECT TO_CHAR(CURRENT_TIMESTAMP,'YYYYMMDD') day");
    SQL_SELECT_TODAY.put(DbmsName.ORACLE,    "SELECT TO_CHAR(CURRENT_TIMESTAMP,'YYYYMMDD') day FROM DUAL");
    SQL_SELECT_TODAY.put(DbmsName.MSSQL,     "SELECT CONVERT(VARCHAR, FORMAT(GETDATE(), 'yyyyMMdd')) day");
    SQL_SELECT_TODAY.put(DbmsName.SQLITE,    "SELECT strftime('%Y%m%d', 'now', 'localtime') day");
    SQL_SELECT_TODAY.put(DbmsName.DB2, "SELECT TO_CHAR(CURRENT_TIMESTAMP,'YYYYMMDD') day FROM SYSIBM.DUAL");
  }
  
  /**
   * DBMS別 現在日付取得.
   *
   * @param conn DB接続
   * @return 現在日付（YYYYMMDD形式）
   */
  public static String getToday(final Connection conn) {
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final String sql = SQL_SELECT_TODAY.get(dbmsName);
    if (ValUtil.isBlank(sql)) {
      throw new RuntimeException("Current date retrieval SQL is undefined for this DBMS. " + LogUtil.joinKeyVal("dbmsName", dbmsName));
    }
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery(sql);
    final IoItems ret = selectOne(conn, sb);
    return ret.getString("day");
  }

}
