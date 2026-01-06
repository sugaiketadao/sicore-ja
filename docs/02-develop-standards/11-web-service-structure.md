# Webサービス構成標準

## 概要
- Webサービス（Webサーバー処理）の標準ルールを定義します。
- 本標準や本フレームワークに準じることで、プログラムコードの統一化と開発・保守の効率化を目指します。
- 本資料は下記のサンプルを用いて説明します。
    - HTML/JavaScript: `pages/app/exmodule/`
    - Java: `src/com/example/app/service/exmodule/`

## 前提条件
- Java 11 以上を使用する。
- Webサービスへのリクエストは JSON形式または URLパラメーターとする。
- Webページへのレスポンスは JSON形式のみとする。
- HTML は静的ファイルのダウンロードでのみ取得し、Webサービスでの動的な HTML生成は行わない。

<!-- AI_SKIP_START -->
## フレームワークの特徴と設計思想

本フレームワークは、JSON中心のシンプルなアーキテクチャにより、バックエンド開発の複雑さを排除しています。

### ブラウザとはJSONでしかやりとりしない

**利点**:
- プロトコルが単純である。
- フロントエンドとバックエンドが完全に分離されている。
- API開発も画面開発も同じ仕組みで行える。
- JavaScript だけでテスト可能である。
- モバイルアプリとの連携も容易である。

**実装**:
```
ブラウザ             サーバー
  │                   │
  │------ JSON ------>│
  │　　　　　　  Webサービス処理
  │<------ JSON ------│
```

### URLルーティング

**URLがそのまま実行するWebサービスになる**:
```
URL: http://localhost:8080/services/exmodule/ExampleListSearch

↓ 自動マッピング

実行 Webサービスクラス: com.example.app.service.exmodule.ExampleListSearch
```

**実装例**:
```java
package com.example.app.service.exmodule;

public class ExampleListSearch extends AbstractDbAccessWebService {
  
  @Override
  public void doExecute(Io io) throws Exception {
    // 処理実装
  }
}
```

**利点**:
- URLからWebサービスクラス名が明確に判別できる。
- ルーティング設定が不要である。
- アノテーションが不要である。
- 迷わない明快さがある。
<!-- AI_SKIP_END -->

## ファイル構成

### ディレクトリ構成
- ［※］は設定ファイルで指定する箇所を表します。
- 機能単位 Javaパッケージには、機能的にまとまりのある複数（１つも可）の Javaクラスを格納します。
- 機能単位 Javaパッケージ名は Webページディレクトリ配下の機能単位ディレクトリ名と同じにし、その機能の Webページが使用する Webサービス Javaクラスを格納します（Webページディレクトリと１対１で Webサービス Javaパッケージを作成します）。

```
[project root]/                       # プロジェクトルート：ほとんどの場合、プロジェクト名となる。
├── config/                        # 設定ファイル格納ディレクトリ ［※ディレクトリパスを設定］
├── resources/                     # リソースファイル格納ディレクトリ
├── src/                           # Javaソースファイル格納ディレクトリ
│   ├── [project domain]/         # プロジェクトドメイン Javaパッケージ：複数階層でもよい。［例］ "com/example/"
│   │   ├── app/                 # 機能 Javaパッケージ：共通部品 Javaパッケージとの分岐点
│   │   │   └── service/        # Webサービス Javaパッケージ ［※パッケージ名を設定、コンテキスト名も設定］
│   │   │        └── [module]/  # 機能単位 Javaパッケージ：Webサービス Javaクラスを格納する。
│   │   └── util/                # 共通部品 Javaパッケージ：Webサービス Javaクラスから使用する共通部品を格納する。［パッケージ名 util は例］
│   └── com/onpg/                 # 本フレームワークドメイン Javaパッケージ
├── classes/                       # Javaクラスコンパイル先ディレクトリ
└── lib/                           # Javaライブラリファイル格納ディレクトリ
```

### ファイル作成単位
- １イベント処理ごとに１つの Webサービス Javaクラスを作成します。

### 設定ファイル
- 下記設定ファイルは本フレームワークで使用するため、個別機能では下記以外のファイル名で設定ファイルを用意します。
- 個別機能の設定ファイルは複数個あってもよいですが、設定キーは全ファイルを通じて一意にします。
- `config.properties` 以外の設定ファイルはデフォルトから変更したディレクトリに配置できますが、`config.properties` は必ず `[アプリケーション配備ディレクトリ]/config/config.properties` に存在する必要があります。

