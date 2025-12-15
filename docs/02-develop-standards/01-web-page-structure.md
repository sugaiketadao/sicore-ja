# Webページ構成標準

## 概要
- ブラウザを使用するシステム開発の標準ルールを定義します。
- 本標準や本フレームワークに準じることでプログラムコードの統一化と開発・保守の効率化を目指します。
- 本資料では一覧（繰り返し行）と明細（繰り返し行）を総称して「リスト」と呼び、リストについて説明している箇所は両方に適用されます。
- 本資料は下記のサンプルを用いて説明します。
    - HTML/JavaScript: `pages/app/exmodule/`
    - Java: `src/com/example/app/service/exmodule/`

## 前提条件
- HTML5 と ES6(ECMAScript 2015), CSS3+α を使用する。

<!-- AI_SKIP_START -->
## フレームワークの特徴と設計思想

本フレームワークは、シンプルさと開発効率を重視した設計となっています。以下の特徴により、プロトタイプから本番まで一貫した開発が可能です。

### HTMLファイルはそのまま使える（プロトタイプ駆動開発）

**プロトタイプがそのまま動く**:
```html
<!-- プロトタイプ時 -->
<input type="text" name="user_nm" value="マイク・デイビス">

<!-- 本番時も同じHTML -->
<input type="text" name="user_nm">
```

**利点**:
- デザイナーが作成した HTML をそのまま使用できる。
- プロトタイプ段階で操作性を確認できる。
- テンプレートエンジンが不要である。
- HTML の学習コストだけで開発可能である。

### ステートレスアーキテクチャ

**サーバー側にセッションなし**:
- ブラウザの `sessionStorage` で状態を管理する。
- スケールアウトが容易である。
- サーバー再起動の影響を受けない。

**URLパラメーターも併用可能**:
```javascript
// ページ遷移時にデータを渡す
HttpUtil.movePage('editpage.html', {
  user_id: 'U001',
  upd_ts: '20250123T235959123456'
});
```

### ブラウザ⇔サーバー間のデータフロー

一覧ページ検索処理を例に、ブラウザからサーバーまでのデータの流れを示します。

```
[ブラウザ] 一覧ページ listpage.html
     │
     │ 1. 検索ボタン押下
     │
     ▼
[JavaScript] listpage.js
     │
     │ 2. PageUtil.getValues() でブラウザ入力値からリクエストJSON生成
     │    → { user_id: "U001", user_nm: "マイク・デイビス" }
     │
     │ 3. HttpUtil.callJsonService() でJSON送信
     │
     ▼
[フレームワーク]
     │ 4. リクエストJSON → Io オブジェクトに変換
     │
     ▼
[Java] ExampleListSearch.java
     │
     │ 5. Io オブジェクトのリクエスト値から SqlBuilder でSQL組み立て
     │
     │ 6. SqlUtil.selectBulk() でDB抽出
     │
     │ 7. DB抽出結果 → Io オブジェクトに格納
     │
     ▼
[フレームワーク]
     │ 8. Io オブジェクト → レスポンスJSON に変換
     │
     ▼
[JavaScript] listpage.js
     │
     │ 9. PageUtil.setValues() でレスポンスJSON をブラウザ表示
     │
     ▼
[ブラウザ] DB抽出結果が一覧ページに表示される
```

### HTML⇔JSON相互変換の仕組み

本フレームワークの中核機能の一つが、HTML と JSON の自動相互変換です。この仕組みにより、フロントエンドとバックエンド間のデータ受け渡しが極めてシンプルになります。

#### フォーム入力要素からJSONへの変換

**対応HTML**:
```html
<!-- 単純な入力 -->
<input type="text" name="user_id" value="U001">
<input type="text" name="user_nm" value="マイク・デイビス">
<input type="text" name="email" value="mike.davis@example.com">

<!-- テーブル行データ -->
<table>
  <tbody id="list">
    <tr>
      <td><input name="list.pet_nm" value="ポチ"></td>
      <td><input name="list.weight_kg" value="5.0"></td>
    </tr>
    <tr>
      <td><input name="list.pet_nm" value="タマ"></td>
      <td><input name="list.weight_kg" value="2.5"></td>
    </tr>
  </tbody>
</table>
```

