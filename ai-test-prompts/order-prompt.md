## 業務画面作成依頼

### 機能名
発注管理

### モジュール名
ordermng

### ヘッダーテーブル
```sql
create table t_order (
  order_no varchar(10)
 ,order_dt date
 ,supplier_cd varchar(6)
 ,supplier_nm varchar(50)
 ,delivery_dt date
 ,status_cs varchar(2)
 ,total_am numeric(12)
 ,remarks varchar(200)
 ,upd_ts timestamp(6)
 ,primary key (order_no)
);
```

### 明細テーブル
```sql
create table t_order_detail (
  order_no varchar(10)
 ,line_no numeric(3)
 ,goods_cd varchar(10)
 ,goods_nm varchar(50)
 ,quantity numeric(6)
 ,unit_price numeric(10)
 ,amount numeric(12)
 ,upd_ts timestamp(6)
 ,primary key (order_no, line_no)
);
```

### コード値定義
- status_cs: 10=起票, 20=発注済, 30=入荷済, 90=取消

### 画面種類
一覧＋編集（ヘッダー明細構成）

### 検索条件（一覧画面）
- 発注No: 前方一致
- 仕入先コード: 完全一致
- 仕入先名: 部分一致
- 発注日From〜To: 範囲検索
- ステータス: 完全一致（セレクトボックス）

### 一覧表示項目
- 発注No
- 発注日
- 仕入先コード
- 仕入先名
- 納品予定日
- ステータス
- 合計金額

### 編集画面（ヘッダー部）
- 発注No（新規時は自動採番、編集時は読み取り専用）
- 発注日（必須、日付）
- 仕入先コード（必須）
- 仕入先名（必須）
- 納品予定日（日付）
- ステータス（必須、セレクトボックス）
- 備考（テキストエリア）

### 編集画面（明細部）
- 明細No（行番号、自動採番）
- 物品コード（必須）
- 物品名（必須）
- 数量（必須、数値）
- 単価（必須、数値）
- 金額（自動計算：数量×単価）
- 行削除ボタン
- 行追加ボタン

### バリデーション（編集画面）
- 発注日: 必須
- 仕入先コード: 必須、半角英数6桁
- 仕入先名: 必須
- ステータス: 必須
- 物品コード: 必須、半角英数10桁以内
- 物品名: 必須
- 数量: 必須、正の整数
- 単価: 必須、正の数値

### 業務ロジック
- 合計金額は明細の金額を合計して自動計算
- 明細の金額は数量×単価で自動計算
- 明細は最低1行必須

### 生成依頼
上記の要件で以下のファイルを生成してください：
- pages/app/ordermng/listpage.html
- pages/app/ordermng/listpage.js
- pages/app/ordermng/editpage.html
- pages/app/ordermng/editpage.js
- src/com/example/app/service/ordermng/package-info.java
- src/com/example/app/service/ordermng/OrderListInit.java
- src/com/example/app/service/ordermng/OrderListSearch.java
- src/com/example/app/service/ordermng/OrderLoad.java
- src/com/example/app/service/ordermng/OrderUpsert.java
- src/com/example/app/service/ordermng/OrderDelete.java

明細テーブルは編集画面でヘッダーと一緒に編集できるようにしてください。
実装パターンは docs/02-develop-standards/21-event-coding-pattern.md に従ってください。
