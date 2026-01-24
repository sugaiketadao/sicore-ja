# バッチ処理別コーディングパターン

<!-- AI_SKIP_START -->
サンプルプログラムに基づく、バッチ処理ごとの実装パターンである。
人間もAIもこのパターンに従えば、類似機能をすぐに作成できる。
<!-- AI_SKIP_END -->

---

## 目次

- [サンプルデータ構造](#サンプルデータ構造)
- [1. データエクスポート処理](#1-データエクスポート処理)
- [2. データインポート処理](#2-データインポート処理)
- [バリデーションパターン一覧](#バリデーションパターン一覧)
- [ファイル命名規則](#ファイル命名規則)
- [参考](#参考)

---

## サンプルデータ構造

### テーブル定義

**テーブル: t_user**
| 項目 | 物理名 | 型 | 備考 |
|------|--------|-----|------|
| ユーザーID | user_id | VARCHAR(4) | PK |
| ユーザー名 | user_nm | VARCHAR(20) | |
| Email | email | VARCHAR(50) | |
| 出身国 | country_cs | VARCHAR(2) | JP/US/BR/AU |
| 性別 | gender_cs | VARCHAR(1) | M/F |
| 配偶者 | spouse_cs | VARCHAR(1) | Y/N |
| 年収 | income_am | NUMERIC(10) | |
| 誕生日 | birth_dt | DATE | |
| 更新タイムスタンプ | upd_ts | TIMESTAMP(6) | ログ・排他制御用。 |

---

## 1. データエクスポート処理

**用途**: DBからデータを抽出し、CSVファイルに出力する。

<!-- AI_SKIP_START -->
**処理フロー**:
1. 引数から出力ファイルパスを取得する。
2. 引数バリデーション（必須チェック、ファイル存在チェック）を実行する。
3. DB抽出SQLを実行する。
4. CSVファイルにヘッダ行を出力する。
5. DB抽出結果を1行ずつCSV形式で出力する。
6. 抽出件数が0件の場合は情報ログを出力する。
<!-- AI_SKIP_END -->

### Java（ExampleExport.java）

```java
public class ExampleExport extends AbstractDbAccessBatch {

  /** SQL定義：ユーザー抽出. */
  private static final SqlConst SQL_SEL_USER = SqlConst.begin()
    .addQuery("SELECT ")
    .addQuery("  u.user_id ")
    .addQuery(", u.user_nm ")
    .addQuery(", u.email ")
    .addQuery(", u.country_cs ")
    .addQuery(", u.gender_cs ")
    .addQuery(", u.spouse_cs ")
    .addQuery(", u.income_am ")
    .addQuery(", u.birth_dt ")
    .addQuery(", u.upd_ts ")
    .addQuery(" FROM t_user u ")
    .addQuery(" ORDER BY u.user_id ")
    .end();

  public static void main(String[] args) {
    final ExampleExport batch = new ExampleExport();
    batch.callMain(args);
  }

  @Override
  public int doExecute(final IoItems io) throws Exception {
    // 出力ファイルパス取得
    final String outputPath = io.getString("output");

    // 引数バリデーション
    if (ValUtil.isBlank(outputPath)) {
      throw new RuntimeException("'output' is required.");
    }
    if (FileUtil.exists(outputPath)) {
      throw new RuntimeException("Output path already exists. " + LogUtil.joinKeyVal("output", outputPath));
    }
    if (!FileUtil.existsParent(outputPath)) {
      throw new RuntimeException("Output parent directory not exists. " + LogUtil.joinKeyVal("output", outputPath));
    }

    // DB抽出してファイル出力
    try (final SqlResultSet rSet = SqlUtil.select(getDbConn(), SQL_SEL_USER);
        final TxtWriter tw = new TxtWriter(outputPath, LineSep.LF, CharSet.UTF8)) {
      // ヘッダ行出力
      final String[] itemNames = rSet.getItemNames();
      tw.println(ValUtil.joinCsvAllDq(itemNames));
      // データ行出力
      for (final IoItems row : rSet) {
        tw.println(row.createCsvAllDq());
      }
      // 0件ログ
      if (rSet.getReadedCount() == 0) {
        super.logger.info("No data found to export. " + LogUtil.joinKeyVal("output", outputPath));
      }
    }
    return 0;
  }
}
```

**実行例**:
```
java com.example.app.bat.exmodule.ExampleExport "output=/tmp/user_export.csv"
```

<!-- AI_SKIP_START -->
### 応用ポイント

- `SqlResultSet#getItemNames()`: 抽出項目名の配列を取得する。
- `ValUtil.joinCsvAllDq(String[])`: 全項目をダブルクォーテーションで囲みCSV形式で結合する。
- `IoItems#createCsvAllDq()`: 行データをCSV形式（全項目ダブルクォーテーション）で出力する。
- `SqlResultSet#getReadedCount()`: 処理済みの行数を取得する。
<!-- AI_SKIP_END -->

---

## 2. データインポート処理

**用途**: CSVファイルを読み込み、DBにデータを登録・更新する。

<!-- AI_SKIP_START -->
**処理フロー**:
1. 引数から入力ファイルパスを取得する。
2. 引数バリデーション（必須チェック、ファイル存在チェック）を実行する。
3. CSVファイルを開き、ヘッダ行を取得する。
4. データ行を1行ずつ読み込み、項目名配列でマッピングする。
5. UPDATE SQLを実行し、更新件数が0件の場合は INSERT SQLを実行する（UPSERT処理）。
6. データ行が0件の場合は情報ログを出力する。
<!-- AI_SKIP_END -->

### Java（ExampleImport.java）

```java
public class ExampleImport extends AbstractDbAccessBatch {

  /** SQL定義：ユーザー登録. */
  private static final SqlConst SQL_INS_USER = SqlConst.begin()
    .addQuery("INSERT INTO t_user ( ")
    .addQuery("  user_id ")
    .addQuery(", user_nm ")
    .addQuery(", email ")
    .addQuery(", country_cs ")
    .addQuery(", gender_cs ")
    .addQuery(", spouse_cs ")
    .addQuery(", income_am ")
    .addQuery(", birth_dt ")
    .addQuery(", upd_ts ")
    .addQuery(" ) VALUES ( ")
    .addQuery("  ? ", "user_id", BindType.STRING)
    .addQuery(", ? ", "user_nm", BindType.STRING)
    .addQuery(", ? ", "email", BindType.STRING)
    .addQuery(", ? ", "country_cs", BindType.STRING)
    .addQuery(", ? ", "gender_cs", BindType.STRING)
    .addQuery(", ? ", "spouse_cs", BindType.STRING)
    .addQuery(", ? ", "income_am", BindType.BIGDECIMAL)
    .addQuery(", ? ", "birth_dt", BindType.DATE)
    .addQuery(", ? ", "upd_ts", BindType.TIMESTAMP)
    .addQuery(" ) ")
    .end();

  /** SQL定義：ユーザー更新. */
  private static final SqlConst SQL_UPD_USER = SqlConst.begin()
    .addQuery("UPDATE t_user SET ")
    .addQuery("  user_nm = ? ", "user_nm", BindType.STRING)
    .addQuery(", email = ? ", "email", BindType.STRING)
    .addQuery(", country_cs = ? ", "country_cs", BindType.STRING)
    .addQuery(", gender_cs = ? ", "gender_cs", BindType.STRING)
    .addQuery(", spouse_cs = ? ", "spouse_cs", BindType.STRING)
    .addQuery(", income_am = ? ", "income_am", BindType.BIGDECIMAL)
    .addQuery(", birth_dt = ? ", "birth_dt", BindType.DATE)
    .addQuery(", upd_ts = ? ", "upd_ts", BindType.TIMESTAMP)
    .addQuery(" WHERE user_id = ? ", "user_id", BindType.STRING)
    .end();

  public static void main(String[] args) {
    final ExampleImport batch = new ExampleImport();
    batch.callMain(args);
  }

  @Override
  public int doExecute(final IoItems io) throws Exception {
    // 入力ファイルパス取得
    final String inputPath = io.getString("input");

    // 引数バリデーション
    if (ValUtil.isBlank(inputPath)) {
      throw new RuntimeException("'input' is required.");
    }
    if (!FileUtil.exists(inputPath)) {
      throw new RuntimeException("Input path not exists. " + LogUtil.joinKeyVal("input", inputPath));
    }

    // ファイル読み込んでDB登録
    try (final TxtReader tr = new TxtReader(inputPath, CharSet.UTF8)) {
      // ヘッダ行取得
      final String headerLine = tr.getFirstLine();
      if (ValUtil.isBlank(headerLine)) {
        throw new RuntimeException("Input file is empty. " + LogUtil.joinKeyVal("input", inputPath));
      }
      final String[] itemNames = ValUtil.splitCsvDq(headerLine);
      // データ行処理
      for (final String line : tr) {
        final IoItems row = new IoItems();
        row.putAllByCsvDq(itemNames, line);
        // UPSERT処理（UPDATE → INSERT）
        if (!SqlUtil.executeOne(getDbConn(), SQL_UPD_USER.bind(row))) {
          SqlUtil.executeOne(getDbConn(), SQL_INS_USER.bind(row));
        }
      }
      // 0件ログ
      if (tr.getReadedCount() == 1) {
        super.logger.info("No data found to import. " + LogUtil.joinKeyVal("input", inputPath));
      }
    }
    return 0;
  }
}
```

**実行例**:
```
java com.example.app.bat.exmodule.ExampleImport "input=/tmp/user_import.csv"
```

<!-- AI_SKIP_START -->
### 応用ポイント

- `TxtReader#getFirstLine()`: 最初の行（ヘッダ行）を取得し、その行はループ対象から除外される。
- `ValUtil.splitCsvDq(String)`: ダブルクォーテーション付きCSV形式の文字列を分割する。
- `IoItems#putAllByCsvDq(String[], String)`: 項目名配列とCSV行からマップを作成する。
- `SqlConst#bind(IoItems)`: 定義済みSQLに値をバインドして SqlBuilder を生成する。
- `SqlUtil.executeOne()`: 更新件数が1件なら true、0件なら false を返す。
<!-- AI_SKIP_END -->

---

## バリデーションパターン一覧

### 引数バリデーション

| チェック | コード例 |
|---------|----------|
| 必須チェック | `if (ValUtil.isBlank(path)) { throw new RuntimeException("'param' is required."); }` |
| ファイル存在チェック（入力） | `if (!FileUtil.exists(path)) { throw new RuntimeException("Input path not exists."); }` |
| ファイル非存在チェック（出力） | `if (FileUtil.exists(path)) { throw new RuntimeException("Output path already exists."); }` |
| ディレクトリ存在チェック | `if (!FileUtil.existsParent(path)) { throw new RuntimeException("Parent directory not exists."); }` |

### エラーログ出力パターン

```java
// キー・バリュー形式でパラメーターを出力
throw new RuntimeException("Error message. " + LogUtil.joinKeyVal("key1", val1, "key2", val2));
```

---

## サンプルコード

- Java: `src/com/example/app/bat/exmodule/`
