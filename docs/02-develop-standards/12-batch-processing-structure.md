# バッチ処理構成標準

## 概要
- バッチ処理の標準ルールを定義します。
- 本標準や本フレームワークに準じることで、プログラムコードの統一化と開発・保守の効率化を目指します。
- 本資料は下記のサンプルを用いて説明します。
    - Java: `src/com/example/app/bat/exmodule/`

## 前提条件
- Java 11 以上を使用する。
- バッチ処理の引数は URLパラメータ形式で第一引数（`args[0]`）のみを使用する。
- バッチ処理の戻り値は正常終了時 0、異常終了時 1 とする。

## ファイル構成

### ディレクトリ構成
- 機能単位 Javaパッケージには、機能的にまとまりのある１つ以上の Javaクラスを格納します。

```
[project root]/                       # プロジェクトルート：ほとんどの場合、プロジェクト名となる。
├── config/                        # 設定ファイル格納ディレクトリ ［※ディレクトリパスを設定］
├── resources/                     # リソースファイル格納ディレクトリ
├── src/                           # Javaソースファイル格納ディレクトリ
│   ├── [project domain]/         # プロジェクトドメイン Javaパッケージ ［例］ "com/example/"
│   │   ├── app/                 # 機能 Javaパッケージ：共通部品 Javaパッケージとの分岐点
│   │   │   └── bat/            # バッチ処理 Javaパッケージ
│   │   │        └── [module]/  # 機能単位 Javaパッケージ：バッチ処理 Javaクラスを格納する。
│   │   └── util/                # 共通部品 Javaパッケージ：バッチ処理 Javaクラスから使用する共通部品を格納する。［パッケージ名 util は例］
│   └── com/onpg/                 # 本フレームワークドメイン Javaパッケージ
├── classes/                       # Javaクラスコンパイル先ディレクトリ
└── lib/                           # Javaライブラリファイル格納ディレクトリ
```

### ファイル作成単位
- １バッチ処理ごとに１つの Javaクラスを作成します。

