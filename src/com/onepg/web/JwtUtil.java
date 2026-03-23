package com.onepg.web;

import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * JWT ユーティリティクラス.
 * <ul>
 *   <li>JSON Web Token の生成・検証を行う。</li>
 *   <li>署名アルゴリズムは HMAC-SHA256（HS256）を使用する。</li>
 * </ul>
 * @hidden
 */
final class JwtUtil {

  /** 署名アルゴリズム. */
  private static final String SIGN_ALG = "HmacSHA256";
  /** JWT ヘッダー（Base64URL エンコード）. */
  private static final String JWT_HEADER = base64UrlEncode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
  /** 署名秘密鍵. */
  private static final String SECRET_KEY = ServerUtil.PROP_MAP.getStringOrDefault("jwt.secret.key", "must-be-configured-in-web.properties");
  /** 有効期間（秒）. */
  private static final long EXPIRE_SEC = ServerUtil.PROP_MAP.getLongOrDefault("jwt.expire.sec", 86_400L); // デフォルトは24時間

  /**
   * コンストラクタ.
   */
  private JwtUtil() {
    // 処理なし
  }

  /**
   * JWT 生成.
   *
   * @param id サインインID
   * @return JWT 文字列
   */
  static String createToken(final String id) {
    final long now = System.currentTimeMillis() / 1_000L;
    final String payloadJson = "{\"sub\":\"%s\",\"iat\":%d,\"exp\":%d}".formatted(id, now, now + EXPIRE_SEC);
    final String payload = base64UrlEncode(payloadJson);
    final String signData = JWT_HEADER + "." + payload;
    return signData + "." + sign(signData);
  }

  /**
   * JWT 検証.
   *
   * @param token JWT 文字列
   * @return サインインID
   */
  static String validateToken(final String token) {
    if (ValUtil.isBlank(token)) {
      throw new RuntimeException("JWT token is blank.");
    }
    // エラーログ用トークン（部分マスク済み）
    final String errToken = ValUtil.substring(token, 0, 1) + "***";

    final String[] parts = token.split("\\.", -1);
    if (parts.length != 3) {
      throw new RuntimeException("JWT format is invalid." + LogUtil.joinKeyVal("token", errToken));
    }
    // 署名検証
    final String signData = parts[0] + "." + parts[1];
    final String signed = sign(signData);
    if (!signed.equals(parts[2])) {
      throw new RuntimeException("JWT signature is invalid." + LogUtil.joinKeyVal("token", errToken));
    }
    // ペイロード検証（有効期限）
    final String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    final long exp = getExpField(json, errToken);
    if (System.currentTimeMillis() / 1_000L > exp) {
      throw new RuntimeException("JWT token is expired." + LogUtil.joinKeyVal("token", errToken));
    }
    return getSubField(json, errToken);
  }

  /**
   * HMAC-SHA256 署名生成.
   *
   * @param data 署名対象データ
   * @return Base64URL エンコードされた署名
   */
  private static String sign(final String data) {
    try {
      final Mac mac = Mac.getInstance(SIGN_ALG);
      final SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), SIGN_ALG);
      mac.init(keySpec);
      final byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
    } catch (final Exception e) {
      throw new RuntimeException("JWT signature generation failed.", e);
    }
  }

  /**
   * ペイロードから sub フィールドを取得.
   *
   * @param payloadJson ペイロード JSON 文字列
   * @param errToken エラーログ用トークン（部分マスク済み）
   * @return sub フィールド値（サインインID）
   */
  private static String getSubField(final String payloadJson, final String errToken) {
    final String key = "\"sub\":\"";
    final int start = payloadJson.indexOf(key);
    if (start < 0) {
      throw new RuntimeException("JWT payload 'sub' field not found." + LogUtil.joinKeyVal("token", errToken));
    }
    final int fStart = start + key.length();
    final int fEnd = payloadJson.indexOf("\"", fStart);
    if (fEnd < 0) {
      throw new RuntimeException("JWT payload 'sub' field is malformed." + LogUtil.joinKeyVal("token", errToken));
    }
    return payloadJson.substring(fStart, fEnd);
  }

  /**
   * ペイロードから exp フィールドを取得.
   *
   * @param payloadJson ペイロード JSON 文字列
   * @param errToken エラーログ用トークン（部分マスク済み）
   * @return exp フィールド値（有効期限）
   */
  private static long getExpField(final String payloadJson, final String errToken) {
    final String key = "\"exp\":";
    final int start = payloadJson.indexOf(key);
    if (start < 0) {
      throw new RuntimeException("JWT payload 'exp' field not found." + LogUtil.joinKeyVal("token", errToken));
    }
    final int fStart = start + key.length();
    int fEnd = fStart;
    while (fEnd < payloadJson.length() && Character.isDigit(payloadJson.charAt(fEnd))) {
      fEnd++;
    }
    return Long.parseLong(payloadJson.substring(fStart, fEnd));
  }
  
  /**
   * Base64URL エンコード変換.<br>
   * <ul>
   * <li>JWT 仕様（RFC 7515）に準拠し、末尾のパディング文字（<code>=</code>）を除いた Base64URL 形式にエンコードする。</li>
   * </ul>
   *
   * @param value 変換対象文字列
   * @return 変換後文字列
   */
  private static String base64UrlEncode(final String value) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }
}
