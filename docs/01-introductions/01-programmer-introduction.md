# SICoreフレームワーク プログラマー向け紹介

本資料では、SICoreフレームワークの特徴と基本的な使い方を紹介します。

---

## 1. フレームワークの特徴

### 1.1 プロトタイプ駆動開発

```html
<!-- プロトタイプ時 -->
<input type="text" name="user_nm" value="マイク・デイビス">

<!-- 本番時も同じHTML -->
<input type="text" name="user_nm">
```

**利点**:
- デザイナーが作成したHTMLをそのまま使用できる。
- プロトタイプ段階で操作性を確認できる。
- テンプレートエンジンが不要である。

### 1.2 JSONのみで通信

```
ブラウザ             サーバー
  │                   │
  │------ JSON ------>│
  │　　　　　　  Webサービス処理
  │<------ JSON ------│
```

**利点**:
- プロトコルが単純である。
- フロントエンドとバックエンドが完全に分離されている。
- API開発も画面開発も同じ仕組みで行える。
- モバイルアプリとの連携も容易である。

### 1.3 URLがそのままクラス名

```
URL: http://localhost:8080/services/exmodule/ExampleListSearch
↓ 自動マッピング
実行クラス: com.example.app.service.exmodule.ExampleListSearch
```

**利点**:
- ルーティング設定が不要である。
- アノテーションが不要である。
- URLからクラス名が明確に判別できる。

### 1.4 ステートレスアーキテクチャ

- サーバー側にセッションを持たない。
- ブラウザの`sessionStorage`で状態を管理する。
- スケールアウトが容易である。

### 1.5 セッションストレージの3階層スコープ

| スコープ | メソッド | 使用例 |
|-|-|-|
| ページ単位 | `get/setPageObj()` | 一覧ページで検索条件を保持する。 |
| モジュール単位 | `get/setModuleObj()` | ページ間で入力中データを保持する。 |
| システム単位 | `get/setSystemObj()` | ログイン情報を保持する。 |

### 1.6 独自CSS（外部フレームワーク不使用）

```html
<!-- 12分割グリッドシステム -->
<div class="grid-row">
  <div class="grid-col-6">全幅の1/2</div>
  <div class="grid-col-6">全幅の1/2</div>
</div>

<!-- レスポンシブ対応済み -->
<!-- スマートフォン: 列が縦並びに自動切替 -->
<!-- タブレット: 列内のラベル要素とフォーム入力要素が縦並びに自動切替 -->
```

**利点**:
- Bootstrap等の外部フレームワークが不要である。
- 必要最小限のCSSのみで構成されている（onepg-base.css 1ファイル）。
- レスポンシブに対応済みである（PC・タブレット・スマートフォン）。
- 学習コストが低い（覚えるクラス名が少ない）。

---

## 2. データフロー

```
[ブラウザ] listpage.html
     │
     │ 1. 検索ボタン押下
     ▼
[JavaScript] listpage.js
     │ 2. PageUtil.getValues() → { user_id: "U001", user_nm: "マイク・デイビス" }
     │ 3. HttpUtil.callJsonService() でJSON送信
     ▼
[フレームワーク]
     │ 4. リクエストJSON → Io オブジェクトに変換
     ▼
[Java] ExampleListSearch.java
     │ 5. SqlBuilder でSQL組み立て
     │ 6. SqlUtil.selectBulk() でDB抽出
     │ 7. 結果 → Io オブジェクトに格納
     ▼
[フレームワーク]
     │ 8. Io オブジェクト → レスポンスJSON に変換
     ▼
[JavaScript]
     │ 9. PageUtil.setValues() でブラウザ表示
     ▼
[ブラウザ] DB抽出結果が一覧に表示
```

---

## 3. HTML⇔JSON自動変換

### 3.1 HTMLからJSON

```html
<input type="text" name="user_id" value="U001">
<input type="text" name="user_nm" value="マイク・デイビス">

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
```

```javascript
const req = PageUtil.getValues();
// {
//   "user_id": "U001",
//   "user_nm": "マイク・デイビス",
//   "list": [
//     {"pet_nm": "ポチ", "weight_kg": "5.0"},
//     {"pet_nm": "タマ", "weight_kg": "2.5"}
//   ]
// }
```

- `<form>`タグは不要である。
- `name`属性さえあれば値を取得できる。

### 3.2 JSONからHTML

```javascript
const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListSearch', req);
PageUtil.setValues(res);
```

```html
<!-- 自動でセットされる -->
<input type="text" name="user_id" value="U001">
<span data-name="user_nm">マイク・デイビス</span>
```

---

## 4. 独自HTML属性

### 4.1 data-name属性

表示専用であり、値の取得は行わない:

```html
<span data-name="user_nm"></span>
<td data-name="list.pet_nm"></td>
```

### 4.2 data-check-off-value属性

チェックボックスOFF時に送信する値を指定する:

```html
<input type="checkbox" name="is_dog" value="1" data-check-off-value="0">
```

### 4.3 data-value-format-type属性

表示値を自動フォーマットする:

| 設定値 | 項目値 | フォーマット値 |
|-|-|-|
| `num` | `1000000` | `1,000,000` |
| `ymd` | `20251231` | `2025/12/31` |
| `hms` | `123456` | `12:34:56` |