### 設定ファイル
[Webサービス処理構成標準 - 設定ファイル](../02-develop-standards/11-web-service-structure.md#設定ファイル) 参照

### リソースファイル
[Webサービス処理構成標準 - リソースファイル](../02-develop-standards/11-web-service-structure.md#リソースファイル) 参照


## バッチ処理構成

バッチ処理の実装ルールと、フレームワークが提供する主要機能について説明します。

### バッチ処理クラス構成
- バッチ処理クラスは `AbstractBatch` クラスまたは `AbstractDbAccessBatch` クラスを継承し、GoF テンプレートメソッドパターンを用いた構造になります。
- `main` メソッドから `callMain` メソッドを呼び出すことで、共通処理（ログ出力、例外処理等）が実行されます。
- テンプレートメソッド（実装が必要なメソッド）である `doExecute` の引数 `io` には、URLパラメータ形式の引数がマップ形式に変換されてセットされます。
- `doExecute` メソッドの戻り値は `main` メソッドの戻り値となります（0: 正常終了、0以外: 異常終了）。

**実装例**:
```java
public class ExampleBatch extends AbstractBatch {

  public static void main(String[] args) {
    final ExampleBatch batch = new ExampleBatch();
    batch.callMain(args);
  }

  @Override
  public int doExecute(final IoItems io) throws Exception {
    // バッチ処理内容を実装
    return 0;
  }
}
```

**実行例**:
```
java com.example.app.bat.exmodule.ExampleBatch "param1=value1&param2=value2"
```

### DBアクセス処理
- 処理内で DBアクセスする場合は、`AbstractDbAccessBatch` クラスを継承することで、`doExecute` メソッド処理中はクラス内のどこでも `getDbConn` メソッドから DBコネクションを取得できます。
- SQL実行には `SqlUtil` メソッドを必ず使用します。
- `doExecute` メソッドの戻り値が 0 の場合、処理が正常終了したとみなし DBコミットされます。
- `doExecute` メソッドの戻り値が 0 以外の場合または例外エラーが発生した場合、処理が異常終了したとみなし DBロールバックされます。

**実装例**:
```java
public class ExampleDbBatch extends AbstractDbAccessBatch {

  private static final SqlConst SQL_SEL_USER = SqlConst.begin()
    .addQuery("SELECT u.user_id, u.user_nm FROM t_user u ")
    .addQuery(" ORDER BY u.user_id ")
    .end();

  public static void main(String[] args) {
    final ExampleDbBatch batch = new ExampleDbBatch();
    batch.callMain(args);
  }

  @Override
  public int doExecute(final IoItems io) throws Exception {
    // DB抽出実行
    try (final SqlResultSet rSet = SqlUtil.select(getDbConn(), SQL_SEL_USER)) {
      for (final IoItems row : rSet) {
        // 1行ずつ処理
      }
    }
    return 0; // 正常終了 → 自動コミット
  }
}
```

#### 固定SQL定義（SqlConst）
- バッチ処理では固定SQLを `SqlConst` クラスで定義し、クラスフィールドに `static final` で保持します。
- `SqlConst` はイミュータブルな SQL定義です。SQL実行時は `bind` メソッドでパラメーターをバインドし、`SqlBean` を生成して使用します。

**基本的な使い方（SELECT）**:
```java
// SQL定義（クラスフィールド）
private static final SqlConst SQL_SEL_USER = SqlConst.begin()
  .addQuery("SELECT ")
  .addQuery("  u.user_id ")
  .addQuery(", u.user_nm ")
  .addQuery(", u.email ")
  .addQuery(" FROM t_user u ")
  .addQuery(" ORDER BY u.user_id ")
  .end();

// SQL実行（メソッド内）
try (final SqlResultSet rSet = SqlUtil.select(getDbConn(), SQL_SEL_USER)) {
  for (final IoItems row : rSet) {
    // 1行ずつ処理
  }
}
```

**パラメーターバインド（INSERT/UPDATE）**:
```java
// SQL定義（クラスフィールド）
private static final SqlConst SQL_INS_USER = SqlConst.begin()
  .addQuery("INSERT INTO t_user ( ")
  .addQuery("  user_id ")
  .addQuery(", user_nm ")
  .addQuery(", income_am ")
  .addQuery(", birth_dt ")
  .addQuery(" ) VALUES ( ")
  .addQuery("  ? ", "user_id", BindType.STRING)
  .addQuery(", ? ", "email", BindType.STRING)
  .addQuery(", ? ", "income_am", BindType.BIGDECIMAL)
  .addQuery(", ? ", "birth_dt", BindType.DATE)
  .addQuery(" ) ")
  .end();

// SQL実行（メソッド内）
IoItems row = new IoItems();
row.put("user_id", "U001");
row.put("email", "test@example.com");
row.put("income_am", 1200000);
row.put("birth_dt", "19900101");
SqlUtil.executeOne(getDbConn(), SQL_INS_USER.bind(row));
```

**BindType一覧**:
| BindType | 用途 | 備考 |
|-|-|-|
| `STRING` | 文字列 | VARCHAR等 |
| `BIGDECIMAL` | 数値 | INTEGER, NUMERIC, DECIMAL等（数値はすべてこの型） |
| `DATE` | 日付 | DATE（yyyyMMdd形式） |
| `TIMESTAMP` | タイムスタンプ | TIMESTAMP（yyyyMMddHHmmssSSS形式） |

**主要メソッド**:
```java
// SQL定義開始
SqlConst.begin()

// SQLのみ追加
.addQuery(" FROM t_user u ")

// SQL＆バインド定義追加
.addQuery(" WHERE user_id = ? ", "user_id", BindType.STRING)

// SQL定義終了（イミュータブル化）
.end()

// 値をバインドしてSqlBean生成
SQL_INS_USER.bind(row)
```

### ログ出力処理
ログ出力には、スーパークラス `AbstractBatch`（`AbstractDbAccessBatch`を含む）が持つ `logger` インスタンスを使用します。
ログ出力の用途については [Webサービス処理構成標準 - ログ出力処理](../02-develop-standards/11-web-service-structure.md#ログ出力処理) を参照すること。

