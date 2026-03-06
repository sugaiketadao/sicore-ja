/**
 * 初期処理.
 */
const init = async function () {
  // メッセージクリア
  PageUtil.clearMsg();
};

/**
 * サインイン処理.
 * LDAP認証を行い、成功時はポータルページへ遷移する。
 */
const signin = async function () {
  // メッセージクリア
  PageUtil.clearMsg();
  // ページ全体の値を取得
  const req = PageUtil.getValues();
  // サインイン Webサービス呼び出し
  const res = await HttpUtil.callJsonService('/signin', req);
  console.log('#signin res:', res);
  // メッセージ表示
  PageUtil.setMsg(res);
  // レスポンスをセット
  PageUtil.setValues(res);
  // トークン確認
  if (!SessionUtil.hasToken()) {
    // 認証失敗
    return;
  }
  // 認証成功
  // ポータルページへ遷移
  HttpUtil.movePage('../index.html');
};

// 初期処理実行
init();