| 設定ファイル名 | 用途 | おもな設定内容 |
|-|-|-|
| `config.properties` | 設定ディレクトリ指定 | 設定ディレクトリをデフォルトから変更したい場合に指定する。 |
| `web.properties` | Webサーバー | ポート番号、同時処理数、Webサービス Javaパッケージを設定する。 | 
| `bat.properties` | バッチ処理 | バッチ処理の設定を行う。 |
| `db.properties` | DB接続 | DB接続情報を設定する。 |
| `log.properties` | ログ出力 | ログファイル出力先を設定する。 |

<!-- AI_SKIP_START -->
**個別機能の設定ファイル例**:

```properties
my.custom.key=value
```

**取得方法**:

```java
// 機能設定取得
String myValue = PropertiesUtil.MODULE_PROP_MAP.getString("my.custom.key");
```
<!-- AI_SKIP_END -->


### リソースファイル
下記リソースファイルは本フレームワークで使用するため、個別機能では下記以外のファイル名でリソースファイルを追加します。

| リソースファイル名 | 用途 | おもな設定内容 |
|-|-|-|
| `msg.json` | メッセージテキスト | エラーメッセージ等の文言を定義する。 |

## Webサービス構成

Webサービスの実装ルールと、フレームワークが提供する主要機能について説明します。

### Webサービスクラス構成
- Webサービスクラスは `AbstractWebService` クラスまたは `AbstractDbAccessWebService` クラスを継承し、GoF テンプレートメソッドパターンを用いた構造になります。
- テンプレートメソッド（実装が必要なメソッド）である `#doExecute` の引数 `io` インスタンスは、そのまま戻り値になります。
- Webページからのリクエストは `io` にセットされた状態で `#doExecute` が実行されます。
- Webページへのレスポンスは `io` にセットし、戻り値として `#doExecute` を終了します。リクエストの値は変更・削除しない限り、そのままレスポンスになります。

### Ioクラスによる型安全なデータ処理

<!-- AI_SKIP_START -->
#### JSONとマップが相互変換される仕組み
**Ioクラスの特徴**:
- `Map<String, String>` を継承
- JSON ⇔ `Io` オブジェクト が相互変換可能
- ３階層までの JSON階層構造に対応
- 型安全な `get()`・`put()` メソッド
- リクエストとレスポンスが一体

**サンプルコード**:
```java
// リクエストJSONはフレームワークが自動的にIoオブジェクトに変換
// （以下のコードは実装不要）
// String reqJson = "{\"user_id\":\"U001\",\"income_am\":\"1200000\",\"birth_dt\":\"19870321\"}";
// Io io = new Io();
// io.putAllByJson(reqJson);

// 型安全に値を取得
String userId = io.getString("user_id"); // "U001"
long incomeAm = io.getLong("income_am"); // 1200000
LocalDate birthDt = io.getDateNullable("birth_dt"); // null取得の可能性を明示

// 型指定不要で値を補完（内部では String型で格納）
io.put("user_nm", "マイク・デイビス");
io.put("income_am", 1230000); // 数値も内部で "1230000" に変換される

// レスポンスJSONへの変換はフレームワークが自動的に行う
// （以下のコードは実装不要）
// String resJson = io.createJson();
```
<!-- AI_SKIP_END -->

<!-- AI_SKIP_START -->
#### リクエストとレスポンス一体
**Ioクラスだけで扱える**:
```java
public class ExampleLoadName extends AbstractDbAccessWebService {
  
  @Override
  public void doExecute(Io io) throws Exception {
    // リクエストからSQL作成
    SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  u.user_nm ");
    sb.addQuery(", u.email ");
    sb.addQuery(" FROM t_user u ");
    sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));

    // DB抽出実行
    final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
    if (!ValUtil.isNull(row)) {
      // DB抽出結果をレスポンスとしてセット
      io.putAll(row);
    } else {
      // エラーメッセージをレスポンスとしてセット
      io.putMsg(MsgType.ERROR, "x0001"); // ユーザー情報が見つかりません。
    }
    // ioオブジェクトがそのままレスポンスになる
  }
}
```

**利点**:
- リクエスト値を変更しない限りそのままレスポンスになる。
- 入力値のエコーバックが自動で行われる。
- コード量を削減できる。
<!-- AI_SKIP_END -->