**JavaScript側**:
```javascript
// ブラウザ入力値からリクエストJSON生成
const req = PageUtil.getValues();

// 実行結果
// {
//   "user_id": "U001",
//   "user_nm": "マイク・デイビス",
//   "email": "mike.davis@example.com",
//   "list": [
//     {"pet_nm": "ポチ", "weight_kg": "5.0"},
//     {"pet_nm": "タマ", "weight_kg": "2.5"}
//   ]
// }
```

**<form>タグ不要**:
- `PageUtil.getValues()` が自動で値を収集する。
- `name`属性さえあれば取得可能である。
- フォーム送信イベントを使用しない。

#### JSONからHTMLへの変換

**JavaScript側**:
```javascript
// サーバーからレスポンスJSON受信
const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListSearch', req);

// レスポンスJSON をブラウザ表示
PageUtil.setValues(res);
```

**自動で値がセットされる**:
```html
<!-- レスポンス前 -->
<input type="text" name="user_id">
<span data-name="user_nm"></span>

<!-- レスポンス後（自動でセットされる） -->
<input type="text" name="user_id" value="U001">
<span data-name="user_nm">マイク・デイビス</span>
```
<!-- AI_SKIP_END -->

## ファイル構成

### ディレクトリ構成
- ［※］は設定ファイルで変更可能な箇所を表す。
- 機能単位ディレクトリに機能的にまとまりのある複数（１つも可）の HTMLファイル（以下、ページと呼ぶ）を格納する。
- ページごとの JavaScriptファイルおよび CSSファイルは HTMLファイルと同じ機能単位ディレクトリに格納する。
- 下記は例であり、各ディレクトリ名および階層の深さは任意とする。

```
[project root]/               # プロジェクトルート：ほとんどの場合、プロジェクト名となる。
└── pages/                 # Webページディレクトリ ［※ディレクトリ名のみ設定］
     ├── app/              # 機能ディレクトリ：共通部品ディレクトリとの分岐点
     │   └── [module]/    # 機能単位ディレクトリ：おもに HTMLファイル・JavaScriptファイルを格納する。
     ├── util/             # 共通部品ディレクトリ：機能単位ページから使用する共通部品を格納する。
     └── lib/              # 本フレームワークディレクトリ
```

### ディレクトリ［例］
- 機能単位ディレクトリ：[pages/app/exmodule/](../../pages/app/exmodule/)
- フレームワークディレクトリ：[pages/lib/](../../pages/lib/)

## HTMLルール

HTML の記述ルールを定義します。基本的な構成から順に説明します。

### 最小構成（言語:日本語）
```HTML
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <title>【ページタイトル】</title>
  <link href="../../lib/css/onepg-base.css" rel="stylesheet">
  <link href="【システム単位や機能単位の共通 CSSファイル（省略可能）】" rel="stylesheet">
  <style>
    /* ページごとの CSS（省略可能）*/
  </style>
</head>
<body>
<body>
  <main>
  【ページコンテンツ】
  </main>
  <script src="../../lib/js/onepg-utils.js" defer></script>
  <script src="ページごとの JavaScriptファイル名（省略可能）】" defer></script>
</body>
</html>
```

### HTMLタグ使用
- HTML5 で追加された `<input>` の `type="number"` や `type="date"` などは使用せず、`type="text"` を使用する。
- ボタンは `<input type="button">` を使用せず、`<button>` を使用する。
- レスポンス表示は行うがリクエストする必要がない、常時入力不可のテキスト表示には `<span>` を使用する。リスト内のテキスト表示は `<td>` をそのまま使用する。
- フォーム入力要素に紐づくテキスト表示には `<label>` を使用し、必要に応じてフォーム入力要素に `id`属性、`<label>` に `for`属性を設定する。
- チェックボックスとラジオボタンは、要素と値名称を `<label>` で囲む。
  ［例］`<label><input type="checkbox">値名称</label>`

