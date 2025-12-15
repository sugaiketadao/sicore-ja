
/**
 * 初期処理.
 */
const init = async function () {
  // メッセージクリア
  PageUtil.clearMsg();
  // パラメーター取得
  const params = HttpUtil.getUrlParams();
  console.log('#init params:', params);
  if (ValUtil.isBlank(params['user_id'])) {
    // キーがパラメーターに無い場合は新規登録と判定
    // 新規登録初期処理
    initInsert();
    return;
  }
  // 更新初期処理
  await initUpdate(params);
};

/**
 * 新規登録初期処理.
 */
const initInsert = function () {
  // 5行明細追加して表示
  PageUtil.addRow('detail', new Array(5));
};

/**
 * 更新初期処理.
 */
const initUpdate = async function (params) {
  // キー項目を非活性化
  const codeElm = DomUtil.getByName('user_id');
  DomUtil.setEnable(codeElm, false);
  // データ取得 Webサービス呼び出し
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleLoad', params);
  console.log('#initUpdate res:', res);
  // メッセージ表示
  PageUtil.setMsg(res);
  // エラー発生時は処理終了
  if (PageUtil.hasError(res)) {
    return;
  }
  // レスポンスをセット
  PageUtil.setValues(res);
  if (ValUtil.isEmpty(res['detail'] )) {
    // 明細行がゼロ件の場合は5行明細追加して表示
    PageUtil.addRow('detail', new Array(5));
    return;
  }
};

/**
* 行追加処理.
*/
const addRow = function () {
  PageUtil.addRow('detail');
};

/**
* 行削除処理.
*/
const removeRow = function () {
  PageUtil.removeRow('detail.chk', '1');
};

/**
 * 登録・更新処理.
 */
const upsert = async function () {
  // メッセージクリア
  PageUtil.clearMsg();
  // ページ全体の値を取得
  const req = PageUtil.getValues();
  // 登録・更新 Webサービス呼び出し
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleUpsert', req);
  console.log('#upsert res:', res);
  // メッセージ表示
  PageUtil.setMsg(res);
};

/**
 * 削除処理.
 */
const del = async function () {
  // メッセージクリア
  PageUtil.clearMsg();
  // ページ全体の値を取得
  const req = PageUtil.getValues();
  // 削除 Webサービス呼び出し
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleDelete', req);
  console.log('#del res:', res);
  // メッセージ表示
  PageUtil.setMsg(res);
};

/**
 * キャンセル処理.
 */
const cancel = async function () {
  HttpUtil.movePage('listpage.html');
};

// 初期処理実行
init();