### DBアクセス処理
- 処理内で DBアクセスする場合は、`AbstractDbAccessWebService` クラスを継承することで、`#doExecute` メソッド処理中はクラス内のどこでも `#getDbConn` メソッドから DBコネクションを取得できます。
- SQL実行には `SqlUtil` メソッドを必ず使用します。
- `#doExecute` メソッド内で使用した DBコネクションは、`#doExecute` が正常終了すると通常 DBコミットされます。
- `io` にエラーメッセージがセットされている場合は DBロールバックされます。例外エラーが発生した場合も DBロールバックされます。

<!-- AI_SKIP_START -->
#### トランザクション管理の仕組み
**AbstractDbAccessWebServiceの役割**:
```java
public abstract class AbstractDbAccessWebService extends AbstractWebService {
  
  @Override
  void execute(final Io io) throws Exception {
    // 1. DBコネクションをプールから取得
    try (final Connection conn = DbUtil.getConnPooled(super.traceCode)) {
      this.dbConn = conn;
      
      // 2. サブクラスのdoExecute()を実行
      super.execute(io);
      
      // 3. エラーがなければ自動コミット
      if (!io.hasErrorMsg()) {
        this.dbConn.commit();
      }
      // 4. try-with-resources終了時にDB接続解放、DB接続側で自動ロールバック
    } finally {
      this.dbConn = null;
    }
  }
}
```

**使用例**:
```java
public class ExampleUpsert extends AbstractDbAccessWebService {
  
  @Override
  public void doExecute(Io io) throws Exception {
    // バリデーション
    if (ValUtil.isBlank(io.getString("user_id"))) {
      io.putMsg(MsgType.ERROR, "ev001", new String[]{"ユーザーID"}, "user_id");
      return; // エラー有り → 自動ロールバック
    }
    
    // ヘッダー登録
    SqlUtil.upsert(getDbConn(), "t_user_header", "user_id", io);
    
    // 明細登録（複数行）
    IoRows details = io.getRows("detail");
    for (IoItems detail : details) {
      detail.put("user_id", io.getString("user_id"));
      SqlUtil.insert(getDbConn(), "t_user_detail", detail);
    }
    
    // 成功メッセージ
    io.putMsg(MsgType.INFO, "i0002", new String[]{io.getString("user_id")});
    
    // 正常終了（エラー無し） → 自動コミット
  }
}
```

**利点**:
- コミット・ロールバックを明示的に書く必要がない。
- エラー時のロールバック忘れが発生しない。
- トランザクション境界が明確である（1リクエスト = 1トランザクション）。
<!-- AI_SKIP_END -->

#### 動的SQL組み立て（SqlBuilder）
**基本的な使い方**:
```java
// SQLビルダー生成
SqlBuilder sb = new SqlBuilder();

// SQL組み立て（メソッドチェーン可能）
sb.addQuery("SELECT ");
sb.addQuery("  u.user_id ");
sb.addQuery(", u.user_nm ");
sb.addQuery(", u.email ");
sb.addQuery(" FROM t_user u ").addQuery(" WHERE 1=1 ");

// 値がある場合のみ条件追加（ブランク時はスキップ）
sb.addQnotB("   AND u.user_id = ? ", io.getString("user_id"));
sb.addQnotB("   AND u.user_nm LIKE '%' || ? || '%' ", io.getString("user_nm"));
sb.addQnotB("   AND u.email LIKE ? || '%' ", io.getString("email"));

sb.addQuery(" ORDER BY u.user_id ");

// DB抽出実行
IoRows rows = SqlUtil.selectBulk(getDbConn(), sb);
```

<!-- AI_SKIP_START -->
**実行されるSQL** (リクエスト user_id が "U001"、user_nm が 空、email が "test" の場合):
```sql
SELECT 
  u.user_id 
, u.user_nm 
, u.email 
 FROM t_user u 
 WHERE 1=1 
   AND u.user_id = ?         -- パラメーター: "U001"
   AND u.email LIKE ? || '%' -- パラメーター: "test"
 ORDER BY u.user_id
```
<!-- AI_SKIP_END -->

**主要メソッド**:
```java
// SQLのみ追加
sb.addQuery(" FROM t_user u ");
// SQL＆必須パラメーター追加
sb.addQuery(" AND user_id = ? ", userId);

// 値がブランクでない場合のみSQL＆パラメーター追加
sb.addQnotB(" AND user_id = ? ", userId);

// パラメーターのみ追加
sb.addParams(userId);

// 他のSqlBuilderを統合
sb.addSqlBuilder(otherSb);
```