```html
<input type="text" name="income_am" data-value-format-type="num">
```

---

## 5. Webサービス実装

### 5.1 基本構造

```java
package com.example.app.service.exmodule;

public class ExampleListSearch extends AbstractDbAccessWebService {
  
  @Override
  public void doExecute(Io io) throws Exception {
    // 処理実装
  }
}
```

- `AbstractDbAccessWebService`を継承する。
- `io`がリクエストであり、同時にレスポンスでもある。

### 5.2 Ioクラス

```java
// 型安全に値を取得する
String userId = io.getString("user_id");
long incomeAm = io.getLong("income_am");
LocalDate birthDt = io.getDateNullable("birth_dt"); // nullを返す可能性があることを明示する

// 値をセットする（型指定不要）
io.put("user_nm", "マイク・デイビス");
io.put("income_am", 1230000);
```

### 5.3 リクエストとレスポンス一体

```java
public void doExecute(Io io) throws Exception {
  SqlBuilder sb = new SqlBuilder();
  sb.addQuery("SELECT u.user_nm, u.email FROM t_user u ");
  sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));

  final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
  if (!ValUtil.isNull(row)) {
    io.putAll(row);
  } else {
    io.putMsg(MsgType.ERROR, "x0001");
  }
  // ioがそのままレスポンスになる
}
```

---

## 6. トランザクション管理

```java
public class ExampleUpsert extends AbstractDbAccessWebService {
  
  @Override
  public void doExecute(Io io) throws Exception {
    if (ValUtil.isBlank(io.getString("user_id"))) {
      io.putMsg(MsgType.ERROR, "ev001", new String[]{"ユーザーID"}, "user_id");
      return; // → エラー有り → 自動ロールバック
    }
    
    SqlUtil.upsert(getDbConn(), "t_user_header", "user_id", io);
    
    io.putMsg(MsgType.INFO, "i0002", new String[]{io.getString("user_id")});
    // 正常終了（エラー無し） → 自動コミット
  }
}
```

**利点**:
- コミット・ロールバックを明示的に書く必要がない。
- エラー時のロールバック忘れが発生しない。
- 1リクエスト = 1トランザクションであることが保証される。

---

## 7. SqlBuilder

```java
SqlBuilder sb = new SqlBuilder();
sb.addQuery("SELECT u.user_id, u.user_nm, u.email ");
sb.addQuery(" FROM t_user u WHERE 1=1 ");

// 値がある場合のみ条件追加
sb.addQnotB(" AND u.user_id = ? ", io.getString("user_id"));
sb.addQnotB(" AND u.user_nm LIKE '%' || ? || '%' ", io.getString("user_nm"));

sb.addQuery(" ORDER BY u.user_id ");

IoRows rows = SqlUtil.selectBulk(getDbConn(), sb);
```

**利点**:
- SQLとパラメーターを同時に組み立てられる。
- SQLインジェクション対策済みである。
- `toString()`でSQLを確認できる。

---

## 8. メッセージ表示

**msg.json**:
```json
{
  "ev001": "{0} は必須です。",
  "i0002": "{0} を登録しました。"
}
```

**Java側**:
```java
io.putMsg(MsgType.ERROR, "ev001", new String[]{"ユーザーID"}, "user_id");
io.putMsg(MsgType.INFO, "i0002", new String[]{"U001"});
```

**JSON出力**:
```json
{
  "_msgs": [{"type": "error", "id": "ev001", "text": "ユーザーID は必須です。", "item": "user_id"}],
  "_has_err": true
}
```

---

## 9. Ioクラスのバグ対策

### 9.1 NULL安全

```java
String value = io.getString("key");         // "" を返す。
String value = io.getStringNullable("key"); // null を返す（明示的）。
```

### 9.2 型安全

```java
int age = io.getInt("age");
LocalDate birthDt = io.getDateNullable("birth_dt");
BigDecimal income = io.getBigDecimal("income_am");
```

### 9.3 キー重複チェック

```java
io.put("user_id", "U001");
io.put("user_id", "U002");      // エラーになる。
io.putForce("user_id", "U002"); // 明示的に上書きする。
```

### 9.4 存在しないキー

```java
io.getString("userid"); // 存在しないキーはエラーになる。
io.getStringOrDefault("userid", ""); // デフォルト値を返す。
```

### 9.5 ディープコピーによる安全性

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

---

## 10. 項目名統一のメリット

```sql
CREATE TABLE t_user (user_id VARCHAR(10), user_nm VARCHAR(50));
```

```html
<input name="user_id">
<input name="user_nm">
```

```java
String userId = io.getString("user_id");
SqlUtil.insertOne(conn, "t_user", io);
```

**メリット**:
- 変換コードが不要である。
- マッピング処理が不要である。
- バグを削減できる。
- DB設計書がそのまま仕様書として機能する。

---

## 関連ドキュメント

- [マネージャー向け紹介](../01-introductions/02-manager-introduction.md)
- [Webページ構成標準 (HTML/JavaScript/CSS)](../02-develop-standards/01-web-page-structure.md)
- [Webサービス構成標準 (Java)](../02-develop-standards/11-web-service-structure.md)
- [バッチ処理構成標準 (Java)](../02-develop-standards/12-batch-processing-structure.md)
