-- 発注管理テストデータ
drop table if exists t_order;
drop table if exists t_order_detail;

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

delete from t_order;
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD001', '2025-12-01', 'SUP001', '株式会社山田商事', '2025-12-10', '10', 150000, '急ぎの発注です', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD002', '2025-12-02', 'SUP002', '田中物産株式会社', '2025-12-15', '20', 280000, '', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD003', '2025-12-03', 'SUP001', '株式会社山田商事', '2025-12-20', '30', 95000, '入荷完了', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD004', '2025-11-15', 'SUP003', '佐藤工業株式会社', '2025-11-25', '90', 50000, 'キャンセル', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD005', '2025-12-04', 'SUP004', '鈴木電機株式会社', '2025-12-25', '10', 420000, '年末納品希望', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD006', '2025-11-20', 'SUP002', '田中物産株式会社', '2025-12-01', '30', 180000, '', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD007', '2025-12-01', 'SUP005', '高橋金属工業', '2025-12-18', '20', 320000, '分割納品可', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD008', '2025-11-28', 'SUP003', '佐藤工業株式会社', '2025-12-05', '30', 75000, '', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD009', '2025-12-02', 'SUP001', '株式会社山田商事', '2025-12-12', '10', 560000, '大口発注', '2025-01-01 12:00:00.000000');
insert into t_order (order_no, order_dt, supplier_cd, supplier_nm, delivery_dt, status_cs, total_am, remarks, upd_ts) values ('ORD010', '2025-12-03', 'SUP006', 'ABC Trading Co.', '2025-12-28', '10', 890000, '海外仕入先', '2025-01-01 12:00:00.000000');

delete from t_order_detail;
-- ORD001の明細
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD001', 1, 'GOODS001', 'ボールペン（黒）', 100, 150, 15000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD001', 2, 'GOODS002', 'ノートA4', 50, 300, 15000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD001', 3, 'GOODS003', 'クリアファイル', 200, 600, 120000, '2025-01-01 12:00:00.000000');

-- ORD002の明細
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD002', 1, 'GOODS004', 'コピー用紙A4', 20, 3500, 70000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD002', 2, 'GOODS005', 'トナーカートリッジ', 5, 12000, 60000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD002', 3, 'GOODS006', 'USBメモリ32GB', 10, 1500, 15000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD002', 4, 'GOODS007', 'マウス', 15, 2000, 30000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD002', 5, 'GOODS008', 'キーボード', 10, 3500, 35000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD002', 6, 'GOODS009', 'モニタースタンド', 10, 7000, 70000, '2025-01-01 12:00:00.000000');

-- ORD003の明細
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD003', 1, 'GOODS010', '付箋紙セット', 100, 200, 20000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD003', 2, 'GOODS011', 'ホワイトボードマーカー', 50, 250, 12500, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD003', 3, 'GOODS012', '消しゴム', 100, 50, 5000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD003', 4, 'GOODS013', 'シャープペン', 50, 350, 17500, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD003', 5, 'GOODS014', '定規30cm', 100, 400, 40000, '2025-01-01 12:00:00.000000');

-- ORD004の明細（取消分）
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD004', 1, 'GOODS015', 'デスクライト', 10, 5000, 50000, '2025-01-01 12:00:00.000000');

-- ORD005の明細
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD005', 1, 'GOODS016', 'ノートPC', 5, 80000, 400000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD005', 2, 'GOODS017', 'PCバッグ', 5, 4000, 20000, '2025-01-01 12:00:00.000000');

-- ORD006の明細
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD006', 1, 'GOODS018', 'プリンター', 2, 45000, 90000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD006', 2, 'GOODS019', 'プリンターケーブル', 2, 1500, 3000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD006', 3, 'GOODS004', 'コピー用紙A4', 20, 3500, 70000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD006', 4, 'GOODS020', 'インクカートリッジセット', 2, 8500, 17000, '2025-01-01 12:00:00.000000');

-- ORD007の明細
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD007', 1, 'GOODS021', 'スチールラック', 4, 25000, 100000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD007', 2, 'GOODS022', 'ファイルキャビネット', 2, 35000, 70000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD007', 3, 'GOODS023', 'オフィスチェア', 5, 30000, 150000, '2025-01-01 12:00:00.000000');

-- ORD008の明細
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD008', 1, 'GOODS024', '電源タップ', 15, 2500, 37500, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD008', 2, 'GOODS025', 'LANケーブル5m', 25, 1500, 37500, '2025-01-01 12:00:00.000000');

-- ORD009の明細
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 1, 'GOODS026', 'デスク', 10, 35000, 350000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 2, 'GOODS027', 'デスクマット', 10, 3000, 30000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 3, 'GOODS028', 'ペン立て', 20, 800, 16000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 4, 'GOODS029', '書類トレー', 10, 1200, 12000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 5, 'GOODS030', 'ゴミ箱', 10, 1500, 15000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 6, 'GOODS031', 'カレンダー', 10, 500, 5000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 7, 'GOODS032', '時計', 5, 8000, 40000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD009', 8, 'GOODS033', '観葉植物', 4, 23000, 92000, '2025-01-01 12:00:00.000000');

-- ORD010の明細
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD010', 1, 'GOODS034', 'サーバーラック', 1, 250000, 250000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD010', 2, 'GOODS035', 'UPS装置', 2, 180000, 360000, '2025-01-01 12:00:00.000000');
insert into t_order_detail (order_no, line_no, goods_cd, goods_nm, quantity, unit_price, amount, upd_ts) values ('ORD010', 3, 'GOODS036', 'ネットワークスイッチ', 4, 70000, 280000, '2025-01-01 12:00:00.000000');