<!-- AI_SKIP_START -->
**利点**:
- SQL とパラメーターを同時に組み立てられる（ロジック行が分離しない）。
- 条件分岐で SQL文字列結合する必要がない。
- SQLインジェクション対策済みである。
- デバッグが容易である（`SqlBuilder#toString()` で SQL を確認できる）。
<!-- AI_SKIP_END -->

#### テキストファイル・DB抽出結果・画面リストのインターフェース統一
**項目単位と行単位の処理**:
```java
// 画面からのリスト入力: 画面リストを1行(IoItems)ずつ処理
IoRows detail = io.getRows("detail");
for (IoItems row : detail) {
    String userId = row.getString("user_id");
    String userNm = row.getString("user_nm");
    // 処理実行
}

// SqlResultSet: DB結果を1行(IoItems)ずつ取得
try (SqlResultSet rSet = SqlUtil.select(getDbConn(), sb)) {
  // 1行ずつ処理（Iteratorパターン）
  for (IoItems row : rSet) {
    String userId = row.getString("user_id");
    String userNm = row.getString("user_nm");
    // 処理実行
  }
}

// TxtReader: テキストファイルを1行(String)ずつ読み込む
try (TxtReader reader = new TxtReader("/path/to/data.csv", ValUtil.UTF8)) {
  // ヘッダ行をスキップ
  reader.skip();
  
  // キー名配列を定義
  String[] keys = {"user_id", "user_nm", "email"};
  
  // 1行ずつ処理（Iteratorパターン）
  for (String line : reader) {
    // CSV行をIoItemsにセット
    IoItems row = new IoItems();
    row.putAllByCsvDq(keys, line); // ダブルクォーテーション付きCSV対応
    
    String userId = row.getString("user_id");
    String userNm = row.getString("user_nm");
    // 処理実行
  }
}
```

<!-- AI_SKIP_START -->
**利点**:
- 画面リスト（`IoRows`）、`SqlResultSet`、`TxtReader` がすべて同じループ処理である。
- メモリ効率が良い（1行ずつ処理する）。
- 大量データも安全に処理可能である。
- `IoItems` で型安全にデータアクセスできる。
- データソース（画面、DB、ファイル）に依存しないコードである。
<!-- AI_SKIP_END -->

### メッセージ表示の仕組み
メッセージは、必要に応じて下記ファイルに追加します。下記のメッセージIDの採番ルールは一例です。

**リソースファイル** (resources/msg.json):
```json
{
  "ev001": "{0} は必須です。",
  "ev011": "{0} は英数字のみで入力してください。",
  "ev012": "{0} は数字のみで入力してください。",
  "i0002": "{0} を登録しました。",
  "i0004": "検索結果は {0} 件です。"
}
```

**Java側でメッセージセット**:
```java
// エラーメッセージ（項目指定なし）
io.putMsg(MsgType.ERROR, "ev011", new String[]{"ユーザーID"});
// → 「ユーザーID は英数字のみで入力してください。」

// エラーメッセージ（項目指定あり）
io.putMsg(MsgType.ERROR, "ev001", new String[]{"ユーザーID"}, "user_id");
// → 「ユーザーID は必須です。」+ user_id項目をハイライト

io.putMsg(MsgType.ERROR, "ev012", new String[]{"年収"}, "income_am");
// → 「年収 は数字のみで入力してください。」+ income_am項目をハイライト

// 情報メッセージ
io.putMsg(MsgType.INFO, "i0002", new String[]{"U001"});
// → 「U001 を登録しました。」

// 複数メッセージ
io.putMsg(MsgType.ERROR, "ev001", new String[]{"ユーザーID"}, "user_id");
io.putMsg(MsgType.ERROR, "ev001", new String[]{"ユーザー名"}, "user_nm");

// リスト部分項目メッセージ
io.putMsg(MsgType.ERROR, "ev001", new String[]{"ユーザーID"}, "user_id", "detail", rowIdx);
```

**JSON出力**:
```json
// 項目指定なしの場合
{
  "user_id": "U001",
  "_msgs": [
    {
      "type": "error",
      "id": "ev011",
      "text": "ユーザーIDは英数字のみで入力してください。"
    }
  ],
  "_has_err": true
}

// 項目指定ありの場合
{
  "user_id": "U001",
  "_msgs": [
    {
      "type": "error",
      "id": "ev001",
      "text": "ユーザーID は必須です。",
      "item": "user_id"
    },
    {
      "type": "error", 
      "id": "ev001",
      "text": "ユーザー名 は必須です。",
      "item": "user_nm"
    }
  ],
  "_has_err": true
}
```

