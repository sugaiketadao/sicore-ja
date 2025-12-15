
/**
 * 初期処理.
 */
const init = async function () {
  // メッセージクリア
  PageUtil.clearMsg();
  // 一覧初期処理 Webサービス呼び出し
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListInit');
  // 前回の DB抽出条件を取得
  const old = StorageUtil.getPageObj('searchConditions');
  // レスポンスと前回の DB抽出条件をマージ
  Object.assign(res, old);
  // DB抽出条件エリアにレスポンスをセット
  PageUtil.setValues(res, DomUtil.getById('searchConditionsArea'));
};

/**
 * 検索ボタン処理.
 */
const search = async function () {
  // メッセージクリア
  PageUtil.clearMsg();
  // DB抽出結果エリアをクリア
  PageUtil.clearRows('list');
  // DB抽出条件エリアの値を取得
  const req = PageUtil.getValues(DomUtil.getById('searchConditionsArea'));
  // 今回の DB抽出条件をセッション保存
  StorageUtil.setPageObj('searchConditions', req); 
  // 一覧検索 Webサービス呼び出し
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListSearch', req);
  // メッセージ表示
  PageUtil.setMsg(res);
  // エラー発生時は処理終了
  if (PageUtil.hasError(res)) {
    return;
  }
  // DB抽出結果エリアにレスポンスをセット
  PageUtil.setValues(res, DomUtil.getById('searchResultsArea'));
};

/**
 * 編集ボタン処理.
 */
const editMove = async function (btnElm) {
  // ボタンがある行のデータを取得
  const req = PageUtil.getRowValuesByInnerElm(btnElm);
  // 行データをパラメーターとして編集ページへ遷移
  HttpUtil.movePage('editpage.html', req);
};

/**
 * 新規ボタン処理.
 */
const create = async function () {
  // パラメーター無し（新規登録）で編集ページへ遷移
  HttpUtil.movePage('editpage.html');
};

// 初期処理実行
init();

