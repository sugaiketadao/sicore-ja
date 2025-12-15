drop table if exists t_user;
drop table if exists t_user_pet;

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

delete from t_user;
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U001', 'マイク・デイビス', 'mike.davis@example.com', 'US', 'M', 'N', 10000000, '1975-02-10', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U002', '池田健', 'ikeda.ken@example.jp', 'JP', 'M', 'N', 8000000, '1999-05-15', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U003', '岡田由美', 'okada.yumi@example.jp', 'JP', 'F', 'N', 6000000, '1999-08-20', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U004', 'ルーシー・スミス', 'lucy.smith@example.us', 'US', 'F', 'N', 7500000, '1997-12-24', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U005', 'ジュディ・ブラウン', 'judy.brown@example.us', 'US', 'F', 'N', 7200000, '1999-04-15', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U006', 'エレン・ベーカー', 'ellen.baker@example.us', 'US', 'F', 'Y', 9000000, '1985-07-25', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U007', 'マイク・ベーカー', 'mike.baker@example.us', 'US', 'M', 'N', 9500000, '1988-11-30', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U008', 'アン・グリーン', 'anne.green@example.us', 'US', 'F', 'Y', 8500000, '1980-02-15', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U009', '後藤早紀', 'goto.saki@example.jp', 'JP', 'F', 'N', 6000000, '1988-08-20', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U010', '田村大地', 'tamura.daichi@example.jp', 'JP', 'M', 'N', 8000000, '1988-05-15', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U011', 'ルーカス・コスタ', 'lucas.costa@example.br', 'BR', 'M', 'N', 9000000, '1988-12-10', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U012', 'ソフィア・ジョーンズ', 'sophia.jones@example.au', 'AU', 'F', 'N', 7500000, '1989-03-05', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U013', '大道寺美知子', 'daidoji.michiko@example.jp', 'JP', 'F', 'Y', 8000000, '1974-08-03', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U014', '三上俊平', 'mikami.shunpei@example.jp', 'JP', 'M', 'Y', 8000000, '1972-06-06', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U015', '須賀池忠雄', 'sugaike.tadao@example.jp', 'JP', 'M', 'Y', 8000000, '2000-01-01', '2025-01-01 12:00:00.000000');
insert into t_user (user_id, user_nm, email, country_cs, gender_cs, spouse_cs, income_am, birth_dt, upd_ts) values ('U016', '坂上忍', 'sakagami.shinobu@example.jp', 'JP', 'M', 'Y', 100000000, '1987-06-01', '2025-01-01 12:00:00.000000');

delete from t_user_pet;
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U001', 1, 'ポチ', 'DG', 'M', 'Y', 5.0, '2015-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U001', 2, 'タマ', 'CT', 'F', 'N', 2.5, '2016-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U002', 1, 'ミケ', 'CT', 'F', 'Y', 4.1, '2017-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U003', 1, 'シロ', 'DG', 'M', 'Y', 6.3, '2014-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U004', 1, 'レオ', 'DG', 'M', 'N', 7.2, '2013-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U005', 1, 'モモ', 'CT', 'F', 'Y', 3.8, '2016-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U006', 1, 'ハナ', 'DG', 'F', 'Y', 4.5, '2015-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U007', 1, 'マックス', 'DG', 'M', 'N', 8.0, '2014-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U008', 1, 'ルナ', 'CT', 'F', 'Y', 2.9, '2017-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U009', 1, 'チビ', 'DG', 'M', 'Y', 5.4, '2015-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U010', 1, 'クロ', 'CT', 'M', 'N', 3.6, '2016-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U011', 1, 'ボビー', 'DG', 'M', 'Y', 7.5, '2014-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U012', 1, 'ミミ', 'CT', 'F', 'Y', 2.7, '2017-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U013', 1, 'サクラ', 'DG', 'F', 'Y', 4.0, '2015-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U014', 1, 'ロッキー', 'DG', 'M', 'N', 6.8, '2014-01-01', '2025-01-01 12:00:00.000000'); 
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U015', 1, 'キャロ', 'DG', 'F', 'Y', 2.6, '2009-01-07', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U015', 2, 'ニコ', 'DG', 'F', 'Y', 3.2, '2012-03-20', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 1, '佐藤ツトム', 'DG', 'M', 'Y', 3.2, '2010-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 2, '高橋ヨースケ', 'DG', 'M', 'Y', 3.2, '2011-02-11', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 3, '円山ダイチ', 'DG', 'M', 'Y', 3.2, '2012-03-21', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 4, '森田パグゾウ', 'DG', 'M', 'Y', 3.2, '2013-04-30', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 5, '黒田アキ', 'DG', 'F', 'Y', 3.2, '2014-05-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 6, '山本ちくわ', 'CT', 'M', 'N', 3.2, '2015-06-11', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 7, '竹原がんも', 'CT', 'M', 'N', 3.2, '2016-07-21', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 8, '竹原こんぶ', 'CT', 'F', 'N', 3.2, '2017-08-31', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 9, '清名わらび', 'CT', 'F', 'N', 3.2, '2018-09-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 10, 'いちろう', 'CT', 'M', 'N', 3.2, '2019-10-11', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 11, 'じろう', 'CT', 'M', 'N', 3.2, '2020-11-21', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 12, 'さぶろう', 'CT', 'M', 'N', 3.2, '2021-12-31', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 13, 'しろう', 'CT', 'M', 'N', 3.2, '2022-01-01', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 14, 'うみ', 'CT', 'F', 'N', 3.2, '2023-02-11', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 15, 'そら', 'CT', 'M', 'N', 3.2, '2024-03-21', '2025-01-01 12:00:00.000000');
insert into t_user_pet (user_id, pet_no, pet_nm, type_cs, gender_cs, vaccine_cs, weight_kg, birth_dt, upd_ts) values ('U016', 16, 'リョーマ', 'CT', 'M', 'N', 3.2, '2025-04-30', '2025-01-01 12:00:00.000000');
