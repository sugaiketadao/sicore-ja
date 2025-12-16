# AI指示ガイド（デバッグ・修正用）

本資料は、AIが生成したコードのデバッグおよび修正を依頼する際の指示方法を説明します。

---

## 目次

- [1. デバッグの基本フロー](#1-デバッグの基本フロー)
- [2. コンパイルエラー修正依頼](#2-コンパイルエラー修正依頼)
- [3. 動作確認](#3-動作確認)
- [4. 不具合修正依頼](#4-不具合修正依頼)
- [5. よくあるエラーと対処法](#5-よくあるエラーと対処法)

---

## 1. デバッグの基本フロー

```
1. コンパイルエラー修正依頼
   ↓
2. 動作確認（サーバー起動・画面操作）
   ↓
3. 不具合修正依頼（エラーログ・期待動作を提示）
   ↓
4. 動作確認（修正後の再確認）
   ↓
5. 必要に応じて 3〜4 を繰り返す
```

---

## 2. コンパイルエラー修正依頼

### 2.1 基本的な依頼方法

コード生成後、コンパイルエラーが発生した場合は、以下のように依頼します。

**プロンプト例1（シンプル）**:
```
[エラーファイルを開いた状態で依頼]
コンパイルエラーを直して
```

**プロンプト例2（パッケージ指定）**:
```
com.example.app.service.ordermng パッケージ配下のコンパイルエラーを直して
```

**プロンプト例3（エラー箇所を選択して提示）**:
```
[エラー箇所をエディタで選択した状態で依頼]
このコンパイルエラーを修正して
```

**プロンプト例4（エラー箇所を貼り付けて提示）**:
```
[エラーファイルを開いた状態で依頼]
以下のコンパイルエラーを修正して

PropertiesUtil.getString
```

### 2.2 よくあるコンパイルエラーの原因

| エラー種類 | 原因 | 対処法 |
|-----------|------|--------|
| メソッドが見つからない | APIの誤用 | 正しいAPI仕様を確認して修正依頼 |
| クラスが見つからない | import不足 | import文の追加を依頼 |
| 型の不一致 | 戻り値・引数の型違い | 正しい型への変換を依頼 |
| 値の上書き | Ioクラスで既に存在するキーを上書き | 上書きコードを削除 または強制上書き |

---

## 3. 動作確認

### 3.1 サーバー起動

コンパイルエラー解消後、サーバーを起動して動作確認を行います。

**起動手順**:
1. `src/com/onepg/web/StandaloneServerStarter.java` を実行
2. コンソールに起動完了メッセージが表示されるまで待機
3. ブラウザで対象画面にアクセス

**停止手順**:
- `src/com/onepg/web/StandaloneServerStopper.java` を実行
- Javaクラスを修正した場合はサーバー再起動が必要です。

### 3.2 画面アクセス

**URL形式**:
```
http://localhost:{ポート}/pages/app/{モジュール名}/listpage.html
http://localhost:{ポート}/pages/app/{モジュール名}/editpage.html
ポートは 'config\web.properties' 参照
```

**確認項目**:
- [ ] VS Code のコンソールにエラーが出力されないか
- [ ] 画面が正常に表示されるか
- [ ] 初期表示処理が正常に動作するか
- [ ] 検索・登録・更新・削除が正常に動作するか
- [ ] バリデーションが正常に動作するか
- [ ] エラーメッセージが正しく表示されるか

### 3.3 エラー発生時の情報収集

動作確認中にエラーが発生したとき、以下の情報を収集します。

**収集する情報**:
1. **コンソールのエラーログ**: スタックトレース全体をコピー
2. **ブラウザの開発者ツール**: ネットワークタブ、コンソールタブのエラー
3. **操作手順**: エラー発生までの操作手順
4. **期待動作**: 本来どう動作すべきか

---

## 4. 不具合修正依頼

### 4.1 エラーログを提示する場合

実行時エラーが発生した場合は、エラーログを提示して修正を依頼します。

**プロンプト例**:
```
エラーを修正して
java.lang.RuntimeException: Key already exists. key="user_id"
 at com.onepg.util.AbstractIoTypeMap.validateKeyForPut(AbstractIoTypeMap.java:173)
 at com.onepg.util.AbstractIoTypeMap.putVal(AbstractIoTypeMap.java:206)
 at com.onepg.util.AbstractIoTypeMap.putAllByMap(AbstractIoTypeMap.java:989)
 at com.onepg.util.AbstractIoTypeMap.putAll(AbstractIoTypeMap.java:961)
 at com.example.app.service.exmodule.ExampleLoad.getHead(ExampleLoad.java:60)
 at com.example.app.service.exmodule.ExampleLoad.doExecute(ExampleLoad.java:23)
```

**ポイント**:
- スタックトレースの最初の数行（エラー種類と発生箇所）を含める
- エラーメッセージ全体が長い場合は、関連する部分のみ抽出

### 4.2 期待動作と実際の動作を提示する場合

エラーログがない場合や、動作が期待と異なる場合は、期待動作と実際の動作を提示します。

**プロンプト例**:
```
保存ボタンを押すと「保存しました」と表示されるはずが、何も表示されない。
修正して。
```

```
一覧画面で検索すると、検索条件に関係なく全件表示される。
タイトルの部分一致で検索できるように修正して。
```

### 4.3 修正依頼のテンプレート

再現手順や仕様が複雑な不具合を修正する場合は、以下のテンプレートを使用します。

**テンプレート**:
```markdown
## 不具合報告

### 発生箇所
{画面名・機能名}

### 操作手順
1. {手順1}
2. {手順2}
3. {手順3}

### 期待動作
{本来どう動作すべきか}

### 実際の動作
{実際にどう動作したか}

### エラーログ（ある場合）
```
{エラーログ}
```

### 修正依頼
上記の不具合を修正してください。
```

---

## 5. よくあるエラーと対処法

### 5.1 Key already exists（キー重複エラー）

**症状**:
```
java.lang.RuntimeException: Key already exists. key="user_id"
 at com.onepg.util.AbstractIoTypeMap.validateKeyForPut(AbstractIoTypeMap.java:173)
 at com.onepg.util.AbstractIoTypeMap.putVal(AbstractIoTypeMap.java:206)
 at com.onepg.util.AbstractIoTypeMap.putAllByMap(AbstractIoTypeMap.java:989)
 at com.onepg.util.AbstractIoTypeMap.putAll(AbstractIoTypeMap.java:961)
 at com.example.app.service.exmodule.ExampleLoad.getHead(ExampleLoad.java:60)
 at com.example.app.service.exmodule.ExampleLoad.doExecute(ExampleLoad.java:23)
```

**原因**:
- `Io` クラスに同じキーで値を2回設定しようとしている。
- `Io.put()` は同一キーへの上書きを禁止しているため、既に存在するキーに再度 `put()` を呼ぶとエラーになる。
- `Io.putAll()` でも同様のエラーになる。

**よくあるパターン**:
- 画面から送信されたキー（例: `user_id`）を含む SELECT結果を、`io.putAll(row)` で設定している

**対処法**:

1. **SELECT文から重複キーを除外する**:
   画面から送信された値は既に `Io` に格納されているため、SELECT で再取得する必要はない。
   ```java
   // NG: SELECT結果に画面から送信済みのキー（user_id）が含まれている
   sb.addQuery("SELECT ");
   sb.addQuery("  u.user_id "); // ← user_id を取得
   sb.addQuery(", u.user_nm ");
   sb.addQuery(", u.email ");
   sb.addQuery(" FROM t_user u ");
   sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id")); // ← io に user_id が存在している
   final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
   io.putAll(row); // ← user_id が重複してエラー
   
   // OK: SELECT文から user_id を除外する
   sb.addQuery("SELECT ");
   // ...（user_id は SELECT しない）
   sb.addQuery("  u.user_nm ");
   sb.addQuery(", u.email ");
   sb.addQuery(" FROM t_user u ");
   sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id")); 
   final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
   io.putAll(row); // ← 重複なし
   ```

2. **強制上書きが必要な場合は `putAllForce()` を使用する**:
   ```java
   // 強制的に上書きする場合
   io.putAllForce(row);
   ```

**プロンプト例**:
```
Key already exists エラーが発生しています。
画面から送信済みのキーを再設定しているコードを削除してください。

java.lang.RuntimeException: Key already exists. key="user_id"
 at com.onepg.util.AbstractIoTypeMap.validateKeyForPut(AbstractIoTypeMap.java:173)
 at com.onepg.util.AbstractIoTypeMap.putVal(AbstractIoTypeMap.java:206)
 at com.onepg.util.AbstractIoTypeMap.putAllByMap(AbstractIoTypeMap.java:989)
 at com.onepg.util.AbstractIoTypeMap.putAll(AbstractIoTypeMap.java:961)
 at com.example.app.service.exmodule.ExampleLoad.getHead(ExampleLoad.java:60)
 at com.example.app.service.exmodule.ExampleLoad.doExecute(ExampleLoad.java:23)
```


### 5.2 JavaScript実行エラー

**症状**:
ブラウザのコンソールに JavaScript エラーが表示される。

**対処法**:
ブラウザの開発者ツールからエラーメッセージをコピーして提示。

**プロンプト例**:
```
ブラウザコンソールに以下のエラーが表示されます。修正してください。

Uncaught TypeError: Cannot read properties of undefined (reading 'value')
    at editpage.js:45
```

---

## 6. 効率的なデバッグのコツ

### 6.1 段階的に確認する

1. **コンパイルエラー**: まずコンパイルが通ることを確認
2. **画面表示**: 画面が表示されることを確認
3. **初期処理**: 初期表示処理が動作することを確認
4. **各機能**: 検索・登録・更新・削除を順に確認

### 6.2 エラー情報は具体的に

- スタックトレースは省略せずに提示
- 操作手順は具体的に記述
- 期待動作と実際の動作を明確に区別

### 6.3 修正後は必ず再確認

AIが修正したコードは、必ず再度動作確認を行ってください。
修正により別の問題が発生する場合もあるため、関連機能も確認する。