### name属性
- リクエストするフォーム入力要素には `name`属性を付加する。レスポンス表示するフォーム入力要素も同様とする。
- 本来 `name`属性を持たない `<span>` や `<td>` でレスポンス表示する場合は `data-name`属性（独自属性）を付与する。本フレームワークではレスポンス表示に関して `data-name`属性と `name`属性を同じ扱いとする。
- リスト部分（繰り返し部分）の要素は下記のルールに従う。
    - 行内の要素（以下、行内要素と呼ぶ）は `name`属性を `.` 区切りとし、`表id.項目名` 形式で設定する。`.` 区切りの `name`属性は行内要素にのみ使用する。
    - 行内要素の親・祖父要素として `.` 区切りの前部分 `表id` を `id`属性とする要素（以下、表要素と呼ぶ）が存在すること。ほとんどの場合、表要素は `<tbody>` または `<table>` となる。
    - 表要素（`id`属性を割り当てた要素）直下の子要素は、繰り返される部分の最上位要素（以下、行要素と呼ぶ）となること。ほとんどの場合、行要素は `<tr>` となる。
　
### name属性・data-name属性（独自属性）のネーミング
- 通常、`name`属性と `data-name`属性の値は Webサービス処理で入出力する DB項目物理名に一致させる。同じDB項目が同一ページ上に複数存在する場合は適切なサフィックスを付加する。
- DB項目物理名に一致させることで Webサービス処理のコード量が削減される。ページ入力値のまま DB登録または DB更新するのであれば、Webページからのリクエストをそのまま SQL実行ユーティリティJavaクラスに渡すことで SQL組み立てすることなく SQL実行できる。

| 用途 | 書式 | 例 |
|-|-|-|
| 基本 | 入出力対象または抽出対象となる DB項目物理名と一致させる。 | `user_id` |
| 行内要素 | 「表要素 `id`属性 + "." + DB項目物理名」とする。 | `list.user_id` や `detail.user_id` |
| 重複する場合１ | 「DB項目物理名 + "_" + 適切なサフィックス」 とする。 | 【DB抽出条件の自至】<br>自：`birth_dt_from`<br>至：`birth_dt_to`|
| 重複する場合２ | 「トランザクション項目 DB項目物理名 + "_" + 適切なサフィックス」とする。 | 【同一マスタの名称】<br>散歩当番氏名：`strollduty_id_name`<br>食事当番氏名：`mealduty_id_name` |

<!-- AI_SKIP_START -->
#### 項目名統一のメリット
DB項目物理名 = HTML`name`属性 = Javaマップキー を統一することで以下のメリットがあります:

```sql
-- DB項目
CREATE TABLE t_user (
  user_id VARCHAR(10),
  user_nm VARCHAR(50),
  ...
);
```

```html
<!-- HTML -->
<input name="user_id">
<input name="user_nm">
```

```java
// Java マップキー
String userId = io.getString("user_id");
String userNm = io.getString("user_nm");

// SqlUtil で自動バインド
SqlUtil.insertOne(conn, "t_user", io);
```

**メリット**:
- **変換コード不要**: キャメルケース変換などが不要である。
- **コード量削減**: マッピング処理を書く必要がない。
- **バグ削減**: 変換ミスがなくなる。
- **保守性向上**: DB設計書がそのまま仕様書として機能する。
<!-- AI_SKIP_END -->

### 非表示／非活性／読取専用スタイル適用
- `<input>` などフォーム入力要素が入力不可となる場合は非活性属性 `disabled` を使用する。
- 通常のフォーム送信と異なり、本フレームワークでは非活性要素もリクエスト対象となるので注意する。
- 要素を非表示にするには `display`スタイルまたは `visibility`スタイルを変更する。
    - `display`スタイルで非表示にする場合（`display:none`）は、要素のスペースは保持されない。
    - `visibility`スタイルで非表示にする場合（`visibility:hidden`）は、要素のスペースは保持される。
- 通常のフォーム送信と異なり、本フレームワークでは非表示要素はリクエスト対象外となるので注意する。
- 更新ページを照会ページや削除ページなど全項目入力不可のページに流用する場合のみ、読取専用属性 `readonly` を使用する。
- `<select>` の読取専用は、選択値以外の `<option>` に `disabled` を適用することで代替する。