<!-- AI_SKIP_START -->
**利点**:
- メッセージ文言を一元管理できる。
- 多言語化が容易である。
- 項目単位でハイライト表示でき、リスト部分項目にも対応している。
- 自動でハイライト表示CSSクラスが付与される。

**補足**:
- `_msgs` と `_has_err` はフレームワークで処理するため、アプリケーションから直接使用しない。
<!-- AI_SKIP_END -->

### ログ出力処理
ログ出力には、スーパークラス `AbstractWebService`（`AbstractDbAccessWebService`を含む）が持つ `logger` インスタンスを使用します。ログ出力の用途は下記のとおりです。

| 用途 | 使用メソッド |
|-|-|
| 開発 デバッグ | `logger#develop` |
| 本番 情報監視 | `logger#info` |
| 本番 エラー監視 | `logger#error` |
| 本番 並列数監視 | `logger#begin`, `logger#end` |
| 本番 性能監視 | `logger#startWatch`, `logger#stopWatch` |

- 開発デバッグの出力に性能負荷がかかる場合は、`logger#isDevelopMode` メソッドで事前にデバッグログが出力されることを確認します。
```java
if (logger.isDevelopMode()) {
    logger.develop("削除件数. " + LogUtil.joinKeyVal("count", delCnt);
}
```

<!-- AI_SKIP_START -->
#### ログファイルのローテーション
**自動ローテーション**:
- 日付単位でログファイル切り替え
- ログレベル制御

**ログ出力設定** (log.properties):
```properties
develop.mode=true
default.inf.file=/tmp/logs/info.log
default.err.file=/tmp/logs/error.log
```

**使用方法**:
```java
// 開発デバッグログ
// develop.mode=trueの場合のみ出力される
logger.develop("DB抽出条件: " + LogUtil.joinKeyVal("userId", userId));

// 情報ログ
// default.inf.file のファイルパスに出力される
logger.info("処理開始: " + traceCode);

// エラーログ
// default.err.file のファイルパスに出力される
logger.error(exception, "エラー発生");

// 性能測定
logger.startWatch();
// ... 処理 ...
logger.stopWatch(); // 経過時間を自動出力
```
<!-- AI_SKIP_END -->

### バグ対策機能

`Io` クラスは、一般的な Map クラスで発生しやすいバグを防止する機能を持っています。

#### NULL安全な取り扱い
**問題**: 一般的なMapでは `get()` が `null` を返す
```java
// 一般的なMap
Map<String, String> map = new HashMap<>();
String value = map.get("key"); // null が返る → NullPointerException の原因
```

**解決**: Ioクラスでは意識的にnull取得
```java
// Ioクラス
Io io = new Io();

// 基本メソッドはnullを返さない（ブランクを返す）
String value = io.getString("key"); // ""

// null を判定したい場合は明示的に
String value = io.getStringNullable("key"); // null
```

<!-- AI_SKIP_START -->
**バグ対策効果**:
- NullPointerException を防止できる。
- null 処理を意識化できる。
<!-- AI_SKIP_END -->

#### 型安全な取り扱い
**問題**: 文字列から数値への変換でエラー
```java
// 一般的なMap
String str = map.get("age");
int age = Integer.parseInt(str); // NumberFormatException の可能性
```

**解決**: 型変換メソッド
```java
// Ioクラス
int age = io.getInt("age");  // ブランクはゼロへ変換、数値以外はエラーでキーと値をログ出力
LocalDate birthDt = io.getDateNullable("birth_dt");   // 日付形式チェック
BigDecimal income_am = io.getBigDecimal("income_am"); // ブランクはゼロへ変換、精度を保つ
```

<!-- AI_SKIP_START -->
**バグ対策効果**:
- 型変換エラーを早期発見できる。
- 暗黙的な型変換を排除できる。
- 内部は文字列格納で数値精度を保証できる。
<!-- AI_SKIP_END -->

#### キー重複の厳密チェック
**問題**: 意図しない上書き
```java
// 一般的なMap
map.put("user_id", "U001");
map.put("user_id", "U002"); // 上書きされる（警告なし）
```

**解決**: 重複エラー
```java
// Ioクラス
io.put("user_id", "U001");
io.put("user_id", "U002"); // 基本的に上書きはエラーでキーをログ出力

// 上書きする場合は意図的に
io.putForce("user_id", "U002"); // OK
```

