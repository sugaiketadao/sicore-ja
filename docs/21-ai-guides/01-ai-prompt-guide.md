# AI指示ガイド（業務画面作成用）

本資料は、AIに業務画面を作成させるための指示方法を標準化したガイドです。

---

## 目次

- [1. 基本的な指示の流れ](#1-基本的な指示の流れ)
- [2. 必要な入力情報](#2-必要な入力情報)
- [3. プロンプトテンプレート](#3-プロンプトテンプレート)
- [4. DB項目→HTML要素 変換ルール](#4-db項目html要素-変換ルール)
- [5. コード値定義の書き方](#5-コード値定義の書き方)
- [6. 生成されるファイル一覧](#6-生成されるファイル一覧)
- [7. 実例：ユーザー管理画面](#7-実例ユーザー管理画面)
- [8. エラーメッセージ定義](#8-エラーメッセージ定義)
- [9. 参考資料](#9-参考資料)

---

## 1. 基本的な指示の流れ

```
1. テーブル定義を提示
2. 画面種類を指定（一覧＋編集 / 一覧のみ / 編集のみ）
3. コード値を定義
4. バリデーション要件を指定
5. 生成依頼
6. コンパイルエラー修正依頼
7. 動作確認
8. 不具合修正依頼
```

> **Note**: 手順6〜8の詳細は [AI指示ガイド（デバッグ・修正用）](02-ai-debug-guide.md) を参照してください。

---

## 2. 必要な入力情報

### 2.1 必須情報

| 項目 | 説明 | 例 |
|------|------|-----|
| 機能名 | 画面の機能名（日本語） | ユーザー管理 |
| モジュール名 | ディレクトリ/パッケージ名（英字小文字） | usermst |
| テーブル定義 | CREATE TABLE形式 | 下記参照 |
| 画面種類 | 一覧＋編集 / 一覧のみ / 編集のみ | 一覧＋編集 |

### 2.2 任意情報

| 項目 | 説明 | 例 |
|------|------|-----|
| コード値定義 | 選択肢の値と表示名 | 性別: M=男性, F=女性 |
| 検索条件 | 一覧の検索条件項目 | ユーザーID（前方一致）、名前（部分一致） |
| バリデーション | 必須・桁数・書式 | ユーザーID: 必須、4桁、半角英数字 |
| 初期値 | 初期表示する値 | 誕生日に今日の日付 |

---

## 3. プロンプトテンプレート

### 3.1 一覧＋編集画面（標準パターン）

```markdown
## 業務画面作成依頼

### 機能名
{機能名}

### モジュール名
{モジュール名}

### テーブル定義
```sql
create table t_{テーブル名} (
  {カラム定義}
  ,primary key ({主キー})
);
```

### コード値定義
- {カラム名}_cs: {コード}={表示名}, {コード}={表示名}

### 画面種類
一覧＋編集

### 検索条件（一覧画面）
- {項目名}: {検索方式}

### バリデーション（編集画面）
- {項目名}: {チェック内容}

### 生成依頼
上記の要件で以下のファイルを生成してください：
- pages/app/{モジュール名}/listpage.html
- pages/app/{モジュール名}/listpage.js
- pages/app/{モジュール名}/editpage.html
- pages/app/{モジュール名}/editpage.js
- src/com/example/app/service/{モジュール名}/{Module}ListInit.java
- src/com/example/app/service/{モジュール名}/{Module}ListSearch.java
- src/com/example/app/service/{モジュール名}/{Module}Load.java
- src/com/example/app/service/{モジュール名}/{Module}Upsert.java
- src/com/example/app/service/{モジュール名}/{Module}Delete.java

実装パターンは docs/02-develop-standards/21-event-coding-pattern.md に従ってください。
```

### 3.2 一覧のみ（参照画面）

```markdown
## 業務画面作成依頼

### 機能名
{機能名}

### モジュール名
{モジュール名}

### テーブル定義
{CREATE TABLE}

### 画面種類
一覧のみ（参照専用）

### 生成依頼
- pages/app/{モジュール名}/listpage.html
- pages/app/{モジュール名}/listpage.js
- src/com/example/app/service/{モジュール名}/{Module}ListInit.java
- src/com/example/app/service/{モジュール名}/{Module}ListSearch.java
```

### 3.3 ヘッダー＋明細画面

```markdown
## 業務画面作成依頼

### 機能名
{機能名}

### モジュール名
{モジュール名}

### ヘッダーテーブル
```sql
create table t_{ヘッダー} (
  {カラム定義}
  ,primary key ({主キー})
);
```

### 明細テーブル
```sql
create table t_{明細} (
  {カラム定義}
  ,primary key ({ヘッダー主キー}, {明細連番})
);
```

### 画面種類
一覧＋編集（ヘッダー明細構成）

### 生成依頼
明細テーブルは編集画面でヘッダーと一緒に編集できるようにしてください。
```

---

## 4. DB項目→HTML要素 変換ルール

### 4.1 サフィックスによる自動判定

| サフィックス | 意味 | HTML要素 | 例 |
|-------------|------|----------|-----|
| `_id` | ID/コード | `<input type="text">` | user_id |
| `_nm` | 名称 | `<input type="text">` | user_nm |
| `_cs` | コード区分 | `<select>` or `<radio>` | gender_cs |
| `_dt` | 日付 | `<input>` + `data-value-format-type="ymd"` | birth_dt |
| `_ts` | タイムスタンプ | `<input type="hidden">` | upd_ts |
| `_am` | 金額 | `<input>` + `data-value-format-type="num"` | income_am |
| `_kg`, `_cm` | 数量 | `<input>` + `data-value-format-type="num"` | weight_kg |
| `_no` | 番号 | `<input type="text">` or 表示のみ | pet_no |

### 4.2 型による判定

| DB型 | HTML要素 |
|------|----------|
| VARCHAR(1) + `_cs` | `<radio>` or `<checkbox>` |
| VARCHAR(2〜) + `_cs` | `<select>` |
| NUMERIC | `<input type="text" class="align-right">` |
| DATE | `<input>` + `data-value-format-type="ymd"` |
| TIMESTAMP | `<input type="hidden">`（排他制御用） |

### 4.3 HTML属性の使い分け

| 用途 | 属性 | 例 |
|------|------|-----|
| 入力＆取得 | `name` | `<input name="user_id">` |
| 表示のみ | `data-name` | `<td data-name="user_nm">` |
| 数値カンマ | `data-value-format-type="num"` | `1000000` → `1,000,000` |
| 日付スラッシュ | `data-value-format-type="ymd"` | `20251231` → `2025/12/31` |
| チェックOFF値 | `data-check-off-value` | `<input type="checkbox" data-check-off-value="N">` |

---

## 5. コード値定義の書き方

### 5.1 標準フォーマット

```markdown
### コード値定義
- gender_cs（性別）: M=男性, F=女性
- country_cs（国）: JP=日本, US=アメリカ, BR=ブラジル, AU=オーストラリア
- type_cs（種類）: DG=犬, CT=猫, BD=鳥
- spouse_cs（配偶者有無）: Y=あり, N=なし ※チェックボックス
- vaccine_cs（接種済）: Y=接種済み, N=未接種 ※チェックボックス
```

### 5.2 HTML生成例

**selectの場合**:
```html
<select name="country_cs">
  <option value="">未選択</option>
  <option value="JP">日本</option>
  <option value="US">アメリカ</option>
</select>
```

**radioの場合**:
```html
<label><input type="radio" name="gender_cs" value="">未選択</label>
<label><input type="radio" name="gender_cs" value="M">男性</label>
<label><input type="radio" name="gender_cs" value="F">女性</label>
```

**checkboxの場合**:
```html
<label><input type="checkbox" name="spouse_cs" value="Y" data-check-off-value="N">あり</label>
```

---

## 6. 生成されるファイル一覧

### 6.1 一覧＋編集画面の場合

```
pages/app/{module}/
├── listpage.html      # 一覧画面HTML
├── listpage.js        # 一覧画面JavaScript
├── editpage.html      # 編集画面HTML
└── editpage.js        # 編集画面JavaScript

src/com/example/app/service/{module}/
├── {Module}ListInit.java    # 一覧初期処理
├── {Module}ListSearch.java  # 一覧検索処理
├── {Module}Load.java        # データ取得処理
├── {Module}Upsert.java      # 登録・更新処理
└── {Module}Delete.java      # 削除処理
```

### 6.2 ファイル命名規則

| ファイル種類 | 命名規則 | 例 |
|-------------|---------|-----|
| 一覧HTML | listpage.html | listpage.html |
| 編集HTML | editpage.html | editpage.html |
| 一覧JS | listpage.js | listpage.js |
| 編集JS | editpage.js | editpage.js |
| Java一覧初期 | {Module}ListInit.java | UserListInit.java |
| Java一覧検索 | {Module}ListSearch.java | UserListSearch.java |
| Javaデータ取得 | {Module}Load.java | UserLoad.java |
| Java登録更新 | {Module}Upsert.java | UserUpsert.java |
| Java削除 | {Module}Delete.java | UserDelete.java |

---

## 7. 実例：ユーザー管理画面

### 7.1 完全なプロンプト例

```markdown
## 業務画面作成依頼

### 機能名
ユーザー管理

### モジュール名
exmodule

### ヘッダーテーブル
```sql
create table t_user (
  user_id varchar(4)
 ,user_nm varchar(20)
 ,email varchar(50)
 ,country_cs varchar(2)
 ,gender_cs varchar(1)
 ,spouse_cs varchar(1)
 ,income_am numeric(10)
 ,birth_dt date
 ,upd_ts timestamp(6)
 ,primary key (user_id)
);
```

### 明細テーブル
```sql
create table t_user_pet (
  user_id varchar(4)
 ,pet_no numeric(2)
 ,pet_nm varchar(10)
 ,type_cs varchar(2)
 ,gender_cs varchar(1)
 ,vaccine_cs varchar(1)
 ,weight_kg numeric(3,1)
 ,birth_dt date
 ,upd_ts timestamp(6)
 ,primary key (user_id, pet_no)
);
```

### コード値定義
- country_cs（出身国）: JP=日本, US=アメリカ, BR=ブラジル, AU=オーストラリア
- gender_cs（性別）: M=男性, F=女性
- spouse_cs（配偶者）: Y=あり, N=なし ※チェックボックス
- type_cs（ペット種類）: DG=犬, CT=猫, BD=鳥
- vaccine_cs（ワクチン接種）: Y=接種済み, N=未接種 ※チェックボックス

### 画面種類
一覧＋編集（ヘッダー明細構成）

### 検索条件（一覧画面）
- user_id: 前方一致
- user_nm: 部分一致
- email: 部分一致
- country_cs: 完全一致（プルダウン）
- gender_cs: 完全一致（ラジオ）
- spouse_cs: チェック時のみ条件
- income_am: 以上
- birth_dt: 以降

### バリデーション（編集画面）
ヘッダー:
- user_id: 必須、4桁固定、半角英数字
- user_nm: 必須、20桁以内
- email: 50桁以内
- income_am: 数値、10桁以内
- birth_dt: 日付形式

明細:
- pet_nm: 必須、10桁以内
- weight_kg: 数値（小数1桁）

### 初期値
- 新規登録時は明細5行を空で表示

### 生成依頼
上記の要件で以下のファイルを生成してください：
- pages/app/exmodule/listpage.html
- pages/app/exmodule/listpage.js
- pages/app/exmodule/editpage.html
- pages/app/exmodule/editpage.js
- src/com/example/app/service/exmodule/ExampleListInit.java
- src/com/example/app/service/exmodule/ExampleListSearch.java
- src/com/example/app/service/exmodule/ExampleLoad.java
- src/com/example/app/service/exmodule/ExampleUpsert.java
- src/com/example/app/service/exmodule/ExampleDelete.java

実装パターンは docs/02-develop-standards/21-event-coding-pattern.md に従ってください。
```

---

## 8. エラーメッセージ定義

### 8.1 標準メッセージID

**バリデーションエラー（ev***）**:
| ID | メッセージ | 用途 |
|----|-----------|------|
| ev001 | {0} は必須です。 | 必須チェック |
| ev011 | {0} は半角英数字で入力してください。 | 書式チェック |
| ev012 | {0} は数値で入力してください。 | 数値チェック |
| ev013 | {0} は日付形式で入力してください。 | 日付チェック |
| ev021 | {0} は {1} 桁以内で入力してください。 | 桁数チェック |
| ev022 | {0} は {1} 桁で入力してください。 | 固定桁数チェック |

**業務エラー（e****）**:
| ID | メッセージ | 用途 |
|----|-----------|------|
| e0001 | {0} は既に登録されています。 | 重複エラー |
| e0002 | {0} は他のユーザーに更新されました。 | 排他制御エラー |

**成功メッセージ（i****）**:
| ID | メッセージ | 用途 |
|----|-----------|------|
| i0001 | {0} を登録しました。 | 新規登録成功 |
| i0002 | {0} を更新しました。 | 更新成功 |
| i0003 | {0} を削除しました。 | 削除成功 |

---

## 9. 参考資料

- [プロンプト生成テンプレート](11-ai-prompt-generator-template.md) - テーブル定義からプロンプトを生成するためのテンプレート
- [イベント別コーディングパターン](../02-develop-standards/21-event-coding-pattern.md) - 実装パターンの詳細
- [Webページ構成標準](../02-develop-standards/01-web-page-structure.md) - HTML/JS構成の詳細
- [Webサービス構成標準](../02-develop-standards/11-web-service-structure.md) - Java構成の詳細
- サンプル: `pages/app/exmodule/`, `src/com/example/app/service/exmodule/`