### ページセクション分け
- 本フレームワークの JavaScript部品でページ全体を処理すると冗長になる場合は、ページをセクション分けして処理対象を絞り込むことで効率化できる。
- `<section id="セクション名">` で囲むことでセクション分けを行い、`id`属性にはその部分を表すセクション名を設定する。`<section>` 以外の HTMLタグも使用できる。

### セクション命名例
| セクション用途 | セクション名 | 例 |
|-|-|-|
| DB抽出条件部分 | `searchConditionsArea` | `<section id="searchConditionsArea">` |
| DB抽出結果一覧部分 | `searchResultsArea` | `<section id="searchResultsArea">` |

### フレームワーク独自属性
- 下記は本フレームワークが独自に定義する要素属性であるため、他の用途で使用しないこと。
- 使用する際は本フレームワークが用意したメソッドから使用する。

| 付与先要素 | 独自属性 | 例 | 用途 |
|-|-|-|-|
| フォーム入力要素以外 | `data-name` | `data-name="user_nm"` | `name`属性の代替え |
| フォーム入力要素 | `data-obj-row-idx` | `data-obj-row-idx="0"` | リクエスト内配列データのインデックス値 |
| `<input type="checkbox">` | `data-check-off-value` | `value="1" data-check-off-value="0"` | チェックOFF時のリクエストデータ値 |
| `<input type="text">` | `data-value-format-type` | `data-value-format-type="num"` | このタイプによって値をレスポンス時にフォーマット、リクエスト時にアンフォーマットする |
| 全要素 | `data-style-display-backup` | `data-style-display-backup="inline-block"` | 非表示化する前の `display`スタイル値で再表示する際に用いる |
| 全要素 | `data-style-visibility-backup` | `data-style-visibility-backup="visible"` | 非表示化する前の `visibility`スタイル値で再表示する際に用いる |
| エラー項目 | `data-title-backup` | `data-title-backup="タイトル"` | エラーメッセージをセットする前の `title`属性値で解除する際に用いる |
| `<input type="radio">` | `data-radio-obj-name` | `data-radio-obj-name="gender_cs"` | 行要素内のラジオボタンの行インデックス付与前の `name`属性値でリクエスト時に用いる |

<!-- AI_SKIP_START -->
#### data-name属性の使用例とメリット
**用途**: フォーム要素以外でのデータ表示（取得はしない）

```html
<!-- 入力・表示要素（name属性） -->
<input type="text" name="user_id">

<!-- 表示専用要素（data-name属性） -->
<span data-name="user_nm"></span>
<td data-name="list.pet_nm"></td>
```

**JavaScript処理**:
```javascript
// 取得時はname属性のみ取得（data-nameは取得しない）
const values = PageUtil.getValues(); // name属性のみ

// セット時はnameとdata-name両方にセット
PageUtil.setValues(values);          // nameとdata-name両方にセット
```

**利点**:
- `<span>`、`<td>` などにも値をセット可能である。
- `name`属性と同じように表示できる。
- 表示専用項目と入力項目を明確に分離できる。
<!-- AI_SKIP_END -->

<!-- AI_SKIP_START -->
#### data-check-off-value属性の使用例とメリット
**用途**: チェックボックスの OFF時の値を定義

```html
<!-- チェックON時: "1"、OFF時: "0" -->
<input type="checkbox" name="is_dog" value="1" data-check-off-value="0">

<!-- チェックON時: "true"、OFF時: "false" -->
<input type="checkbox" name="is_cat" value="true" data-check-off-value="false">
```

**リクエストJSON**:
```javascript
// チェックONの場合
// { "is_dog": "1", "is_cat": "true" }

// チェックOFFの場合
// { "is_dog": "0", "is_cat": "false" }
```

**利点**:
- チェックボックスの OFF値を明示的に定義することで OFF時の補完ロジックが不要となる。
- `boolean`値も扱うことができる。

**注意点**:
- 必須ではない DB抽出条件では使用しないこと。
<!-- AI_SKIP_END -->


### data-value-format-type属性設定値
`data-value-format-type` の設定値は下記のとおり。

