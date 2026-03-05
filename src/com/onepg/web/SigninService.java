package com.onepg.web;

import com.onepg.util.AbstractIoTypeMap;
import com.onepg.util.Io;
import java.util.ArrayList;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;
import com.onepg.util.Io.MsgType;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

/**
 * サインインサービスクラス.
 * @hidden
 */
final class SigninService extends AbstractWebService {

  /** IOキー - サインインID */
  private static final String IOKEY_SIGNIN_ID = "signin_id";
  /** IOキー - サインインパスワード */
  private static final String IOKEY_SIGNIN_PW = "signin_pw";
  /** IOキー - サインインデバッグモードフラグ */
  private static final String IOKEY_SIGNIN_DEBUG = "signin_debug";
  /** セッションキー - JWT */
  private static final String SSKEY_JWT = "token";
  
  /**
   * {@inheritDoc}
   * <ul>
   *   <li>LDAP接続情報・JWT設定は <code>web.properties</code> から取得する。</li>
   *   <li>セッションデータをクリアする。</li>
   *   <li>リクエストからサインインID・パスワードを取得し LDAP 認証する。</li>
   *   <li>認証成功時は JWT を発行しレスポンスに返す。</li>
   *   <li>認証失敗時は JWT をブランクにしてエラーメッセージを返す。</li>
   *   <li>認証失敗時、サインインデバッグモードフラグが有効"1"の場合のみエラー情報を出力する。</li>
   *   <li>パスワードミスからの認証失敗が想定されるため、常時エラー情報は出力しないが、設定ミスからの認証失敗が想定されるため、デバッグモードでスタックを出力する</li>
   *   <li>JWT 検証はおこなわない（未サインイン状態から呼び出すため）。</li>
   * </ul>
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    // セッションデータクリア（AbstractIoTypeMap の制約により #clear() メソッドは使用不可）
    final AbstractIoTypeMap session = io.session();
    for (final String key : new ArrayList<>(session.keySet())) {
      session.remove(key);
    }
    // ログイン情報
    final String id = io.getString(IOKEY_SIGNIN_ID);
    final String pw = io.remove(IOKEY_SIGNIN_PW);

    // LDAP 認証
    final Hashtable<String, String> param = new Hashtable<>();
    param.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    param.put(Context.PROVIDER_URL, ServerUtil.LDAP_URL);
    param.put(Context.SECURITY_AUTHENTICATION, "simple");
    param.put(Context.SECURITY_PRINCIPAL, ServerUtil.LDAP_USER_DN_FMT.formatted(id));
    param.put(Context.SECURITY_CREDENTIALS, pw);

    try {
      // 認証成功すれば例外は発生しないため、ここではクローズ処理のみ行う
      (new InitialDirContext(param)).close();
    } catch (final NamingException e) {
      // 情報ログとして認証失敗を出力
      if (e instanceof AuthenticationException) {
        super.logger.info("LDAP authentication failed. " + LogUtil.joinKeyVal("id", id));
      } else {
        super.logger.error("LDAP server error during authentication. " + LogUtil.joinKeyVal("id", id));
      }
      if (ValUtil.isTrue(io.getStringNullableOrDefault(IOKEY_SIGNIN_DEBUG, ValUtil.OFF))) {
        // サインインデバッグモードフラグが有効"1"の場合のみエラー情報を出力する
        super.logger.error(e, "LDAP authentication error in debug mode. ");
      }
      // 認証失敗は JWT をブランクにしてエラーメッセージを返す
      io.session().put(SSKEY_JWT, ValUtil.BLANK);
      io.putMsg(MsgType.ERROR, "es001");
      return;
    }

    // JWT 発行
    final String token = JwtUtil.createToken(id);
    io.session().put(SSKEY_JWT, token);
  }
}