#### DB抽出結果を io へセットする際の注意
- リクエスト `io` に存在する項目と同名の DB項目が含まれる DB抽出結果を、レスポンスとして `io.putAll()` でセットするとキー重複エラーになります。
- リクエスト `io` に含まれる項目（主キーや楽観排他制御用タイムスタンプ等）は SELECT句から除外します。
- SELECT句から除外することが難しい場合は、`io.putAllForce()` でセットします。

**正しい例**:

```java
// リクエスト: {"user_id": "U001", "upd_ts": "20250123T235959123456"}

final SqlBuilder sb = new SqlBuilder();
sb.addQuery("SELECT ");
sb.addQuery("  u.user_nm ");      // user_id, upd_ts 以外の項目を取得
sb.addQuery(", u.email ");
sb.addQuery(" FROM t_user u ");
sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));
sb.addQuery("   AND u.upd_ts = ? ", io.getSqlTimestampNullable("upd_ts"));

final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
io.putAll(row); // OK: SELECT句に user_id, upd_ts が含まれていない
```

**エラーになる例**:

```java
// リクエスト: {"user_id": "U001", "upd_ts": "20250123T235959123456"}

final SqlBuilder sb = new SqlBuilder();
sb.addQuery("SELECT ");
sb.addQuery("  u.user_id ");      // NG: リクエストに存在する項目
sb.addQuery(", u.user_nm ");
sb.addQuery(", u.email ");
sb.addQuery(", u.upd_ts ");       // NG: リクエストに存在する項目
sb.addQuery(" FROM t_user u ");
sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));
sb.addQuery("   AND u.upd_ts = ? ", io.getSqlTimestampNullable("upd_ts"));

final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
io.putAll(row); // エラー: user_id, upd_ts がすでに io に存在
```

<!-- AI_SKIP_START -->
**バグ対策効果**:
- キーのタイプミスを検出できる。
- 意図しない上書きを防止できる（マップだが `final` 宣言の扱い）。
- データ整合性を保証できる。
<!-- AI_SKIP_END -->

#### 存在しないキーでの取得エラー
**問題**: タイプミスによる間違い
```java
// 一般的なMap
Map<String, String> map = new HashMap<>();
map.put("user_id", "U001");
String value = map.get("userid"); // null（タイプミスに気付かない）
```

**解決**: 厳密なキーチェック
```java
// Ioクラス
io.put("user_id", "U001");
String value = io.getString("userid"); // 存在しないキーはエラーでキーをログ出力

// 存在チェックが必要な場合
if (io.containsKey("userid")) {
  String value = io.getString("userid");
}

// または OrDefault メソッドを使用
String value = io.getStringOrDefault("userid", ""); // 存在しないキーは "" が返る
```

<!-- AI_SKIP_START -->
**バグ対策効果**:
- タイプミスを早期発見できる。
- キー名の間違いを防止できる（マップだが未宣言の扱い）。
- デバッグ時間を短縮できる。
<!-- AI_SKIP_END -->

#### ディープコピーによる安全性
`Io` クラスは、リスト・ネストマップ・複数行リスト・配列リストの格納・取得時にディープコピーを行います。これにより、以下の安全性が保証されます。

<!-- AI_SKIP_START -->
- **意図しない参照共有の防止**: 格納した元のリストやマップを変更しても、`Io` 内部のデータには影響しない。
- **取得データの独立性**: 取得したリストやマップを変更しても、`Io` 内部のデータには影響しない。
- **バグの未然防止**: 複数箇所で同じデータを参照することによる予期しない副作用を回避できる。
<!-- AI_SKIP_END -->

```java
// 格納時のディープコピー
List<String> srcList = new ArrayList<>(Arrays.asList("A", "B"));
io.putList("items", srcList);
srcList.add("C");  // 元リストを変更
// io.getList("items") は ["A", "B"] のまま（影響なし）

// 取得時のディープコピー
List<String> gotList = io.getList("items");
gotList.add("D");  // 取得リストを変更
// io.getList("items") は ["A", "B"] のまま（影響なし）
```

> **注意**: ディープコピーを行うため、大きなデータの格納・取得を繰り返す処理では性能に影響する可能性があります。

## 関連ドキュメント

- [Webページ構成標準 (HTML/JavaScript/CSS)](../02-develop-standards/01-web-page-structure.md)
- [イベント別コーディングパターン](../02-develop-standards/21-event-coding-pattern.md)