| 設定値 | フォーマットタイプ名 | 項目値［例］ | フォーマット値 |
|-|-|-|-|
| `num` | 数値 - カンマ区切り | `1000000` | `1,000,000` |
| `ymd` | 日付 - YYYY/MM/DD形式 | `20251231` | `2025/12/31` |
| `hms` | 時刻 - HH:MI:SS形式 | `123456` | `12:34:56` |
| `upper` | 大文字変換（コード・ID） | `abc123` | `ABC123` ※アンフォーマット後も大文字のまま |

<!-- AI_SKIP_START -->
#### data-value-format-type属性の使用例とメリット
**用途**: 値の自動フォーマット・アンフォーマット

```html
<!-- 数値（カンマ区切り） -->
<input type="text" name="income_am" data-value-format-type="num">

<!-- 日付（YYYY/MM/DD形式） -->
<input type="text" name="birth_dt" data-value-format-type="ymd">

<!-- 時刻（HH:MI:SS形式） -->
<input type="text" name="stroll_tm" data-value-format-type="hms">

<!-- 大文字変換 -->
<input type="text" name="user_id" data-value-format-type="upper">
```

**JavaScript動作**:
```javascript
// セット時: 自動フォーマット
PageUtil.setValues({
  income_am: "1200000",  // → value="1,200,000"
  birth_dt:  "19870321", // → value="1987/03/21"
  stroll_tm: "123456",   // → value="12:34:56"
  user_id:   "u001"      // → value="U001"
});

// 取得時: 自動アンフォーマット
const req = PageUtil.getValues();
// {
//   income_am: "1200000",  // カンマ除去
//   birth_dt:  "19870321", // スラッシュ除去
//   stroll_tm: "123456",   // コロン除去
//   user_id:   "U001"      // そのまま
// }
```

**Java側での処理**:
```java
// Java側ではフォーマット・アンフォーマット不要
BigDecimal price = io.getBigDecimal("income_am");     // (BigDecimal)1200000 がそのまま取得できる
LocalDate orderDate = io.getDateNullable("birth_dt"); // (LocalDate)1987-03-21 がそのまま取得できる

// DBにそのまま登録可能
SqlUtil.insert(getDbConn(), "t_user", io);
```

**利点**:
- 表示用フォーマットと DB保存用の値を自動変換できる。
- フォーマット処理を Java・JavaScript で書く必要がない。
- 見た目とデータを分離できる。
<!-- AI_SKIP_END -->


### フレームワーク使用 HTML
- 下記は本フレームワークが使用する HTML であるため、個別機能の処理から直接操作しないこと。
- 使用する際は本フレームワークが用意したメソッドから操作する。

| 用途 | HTML | 操作例 |
|-|-|-|
| メッセージ表示エリア | `<section id="_msg"></section>` | PageUtil.setMsg(res); |


## JavaScriptルール

JavaScriptの記述ルールを定義します。

### 記述方法
- JavaScript は HTML内に記述せず、外部ファイルに記述して参照する。
- そのページ専用の JavaScript は１ファイルとし、ファイル名は HTMLファイルと同じとする。
  ［例］ `listpage.html` の場合は `listpage.js`
- ボタン要素等のイベント処理内は、外部ファイルに定義した１つの関数を実行するのみとする。
- JavaScriptファイルは `<body>` 内の最後に記述して読み込む。その際 `defer`属性を付与する。
- ページ表示時の初期処理 JavaScript は JavaScriptファイルの最後に記述する。
- JavaScript 内で Webサービスを実行する場合は `await` を使った同期処理となるため、機能単位の JavaScriptファイル内の関数は `async` で定義する。

```HTML
    <button type="button" onclick="insert()">登録</button>
    <script src="****.js" defer></script>
  </body>
</html>
```

```JavaScript
/**
 * 初期処理.
 */
const init = async function () {
  ：
};

/**
 * 登録処理.
 */
const insert = async function () {
  ：
};

// 初期処理実行
init();
```

### 要素取得方法
- 要素の取得には `name`属性または `data-name`属性を使用する。
- リクエストおよびレスポンスで扱わない要素（`name`属性または `data-name`属性を割り当てない要素）は `id`属性を付加して取得する。

