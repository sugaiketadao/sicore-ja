以下のテーブル定義からヘッダー明細構成の業務画面プロンプトを作成してください。
プロンプトは docs/21-ai-guides/01-ai-prompt-guide.md のセクション3.3と7.1を参考にしてください。

ヘッダーテーブル:
create table t_order (
  order_id varchar(10)
 ,order_dt date
 ,customer_id varchar(6)
 ,upd_ts timestamp(6)
 ,primary key (order_id)
);

明細テーブル:
create table t_order_detail (
  order_id varchar(10)
 ,line_no numeric(3)
 ,product_id varchar(6)
 ,quantity numeric(5)
 ,unit_price_am numeric(8)
 ,upd_ts timestamp(6)
 ,primary key (order_id, line_no)
);
