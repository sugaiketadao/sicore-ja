# イベント別コーディングパターン

<!-- AI_SKIP_START -->
サンプルプログラムに基づく、イベントごとの実装パターンである。
人間もAIもこのパターンに従えば、類似機能をすぐに作成できる。
<!-- AI_SKIP_END -->

---

## 目次

- [サンプルデータ構造](#サンプルデータ構造)
- [1. 一覧初期処理](#1-一覧初期処理)
- [2. 一覧検索処理](#2-一覧検索処理)
- [3. 編集ページ遷移](#3-編集ページ遷移)
- [4. 編集初期処理（新規/更新判定）](#4-編集初期処理新規更新判定)
- [5. データ取得処理](#5-データ取得処理)
- [6. 登録・更新処理](#6-登録更新処理)
- [7. 削除処理](#7-削除処理)
- [8. 行追加・行削除処理](#8-行追加行削除処理)
- [9. キャンセル処理](#9-キャンセル処理)
- [バリデーションパターン一覧](#バリデーションパターン一覧)
- [メッセージパターン](#メッセージパターン)
- [ファイル命名規則](#ファイル命名規則)
- [参考](#参考)

---

## サンプルデータ構造

### テーブル定義

**ヘッダーテーブル: t_user**
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

**明細テーブル: t_user_pet**
| 項目 | 物理名 | 型 | 備考 |
|------|--------|-----|------|
| ユーザーID | user_id | VARCHAR(4) | PK1 |
| ペット番号 | pet_no | NUMERIC(2) | PK2 |
| ペット名 | pet_nm | VARCHAR(10) | |
| 種類 | type_cs | VARCHAR(2) | DG/CT/BD |
| 性別 | gender_cs | VARCHAR(1) | M/F |
| ワクチン接種済 | vaccine_cs | VARCHAR(1) | Y/N |
| 体重 | weight_kg | NUMERIC(3,1) | |
| 誕生日 | birth_dt | DATE | |
| 更新タイムスタンプ | upd_ts | TIMESTAMP(6) | ログ用。 |

---

## 1. 一覧初期処理

**用途**: 一覧ページを開いた時に初期値を設定する。

<!-- AI_SKIP_START -->
**処理フロー**:
1. ブラウザから URL `pages/app/exmodule/listpage.html` にアクセスする。
2. Webサーバーから HTML・CSS・JavaScript ファイルがブラウザに返される。
3. ブラウザで初期処理 `listpage.js#init` が自動実行される。
    1. メッセージをクリアする。
    2. 初期処理 Webサービス `/services/exmodule/ExampleListInit` を呼び出す。
    3. Webサービスクラス `ExampleListInit` が実行される。
    4. Webサービスのレスポンスがブラウザに返される。
    5. セッションから前回の DB抽出条件を取得する。
    6. Webサービスレスポンスと前回 DB抽出条件をマージする。
    7. マージした値を DB抽出条件エリアにセットする。
<!-- AI_SKIP_END -->

### JavaScript（listpage.js）

```javascript
const init = async function () {
  // メッセージクリア
  PageUtil.clearMsg();
  // Webサービス呼び出し
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListInit');
  // 前回の検索条件を取得（任意）
  const old = StorageUtil.getPageObj('searchConditions');
  // マージしてセット
  Object.assign(res, old);
  PageUtil.setValues(res, DomUtil.getById('searchConditionsArea'));
};

// 初期処理実行
init();
```

### Java（ExampleListInit.java）

```java
public class ExampleListInit extends AbstractDbAccessWebService {

  @Override
  public void doExecute(final Io io) throws Exception {
    // 初期値セット（例：今日の日付）
    final String today = SqlUtil.getToday(getDbConn());
    io.put("birth_dt", today);
  }
}
```

<!-- AI_SKIP_START -->
### 応用ポイント

- 初期表示する選択肢（プルダウン等）をDBから取得してセットする。
- ログインユーザー情報に基づく初期値をセットする。
<!-- AI_SKIP_END -->

---

## 2. 一覧検索処理

**用途**: 検索ボタンを押下した時に、DB抽出条件でデータを取得する。

<!-- AI_SKIP_START -->
**処理フロー**:
1. ユーザーが検索条件を入力し、検索ボタンを押下する。
2. JavaScript `listpage.js#search` が実行される。
    1. メッセージをクリアする。
    2. 一覧エリアをクリアする。
    3. DB抽出条件エリアから検索条件を取得する。
    4. 検索条件をセッションに保存する（次回初期表示用）。
    5. 検索 Webサービス `/services/exmodule/ExampleListSearch` を呼び出す。
    6. Webサービスクラス `ExampleListSearch` が実行される。
        1. バリデーションを実行する（エラー時はエラーメッセージをセットして終了する）。
        2. SqlBuilder で動的SQLを組み立てる。
        3. DB抽出を実行する。
        4. 抽出結果をレスポンスにセットする。
    7. Webサービスのレスポンスがブラウザに返される。
    8. メッセージを表示する（エラー時は終了する）。
    9. 抽出結果を一覧エリアにセットする（テンプレートから行を生成する）。
<!-- AI_SKIP_END -->

### JavaScript（listpage.js）

```javascript
const search = async function () {
  // メッセージクリア
  PageUtil.clearMsg();
  // 一覧クリア
  PageUtil.clearRows('list');
  // 検索条件取得
  const req = PageUtil.getValues(DomUtil.getById('searchConditionsArea'));
  // 検索条件をセッション保存（任意）
  StorageUtil.setPageObj('searchConditions', req);
  // Webサービス呼び出し
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListSearch', req);
  // メッセージ表示
  PageUtil.setMsg(res);
  // エラー時は終了
  if (PageUtil.hasError(res)) {
    return;
  }
  // 結果セット
  PageUtil.setValues(res, DomUtil.getById('searchResultsArea'));
};
```

### Java（ExampleListSearch.java）

```java
public class ExampleListSearch extends AbstractDbAccessWebService {

  @Override
  public void doExecute(final Io io) throws Exception {
    // バリデーション
    validate(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // DB抽出
    getList(io);
  }

  private void getList(final Io io) {
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  u.user_id ");
    sb.addQuery(", u.user_nm ");
    sb.addQuery(", u.email ");
    sb.addQuery(", CASE WHEN u.gender_cs = 'M' THEN '男性' ");
    sb.addQuery("       WHEN u.gender_cs = 'F' THEN '女性' ");
    sb.addQuery("       ELSE 'その他' END gender_dn ");
    sb.addQuery(", u.income_am ");
    sb.addQuery(", u.birth_dt ");
    sb.addQuery(", u.upd_ts ");
    sb.addQuery(" FROM t_user u WHERE 1=1 ");
    // 値がある場合のみ条件追加
    sb.addQnotB(" AND u.user_id = ? ", io.getString("user_id"));
    sb.addQnotB(" AND u.user_nm LIKE '%' || ? || '%' ", io.getString("user_nm"));
    sb.addQnotB(" AND u.email LIKE ? || '%' ", io.getString("email"));
    sb.addQnotB(" AND u.country_cs = ? ", io.getString("country_cs"));
    sb.addQnotB(" AND u.gender_cs = ? ", io.getString("gender_cs"));
    sb.addQnotB(" AND u.spouse_cs = ? ", io.getString("spouse_cs"));
    sb.addQnotB(" AND u.income_am >= ? ", io.getBigDecimalNullable("income_am"));
    sb.addQnotB(" AND u.birth_dt = ? ", io.getDateNullable("birth_dt"));
    sb.addQuery(" ORDER BY u.user_id ");

    // 一括取得（最大5件）
    final IoRows rows = SqlUtil.selectBulk(getDbConn(), sb, 5);
    io.putRows("list", rows);
    io.put("list_size", rows.size());

    if (rows.size() <= 0) {
      io.putMsg(MsgType.INFO, "i0004", new String[] { "0" });
    }
  }

  private void validate(final Io io) throws Exception {
    // 数値チェック
    final String incomeAm = io.getString("income_am");
    if (!ValUtil.isBlank(incomeAm) && !ValUtil.isNumber(incomeAm)) {
      io.putMsg(MsgType.ERROR, "ev012", new String[] { "年収" }, "income_am");
    }
    // 日付チェック
    final String birthDt = io.getString("birth_dt");
    if (!ValUtil.isBlank(birthDt) && !ValUtil.isDate(birthDt)) {
      io.putMsg(MsgType.ERROR, "ev013", new String[] { "誕生日" }, "birth_dt");
    }
  }
}
```

### 一覧HTML（テンプレート部分）

```html
<tbody id="list">
  <script type="text/html">
    <tr>
      <td><input type="text" name="list.user_id" disabled>
        <input type="hidden" name="list.upd_ts"></td>
      <td data-name="list.user_nm"></td>
      <td data-name="list.email"></td>
      <td data-name="list.gender_dn"></td>
      <td data-name="list.income_am" data-value-format-type="num"></td>
      <td data-name="list.birth_dt" data-value-format-type="ymd"></td>
      <td><button type="button" onclick="editMove(this)">編集</button></td>
    </tr>
  </script>
</tbody>
```

<!-- AI_SKIP_START -->
### 応用ポイント

- `addQnotB`: 値がブランクでない場合のみ条件を追加する。
- `selectBulk(conn, sb, 件数)`: 最大件数を指定して取得する。
- `selectBulkAll(conn, sb)`: 全件取得する。
<!-- AI_SKIP_END -->

---

## 3. 編集ページ遷移

**用途**: 一覧から編集ページへ遷移する（キー値を渡す）。

<!-- AI_SKIP_START -->
**処理フロー**:
1. ユーザーが一覧の「編集」ボタンをクリックする。
2. `editMove(btnElm)` が実行される。
   1. ボタンがある行のデータを取得する（`getRowValuesByInnerElm`）。
   2. パラメーター付きでページ遷移する（`HttpUtil.movePage`）。
3. ブラウザが `editpage.html?user_id=xxx&upd_ts=xxx` に遷移する。
4. 編集ページの初期処理へ続く（セクション4参照）。
<!-- AI_SKIP_END -->

### JavaScript（listpage.js）

```javascript
// 編集ボタン処理
const editMove = async function (btnElm) {
  // ボタンがある行のデータを取得
  const req = PageUtil.getRowValuesByInnerElm(btnElm);
  // パラメーター付きで遷移
  HttpUtil.movePage('editpage.html', req);
};

// 新規ボタン処理
const create = async function () {
  // パラメーター無しで遷移（新規登録）
  HttpUtil.movePage('editpage.html');
};
```

<!-- AI_SKIP_START -->
### 応用ポイント

- `getRowValuesByInnerElm(elm)`: ボタン等がある行のデータを取得する。
- 遷移先では `HttpUtil.getUrlParams()` でパラメーターを取得する。
<!-- AI_SKIP_END -->

---

## 4. 編集初期処理（新規/更新判定）

**用途**: 編集ページを開いた時に、新規登録か更新かを判定する。

<!-- AI_SKIP_START -->
**処理フロー**:
1. 編集ページがブラウザに読み込まれる。
2. `init()` が実行される。
   1. メッセージをクリアする。
   2. URLパラメーターを取得する（`HttpUtil.getUrlParams()`）。
   3. キー値の有無で分岐する。
      - **キーなし（新規）**: `initInsert()` → 空の明細行を追加する。
      - **キーあり（更新）**: `initUpdate()` → セクション5のデータ取得へ進む。
<!-- AI_SKIP_END -->

### JavaScript（editpage.js）

```javascript
const init = async function () {
  PageUtil.clearMsg();
  // URLパラメーター取得
  const params = HttpUtil.getUrlParams();

  if (ValUtil.isBlank(params['user_id'])) {
    // キーがない → 新規登録
    initInsert();
    return;
  }
  // キーがある → 更新
  await initUpdate(params);
};

// 新規登録初期処理
const initInsert = function () {
  // 明細5行追加
  PageUtil.addRow('detail', new Array(5));
};

// 更新初期処理
const initUpdate = async function (params) {
  // キー項目を非活性化
  DomUtil.setEnable(DomUtil.getByName('user_id'), false);
  // データ取得
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleLoad', params);
  PageUtil.setMsg(res);
  if (PageUtil.hasError(res)) {
    return;
  }
  PageUtil.setValues(res);
  // 明細がゼロ件なら5行追加
  if (ValUtil.isEmpty(res['detail'])) {
    PageUtil.addRow('detail', new Array(5));
  }
};

init();
```

---

## 5. データ取得処理

**用途**: キーを指定してヘッダーと明細を取得する。

<!-- AI_SKIP_START -->
**処理フロー**:
1. 編集初期処理から呼び出される（更新時のみ）。
2. キー項目を非活性化する（`DomUtil.setEnable`）。
3. Webサービス `ExampleLoad` を呼び出す。
   1. ヘッダーを取得する（`selectOne` でキーと排他チェックを行う）。
   2. 排他エラーなら終了する（他ユーザーが更新済みである）。
   3. 明細を取得する（`selectBulkAll`）。
   4. 結果を返却する。
4. レスポンスをメッセージ表示する（`PageUtil.setMsg`）。
5. エラーがなければ画面に値を設定する（`PageUtil.setValues`）。
6. 明細がゼロ件なら空行を追加する。
<!-- AI_SKIP_END -->

### Java（ExampleLoad.java）

```java
public class ExampleLoad extends AbstractDbAccessWebService {

  @Override
  public void doExecute(final Io io) throws Exception {
    // ヘッダー取得
    getHead(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // 明細取得
    getDetail(io);
  }

  private void getHead(final Io io) {
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  u.user_nm, u.email, u.country_cs ");
    sb.addQuery(", u.gender_cs, u.spouse_cs ");
    sb.addQuery(", u.income_am, u.birth_dt ");
    sb.addQuery(" FROM t_user u ");
    sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));
    sb.addQuery("   AND u.upd_ts = ? ", io.getSqlTimestampNullable("upd_ts"));

    final IoItems head = SqlUtil.selectOne(getDbConn(), sb);
    if (ValUtil.isNull(head)) {
      // 排他制御エラー
      io.putMsg(MsgType.ERROR, "e0002", new String[]{io.getString("user_id")});
      return;
    }
    io.putAll(head);
  }

  private void getDetail(final Io io) {
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  d.pet_no, d.pet_nm, d.type_cs ");
    sb.addQuery(", d.gender_cs, d.vaccine_cs ");
    sb.addQuery(", d.weight_kg, d.birth_dt ");
    sb.addQuery(" FROM t_user_pet d ");
    sb.addQuery(" WHERE d.user_id = ? ", io.getString("user_id"));
    sb.addQuery(" ORDER BY d.pet_no ");

    final IoRows detail = SqlUtil.selectBulkAll(getDbConn(), sb);
    io.putRows("detail", detail);
  }
}
```

<!-- AI_SKIP_START -->
### 応用ポイント

- `upd_ts` で排他制御を行う（他ユーザーの更新を検知する）。
- `selectOne`: 1件取得する（なければnull）。
- `putAll(row)`: 取得結果をそのままレスポンスにマージする。
<!-- AI_SKIP_END -->

---

## 6. 登録・更新処理

**用途**: ヘッダーと明細を登録または更新する。

<!-- AI_SKIP_START -->
**処理フロー**:
1. ユーザーが「登録」ボタンをクリックする。
2. `upsert()` が実行される。
   1. メッセージをクリアする。
   2. ページ全体の値を取得する（`PageUtil.getValues`）。
   3. Webサービス `ExampleUpsert` を呼び出す。
3. サーバー側処理を実行する。
   1. ヘッダーバリデーションを実行する（必須・書式・桁数）。
   2. エラーがある場合は終了する（エラーメッセージを返却する）。
   3. 明細バリデーションを実行する（各行ごと）。
   4. エラーがある場合は終了する。
   5. ヘッダーを登録・更新する（`upd_ts` で新規/更新を判定する）。
   6. 明細を全削除してから全登録する。
   7. 成功メッセージを設定する。
4. レスポンスをメッセージ表示する（`PageUtil.setMsg`）。
<!-- AI_SKIP_END -->

### JavaScript（editpage.js）

```javascript
const upsert = async function () {
  PageUtil.clearMsg();
  // ページ全体の値を取得
  const req = PageUtil.getValues();
  // Webサービス呼び出し
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleUpsert', req);
  PageUtil.setMsg(res);
};
```

### Java（ExampleUpsert.java）

```java
public class ExampleUpsert extends AbstractDbAccessWebService {

  @Override
  public void doExecute(final Io io) throws Exception {
    // ヘッダーバリデーション
    validateHeader(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // 明細バリデーション
    validateDetail(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // ヘッダー登録・更新
    upsertHead(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // 明細削除→登録
    delInsDetail(io);
    // 成功メッセージ
    if (ValUtil.isBlank(io.getString("upd_ts"))) {
      io.putMsg(MsgType.INFO, "i0001", new String[] { io.getString("user_id") });
    } else {
      io.putMsg(MsgType.INFO, "i0002", new String[] { io.getString("user_id") });
    }
  }

  private void validateHeader(final Io io) throws Exception {
    // 必須チェック
    final String userId = io.getString("user_id");
    if (ValUtil.isBlank(userId)) {
      io.putMsg(MsgType.ERROR, "ev001", new String[]{"ユーザーID"}, "user_id");
    } else if (!ValUtil.isAlphabetNumber(userId)) {
      io.putMsg(MsgType.ERROR, "ev011", new String[] { "ユーザーID" }, "user_id");
    } else if (!ValUtil.checkLength(userId, 4)) {
      io.putMsg(MsgType.ERROR, "ev021", new String[] { "ユーザーID", "4" }, "user_id");
    }

    final String userNm = io.getString("user_nm");
    if (ValUtil.isBlank(userNm)) {
      io.putMsg(MsgType.ERROR, "ev001", new String[] { "ユーザー名" }, "user_nm");
    }
    // 他の項目も同様にチェック...
  }

  private void validateDetail(final Io io) throws Exception {
    if (!io.containsKeyRows("detail")) {
      return;
    }
    final IoRows detail = io.getRows("detail");
    for (int rowIdx = 0; rowIdx < detail.size(); rowIdx++) {
      final IoItems row = detail.get(rowIdx);
      // 明細行ごとのチェック
      final String petNm = row.getString("pet_nm");
      if (ValUtil.isBlank(petNm)) {
        // 明細エラーは行インデックス指定
        io.putMsg(MsgType.ERROR, "ev001", new String[] { "ペット名" }, "pet_nm", "detail", rowIdx);
      }
    }
  }

  private void upsertHead(final Io io) {
    if (ValUtil.isBlank(io.getString("upd_ts"))) {
      // 新規登録
      if (!SqlUtil.insertOne(getDbConn(), "t_user", io, "upd_ts")) {
        io.putMsg(MsgType.ERROR, "e0001", new String[] { io.getString("user_id") }, "user_id");
      }
      return;
    }
    // 更新
    if (!SqlUtil.updateOne(getDbConn(), "t_user", io, new String[]{"user_id"}, "upd_ts")) {
      io.putMsg(MsgType.ERROR, "e0002", new String[] { io.getString("user_id") }, "user_id");
    }
  }

  private void delInsDetail(final Io io) {
    final Connection conn = getDbConn();
    // 既存明細を全削除
    SqlUtil.delete(conn, "t_user_pet", io, new String[] { "user_id" });

    if (!io.containsKeyRows("detail")) {
      return;
    }
    // 明細を新規登録
    final IoRows detail = io.getRows("detail");
    final String userId = io.getString("user_id");
    int dno = 0;
    for (final IoItems row : detail) {
      dno++;
      row.put("user_id", userId);
      row.put("pet_no", dno);
      SqlUtil.insertOne(conn, "t_user_pet", row);
    }
  }
}
```

<!-- AI_SKIP_START -->
### 応用ポイント

- `upd_ts` がブランクの場合は、新規登録と判定する。
- 明細は「全削除→全登録」パターンが簡潔である。
- `insertOne(conn, table, io, "upd_ts")`: upd_tsに現在時刻を自動セットする。
- `updateOne(conn, table, io, keys, "upd_ts")`: upd_tsで排他制御を行う。
<!-- AI_SKIP_END -->

---

## 7. 削除処理

**用途**: ヘッダーと明細を削除する。

<!-- AI_SKIP_START -->
**処理フロー**:
1. ユーザーが「削除」ボタンをクリックする。
2. `del()` が実行される。
   1. メッセージをクリアする。
   2. ページ全体の値を取得する。
   3. Webサービス `ExampleDelete` を呼び出す。
3. サーバー側処理を実行する。
   1. ヘッダーを削除する（`deleteOne` で排他チェック付き）。
   2. 排他エラーなら終了する。
   3. 明細を削除する（`delete` で全件削除）。
   4. 成功メッセージを設定する。
4. レスポンスをメッセージ表示する。
<!-- AI_SKIP_END -->

### JavaScript（editpage.js）

```javascript
const del = async function () {
  PageUtil.clearMsg();
  const req = PageUtil.getValues();
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleDelete', req);
  PageUtil.setMsg(res);
};
```

### Java（ExampleDelete.java）

```java
public class ExampleDelete extends AbstractDbAccessWebService {

  @Override
  public void doExecute(final Io io) throws Exception {
    // ヘッダー削除
    deleteHead(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // 明細削除
    deleteDetail(io);
    // 成功メッセージ
    io.putMsg(MsgType.INFO, "i0003", new String[] { io.getString("user_id") });
  }

  private void deleteHead(final Io io) {
    if (!SqlUtil.deleteOne(getDbConn(), "t_user", io, new String[]{"user_id"}, "upd_ts")) {
      io.putMsg(MsgType.ERROR, "e0002", new String[] { io.getString("user_id") }, "user_id");
    }
  }

  private void deleteDetail(final Io io) {
    SqlUtil.delete(getDbConn(), "t_user_pet", io, new String[] { "user_id" });
  }
}
```

---

## 8. 行追加・行削除処理

**用途**: 明細行を動的に追加・削除する。

<!-- AI_SKIP_START -->
**処理フロー（行追加）**:
1. ユーザーが「行追加」ボタンをクリックする。
2. `addRow()` が実行される。
   1. `PageUtil.addRow('detail')` でテンプレートから新しい行を生成する。
   2. 明細テーブルの末尾に行が追加される。

**処理フロー（行削除）**:
1. ユーザーが削除したい行のチェックボックスを選択する。
2. 「行削除」ボタンをクリックする。
3. `removeRow()` が実行される。
   1. `PageUtil.removeRow('detail.chk', '1')` でチェックされた行を削除する。
   2. 該当行がDOMから削除される。
<!-- AI_SKIP_END -->

### JavaScript（editpage.js）

```javascript
// 行追加
const addRow = function () {
  PageUtil.addRow('detail');
};

// 行削除（チェックされた行を削除）
const removeRow = function () {
  PageUtil.removeRow('detail.chk', '1');
};
```

### HTML（明細テンプレート）

```html
<tbody id="detail">
  <script type="text/html">
    <tr>
      <td><input type="checkbox" name="detail.chk" value="1"></td>
      <td data-name="detail.pet_no"></td>
      <td><input type="text" name="detail.pet_nm"></td>
      <td><select name="detail.type_cs">
        <option value="">未選択</option>
        <option value="DG">犬</option>
        <option value="CT">猫</option>
      </select></td>
      <!-- 他項目... -->
    </tr>
  </script>
</tbody>
```

---

## 9. キャンセル処理

**用途**: 編集をキャンセルして一覧へ戻る。

<!-- AI_SKIP_START -->
**処理フロー**:
1. ユーザーが「キャンセル」ボタンをクリックする。
2. `cancel()` が実行される。
   1. `HttpUtil.movePage('listpage.html')` で一覧ページへ遷移する。
3. 一覧ページの初期処理が実行される（セクション1参照）。
<!-- AI_SKIP_END -->

### JavaScript（editpage.js）

```javascript
const cancel = async function () {
  HttpUtil.movePage('listpage.html');
};
```

---

## バリデーションパターン一覧

| チェック内容 | メソッド | 使用例 |
|-------------|---------|--------|
| 必須チェック。 | `ValUtil.isBlank(str)` | `if (ValUtil.isBlank(userId))` |
| 半角英数字チェック。 | `ValUtil.isAlphabetNumber(str)` | `if (!ValUtil.isAlphabetNumber(userId))` |
| 数値チェック。 | `ValUtil.isNumber(str)` | `if (!ValUtil.isNumber(incomeAm))` |
| 日付チェック。 | `ValUtil.isDate(str)` | `if (!ValUtil.isDate(birthDt))` |
| 桁数チェック。 | `ValUtil.checkLength(str, len)` | `if (!ValUtil.checkLength(userId, 4))` |
| 数値桁数チェック。 | `ValUtil.checkLengthNumber(str, int, dec)` | `if (!ValUtil.checkLengthNumber(weight, 3, 1))` |

---

## メッセージパターン

### ヘッダー項目エラー

```java
io.putMsg(MsgType.ERROR, "ev001", new String[] { "ユーザーID" }, "user_id");
// → 「ユーザーID は必須です。」+ user_id項目ハイライト
```

### 明細項目エラー

```java
io.putMsg(MsgType.ERROR, "ev001", new String[] { "ペット名" }, "pet_nm", "detail", rowIdx);
// → 「ペット名 は必須です。」+ detail.pet_nm項目ハイライト（該当行）
```

### 成功メッセージ

```java
io.putMsg(MsgType.INFO, "i0001", new String[] { io.getString("user_id") });
// → 「U001 を登録しました。」
```

---

## ファイル命名規則

| 種類 | ファイル名 | クラス名 |
|------|-----------|----------|
| 一覧初期処理。 | ExampleListInit.java | ExampleListInit |
| 一覧検索処理。 | ExampleListSearch.java | ExampleListSearch |
| データ取得処理。 | ExampleLoad.java | ExampleLoad |
| 登録・更新処理。 | ExampleUpsert.java | ExampleUpsert |
| 削除処理。 | ExampleDelete.java | ExampleDelete |

---

## サンプルコード
- HTML/JavaScript: `pages/app/exmodule/`
- Java: `src/com/example/app/service/exmodule/`
- DB定義/テストデータ: `example_db/example_data_create.sql`