### JavaScript 処理範囲
- JavaScript（ブラウザ側）では下記を処理範囲とし、それ以外の処理は Webサービス処理とする。
    - レスポンスデータの表示
    - リクエストデータの作成
    - Webサーバー処理の呼出
    - 要素の非活性化／非表示化／読取専用化 制御
    - セッション管理
- ページ読み込み時にセットするフォーム入力要素の初期値は Webサービスからレスポンスとして返すこととし、JavaScript だけで初期値のセットは行わない。

### フレームワーク基本部品
標準的なページで使用する本フレームワークの JavaScript部品クラスのメソッドは下記のとおり。

| 部品メソッド | 用途 |
|-|-|
| `PageUtil.getValues()` | ページデータ取得 |
| `PageUtil.setValues()` | ページデータセット |
| `PageUtil.setMsg()` | メッセージセット |
| `PageUtil.clearMsg()` | メッセージクリア |
| `PageUtil.hasError()` | エラーメッセージ有無チェック |
| `HttpUtil.callJsonService()` | JSON Webサービス呼出 |
| `HttpUtil.movePage()` | 指定URL遷移 |
| `HttpUtil.getUrlParams()` | URLパラメーター取得 |
| `StorageUtil.getPageObj()` | ページ単位セッションデータ取得 |
| `StorageUtil.setPageObj()` | ページ単位セッションデータ格納 |
| `DomUtil.getByName()` | `name`属性セレクター要素取得 |
| `DomUtil.getById()` | `id`属性セレクター要素取得 |
| `DomUtil.setEnable()` | 要素の活性切替 |
| `DomUtil.setVisible()` | 要素の表示切替 |

<!-- AI_SKIP_START -->
### StorageUtil の３階層スコープ
セッションストレージは用途に応じて３つのスコープで管理できます。

| スコープ | `StorageUtil` 取得メソッド | 格納メソッド | 用途 | 使用例 |
|-|-|-|-|-|
| ページ単位 | `getPageObj()` | `setPageObj()` | URLの HTMLファイル単位、１ページ内でデータ保持 | 一覧ページで検索条件の保持 |
| 機能単位 | `getModuleObj()` | `setModuleObj()` | URLの機能ディレクトリ単位、ページ間でデータ共有 | ヘッダ編集ページ⇔明細編集ページ 間の入力中データ保持 |
| システム単位 | `getSystemObj()` | `setSystemObj()` | システム全体でデータ共有 | ログイン情報の保持 |
<!-- AI_SKIP_END -->


## CSSルール

### セレクター
- ページ単位の CSS ではクラスセレクターを使用することとし、下記のセレクターは使用しない。
    - 要素セレクター
    - 属性セレクター
    - IDセレクター
    - 全称セレクター

### 記述方法
- CSS は HTML内に記述せず、外部ファイルに記述して参照する。
- そのページ専用の CSS は１ファイルとし、ファイル名は HTMLファイルと同じとする。
  ［例］ `listpage.html` の場合は `listpage.css`
- CSSファイルは `<head>` 内の最後に記述して読み込む。
- テーブルや列、項目のサイズ指定は HTML内に直接記述することを許容する。
- HTML内に直接記述する場合は、各要素の `style`属性か `<head>` 内の CSSファイル参照より後ろに `<style>` で記述する。

### サイズ指定単位
- サイズ指定を行う場合は `rem`単位で指定する。

### グリッドレイアウト CSSクラス
- 要素配置にはグリッドシステムを使用する。
- グリッドレイアウトを使用するにあたり本フレームワークの下記 CSSクラスを使用する。
- 列要素 CSSクラス名の末尾数値が列幅を表す。（下記表の `*` 部分）
- 列幅は `1` から `12` までで合計 12 になるよう指定する。

| CSSクラス | 用途 | ルール |
|-|-|-|
| `.grid-row` | グリッドレイアウトの行要素 | `<div>` に使用する |
| `.grid-col-*` | グリッドレイアウトの列要素 | `<div>` に使用し、`<div.grid-row>` の直下に配置する |

<!-- AI_SKIP_START -->
#### グリッドレイアウト使用例
**グリッドシステム**:
```html
<!-- 1行3列のグリッド（grid-col-4 × 3 = 12） -->
<div class="grid-row">
  <div class="grid-col-4">
    <label>ユーザーID</label>
    <input type="text" name="user_id">
  </div>
  <div class="grid-col-4">
    <label>ユーザー名</label>
    <input type="text" name="user_nm">
  </div>
  <div class="grid-col-4">
    <label>メール</label>
    <input type="text" name="email">
  </div>
</div>

<!-- 1行2列のグリッド（grid-col-6 × 2 = 12） -->
<div class="grid-row">
  <div class="grid-col-6">
    <label>ユーザーID</label>
    <input type="text" name="user_id">
  </div>
  <div class="grid-col-6">
    <label>ユーザー名</label>
    <input type="text" name="user_nm">
  </div>
</div>
```

**ポイント**:
- グリッドは12分割システム
- 1行のカラム合計が12になるように配置
- 例: `grid-col-4` × `3` = 12、 `grid-col-6` × `2` = 12、 `grid-col-6` + `4` + `2` = 12

**利点**:
- 覚えるクラス名が少ない。
- レスポンシブに対応済みである。
- 独自CSSのみで構成されている（外部フレームワーク不使用）。
<!-- AI_SKIP_END -->

### フォーム入力要素 CSSクラス
フォーム入力要素の配置にあたり本フレームワークの下記 CSSクラスを使用する。

| CSSクラス | 用途 | ルール |
|-|-|-|
| `.item-head` | 項目単位のラベル要素配置 | `<div>` に使用し、グリッドレイアウトの場合は `<div.grid-col-*>` の直下に配置する |
| `.item-body` | 項目単位のフォーム入力要素配置 | `<div>` に使用し、グリッドレイアウトの場合は `<div.grid-col-*>` の直下に配置する |

```html
<div class="grid-col-1">
  <div class="item-head"><label>項目名</label></div>
  <div class="item-body"><input type="text"></div>
</div>
```

### テーブル要素 CSSクラス
テーブル要素の配置にあたり本フレームワークの下記 CSSクラスを使用する。

| CSSクラス | 用途 | ルール |
|-|-|-|
| `.table` | テーブル要素配置 | `<table>` の親要素となる `<div>` に使用する |

```html
<div class="table"><table></table></div>
```


## 動的リスト表示

リスト（繰り返し部分）のデータ表示について説明します。

### 表示の仕組み
- リスト部分の JSON は連想配列の配列とする。
- リストデータは本フレームワークの JavaScript部品により、下記の要領で表示される。
    1. テンプレートとなる行要素（以下、テンプレート行要素と呼ぶ）を取得する。
    2. テンプレート行要素からリストデータの配列数（行数）分の行要素を生成する。
    3. 1行ごとに連想配列の値をセットする。
- テンプレート行要素は表要素の子要素（先頭）に `<script type="text/html">` 囲みで配置する。

### 動的リスト表示例

```javascript
// レスポンスJSON
{
  "list": [
    {"user_id": "U001", "user_nm": "マイク・デイビス"},
    {"user_id": "U002", "user_nm": "池田健"}
  ]
}
```

```html
<!-- テンプレート（初期状態） -->
<table>
  <thead><tr><th>ID</th><th>名前</th></tr></thead>
  <tbody id="list">
    <script type="text/html">
      <tr><td><input type="text" name="list.user_id"></td><td data-name="list.user_nm"></td></tr>
    </script>
  </tbody>
</table>

<!-- データセット後 -->
<table>
  <thead><tr><th>ID</th><th>名前</th></tr></thead>
  <tbody id="list">
    <script type="text/html">
      <tr><td><input type="text" name="list.user_id"></td><td data-name="list.user_nm"></td></tr>
    </script>
    <tr><td><input type="text" name="list.user_id" value="U001"></td><td data-name="list.user_nm">マイク・デイビス</td></tr>
    <tr><td><input type="text" name="list.user_id" value="U002"></td><td data-name="list.user_nm">池田健</td></tr>
  </tbody>
</table>
```

## 関連ドキュメント

- [Webサービス構成標準 (Java)](../02-develop-standards/11-web-service-structure.md)
- [イベント別コーディングパターン](../02-develop-standards/21-event-coding-pattern.md)
