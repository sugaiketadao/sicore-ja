/**
 * 値操作ユーティリティクラス.
 * @class
 */
const ValUtil = /** @lends ValUtil */ {
  /**
   * <code>null</code> チェック.<br>
   * <ul>
   * <li>Object が <code>null</code> かチェックする。</li>
   * </ul>
   *
   * @param {Object} obj チェック対象
   * @returns {boolean} <code>null</code> の場合は <code>true</code>
   */
  isNull : function(obj) {
    // undefined
    if (obj === void 0) {
      return true;
    }
    // null
    if (obj === null) {
      return true;
    }
    return false;
  },

  /**
   * ブランクチェック.<br>
   * <ul>
   * <li>文字列が 半角スペースのみ／長さゼロ／<code>null</code> のいずれかかチェックする。</li>
   * </ul>
   *
   * @param {string} str チェック対象
   * @returns {boolean} ブランクの場合は <code>true</code>
   */
  isBlank : function(str) {
    if (ValUtil.isNull(str)) {
      return true;
    }
    return String(str).trim().length === 0;
  },

  /**
   * <code>null</code> ブランク置換.<br>
   * <ul>
   * <li>文字列が <code>null</code> の場合はブランクまたは置き換え文字を返す。</li>
   * </ul>
   *
   * @param {string} value チェック対象
   * @param {string} [rep] 置き換え文字（省略可能）省略した場合はブランクを返す。
   * @returns {string} <code>null</code> の場合は ブランクまたは置き換え文字
   */
  nvl : function(value, rep) {
    if (ValUtil.isNull(rep)) {
      rep = '';
    }
    if (ValUtil.isNull(value)) {
      return rep;
    }
    return value;
  },

  /**
   * ブランク置換.<br>
   * <ul>
   * <li>文字列がブランクの場合は置き換え文字を返す。</li>
   * </ul>
   *
   * @param {string} value チェック対象
   * @param {string} rep 置換文字
   * @returns {string} ブランクの場合は置き換え文字
   */
  bvl : function(value, rep) {
    if (ValUtil.isBlank(value)) {
      return rep;
    }
    return value;
  },

  /**
   * 文字列安全切り取り.
   *
   * @param {string} value 対象文字列
   * @param {number} [beginIndex] 開始インデックス（省略可能）省略値 0
   * @param {number} [endIndex] 終了インデックス（省略可能）省略値文字列長
   * @returns {string} 切り取り文字列
   */
  substring: function(value, beginIndex, endIndex) {
    if (ValUtil.isNull(value)) {
      return '';
    }
    // 省略値の補完
    beginIndex = beginIndex || 0;
    endIndex = endIndex || value.length;
    // 範囲外補正
    if (endIndex > value.length) {
      endIndex = value.length;
    }
    // 開始位置が終了位置以降、または開始位置が文字列長以降の場合は空文字を返す
    if (beginIndex < 0 || beginIndex >= endIndex || beginIndex >= value.length) {
      return '';
    }
    return value.substring(beginIndex, endIndex);
  },

  /**
   * 文字列比較.<br>
   * <ul>
   * <li><code>null</code> は空文字として比較する。</li>
   * </ul>
   *
   * @param {string} val1 比較対象その1
   * @param {string} val2 比較対象その2
   * @returns {boolean} 同値の場合は <code>true</code>
   */
  equals : function(val1, val2) {
    return ValUtil.nvl(val1) === ValUtil.nvl(val2) ;
  },

  /**
   * 連想配列比較.<br>
   * <ul>
   *   <li>比較対象の両方に存在するキーの値を比較し、どちらか一方だけに存在するキーは比較対象外とする。</li>
   *   <li>比較対象のどちらかが連想配列でない場合は「同値でない」と判断する。</li>
   * </ul>
   *
   * @param {Object} obj1 比較対象その1
   * @param {Object} obj2 比較対象その2
   * @param {string} ignoreKeys 比較対象外キー（複数指定可能）
   * @returns {boolean} 内容が同値の場合は <code>true</code>
   */
  equalsObj : function(obj1, obj2, ignoreKeys) {
    if (!ValUtil.isObj(obj1)) {
      return false;
    }
    if (!ValUtil.isObj(obj2)) {
      return false;
    }
    const ignoreKeyAry = Array.prototype.slice.call(arguments, 2);
    for (const key in obj1){
      // 比較対象外場合は無視する
      if (ignoreKeyAry.indexOf(key) >= 0) {
        continue;
      }
      // 比較対象その2 にキーが無い場合は無視する
      // ループと混同しないように比較値（<code>false</code>）を書いている
      if (key in obj2 === false) {
        continue;
      }

      const val1 = obj1[key];
      const val2 = obj2[key];

      const t = ValUtil.toType(val1);
      if (t === 'object') {
        if (!ValUtil.equalsObj(val1, val2, ignoreKeys)) {
          return false;
        }
        continue;
      }

      if (t === 'array') {
        // 配列の場合、１つの要素が連想配列である前提で処理する。
        if (val1.length !== val2.length) {
          return false;
        }
        for (let i = 0; i < val1.length; i++) {
          if (!ValUtil.equalsObj(val1[i], val2[i], ignoreKeys)) {
            return false;
          }
        }
        continue;
      }

      if (!ValUtil.equals(val1, val2)) {
        return false;
      }
    }
    return true;
  },

  /**
   * 空チェック.<br>
   * <ul>
   *   <li><code>null</code> が渡された場合は <code>true</code> を返す。</li>
   *   <li>文字列が渡された場合は isBlank() と同じ結果を返す。</li>
   *   <li>配列が渡された場合は長さゼロかチェックする。</li>
   *   <li>HTML要素取得結果が渡された場合は長さゼロかチェックする。</li>
   *   <li>連想配列が渡された場合はキー配列の長さゼロかチェックする。</li>
   * </ul>
   *
   * @param {Object} obj チェック対象
   * @returns {boolean} 空の場合は <code>true</code>
   */
  isEmpty : function(obj) {
    if (ValUtil.isNull(obj)) {
      return true;
    }
    const t = ValUtil.toType(obj);
    if (t === 'string') {
      return ValUtil.isBlank(obj);
    }
    if (t === 'array' || t === 'arguments' || t === 'nodelist' || t === 'htmlcollection') {
      return (obj.length === 0);
    }
    if (t === 'object') {
      return (Object.keys(obj).length === 0);
    }
    // 'number' 'boolean' は判断できている時点で空ではない
    return false;
  },
  
  /** @private 正の整数チェック正規表現 */
  _IS_NUM_UNSIGNED_INT: /^([1-9]\d*|0)$/,
  /** @private 正の小数チェック正規表現 */
  _IS_NUM_UNSIGNED_FLOAT: /^([1-9]\d*|0)(\.\d+)?$/,
  /** @private 整数チェック正規表現 */
  _IS_NUM_INT: /^[-]?([1-9]\d*|0)$/,
  /** @private 小数チェック正規表現 */
  _IS_NUM_FLOAT: /^[-]?([1-9]\d*|0)(\.\d+)?$/,
  /**
   * 数値チェック.<br>
   * <ul>
   * <li>文字列が数値として有効かチェックする。</li>
   * </ul>
   *
   * @param {string} value チェック対象
   * @param {boolean} [minusNg] マイナス値をNGとする場合 <code>true</code>（省略可能）
   * @param {boolean} [decNg] 小数をNGとする場合 <code>true</code>（省略可能）
   * @returns {boolean} 有効な場合は <code>true</code>
   */
  isNum : function(value, minusNg, decNg) {
    if (ValUtil.isNull(value)) {
      return false;
    }
    const t = ValUtil.toType(value);
    if (t !== 'number' && t !== 'string') {
      return false;
    }
    if (t === 'number') {
      if (isNaN(value)) {
        return false;
      }
      return true;
    }
    if (minusNg && decNg) {
      return ValUtil._IS_NUM_UNSIGNED_INT.test(value);
    } else if (minusNg) {
      return ValUtil._IS_NUM_UNSIGNED_FLOAT.test(value);
    } else if (decNg) {
      return ValUtil._IS_NUM_INT.test(value);
    }
    return ValUtil._IS_NUM_FLOAT.test(value);
  },

  /**
   * 実在日チェック.<br>
   * <ul>
   * <li>文字列が日付として有効かチェックする。</li>
   * </ul>
   *
   * @param {string} yyyymmdd チェック対象（YYYYMMDD）
   * @returns {boolean} 有効な場合は <code>true</code>
   */
  isDate : function(yyyymmdd) {
    if (!ValUtil.isNum(yyyymmdd)) {
      return false;
    }
    if (yyyymmdd.length !== 8) {
      return false;
    }
    const y = ~~yyyymmdd.substring(0, 4);
    const m = ~~yyyymmdd.substring(4, 6) - 1;
    const d = ~~yyyymmdd.substring(6, 8);
    if (m > 11 || d > 31) {
      return false;
    }
    try {
      const date = new Date(y, m, d);
      if (date.getFullYear() !== y || date.getMonth() !== m || date.getDate() !== d) {
        return false;
      }
    } catch(e) {
      return false;
    }
    return true;
  },

  /** @private 真偽値「真」とみなす文字列の配列 */
  _TRUE_VALUES: ['1', 'true', 'yes', 'on'],

  /**
   * 真偽値チェック.<br>
   * <ul>
   * <li>文字列が真偽値「真」とみなす値かチェックする。</li>
   * <li>下記の評価をおこなう。
   *   <ol>
   *     <li>"1", "true", "yes", "on"（すべて半角）は <code>true</code>。</li>
   *     <li><code>null</code> またはブランクは <code>false</code> を含み、上記以外は <code>false</code>。</li>
   *     <li>大文字小文字を区別しない。</li>
   *     <li>左右の半角ブランクは無視する。</li>
   *     <li>boolean値はそのまま返す。</li>
   *   </ol>
   * </li>
   * </ul>
   *
   * @param {string|boolean} val チェック対象
   * @returns {boolean} 真偽値「真」とみなす場合は <code>true</code>
   */
  isTrue: function(val) {
    if (ValUtil.isNull(val)) {
      return false;
    }
    if (ValUtil.toType(val) === 'boolean') {
      return val;
    }
    const lowVal = ('' + val).trim().toLowerCase();
    return (ValUtil._TRUE_VALUES.indexOf(lowVal) >= 0);
  },

  /**
   * 文字列→Dateオブジェクト変換.<br>
   * <ul>
   * <li>日付文字列を Dateオブジェクトに変換する。</li>
   * </ul>
   *
   * @param {string} yyyymmdd 変換対象（YYYYMMDD）
   * @returns {Date} Dateオブジェクト
   */
  toDate : function(yyyymmdd) {
    if (!ValUtil.isDate(yyyymmdd)) {
      return null;
    }
    const y = ~~yyyymmdd.substring(0, 4);
    const m = ~~yyyymmdd.substring(4, 6) - 1;
    const d = ~~yyyymmdd.substring(6, 8);
    const date = new Date(y, m, d);
    return date;
  },

  /**
   * Dateオブジェクト→文字列変換.<br>
   * <ul>
   * <li>Dateオブジェクトを日付文字列に変換する。</li>
   * </ul>
   *
   * @param {Date} dateObj 変換対象
   * @returns {string} 日付文字列（YYYYMMDD）
   */
  dateTo : function(dateObj) {
    if (ValUtil.isNull(dateObj)) {
      return null;
    }
    return ValUtil._formatDate(dateObj, 'YYYYMMDD');
  },

  /**
   * 配列チェック.
   *
   * @param {Object} obj チェック対象
   * @returns {boolean} 配列の場合は <code>true</code>
   */
  isAry : function(obj) {
    if (ValUtil.isNull(obj)) {
      return false;
    }
    const t = ValUtil.toType(obj);
    return (t === 'array');
  },

  /**
   * 連想配列チェック.
   *
   * @param {Object} obj チェック対象
   * @returns {boolean} 連想配列の場合は <code>true</code>
   */
  isObj : function(obj) {
    if (ValUtil.isNull(obj)) {
      return false;
    }
    const t = ValUtil.toType(obj);
    return (t === 'object');
  },

  /**
   * 左文字詰め.
   *
   * @param {string} value 処理対象
   * @param {string} pad 詰める文字
   * @param {number} len 詰めた後の文字長さ
   * @returns {string} 左文字詰め後の文字列
   */
  lpad : function(value, pad, len) {
    const pads = pad.repeat(len);
    return (pads + value).slice(len * -1);
  },

  /**
   * 右文字詰め.
   *
   * @param {string} value 処理対象
   * @param {string} pad 詰める文字
   * @param {number} len 詰めた後の文字長さ
   * @returns {string} 右文字詰め後の文字列
   */
  rpad : function(value, pad, len) {
    const pads = pad.repeat(len);
    return (value + pads).substring(0, len);
  },


  /**
   * オブジェクト型取得.<br>
   * <ul>
   * <li>typeof では <code>null</code> や配列も 'object' となるので詳細な判断が必要な場合に使用する。</li>
   * </ul>
   *
   * @param {Object} obj オブジェクト
   * @returns {string} 型文字列 'undefined', 'null', 'boolean', 'number', 'string', 'array', 'object' など
   */
  toType : function(obj) {
    return Object.prototype.toString.call(obj).slice(8, -1).toLowerCase();
  },

  /**
   * @private
   * Dateオブジェクトフォーマット変換.
   *
   * @param {Date} dateObj Dateオブジェクト
   * @param {string} formatter フォーマット文字列
   * @returns {string} フォーマット変換後文字列
   */
  _formatDate : function(dateObj, formatter) {
    formatter = formatter.replace(/YYYY/g, dateObj.getFullYear());
    formatter = formatter.replace(/MM/g, ('0' + (dateObj.getMonth() + 1)).slice(-2));
    formatter = formatter.replace(/DD/g, ('0' + dateObj.getDate()).slice(-2));
    formatter = formatter.replace(/HH/g, ('0' + dateObj.getHours()).slice(-2));
    formatter = formatter.replace(/MI/g, ('0' + dateObj.getMinutes()).slice(-2));
    formatter = formatter.replace(/SS/g, ('0' + dateObj.getSeconds()).slice(-2));
    formatter = formatter.replace(/MS/g, ('00' + dateObj.getMilliseconds()).slice(-3));
    return formatter;
  },
};

/**
 * 値フォーマットクラス.<br>
 * <ul>
 *   <li>ページ表示する際の値のフォーマット処理を受け持つ。</li>
 *   <li>おもにPageUtilからブラケット記法で実行され、機能単位の処理から直接実行されることはほとんどない。</li>
 *   <li>アンフォーマット処理が対で必要。</li>
 * </ul>
 * @class
 */
const FrmUtil = /** @lends FrmUtil */ {

  /**
   * 大文字変換.
   * @param {string} value 処理対象
   * @returns {string} 大文字変換後の文字列
   */
  upper: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    return value.toUpperCase();
  },

  /**
   * 数値（カンマ編集）.<br>
   * 数値以外は編集せず返す。
   * @param {string} value 処理対象
   * @returns {string} カンマ編集後の文字列
   */
  num: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = UnFrmUtil.num(value);
    if (!ValUtil.isNum(unVal)) {
      return value;
    }
    // 小数部を分離
    const vals = unVal.trim().split('.');
    // 整数部をカンマ編集
    vals[0] = vals[0].replace(/(\d)(?=(\d\d\d)+(?!\d))/g, '$1,');
    return vals.join('.');
  },

  /**
   * 日付（YYYY/MM/DD形式）.<br>
   * 非実在日は編集せず返す。
   * @param {string} value 処理対象
   * @returns {string} YYYY/MM/DD形式の日付文字列
   */
  ymd: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = UnFrmUtil.ymd(value);
    if (!ValUtil.isDate(unVal)) {
      return unVal;
    }
    const unValTrim = unVal.trim();
    return unValTrim.substring(0, 4) + '/' + unValTrim.substring(4, 6) + '/' + unValTrim.substring(6, 8);
  },

  /**
   * 時刻（HH:MI:SS形式）.<br>
   * 数値6桁以外は編集せず返す。
   * @param {string} value 処理対象
   * @returns {string} HH:MI:SS形式の時刻文字列
   */
  hms: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = UnFrmUtil.hms(value);
    const unValTrim = unVal.trim();
    if (unValTrim.length !== 6 || !ValUtil.isNum(unValTrim)) {
      return unValTrim;
    }
    return unValTrim.substring(0, 2) + ':' + unValTrim.substring(2, 4) + ':' + unValTrim.substring(4, 6);
  },
};

/**
 * 値アンフォーマットクラス.<br>
 * <ul>
 *   <li>リクエスト作成に際して値のアンフォーマット処理を受け持つ。</li>
 *   <li>おもにPageUtilからブラケット記法で実行され、機能単位の処理から直接実行されることはほとんどない。</li>
 *   <li>フォーマット処理が対で必要。</li>
 * </ul>
 * @class
 */
const UnFrmUtil = /** @lends UnFrmUtil */ {

  /**
   * 大文字変換アンフォーマット.
   * @param {string} value 処理対象
   * @returns {string} 加工無しの文字列
   */
  upper: function(value) {
    // 加工無し
    return value;
  },

  /**
   * 数字アンフォーマット（カンマ除去）.
   * @param {string} value 処理対象
   * @returns {string} カンマ除去後の文字列
   */
  num: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = ('' + value).trim().replace(/,/g, '');
    return unVal;
  },

  /**
   * 日付アンフォーマット（スラッシュ除去）.
   * @param {string} value 処理対象
   * @returns {string} スラッシュ除去後の文字列
   */
  ymd: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = value.trim().replace(/\//g, '');
    return unVal;
  },

  /**
   * 時刻アンフォーマット（コロン除去）.
   * @param {string} value 処理対象
   * @returns {string} コロン除去後の文字列
   */
  hms: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = value.trim().replace(/:/g, '');
    return unVal;
  },
};

/**
 * HTTP操作ユーティリティクラス.
 *
 * @class
 */
const HttpUtil = /** @lends HttpUtil */ {
  /**
   * 連想配列 to URLパラメーター変換.<br>
   * <ul>
   * <li><pre>［例］
   *      <code>params = {p1: 'aaa', p2: 'bbb'}</code> の場合
   *      <code>p1=aaa&p2=bbb</code> に変換する。</pre></li>
   * </ul>
   *
   * @param {Object.<string,string>} obj 連想配列
   * @returns {string} URLパラメーター文字列
   */
  convUrlParam: function(obj) {
    if (!ValUtil.isObj(obj)) {
      return '';
    }
    const ret = [];
    for (const key in obj) {
      const val = obj[key];
      ret.push(key + '=' + encodeURIComponent(ValUtil.nvl(val)));
    }
    return ret.join('&');
  },

  /**
   * URLパラメーター取得.<br>
   * <ul>
   *   <li>URLの?以降を連想配列で取得する。</li>
   *   <li>［例］ 「a=01&b=02」の場合、<code>{a:'01', b:'02'}</code> を返す。</li>
   *   <li>パラメーターは取得後に削除される。</li>
   * </ul>
   * @returns {Object} URLパラメーター連想配列
   */
  getUrlParams: function() {
    const ret = {};
    let params = location.search;
    if (params.length === 0) {
      return ret;
    }
    // 先頭の?を削除
    params = params.substring(1);
    const paramsAry = params.split('&');
    for (const param of paramsAry) {
      if (ValUtil.isBlank(param)) {
        continue;
      }
      const eqPos = param.indexOf('=');
      if (eqPos < 0) {
        ret[param] = '';
        continue;
      }
      const key = param.substring(0, eqPos);
      const val = param.substring(eqPos + 1);
      ret[key] = decodeURIComponent(ValUtil.nvl(val));
    }

    // URLパラメーターを削除する
    const all = location.toString();
    const search = location.search;
    const rep = all.substring(0, all.length - search.length);
    history.replaceState(null, null, rep);

    return ret;
  },

  /**
   * ページ遷移.<br>
   * <ul>
   * <li>指定URLに遷移する。（HTMLファイル取得）</li>
   * <li>パラメーターを指定した場合はURLの?以降に付与する。</li>
   * <li><pre>［例］
   *      <code>url = 'editpage.html'、params = {user_id: 'U001', upd_ts: '20251231T245959001000'}</code> の場合
   *      <code>editpage.html?user_id=U001&upd_ts=20251231T245959001000</code> にアクセスする。</pre></li>
   * </ul>
   *
   * @param {string} url 遷移先URL
   * @param {Object.<string, string>|string} [params] パラメーター（文字列も可能）（省略可能）
   */
  movePage : function(url, params) {
    let loc = '';
    loc += ValUtil.nvl(url);
    if (!ValUtil.isEmpty(params)) {
      loc += '?';
      if (ValUtil.isObj(params)) {
        loc += HttpUtil.convUrlParam(params);
      } else {
        loc += params;
      }
    }
    // replaceにすることで 戻る で戻らせない（一番最初に開いたページまで戻らせる）
    location.replace(loc);
  },

  /**
   * JSON Webサービス実行（async/await対応）.<br>
   * <ul>
   * <li>指定URL に対して POSTメソッド で JSONリクエストを送信して JSONレスポンスを受信する。</li>
   * <li>リクエスト／レスポンスは連想配列でやり取りする。</li>
   * </ul>
   * 
   * @param {string} url 送信先URL
   * @param {Object} [req] リクエスト連想配列（省略可能）
   * @param {Object.<string, string>} [addHeader] 追加HTTPヘッダー（省略可能）
   * @returns {Object} レスポンス連想配列
   */
  callJsonService : async function(url, req, addHeader) {
    req = req || {};
    if (!ValUtil.isObj(req)) {
      // リクエストデータは連想配列とする
      throw new Error('HttpUtil#callJsonService: Request must be an object. ');
    }
    // ヘッダーをマージ
    const header = Object.assign(addHeader || {}, { 'Content-Type': 'application/json' });

    return new Promise(function(resolve, reject) {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', url, true);
      for (const key in header) {
        const val = header[key];
        xhr.setRequestHeader(key, val);
      }
      // 確認のため自動でJSONパースさせない
      xhr.responseType = 'text';

      // 通信完了イベント
      xhr.onload = function() {
        if (200 <= xhr.status && xhr.status < 300) {
          let res = null;
          try {
            // 手動でJSONパース
            res = JSON.parse(xhr.response);
            resolve(res);
          } catch (e) {
            reject(new Error(`Json parse error. \n${e.name}\n : ${e.message}`));
          }
        } else {
          reject(new Error(`HTTP status ${xhr.status}. `));
        }
      };
      
      // ネットワークエラーイベント
      xhr.timeout = 60000;
      xhr.ontimeout = function(e) {
        reject(new Error('Timeout. '));
      };
      xhr.onerror = function(e) {
        reject(new Error('Network error. '));
      };

      // 送信
      xhr.send(JSON.stringify(req));
    });
  },
};

/**
 * HTML要素操作ユーティリティクラス.
 *
 * @class
 */
const DomUtil = /** @lends DomUtil */ {

  /** @private フォーム入力要素以外の <code>name</code>属性の代替え属性名 */
  _ORG_ATTR_NAME: 'data-name',

  /** @private 連想配列化したときの行インデックス属性 */
  _ORG_ATTR_OBJ_ROW_INDEX: 'data-obj-row-idx',
  /** @private チェックOFF時の値 */
  _ORG_ATTR_CHECK_OFF_VALUE: 'data-check-off-value',
  /** @private 値フォーマットタイプ */
  _ORG_ATTR_VALUE_FORMAT_TYPE: 'data-value-format-type',

  /** @private <code>display</code>スタイルのバックアップ */
  _ORG_ATTR_STYLE_DISPLAY_BACKUP: 'data-style-display-backup',
  /** @private <code>visibility</code>スタイルのバックアップ */
  _ORG_ATTR_STYLE_VISIBILITY_BACKUP: 'data-style-visibility-backup',

  /**
   * 要素取得チェック.
   * @param {Element|NodeList} elms チェック対象要素
   * @returns {boolean} HTML要素が取得できている場合 <code>true</code>
   */
  isExists: function(elms) {
    if (ValUtil.isNull(elms)) {
      return false;
    }
    if (DomUtil._isNodeList(elms) || DomUtil._isHtmlCollection(elms)) {
      return (elms.length > 0);
    }
    if (DomUtil._isHtmlElement(elms)) {
      return true;
    }
    if (ValUtil.isAry(elms) && !ValUtil.isEmpty(elms) && DomUtil._isHtmlElement(elms[0])) {
      return true;
    }
    return false;
  },

  /**
   * @private
   * NodeListチェック.
   *
   * @param {Object} elm チェック対象
   * @returns {boolean} <code>NodeList</code>の場合は <code>true</code>
   */
  _isNodeList: function(elm) {
    if (ValUtil.isNull(elm)) {
      return false;
    }
    const t = ValUtil.toType(elm);
    return (t === 'nodelist');
  },

  /**
   * @private
   * HTMLCollectionチェック.
   *
   * @param {Object} elm チェック対象
   * @returns {boolean} <code>HTMLCollection</code>の場合は <code>true</code>
   */
  _isHtmlCollection: function(elm) {
    if (ValUtil.isNull(elm)) {
      return false;
    }
    const t = ValUtil.toType(elm);
    return (t === 'htmlcollection');
  },

  /**
   * @private
   * HTMLElementチェック.
   *
   * @param {Object} elm チェック対象
   * @returns {boolean} <code>HTMLElement</code>の場合は <code>true</code>
   */
  _isHtmlElement: function(elm) {
    if (ValUtil.isNull(elm)) {
      return false;
    }
    const t = ValUtil.toType(elm);
    return (t.startsWith('html') && t.endsWith('element'));
  },

  /**
   * 要素が表示状態かチェック.<br>
   * <ul>
   * <li><code>display:none</code> または <code>visibility:hidden</code> の場合は非表示と判断する。</li>
   * <li>親要素の表示状態も確認する。</li>
   * </ul>
   * @param {Element} elm チェック対象要素
   * @returns {boolean} 表示状態の場合は <code>true</code>
   */
  isVisible: function(elm) {
    if (!DomUtil.isExists(elm)) {
      return false;
    }

    // 要素とその祖先要素をチェック
    let curElm = elm;
    while (DomUtil.isExists(curElm) && curElm !== document.body) {
      // 隠し項目は非表示とみなさない
      if (curElm.tagName.toLowerCase() === 'input' && ValUtil.nvl(curElm.getAttribute('type')).toLowerCase() === 'hidden') {
        // １つ上の要素へ
        curElm = curElm.parentElement;
        continue;
      }

      // getComputedStyleでマージしたスタイルを取得する
      const style = window.getComputedStyle(curElm);
      // display: none または visibility: hidden の場合は非表示
      if (style.display === 'none' || style.visibility === 'hidden') {
        return false;
      }
      // １つ上の要素へ
      curElm = curElm.parentElement;
    }
    return true;
  },

  /**
   * @private
   * NodeListまたは配列の最初の要素取得.<br>
   * <ul>
   * <li><code>NodeList</code> または配列から最初の要素を返す。</li>
   * <li>引数が <code>NodeList</code> または配列でない場合はそのまま返す。</li>
   * <li>引数が不正な場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {NodeList|Element} elm 対象HTML要素
   * @returns {Element|null} 最初の要素
   */
  _getListFirst: function(elm) {
    if (!DomUtil.isExists(elm)) {
      return null;
    }
    if (DomUtil._isNodeList(elm) && elm.length > 0) {
      return elm[0];
    }
    if (ValUtil.isAry(elm) && elm.length > 0) {
      return elm[0];
    }
    return elm;
  },

  /**
   * @private
   * NodeListを配列に変換.<br>
   * <ul>
   * <li><code>forEach</code>等の配列メソッドを使用可能にする。</li>
   * </ul>
   * @param {NodeList} list <code>NodeList</code>
   * @returns {Array<Element>} HTML要素配列
   */
  _listToAry: function(list) {
    if (!DomUtil.isExists(list)) {
      return [];
    }
    if (!DomUtil._isNodeList(list) && !DomUtil._isHtmlCollection(list)) {
      return [list];
    }

    const ret = [];
    for (const node of list) {
      if (node.nodeType === Node.ELEMENT_NODE) {
        ret.push(node);
      }
    }
    return ret;
  },

  /**
   * idセレクター（最初の要素取得）.<br>
   * <ul>
   * <li>引数が不正な場合や取得できなかった場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {string} id <code>id</code>属性
   * @param {Object} [outerElm] 検索範囲要素（省略可能）
   * @returns {Element|null} 取得HTML要素
   */
  getById : function(id, outerElm) {
    if (ValUtil.isBlank(id)) {
      return null;
    }
    if (DomUtil.isExists(outerElm)) {
      const oElm = DomUtil._getListFirst(outerElm);
      if (!DomUtil.isExists(oElm)) {
        return null;
      }
      const selector = '#' + id;
      const retElm = oElm.querySelector(selector);
      // querySelector() は見つからなかったら nullを返す
      return retElm;
    }
    const retElm = document.getElementById(id);
    // getElementById() は見つからなかったら nullを返す
    return retElm;
  },

  /**
   * セレクター（最初の要素取得）.<br>
   * <ul>
   * <li>引数が不正な場合や取得できなかった場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {string} selector セレクター文字列
   * @param {Object} [outerElm] 検索範囲要素（省略可能）
   * @returns {Element|null} 取得HTML要素
   */
  getSelector: function(selector, outerElm) {
    if (ValUtil.isBlank(selector)) {
      return null;
    }
    if (DomUtil.isExists(outerElm)) {
      const oElm = DomUtil._getListFirst(outerElm);
      if (!DomUtil.isExists(oElm)) {
        return null;
      }
      const retElm = oElm.querySelector(selector);
      // querySelector() は見つからなかったら nullを返す
      return retElm;
    }
    const retElm = document.querySelector(selector);
    // querySelector() は見つからなかったら nullを返す
    return retElm;
  },

  /**
   * nameセレクター（最初の要素取得）.<br>
   * <ul>
   * <li>引数が不正な場合や取得できなかった場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {string} name <code>name</code>属性
   * @param {Object} [outerElm] 検索範囲要素（省略可能）
   * @returns {Element|null} 取得HTML要素
   */
  getByName: function(name, outerElm) {
    if (ValUtil.isBlank(name)) {
      return null;
    }
    const selector = `[name="${name}"]`;
    const retElm = DomUtil.getSelector(selector, outerElm);
    return retElm;
  },

  /**
   * @private
   * nameかつvalueセレクター（最初の要素取得）.<br>
   * <ul>
   * <li>ラジオボタン用。</li>
   * <li>引数が不正な場合や取得できなかった場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {string} name <code>name</code>属性
   * @param {string} value <code>value</code>属性
   * @param {Object} [outerElm] 検索範囲要素（省略可能）
   * @returns {Element|null} 取得HTML要素
   */
  _getByNameAndValue: function(name, value, outerElm) {
    if (ValUtil.isBlank(name) || ValUtil.isBlank(value)) {
      return null;
    }
    const selector = `[name="${name}"][value="${value}"]`;
    const retElm = DomUtil.getSelector(selector, outerElm);
    return retElm;
  },

  /**
   * data-nameセレクター（最初の要素取得）.<br>
   * <ul>
   * <li>フォーム入力要素以外の要素を <code>name</code>属性の代替属性名で取得する。</li>
   * <li>引数が不正な場合や取得できなかった場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {string} name <code>data-name</code>属性
   * @param {Object} [outerElm] 検索範囲要素（省略可能）
   * @returns {Element|null} 取得HTML要素
   */
  getByDataName: function(name, outerElm) {
    if (ValUtil.isBlank(name)) {
      return null;
    }
    const selector = `[${DomUtil._ORG_ATTR_NAME}="${name}"]`;
    const retElm = DomUtil.getSelector(selector, outerElm);
    return retElm;
  },

  /**
   * @private
   * tagセレクター（最初の要素取得）.<br>
   * <ul>
   * <li>引数が不正な場合や取得できなかった場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {string} tag HTMLタグ名
   * @param {Object} [outerElm] 検索範囲要素（省略可能）
   * @returns {Element|null} 取得HTML要素
   */
  _getByTag: function(tag, outerElm) {
    if (ValUtil.isBlank(tag)) {
      return null;
    }
    const selector = tag;
    const retElm = DomUtil.getSelector(selector, outerElm);
    return retElm;
  },

  /**
   * セレクター（複数要素取得）.<br>
   * <ul>
   * <li>HTML要素の配列を返す。</li>
   * <li>取得できなかった場合は長さゼロの配列を返す。</li>
   * <li>引数が不正な場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {string} selector セレクター文字列
   * @param {Object} [outerElm] 検索範囲要素（省略可能）
   * @returns {Array<Element>|null} 複数HTML要素配列 
   */
  getsSelector: function(selector, outerElm) {
    if (ValUtil.isBlank(selector)) {
      return null;
    }
    if (DomUtil.isExists(outerElm)) {
      const oElm = DomUtil._getListFirst(outerElm);
      if (!DomUtil.isExists(oElm)) {
        return null;
      }
      const retElms = oElm.querySelectorAll(selector);
      // querySelectorAll() は見つからなかった場合、長さゼロのNodeListを返す
      return DomUtil._listToAry(retElms);
    }
    const retElms = document.querySelectorAll(selector);
    // querySelectorAll() は見つからなかった場合、長さゼロのNodeListを返す
    return DomUtil._listToAry(retElms);
  },

  /**
   * @private
   * classセレクター（複数要素取得）.<br>
   * <ul>
   * <li>HTML要素の配列を返す。</li>
   * <li>取得できなかった場合は長さゼロの配列を返す。</li>
   * <li>引数が不正な場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {string} cls <code>class</code>属性
   * @param {Object} [outerElm] 検索範囲要素（省略可能）
   * @returns {Array<Element>|null} 複数HTML要素配列 
   */
  _getsByClass: function(cls, outerElm) {
    if (ValUtil.isBlank(cls)) {
      return null;
    }
    const selector = '.' + cls;
    const retElms = DomUtil.getsSelector(selector, outerElm);
    return retElms;
  },

  /**
   * @private
   * 祖先要素idセレクター（最初の要素取得）.<br>
   * <ul>
   * <li>指定要素から祖先要素を検索して id に一致する最も近い要素を返す。</li>
   * <li>引数が不正な場合や取得できなかった場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {Element} baseElm 指定要素
   * @param {string} id <code>id</code>属性
   * @returns {Element|null} 取得HTML要素
   */
  _getParentById: function(baseElm, id) {
    if (ValUtil.isBlank(id)) {
      return null;
    }
    if (!DomUtil.isExists(baseElm)) {
      return null;
    }
    const selector = '#' + id;
    const retElm = baseElm.closest(selector);
    if (!DomUtil.isExists(retElm)) {
      return null;
    }
    return retElm;
  },

  /**
   * 祖先要素tagセレクター（最初の要素取得）.<br>
   * <ul>
   * <li>基点要素から祖先要素を検索してHTMLタグが一致する最も近い要素を返す。</li>
   * <li>押下ボタンを包括する行要素を取得する場合などに使用する。</li>
   * <li>引数が不正な場合や取得できなかった場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {Element} baseElm 基点要素
   * @param {string} tag HTMLタグ名
   * @returns {Element|null} 取得HTML要素
   */
  getParentByTag: function(baseElm, tag) {
    if (ValUtil.isBlank(tag)) {
      return null;
    }
    if (!DomUtil.isExists(baseElm)) {
      return null;
    }
    const retElm = baseElm.closest(tag);
    if (!DomUtil.isExists(retElm)) {
      return null;
    }
    return retElm;
  },

  /**
   * @private
   * すべての直接子要素を取得.<br>
   * <ul>
   * <li>指定要素直下の子要素をすべて取得する（テキストノードは除く）。</li>
   * <li>HTML要素の配列を返す。</li>
   * <li>子要素がない場合は長さゼロの配列を返す。</li>
   * <li>引数が不正な場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {Element} parentElm 親要素
   * @returns {Array<Element>|null} 子要素配列
   */
  _getAllChildren: function(parentElm) {
    if (!DomUtil.isExists(parentElm)) {
      return null;
    }
    // children プロパティを使用（テキストノードは含まれない）
    return DomUtil._listToAry(parentElm.children);
  },

  /**
   * 要素の値取得.<br>
   * <ul>
   * <li><code>&lt;input&gt;</code>, <code>&lt;select&gt;</code>, <code>&lt;textarea&gt;</code>の <code>value</code>属性値を取得する。</li>
   * <li>引数が不正な場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {Element} elm 対象要素
   * @returns {string|null} 値
   */
  getVal: function(elm) {
    if (!DomUtil.isExists(elm)) {
      return null;
    }
    return ValUtil.nvl(elm.value);
  },

  /**
   * 要素の値設定.<br>
   * <ul>
   * <li><code>&lt;input&gt;</code>, <code>&lt;select&gt;</code>, <code>&lt;textarea&gt;</code>の <code>value</code>属性値を設定する。</li>
   * <li>引数が不正な場合は <code>false</code> を返す。</li>
   * </ul>
   * @param {Element} elm 対象要素
   * @param {string} value 設定値
   * @returns {boolean} 設定成功時は <code>true</code>
   */
  setVal: function(elm, value) {
    if (!DomUtil.isExists(elm)) {
      return false;
    }
    elm.value = ValUtil.nvl(value);
    return true;
  },

  /**
   * 要素のテキスト取得.<br>
   * <ul>
   * <li>要素の <code>textContent</code> を取得する。</li>
   * <li>引数が不正な場合は <code>null</code> を返す。</li>
   * </ul>
   * @param {Element} elm 対象要素
   * @returns {string|null} テキスト
   */
  getTxt: function(elm) {
    if (!DomUtil.isExists(elm)) {
      return null;
    }
    return ValUtil.nvl(elm.textContent);
  },

  /**
   * 要素のテキスト設定.<br>
   * <ul>
   * <li>要素の <code>textContent</code> を設定する。</li>
   * <li>引数が不正な場合は <code>false</code> を返す。</li>
   * </ul>
   * @param {Element} elm 対象要素
   * @param {string} text 設定テキスト
   * @returns {boolean} 設定成功時は <code>true</code>
   */
  setTxt: function(elm, text) {
    if (!DomUtil.isExists(elm)) {
      return false;
    }
    elm.textContent = ValUtil.nvl(text);
    return true;
  },

  /**
   * 要素の活性切替.<br>
   * <ul>
   * <li>要素の <code>disabled</code>属性を切り替える。</li>
   * <li>引数が不正な場合は <code>false</code> を返す。</li>
   * </ul>
   * @param {Element} elm 対象要素
   * @param {boolean|string} isEnable 活性する場合は <code>true</code>
   * @returns {boolean} 切替成功時は <code>true</code>
   */
  setEnable: function(elm, isEnable) {
    if (!DomUtil.isExists(elm)) {
      return false;
    }
    isEnable = ValUtil.isTrue(isEnable);
    const oldEnable = !DomUtil.hasAttr(elm, 'disabled');
    if (oldEnable === isEnable) {
      // 変更なし
      return false;
    }
    if (isEnable) {
      // 要素を活性化
      DomUtil.removeAttr(elm, 'disabled');
    } else {
      // 要素を非活性化
      DomUtil.setAttr(elm, 'disabled', 'disabled');
    }
    return true;
  },

  /**
   * 要素の表示切替.<br>
   * <ul>
   * <li>要素の <code>display</code>スタイル または <code>visibility</code>スタイル を切り替える。</li>
   * <li>要素のスペースを保持（レイアウトを保持）する場合は <code>visibility</code>スタイルを切り替える。</li>
   * <li>引数が不正な場合は <code>false</code> を返す。</li>
   * </ul>
   * @param {Element} elm 対象要素
   * @param {boolean|string} isShow 表示する場合は <code>true</code>
   * @param {boolean} keepLayout 要素のスペースを保持する場合は <code>true</code>
   * @returns {boolean} 切替成功時は <code>true</code>
   */
  setVisible: function(elm, isShow, keepLayout) {
    if (!DomUtil.isExists(elm)) {
      return false;
    }
    isShow = ValUtil.isTrue(isShow);
    keepLayout = ValUtil.isTrue(keepLayout);
    
    if (keepLayout) {
      // visibilityスタイルで切り替え
      return DomUtil._setVisibilityStyle(elm, isShow);
    }

    // displayスタイルで切り替え
    return DomUtil._setDisplayStyle(elm, isShow);
  },

  /**
   * @private
   * 要素のvisibilityスタイル設定.
   * @param {Element} elm 対象要素
   * @param {boolean} isShow 表示する場合は <code>true</code>
   * @returns {boolean} 切替成功時は <code>true</code>
   */
  _setVisibilityStyle: function(elm, isShow) {
    // visibilityスタイルで切り替え
    if (ValUtil.isTrue(isShow)) {
      if (elm.style.visibility !== 'hidden') {
        return false;
      }
      if (DomUtil.hasAttr(elm, DomUtil._ORG_ATTR_STYLE_VISIBILITY_BACKUP)) {
        // バックアップしているvisibilityスタイルを復元
        elm.style.visibility = DomUtil.getAttr(elm, DomUtil._ORG_ATTR_STYLE_VISIBILITY_BACKUP);
      } else {
        elm.style.visibility = '';
      }
    } else {
      if (elm.style.visibility === 'hidden') {
        return false;
      }
      if (!ValUtil.isBlank(elm.style.visibility)) {
        // visibilityスタイルをバックアップ
        DomUtil.setAttr(elm, DomUtil._ORG_ATTR_STYLE_VISIBILITY_BACKUP, elm.style.visibility);
      }
      elm.style.visibility = 'hidden';
    }
    return true;
  },

  /**
   * @private
   * 要素のdisplayスタイル設定.
   * @param {Element} elm 対象要素
   * @param {boolean} isShow 表示する場合は <code>true</code>
   * @returns {boolean} 切替成功時は <code>true</code>
   */  
  _setDisplayStyle: function(elm, isShow) {
    // displayスタイルで切り替え
    if (ValUtil.isTrue(isShow)) {
      if (elm.style.display !== 'none') {
        return false;
      }
      if (DomUtil.hasAttr(elm, DomUtil._ORG_ATTR_STYLE_DISPLAY_BACKUP)) {
        // バックアップしているdisplayスタイルを復元
        elm.style.display = DomUtil.getAttr(elm, DomUtil._ORG_ATTR_STYLE_DISPLAY_BACKUP);
      } else {
        elm.style.display = '';
      }
    } else {
      if (elm.style.display === 'none') {
        return false;
      }
      if (!ValUtil.isBlank(elm.style.display)) {
        // displayスタイルをバックアップ
        DomUtil.setAttr(elm, DomUtil._ORG_ATTR_STYLE_DISPLAY_BACKUP, elm.style.display);
      }
      elm.style.display = 'none';
    }
    return true;
  },

  /**
   * 要素の属性取得.<br>
   * <ul>
   * <li>指定属性の値を取得する。</li>
   * <li>引数が不正な場合は <code>null</code> を返す。</li>
   * <li>data-*属性の場合、dataset APIを使用する。</li>
   * </ul>
   * @param {Element} elm 対象要素
   * @param {string} attrName 属性名
   * @returns {string|number|null} 属性値
   */
  getAttr: function(elm, attrName) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(attrName)) {
      return null;
    }

    // data-*属性の場合、dataset API取得
    if (attrName.startsWith('data-')) {
      const datasetKey = DomUtil._convDataAttrToDatasetKey(attrName);
      return elm.dataset[datasetKey];
    }

    return elm.getAttribute(attrName);
  },

  /**
   * 要素の属性設定.<br>
   * <ul>
   * <li>指定属性の値を設定する。</li>
   * <li>引数が不正な場合は <code>false</code> を返す。</li>
   * <li>data-*属性の場合、dataset APIを使用する。</li>
   * </ul>
   * @param {Element} elm 対象要素
   * @param {string} attrName 属性名
   * @param {string} val 設定値
   * @returns {boolean} 設定成功時は <code>true</code>
   */
  setAttr: function(elm, attrName, val) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(attrName)) {
      return false;
    }
    const value = ValUtil.nvl(val);

    // data-*属性の場合、dataset APIセット
    if (attrName.startsWith('data-')) {
      const datasetKey = DomUtil._convDataAttrToDatasetKey(attrName);
      elm.dataset[datasetKey] = value;
      return true;
    }

    elm.setAttribute(attrName, value);
    return true;
  },

  /**
   * 要素の属性有無チェック.<br>
   * <ul>
   * <li>data-*属性の場合、dataset APIを使用する。</li>
   * </ul>
   * @param {Element} elm 対象要素
   * @param {string} attrName 属性名
   * @returns {boolean} 存在する場合は <code>true</code>
   */
  hasAttr: function(elm, attrName) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(attrName)) {
      return false;
    }

    // data-*属性の場合、dataset API取得
    if (attrName.startsWith('data-')) {
      const datasetKey = DomUtil._convDataAttrToDatasetKey(attrName);
      // 属性の有無を確認
      if (ValUtil.isNull(elm.dataset[datasetKey])) {
        return false;
      }
      return true;
    }

    return elm.hasAttribute(attrName);
  },

  /**
   * 要素の属性削除.<br>
   * <ul>
   * <li>指定属性を削除する。</li>
   * <li>引数が不正な場合は <code>false</code> を返す。</li>
   * <li>data-*属性の場合、dataset APIを使用する。</li>
   * </ul>
   * @param {Element} elm 対象要素
   * @param {string} attrName 属性名
   * @returns {boolean} 削除成功時は <code>true</code>
   */
  removeAttr: function(elm, attrName) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(attrName)) {
      return false;
    }

    // data-*属性の場合、dataset API削除
    if (attrName.startsWith('data-')) {
      const datasetKey = DomUtil._convDataAttrToDatasetKey(attrName);
      // 属性の有無を確認
      if (ValUtil.isNull(elm.dataset[datasetKey])) {
        return false;
      }
      delete elm.dataset[datasetKey];
      return true;
    }

    // 属性の有無を確認
    if (!elm.hasAttribute(attrName)) {
      return false;
    }
    elm.removeAttribute(attrName);
    return true;
  },

  /**
   * @private
   * data-*属性名をdatasetキー名に変換.<br>
   * <ul>
   * <li>［例］ <code>'data-obj-row-idx'</code> → <code>'objRowIndex'</code></li>
   * <li>［例］ <code>'data-check-off-value'</code> → <code>'checkOffValue'</code></li>
   * </ul>
   * @param {string} attrName data-*属性名
   * @returns {string} datasetキー名
   */
  _convDataAttrToDatasetKey: function(attrName) {
      if (attrName.indexOf('data-') !== 0) {
        return attrName;
    }
    // 'data-'を除去
    const datasetKey = attrName.substring(5);

    // ハイフンをキャメルケースに変換
    return datasetKey.replace(/-([a-z])/g, function(match, letter) {
      return letter.toUpperCase();
    });
  },

  /**
   * CSSクラス追加.<br>
   * <ul>
   * <li>要素にCSSクラスを追加する。</li>
   * <li>引数が不正な場合は <code>false</code> を返す。</li>
   * </ul>
   * @param {Element} elm 対象要素
   * @param {string} cls クラス名
   * @returns {boolean} 追加成功時は <code>true</code>
   */
  addClass: function(elm, cls) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(cls)) {
      return false;
    }
    elm.classList.add(cls);
    return true;
  },

  /**
   * CSSクラス削除.<br>
   * <ul>
   * <li>要素からCSSクラスを削除する。</li>
   * <li>引数が不正な場合は <code>false</code> を返す。</li>
   * <li>削除対象クラスが存在しない場合は <code>false</code> を返す。</li>
   * </ul>
   * @param {Element} elm 対象要素
   * @param {string} cls クラス名
   * @returns {boolean} 削除成功時は <code>true</code>
   */
  removeClass: function(elm, cls) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(cls) || !DomUtil.hasClass(elm, cls)) {
      return false;
    }
    elm.classList.remove(cls);
    return true;
  },

  /**
   * CSSクラス存在チェック.<br>
   * <ul>
   * <li>要素が指定CSSクラスを持っているかチェックする。</li>
   * </ul>
   * @param {Element} elm 対象要素
   * @param {string} cls クラス名
   * @returns {boolean} クラスを持っている場合は <code>true</code>
   */
  hasClass: function(elm, cls) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(cls)) {
      return false;
    }
    return elm.classList.contains(cls);
  },
};

/**
 * ページ操作ユーティリティクラス.<br>
 * <ul>
 *   <li>ページ全体またはページのエリアを指定した操作を行う。</li>
 *   <li>メッセージ表示、エラー存在確認、フォームクリア、フォーム活性/非活性化、フォーム表示/非表示化などの操作を行う。</li>
 *   <li>一覧（繰り返し行）と明細（繰り返し行）を総称して「リスト」と呼ぶ。リストについて説明している箇所は両方に適用される。</li>
 * </ul>
 * @class
 */
const PageUtil = /** @lends PageUtil */ {

  /** @private メッセージ表示エリア要素の <code>id</code>属性名 および レスポンスデータ内のメッセージ配列のキー（Io.java で指定）. */
  _ITEMID_MSG: '_msg',
  /** @private エラーメッセージがあれば <code>true</code> となるキー（Io.java で指定）. */
  _ITEMID_HAS_ERR: '_has_err',
  /** @private <code>title</code>属性バックアップ用の属性名. */
  _ORG_ATTR_TITLE_BACKUP: 'data-title-backup',
  /** @private リスト部ラジオボタンの連想配列化時の <code>name</code>属性名. */
  _ORG_ATTR_DETAIL_RADIO_OBJ_NAME: 'data-radio-obj-name',

  /**
   * メッセージ表示.<br>
   * <ul>
   *   <li>レスポンスデータからメッセージを表示する。</li>
   *   <li>レスポンスデータのキーは <code>'_msg'</code> とする。</li>
   *   <li><code>id</code>属性が <code>'_msg'</code> の要素を メッセージ表示エリア要素とする。</li>
   *   <li>メッセージ表示エリア要素が複数存在する場合は最初の要素に設定する。</li>
   *   <li>コントロールの表示制御を行う場合はページの初期処理にて <code>PageUtil#clearMsg()</code> を実行して非表示化する。</li>
   *   <li>レスポンスデータにメッセージが存在しない場合はメッセージ表示エリア要素のテキストをクリアする。（<code>PageUtil#clearMsg()</code> が実行される）</li>
   * </ul>
   * @param {Object} res レスポンスデータ
   */
  setMsg: function(res) {
    if (!ValUtil.isObj(res)) {
      throw new Error('PageUtil#setMsg: Argument response is invalid. ');
    }
    const msgs = res[PageUtil._ITEMID_MSG];
    if (ValUtil.isEmpty(msgs)) {
      PageUtil.clearMsg();
      return;
    }
    // メッセージ表示エリアにメッセージ表示
    const msgElm = DomUtil.getById(PageUtil._ITEMID_MSG);
    if (!DomUtil.isExists(msgElm)) {
      throw new Error('PageUtil#setMsg: Message element not found. ');
    }
    let msgHtml = '<ul>';
    for (const msg of msgs) {
      if (!ValUtil.isObj(msg)) {
        throw new Error('PageUtil#setMsg: Message is invalid. ');
      }
      let cls = '';
      if (msg['type'] === 'INFO') {
        cls = 'info-msg';
      } else if (msg['type'] === 'WARN') {
        cls = 'warn-msg';
      } else {
        cls = 'err-msg';
      }
      msgHtml += (`<li class="${cls}">${msg['text']}</li>`);
    }
    msgHtml += '</ul>';
    msgElm.innerHTML = msgHtml;
    DomUtil.setVisible(msgElm, true, false);    
    // メッセージエリアまでスクロール
    msgElm.scrollIntoView(true);

    // 項目をハイライト表示かつ <code>title</code>属性にメッセージを設定
    for (const msg of msgs) {
      const itemName = msg['item'];
      const rowIdx = msg['row'];
      if (!ValUtil.isBlank(itemName)) {
        let elm;
        if (!ValUtil.isBlank(rowIdx)) {
          // 行インデックス値が指定されている場合は <code>data-obj-row-idx</code>属性も考慮して取得
          elm = DomUtil.getSelector(`[name="${itemName}"][${DomUtil._ORG_ATTR_OBJ_ROW_INDEX}="${rowIdx}"]`);
        } else {
          elm = DomUtil.getByName(itemName);
        }
        if (DomUtil.isExists(elm)) {
          let cls = '';
          if (msg['type'] === 'INFO') {
            cls = 'info-item';
          } else if (msg['type'] === 'WARN') {
            cls = 'warn-item';
          } else {
            cls = 'err-item';
          }
          // CSSクラスを追加
          DomUtil.addClass(elm, cls);
          // <code>title</code>属性をバックアップしてメッセージを設定
          if (!ValUtil.isBlank(elm.title)) {
            DomUtil.setAttr(elm, PageUtil._ORG_ATTR_TITLE_BACKUP, elm.title);
          }
          elm.title = msg['text'];
        }
      }
    }
  },

  /**
   * エラー存在確認.<br>
   * <ul>
   *   <li>レスポンスデータからエラーの有無を確認する。</li>
   *   <li>レスポンスデータのキーは <code>'_has_err'</code> とする。</li>
   * </ul>
   * @param {Object} res レスポンスデータ
   * @returns {boolean} エラーが存在する場合は <code>true</code>
   */
  hasError: function (res) {
    if (!ValUtil.isObj(res)) {
      throw new Error('PageUtil#hasErr: Argument response is invalid. ');
    }
    const hasErr = res[PageUtil._ITEMID_HAS_ERR];
    return ValUtil.isTrue(hasErr);
  },

  /**
   * メッセージクリア.<br>
   * <ul>
   *   <li>メッセージ表示エリア要素のテキストをクリアする。</li>
   * </ul>
   */
  clearMsg: function() {
    const msgElm = DomUtil.getById(PageUtil._ITEMID_MSG);
    if (!DomUtil.isExists(msgElm)) {
      throw new Error('PageUtil#clearMsg: Message element not found. ');
    }
    msgElm.innerHTML = '<ul></ul>';
    DomUtil.setVisible(msgElm, false, false);
    
    // 項目のハイライト表示解除および <code>title</code>属性の復元
    const elms = DomUtil.getsSelector('.info-item, .warn-item, .err-item');
    for (const elm of elms) {
      // CSSクラスを削除 
      DomUtil.removeClass(elm, 'info-item');
      DomUtil.removeClass(elm, 'warn-item');
      DomUtil.removeClass(elm, 'err-item');
      // <code>title</code>属性を復元
      if (DomUtil.hasAttr(elm, PageUtil._ORG_ATTR_TITLE_BACKUP)) {
        elm.title = DomUtil.getAttr(elm, PageUtil._ORG_ATTR_TITLE_BACKUP);
      } else {
        elm.title = '';
      }
    }
  },

  /**
   * ページデータ取得.<br>
   * <ul>
   *   <li>ページ上にあるデータ送信HTML要素（<code>&lt;input&gt;</code>, <code>&lt;select&gt;</code>, <code>&lt;textarea&gt;</code>）の値を連想配列で取得する。</li>
   *   <li>取得するHTML要素は <code>name</code>属性が設定されているものとし、<code>name</code>属性が連想配列のキーとなる。</li>
   *   <li>引数の取得範囲要素が省略された場合は <code>&lt;main&gt;</code> を取得範囲とし、<code>&lt;main&gt;</code> が存在しない場合は <code>document.body</code> を取得範囲とする。</li>
   *   <li>主に Webサービスへのリクエストデータとして取得する。</li>
   *   <li>下記は通常の <code>&lt;form&gt;</code> による POST送信と異なる。
   *   <ul>
   *     <li>非活性項目も含む。</li>
   *     <li>スタイルによる非表示項目（<code>display:none</code> または <code>visibility:hidden</code>）は含まない。</li>
   *   </ul></li>
   *   <li>値フォーマットタイプを定義する <code>data-value-format-type</code>属性が設定されている場合は <code>UnFrmUtil</code> の該当メソッドでアンフォーマットした値を取得する。</li>
   *   <li>テキストボックス・テキストエリア	の場合はタブ文字や右ブランク改行コードを除去した値を取得する。</li>
   *   <li>チェックボックスの場合、チェックされているときは <code>value</code>属性の値を返し、チェックされていないときは <code>data-check-off-value</code>属性の値を取得する。</li>
   *   <li>リスト部分（繰り返し部分）のデータは配列となり１つのキーで連想配列に格納される。</li>
   *   <li><pre>［例］ <code>&lt;input name="user_id" value="U001"&gt;
   *      &lt;input name="birth_dt" value="2025/02/10"&gt;
   *      &lt;table&gt;...省略...&lt;tbody id="detail"&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_no" value="1"&gt;&lt;/td&gt;
   *             &lt;td&gt;&lt;input name="detail.weight_kg" value="8.9"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_no" value="2"&gt;&lt;/td&gt;
   *             &lt;td&gt;&lt;input name="detail.weight_kg" value="12.1"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code> は
   *       <code>{ user_id:'U001', birth_dt:'20250210', detail:[{pet_no:'1', weight_kg:'8.9'}, {pet_no:'2', weight_kg:'12.1'}] }</code> として取得される。</pre></li>
   *   <li>リスト部分の要素は下記ルールに従うこと。
   *   <ul> 
   *     <li>行内の要素（以下、行内要素と呼ぶ）は <code>name</code>属性を <code>"."</code> 区切りとし、<code>表id.項目名</code> 形式で設定する。なお <code>"."</code> 区切りの <code>name</code>属性は行内要素にのみ使用すること。</li>
   *     <li>行内要素の親・祖父要素として <code>"."</code> 区切りの前部分 <code>表id</code> を <code>id</code>属性とする要素（以下、表要素と呼ぶ）が存在すること。<br>
   *         ほとんどの場合、表要素は <code>&lt;tbody&gt;</code> または <code>&lt;table&gt;</code> となる。</li>
   *     <li>表要素直下の子要素は、繰り返される部分の最上位要素（以下、行要素と呼ぶ）となること。<br>
   *         ほとんどの場合、行要素は <code>&lt;tr&gt;</code> となる。</li>
   *     <li><pre>［NG例１］ 表要素が存在しない。（<code>&lt;table&gt;</code>, <code>&lt;tbody&gt;</code> のどちらにも <code>id</code>属性が無い）
   *       <code>&lt;table&gt;...省略...&lt;tbody&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code></pre></li>
   *     <li><pre>［NG例２］ 表要素直下に行要素が存在しない。（<code>&lt;table&gt;</code> に <code>id</code>属性が付与されているが <code>&lt;tbody&gt;</code> が間にある）
   *       <code>&lt;table id="detail"&gt;...省略...&lt;tbody&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code></pre></li>
   *     <li><pre>［OK例１］ <code>&lt;tbody&gt;</code> に <code>id</code>属性が付与されている場合は <code>&lt;tbody&gt;</code> が表要素となる。
   *       <code>&lt;table&gt;...省略...&lt;tbody id="detail"&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code></pre></li>
   *     <li><pre>［OK例２］ <code>&lt;table&gt;</code> に <code>id</code>属性が付与されている場合は <code>&lt;table&gt;</code> が表要素となる。（複数個の <code>&lt;tbody&gt;</code> を使用する例）
   *       <code>&lt;table id="detail"&gt;...省略...
   *         &lt;tbody&gt;&lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/tbody&gt;
   *         &lt;tbody&gt;&lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/tbody&gt;
   *       &lt;/table&gt;</code></pre></li>
   *   </ul></li>
   *   <li>行内要素には <code>data-obj-row-idx</code>属性として行インデックス値が格納され、そのインデックスに基づいて配列化される。</li>
   *   <li>行内のラジオボタンは <code>name</code>属性の最後の [行インデックス] を除去して戻り値のキーとする。（<code>PageUtil#setValue</code> 参照）</li>
   * </ul>
   * @param {Object} [outerElm] 取得範囲要素（省略可能）
   * @returns {Object} ページデータ連想配列
   */
  getValues: function(outerElm) {
    outerElm = outerElm || DomUtil._getByTag('main') || document.body;
    if (!DomUtil.isExists(outerElm)) {
      throw new Error('PageUtil#getValues: Argument element is invalid. ');
    }

    // 行インデックスを付加
    PageUtil._setRowIndex(outerElm);
    // 対象要素を取得
    const targetElms = DomUtil.getsSelector('input[name],select[name],textarea[name]', outerElm);

    const jsonData = {};
    const listObj = {};
    for (const elm of targetElms) {
      if (!DomUtil.isVisible(elm)) {
        // 非表示要素は無視
        continue;
      }
      if (PageUtil._isRadioOff(elm)) {
        // ラジオボタンでチェックされていないものは無視
        continue;
      }
      const name = elm.getAttribute('name');
      const listNameSepPos = name.indexOf('.');
      if (listNameSepPos > 0 && DomUtil.hasAttr(elm, DomUtil._ORG_ATTR_OBJ_ROW_INDEX)) {
        // リストの変換
        // 行ごとの配列をいったんマップに格納する
        const listId = name.substring(0, listNameSepPos);
        let colName = name.substring(listNameSepPos + 1);
        const nameIndexWrapPos = colName.indexOf('[');
        if (nameIndexWrapPos > 1) {
          // 行内のラジオボタンの場合は <code>name</code>属性の [n] を除去する
          colName = colName.substring(0, nameIndexWrapPos);
        }
        const rowIdx = ~~DomUtil.getAttr(elm, DomUtil._ORG_ATTR_OBJ_ROW_INDEX);
        if (ValUtil.isNull(listObj[listId])) {
          // マップに無ければ配列新規作成
          listObj[listId] = [];
        }
        const list = listObj[listId];
        let row = list[rowIdx];
        if (ValUtil.isNull(row)) {
          // 配列になければ行オブジェクトを新規作成
          row = {};
          list[rowIdx] = row;
        }
        row[colName] = PageUtil._getElmUnFormatVal(elm);
      } else {
        if (jsonData[name]) {
          throw new Error(`PageUtil#getValues: Name attribute is duplicated. name = ${name}`);
        }
        jsonData[name] = PageUtil._getElmUnFormatVal(elm);
      }
    }

    for (const listId in listObj) {
      if (jsonData[listId]) {
        throw new Error(`PageUtil#getValues: Name attribute is duplicated. listId = ${listId}`);
      }
      jsonData[listId] = listObj[listId];
    }
    return jsonData;
  },


  /**
   * 行データ取得.<br>
   * <ul>
   *   <li>リスト内の１行のページデータを連想配列で取得する。</li>
   *   <li>データの取得ルールは <code>PageUtil#getValues</code> と同じ。</li>
   *   <li><code>name</code>属性の <code>"."</code> より前の部分（＝表要素の <code>id</code>属性）は除去して連想配列のキーとする。</li>
   *   <li><pre>［例］ <code>&lt;table&gt;...省略...&lt;tbody id="detail"&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_no" value="1"&gt;&lt;/td&gt;
   *             &lt;td&gt;&lt;input name="detail.weight_kg" value="8.9"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_no" value="2"&gt;&lt;/td&gt;
   *             &lt;td&gt;&lt;input name="detail.weight_kg" value="12.1"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code> の２行目を引数に指定した場合は
   *       <code>{ pet_no:'2', weight_kg:'12.1' }</code> が取得される。</pre></li>
   *   <li>行内のラジオボタンは <code>name</code>属性の最後の [行インデックス] を除去して戻り値のキーとする。（<code>PageUtil#setValues</code> 参照）</li>
   * </ul>
   * @param {Element} rowElm 行要素（通常 <code>&lt;tr&gt;</code> となる）
   * @returns {Object} 行データ連想配列
   */
  getRowValues: function(rowElm) {
    if (!DomUtil.isExists(rowElm)) {
      throw new Error('PageUtil#getRowValues: Argument element is invalid. ');
    }

    // 対象要素を取得
    const targetElms = DomUtil.getsSelector('input[name],select[name],textarea[name]', rowElm);

    const jsonData = {};
    for (const elm of targetElms) {
      if (!DomUtil.isVisible(elm)) {
        // 非表示要素は無視
        continue;
      }
      if (PageUtil._isRadioOff(elm)) {
        // ラジオボタンでチェックされていないものは無視
        continue;
      }
      let colName = elm.getAttribute('name');
      const listNameSepPos = colName.indexOf('.');
      if (listNameSepPos > 0) {
        colName = colName.substring(listNameSepPos + 1);
        const nameIndexWrapPos = colName.indexOf('[');
        if (nameIndexWrapPos > 1) {
          // 行内のラジオボタンの場合は <code>name</code>属性の [n] を除去する
          colName = colName.substring(0, nameIndexWrapPos);
        }
      }
      if (jsonData[colName]) {
        throw new Error(`PageUtil#getRowValues: Name attribute is duplicated. name = ${colName}`);
      }
      jsonData[colName] = PageUtil._getElmUnFormatVal(elm);
    }
    return jsonData;
  },

  /**
   * 行データ取得.<br>
   * <ul>
   *   <li>引数の基点要素から親要素をたどって行要素（ほとんどの場合 <code>&lt;tr&gt;</code>）を取得し、その行要素のページデータを連想配列で取得する。</li>
   *   <li>データの取得ルールは <code>PageUtil#getRowValues</code> と同じ。</li>
   * </ul> 
   * @param {Element} baseElm 基点要素
   * @param {string} [rowTag] 行要素のタグ名（省略可能）省略した場合は 'tr' とする
   * @returns {Object} 行データ連想配列
   */
  getRowValuesByInnerElm: function(baseElm, rowTag) {
    rowTag = rowTag || 'tr';
    if (!DomUtil.isExists(baseElm)) {
      throw new Error('PageUtil#getRowValuesByInnerElm: Argument element is invalid. ');
    }
    const rowElm = DomUtil.getParentByTag(baseElm, rowTag);
    if (!DomUtil.isExists(rowElm)) {
      throw new Error(`Row element not found. rowTag = ${rowTag}, baseElm = ${baseElm.name}`);
    }
    return PageUtil.getRowValues(rowElm);
  },

  /**
   * ページデータセット.<br>
   * <ul>
   *   <li>ページ上にあるHTML要素に連想配列の値をセットする。</li>
   *   <li>セットするHTML要素は <code>name</code>属性または <code>data-name</code>属性が設定されているものとし、連想配列のキーが値セット先の <code>name</code>属性または <code>data-name</code>属性となる。</li>
   *   <li><code>&lt;span&gt;</code> や <code>&lt;td&gt;</code> など入力フォーム要素以外の（本来 <code>name</code>属性を持たない）要素に値をセットする場合は <code>data-name</code>属性を設定する。</li>
   *   <li><code>&lt;span&gt;</code> や <code>&lt;td&gt;</code> など <code>value</code>属性を持たないHTML要素の場合は <code>textContent</code> にセットする。</li>
   *   <li>引数のセット範囲要素が省略された場合は <code>&lt;main&gt;</code> をセット範囲とし、<code>&lt;main&gt;</code> が存在しない場合は <code>document.body</code> をセット範囲とする。</li>
   *   <li>主に Webサービスからのレスポンスデータをセットする。</li>
   *   <li>連想配列のキーがアンダースコア始まりの値は本フレームワークで使用する値なので本クラス外から使用不可とし本メソッドでは無視する。</li>
   *   <li>値フォーマットタイプを定義する <code>data-value-format-type</code>属性が設定されている場合は <code>FrmUtil</code> の該当メソッドでフォーマットした値をセットする。</li>
   *   <li>リスト部分のデータは連想配列内の１つのキーに配列として格納されている前提とする。</li>
   *   <li><pre>［例］ <code>{ user_id:'U001', birth_dt:'20250210', list:[{pet_no:'1', weight_kg:'8.9'}, {pet_no:'2', weight_kg:'12.1'}] }</code>は
   *     <code>&lt;input name="user_id" value="U001"&gt;
   *     &lt;input name="birth_dt" value="2025/02/10"&gt;
   *     &lt;table&gt;...省略...&lt;tbody id="detail"&gt;
   *       &lt;tr&gt;&lt;td data-name="detail.pet_no"&gt;1&lt;/td&gt;
   *           &lt;td&gt;&lt;input name="detail.weight_kg" value="8.9"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;tr&gt;&lt;td data-name="detail.pet_no"&gt;2&lt;/td&gt;
   *           &lt;td&gt;&lt;input name="detail.weight_kg" value="12.1"&gt;&lt;/td&gt;&lt;/tr&gt;
   *     &lt;/tbody&gt;&lt;/table&gt;</code> のようにセットされる。</pre></li>
   *   <li>リスト内の表要素、行要素、行内要素のルールは <code>PageUtil#getValues</code> と同じ。</li>
   *   <li>連想配列内の配列の数とリストの行数を連動させて表示したい場合は、テンプレートとなる行要素（以下、テンプレート行要素と呼ぶ）から動的に要素を生成して値をセットする。</li>
   *   <li>テンプレート行要素は表要素の子要素（先頭）に <code>&lt;script&gt;</code> 囲みで配置しておく。</li>
   *   <li><pre>［例］ 前述例の場合は下記のようにテンプレート行要素を配置する。
   *     <code>&lt;table&gt;...省略...&lt;tbody id="detail"&gt;
   *       &lt;script type="text/html"&gt;&lt;tr&gt;&lt;td data-name="detail.pet_no"&gt;&lt;/td&gt;
   *                                    &lt;td&gt;&lt;input name="detail.weight_kg"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/script&gt;
   *     &lt;/tbody&gt;&lt;/table&gt;</code></pre></li>
   *   <li>テンプレート行要素内のラジオボタンは <code>name</code>属性の最後に [行インデックス] を付加して行単位でグループ化する。</li>
   * </ul>
   * @param {Object} obj 連想配列データ
   * @param {Element} [outerElm] セット範囲要素（省略可能）省略した場合は <code>document.body</code> が対象となる）
   */
  setValues: function(obj, outerElm) {
    if (ValUtil.isNull(obj) || typeof (obj) !== 'object') {
      throw new Error('PageUtil#setValues: Argument is invalid. ');
    }

    outerElm = outerElm || DomUtil._getByTag('main') || document.body;
    if (!DomUtil.isExists(outerElm)) {
      throw new Error('PageUtil#setValues: Argument element is invalid. ');
    }

    for (const name in obj) {
      if (typeof (name) !== 'string') {
        continue;
      }
      // アンダースコア始まりの値はフレームワークで使用する値なので本クラス外から使用不可と本メソッド無視する
      if (name.indexOf('_') === 0) {
        continue;
      }

      let val = obj[name];

      // 配列の場合はリスト部分にセット
      if (ValUtil.isAry(val)) {
        PageUtil._setRowValues(name, val, outerElm);
        continue;
      }

      if (val != null) {
        const valto = typeof (val);
        if (valto !== 'string' && valto !== 'number' && valto !== 'boolean') {
          // プリミティブな型でなければスキップ
          continue;
        }
      }
      val = ValUtil.nvl(val);
      PageUtil._getElmToSetElmFormatVal(name, val, outerElm);
    }
  },

  /**
   * @private
   * リスト行追加（複数行）.<br>
   * <ul>
   *   <li>引数のリスト親要素直下にテンプレート行要素が存在する前提で、引数の連想配列の配列データ分の行要素を生成して値をセットし、リスト親要素に追加する。</li>
   * </ul>
   * @param {string} listId 表要素（親要素）の <code>id</code>属性（［例］ <code>'detail'</code>）
   * @param {Element} listElm 表要素（親要素）
   * @param {Array<Object>} objAry 行データ配列（各要素が1行分のデータを表す連想配列）
   */
  _addRows: function(listId, listElm, objAry) {
    // テンプレート行を取得
    const templateScript = DomUtil._getByTag('script', listElm);
    if (!DomUtil.isExists(templateScript)) {
      console.warn(`PageUtil#_addRows: Template script not found in list. id=${listId}`);
      return;
    }
    const tempHtml = templateScript.innerHTML.trim();
    const tempHtmls = PageUtil._splitHtmlTagsOuterInner(tempHtml);
    if (ValUtil.isEmpty(tempHtmls)) {
      console.warn(`PageUtil#_addRows: Template script is invalid HTML. id=${listId}`);
      return;
    }
    const outerHtmlBegin = tempHtmls[0];
    const innerHtml = tempHtmls[1];

    // 開始タグから要素名と属性を抽出
    const rowElmInfo = PageUtil._parseHtmlOpenTag(outerHtmlBegin);
    if (ValUtil.isEmpty(rowElmInfo)) {
      console.error(`PageUtil#_addRows: Failed to parse row tag. openTag=${outerHtmlBegin}`);
      return;
    }
    const rowElmTag = rowElmInfo[0];
    const rowElmAttrs = rowElmInfo[1];

    // ラジオボタン用行インデックス
    let radioRowIdx = -1;
    const oldRowElms = DomUtil.getsSelector(`${rowElmTag}`, listElm);
    // 現在の最大行インデックスを取得
    if (DomUtil.isExists(oldRowElms)) {
      // 各行要素内のラジオボタンで <code>name</code>属性の最後に [行インデックス] があるものを取得
      for (const oldRowElm of oldRowElms) {
        const radioElm = DomUtil.getSelector('input[type="radio"][name*="["][name$="]"]', oldRowElm);
        if (DomUtil.isExists(radioElm)) {
          const name = DomUtil.getAttr(radioElm, 'name');
          const idx = ~~name.substring(name.lastIndexOf('[') + 1, name.length - 1);
          if (idx > radioRowIdx) {
            radioRowIdx = idx;
          }
        }
      }
    }

    // データ分の行を生成
    for (const obj of objAry) {
      // テンプレートから行要素を生成
      const rowElm = document.createElement(rowElmTag);
      // 行要素の属性を設定
      for (const attrName in rowElmAttrs) {
        DomUtil.setAttr(rowElm, attrName, rowElmAttrs[attrName]);
      }
      // 行内要素を設定
      rowElm.innerHTML = innerHtml;

      // 行内要素に値をセット
      if (ValUtil.isObj(obj)) {
        for (const colName in obj) {
          const val = ValUtil.nvl(obj[colName]);
          const name = listId + '.' + colName;
          PageUtil._getElmToSetElmFormatVal(name, val, rowElm);
        }
      }
      // 行要素内のラジオボタンは <code>name</code>属性の最後に [行インデックス] を付加して行単位でグループ化する。
      // 元の <code>name</code>属性は <code>data-radio-obj-name</code>属性に格納する
      radioRowIdx++;
      const radioElms = DomUtil.getsSelector('input[type="radio"][name]', rowElm);
      for (const radioElm of radioElms) {
        const name = radioElm.getAttribute('name');
        const rotName = name + `[${radioRowIdx}]`;
        DomUtil.setAttr(radioElm, 'name', rotName);
        DomUtil.setAttr(radioElm, PageUtil._ORG_ATTR_DETAIL_RADIO_OBJ_NAME, name);
      }

      // 行要素を表要素に追加
      listElm.appendChild(rowElm);
    }
  },

  /**
   * リスト行追加.<br>
   * <ul>
   *   <li>表要素のテンプレート行要素で行要素を生成・追加してデフォルト値をセットする。</li>
   *   <li>テンプレート行については <code>PageUtil#setValues</code> 参照。</li>
   *   <li><pre>［例］ 下記のテンプレート行の場合。
   *     <code>&lt;table&gt;...省略...&lt;tbody id="detail"&gt;
   *       &lt;script type="text/html"&gt;&lt;tr&gt;&lt;td data-name="detail.pet_no"&gt;&lt;/td&gt;
   *                                    &lt;td&gt;&lt;input name="detail.weight_kg"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/script&gt;
   *     &lt;/tbody&gt;&lt;/table&gt;</code>
   *     デフォルト値のキーは下記のように 表要素 <code>id</code>属性（detail.）は除いた下記となる。
   *    <code>{ pet_no:'1', weight_kg:'8.9' }</code>
   *    連想配列の配列を渡した場合は複数行追加される。
   *    <code>[ { pet_no:'1', weight_kg:'8.9' }, { pet_no:'2', weight_kg:'12.1' } ]</code></pre>
   *   </li>
   *   <li>デフォルト値を省略した場合は空の行要素を１行追加する。</li>
   *   <li>空行を複数行追加する場合は <code>new Array(n)</code> を渡す。</li>
   * </ul>
   * @param {string} listId 表要素（親要素）の <code>id</code>属性（［例］ <code>'detail'</code>）
   * @param {Object|Array<Object>} [obj] 行追加時のデフォルト値連想配列（省略可能）
   */
  addRow: function(listId, obj) {
    obj = obj || {};
    if (ValUtil.isBlank(listId)) {
      throw new Error('PageUtil#addRow: Argument listId is invalid. ');
    }
    const listElm = DomUtil.getById(listId);
    if (!DomUtil.isExists(listElm)) {
      console.warn(`PageUtil#addRow: List element not found. id=${listId}`);
      return;
    }
    if (ValUtil.isAry(obj)) {
      PageUtil._addRows(listId, listElm, obj);
    } else {
      PageUtil._addRows(listId, listElm, [obj]);
    }
  },

  /**
   * リスト行削除.<br>
   * <ul>
   *   <li>指定した <code>name</code>属性 かつ <code>value</code>属性を持つ要素が存在する行要素（ほとんどの場合は <code>&lt;tr&gt;</code>）を削除する。</li>
   * </ul>
   * @param {string} searchElmName 検索対象要素の <code>name</code>属性（［例］ チェックボックスの場合 <code>'detail.chk'</code>）<code>data-name</code>属性は不可
   * @param {string} searchElmVal 検索対象要素の値（［例］ <code>'1'</code>）
   * @param {string} [rowTag] 行要素のタグ名（省略可能）省略した場合は <code>'tr'</code> とする
   * @returns {boolean} 削除成功時は <code>true</code>
   */
  removeRow: function(searchElmName, searchElmVal, rowTag) {
    rowTag = rowTag || 'tr';
    if (ValUtil.isBlank(searchElmName) || ValUtil.isBlank(searchElmVal)) {
      throw new Error(`PageUtil#removeRow: Argument is invalid. name=${searchElmName} value=${searchElmVal}`);
    }
    const searchElms = DomUtil.getsSelector(`[name="${searchElmName}"][value="${searchElmVal}"]`);
    if (searchElms.length <= 0) {
      console.warn(`PageUtil#removeRow: Element not found. searchElmName=${searchElmName} searchElmVal=${searchElmVal}`);
      return false;
    }
    let found = false;
    for (const elm of searchElms) {
      if (PageUtil._isCheckType(elm) && !elm.checked) {
        // チェックボックス・ラジオボタンでチェックされていないものは無視
        continue;
      }
      const rowElm = DomUtil.getParentByTag(elm, rowTag);
      if (!DomUtil.isExists(rowElm)) {
        console.warn(`PageUtil#removeRow: Row element not found. rowTag=${rowTag} searchElmName=${searchElmName} searchElmVal=${searchElmVal}`);
        continue;
      }
      rowElm.parentNode.removeChild(rowElm);
      found = true;
    }
    if (!found) {
      console.warn(`PageUtil#removeRow: Element not found. (non checked) rowTag=${rowTag} searchElmName=${searchElmName} searchElmVal=${searchElmVal}`);
      return false;
    }
    return true;
  },

  /**
   * 全行削除.<br>
   * <ul>
   *   <li>テンプレート行以外のすべての行要素を削除する。</li>
   * </ul>
   * @param {string} listId 表要素（親要素）の <code>id</code>属性（［例］ <code>'detail'</code>）
   */
  clearRows: function (listId) {
    if (ValUtil.isBlank(listId)) {
      throw new Error('PageUtil#addRow: Argument listId is invalid. ');
    }
    const listElm = DomUtil.getById(listId);
    if (!DomUtil.isExists(listElm)) {
      console.warn(`PageUtil#addRow: List element not found. id=${listId}`);
      return;
    }
    PageUtil._removeAllRows(listElm);
  },

  /**
   * @private
   * 全行削除.<br>
   * <ul>
   *   <li>テンプレート行以外のすべての行要素を削除する。</li>
   * </ul>
   * @param {Element} listElm 表要素（親要素）
   */
  _removeAllRows: function(listElm) {
    // 既存の行をすべて削除（テンプレート行以外）
    const oldRowElms = DomUtil._getAllChildren(listElm);
    for (const rowElm of oldRowElms) {
      if (rowElm.tagName.toLowerCase() === 'script') {
        continue;
      }
      listElm.removeChild(rowElm);
    }
  },

  /**
   * @private
   * 行内要素に独自属性でインデックスを付加.<br>
   * <ul>
   *   <li>行内要素（<code>'."</code> 区切りの <code>name</code>属性を持つ要素）かつデータ送信要素（<code>&lt;input&gt;</code>, <code>&lt;select&gt;</code>, <code>&lt;textarea&gt;</code>）を対象とする。</li>
   *   <li><code>data-obj-row-idx</code>属性としてインデックスを付加する。</li>
   *   <li>付加したインデックスは <code>PageUtil#getValues</code> で連想配列化する際に使用する。</li>
   *   <li>付加したインデックスは Webサービスから返ってきた際のレスポンス値表示の目印ともなる。</li>
   *   <li>付加するインデックスはゼロから開始して行要素ごとにインクリメントする。</li>
   *   <li>行要素の子・孫要素に行内要素が存在しない場合はその行は無視する。（インクリメントしない）</li>
   *   <li>詳細は <code>PageUtil#getValues</code> の JSDoc 参照。</li>
   * </ul>
   * @param {Object} outerElm インデックス付加範囲要素
   */
  _setRowIndex : function(outerElm) {
    if (ValUtil.isNull(outerElm)) {
      throw new Error('PageUtil#_setRowIndex: Target element required.');
    }
    // 行内要素を取得
    const rowInElms = DomUtil.getsSelector('input[name*="."],select[name*="."],textarea[name*="."]', outerElm);
    // ページからリスト要素を見つける
    const listObj = {};
    for (const elm of rowInElms) {
      const name = elm.getAttribute('name');
      const listId = name.substring(0, name.indexOf('.'));
      if (listObj[listId]) {
        // すでにマップに存在する場合はスキップ
        continue;
      }
      const listElm = DomUtil._getParentById(elm, listId);
      if (!DomUtil.isExists(listElm)) {
        throw new Error(`PageUtil#_setRowIndex: List parent element not found. id=#${listId} `);
      }
      listObj[listId] = listElm;
    }

    // リストごとのループ
    for (const listId in listObj) {
      const listElm = listObj[listId];
      const rowElms = DomUtil._getAllChildren(listElm);
      // 行ループ
      let i = -1;
      for (const rowElm of rowElms) {
        if (rowElm.tagName.toLowerCase() === 'script') {
          continue;
        }
        const colElms = DomUtil.getsSelector(`[name^="${listId}."]`, rowElm);
        if (colElms.length <= 0) {
          continue;
        }
        i++;
        // 行内要素ループ
        for (const colElm of colElms) {
          // 行内要素にインデックスを付加
          DomUtil.setAttr(colElm, DomUtil._ORG_ATTR_OBJ_ROW_INDEX, i);
        }
      }
    }
  },

  /**
   * @private
   * 要素取得（<code>name</code>属性または<code>data-name</code>属性）.<br>
   * <ul>
   *   <li><code>name</code>属性で要素を取得できない場合は <code>data-name</code>属性で要素を取得する。</li>
   * </ul>
   * @param {string} name 
   * @param {Element} outerElm 
   * @returns 要素
   */
  _getElmBynNameOrDataName: function(name, outerElm) {
    let elm = DomUtil.getByName(name, outerElm);
    if (!DomUtil.isExists(elm)) {
      elm = DomUtil.getByDataName(name, outerElm);
    }
    return elm;
  },

  /**
   * @private
   * 行部分データセット.
   * 
   * @param {string} listId 表要素（親要素）の <code>id</code>属性（［例］ <code>'detail'</code>）
   * @param {Array<Object>} objAry 連想配列の配列データ（１つの連想配列が１行分のデータ）
   * @param {Element} outerElm セット範囲要素
   */
  _setRowValues: function(listId, objAry, outerElm) {
    let listElm;
    if (DomUtil.getAttr(outerElm, 'id') === listId) {
      listElm = outerElm;
    } else {
      listElm = DomUtil.getById(listId, outerElm);
    }
    if (!DomUtil.isExists(listElm)) {
      console.warn(`PageUtil#_setRowValues: List element not found. id=${listId}`);
      return;
    }
    // 既存の行を削除（テンプレート行以外）
    PageUtil._removeAllRows(listElm);
    // 行を追加
    PageUtil._addRows(listId, listElm, objAry);
  },
  
  /**
   * @private
   * 要素値アンフォーマット取得.<br>
   * <ul>
   * <li>ラベルなど <code>value</code>属性を持たない要素の場合は textContent の値を返す。</li>
   * <li>詳細は <code>PageUtil#getValues</code> の JSDoc 参照。</li>
   * </ul>
   */
  _getElmUnFormatVal: function(elm) {
    if (PageUtil._isCheckType(elm)) {
      // チェックボックスまたはラジオボタンの場合
      // ラジオボタンの場合はチェックONのもの渡されている。
      if (elm.checked) {
        return ValUtil.nvl(elm.value);
      } else {
        // OFF の場合、独自属性の値を取得する
        return ValUtil.nvl(DomUtil.getAttr(elm, DomUtil._ORG_ATTR_CHECK_OFF_VALUE));
      }
    }
    let val = '';
    if (PageUtil._hasValueProp(elm)) {
      const orgval = ValUtil.nvl(elm.value);
      if (PageUtil._isTextType(elm)) {
        val = PageUtil._convPostVal(orgval);
      } else if (PageUtil._isTextArea(elm)) {
        val = PageUtil._convPostVal(orgval);
      } else {
        val = orgval;
      }
      if (val !== orgval) {
        // 整形されて値が変わったらページに戻す。
        PageUtil._setElmFormatVal(elm, val);
      }
    } else {
      // ラベル等の処理
      val = ValUtil.nvl(elm.textContent);
    }
    // アンフォーマット
    const fmtType = DomUtil.getAttr(elm, DomUtil._ORG_ATTR_VALUE_FORMAT_TYPE);
    if (!ValUtil.isNull(fmtType) && !ValUtil.isNull(UnFrmUtil[fmtType])) {
      val = UnFrmUtil[fmtType](val);
    }
    return val;
  },

  /**
   * @private
   * 要素を取得して値をセット.<br>
   * <ul>
   * <li>ラジオボタンの場合は適切な要素を選択して値をセットする。</li>
   * </ul>
   * @param {string} name <code>name</code>属性または<code>data-name</code>属性の値
   * @param {string} val セットする値
   * @param {Element} outerElm セット範囲要素
   * @returns {boolean} セットに成功した場合は <code>true</code>、失敗した場合は <code>false</code>
   */
  _getElmToSetElmFormatVal: function(name, val, outerElm) {
    let elm = PageUtil._getElmBynNameOrDataName(name, outerElm);
    if (!DomUtil.isExists(elm)) {
      console.warn(`PageUtil#_getElmToSetElmFormatVal: Element not found. name=${name}`);
      return false;
    }
    if (PageUtil._isRadioNotVal(elm, val)) {
      // ラジオボタンで値が指定値でない要素が渡された（同name要素の先頭が指定値でなかった）場合は指定値の要素に差し替え
      elm = DomUtil._getByNameAndValue(name, val, outerElm);
      if (!DomUtil.isExists(elm)) {
        console.warn(`PageUtil#_getElmToSetElmFormatVal: Element not found. name=${name} value=${val}`);
        return false;
      }
    }
    PageUtil._setElmFormatVal(elm, val);
    return true;
  },

  /**
   * @private
   * 要素値セット.
   */
  _setElmFormatVal: function(elm, val) {
    val = ValUtil.nvl(val);
    if (PageUtil._isCheckType(elm)) {
      // チェックボックスまたはラジオボタンの場合
      // ラジオボタンの場合は <code>value</code>属性で選択された要素が渡されているので必ずチェックONとなる。
      elm.checked = (('' + val) === elm.value);
      return;
    }
    // フォーマット
    const fmtType = DomUtil.getAttr(elm, DomUtil._ORG_ATTR_VALUE_FORMAT_TYPE);
    if (!ValUtil.isNull(fmtType) && !ValUtil.isNull(FrmUtil[fmtType])) {
      val = FrmUtil[fmtType](val);
    }
    if (PageUtil._hasValueProp(elm)) {
      elm.value = val;
    } else {
      elm.textContent = val;
    }
  },

  /**
   * @private
   * 値を持つHTML要素か判断.
   */
  _hasValueProp: function(elm) {
    const tag = elm.tagName.toLowerCase();
    return (tag === 'input' || tag === 'select' || tag === 'textarea');
  },

  /**
   * @private
   * チェックボックスまたはラジオボタンか判断.
   */
  _isCheckType: function(elm) {
    const tag = elm.tagName.toLowerCase();
    if (tag === 'input') {
      const type = ValUtil.nvl(elm.getAttribute('type')).toLowerCase();
      return (type === 'checkbox' || type === 'radio');
    }
    return false;
  },

  /**
   * @private
   * ラジオボタンかつチェックOFFか判断.
   */
  _isRadioOff: function(elm) {
    const tag = elm.tagName.toLowerCase();
    if (tag === 'input') {
      const type = ValUtil.nvl(elm.getAttribute('type')).toLowerCase();
      return (type === 'radio' && !elm.checked);
    }
    return false;
  },

  /**
   * @private
   * ラジオボタンかつ値が指定値以外か判断.
   */
  _isRadioNotVal: function(elm, val) {
    const tag = elm.tagName.toLowerCase();
    if (tag === 'input') {
      const type = ValUtil.nvl(elm.getAttribute('type')).toLowerCase();
      return (type === 'radio' && elm.value !== val);
    }
    return false;
  },

  /**
   * @private
   * テキスト入力要素か判断（隠し項目含む）.
   */
  _isTextType: function(elm) {
    const tag = elm.tagName.toLowerCase();
    if (tag === 'input') {
      const type = ValUtil.nvl(elm.getAttribute('type')).toLowerCase();
      return (type === 'text' || type === 'hidden');
    }
    return false;
  },

  /**
   * @private
   * テキストエリアか判断.
   */
  _isTextArea: function(elm) {
    const tag = elm.tagName.toLowerCase();
    return (tag === 'textarea');
  },

  /**
   * @private
   * Webサービス送信文字変換.<br>
   * <ul>
   * <li>Webサービスで処理するのは負荷が高いのでクライアント側で１項目づつ調整する。</li>
   * <li>タブ文字と右ブランクは除去する。</li>
   * <li>改行コードも除去するか LF に統一する。</li>
   * </ul>
   * 
   * @param {string} val 処理値
   * @param {boolean} [isRetIgnore] 改行を残す場合は <code>true</code>（省略可能）
   * @returns {string} 変換後値
   */
  _convPostVal: function(val, isRetIgnore) {
    // タブ文字除去
    const txt = ValUtil.nvl(val).replace(/\t/g, ' ');
    if (isRetIgnore) {
      // 改行コード残す（LF統一）、右ブランク除去
      return txt.replace(/\r?\n/g, '\n').replace(/ +$/, '');
    }
    // 改行コード除去（1バイトブランク置換）、右ブランク除去
    return txt.replace(/\r?\n/g, ' ').replace(/ +$/, '');
  },

  /** 
   * @private
   * HTMLタグの最初のHTMLタグとその終了タグ、それ以外のインナータグの３つに分割.<br>
   * <ul>
   *   <li>ダブルクォーテーションやシングルクォーテーションに囲まれていない最初の &gt; と最後の &lt; を探して分割する。</li>
   * </ul>
   * @param {string} html HTML文字列
   * @returns {Array<string>} [最初のタグ、インナータグ、最初のタグの終了タグ]（見つからない場合は長さゼロの配列）
   */
  _splitHtmlTagsOuterInner: function(html) {
    html = ValUtil.nvl(html).trim();
    if (ValUtil.isBlank(html)) {
      return [];
    }

    let outerBeginEnd = -1;
    let outerEndStart = -1;

    let inDq;
    let inSq;
    let i;

    i = 0
    inDq = false;
    inSq = false;
    while (i < html.length) {
      const char = html[i];
      if (!inSq && char === '"') {
        // ダブルクォート処理（シングルクォート内でない前提）
        inDq = !inDq;
        i++;
        continue
      }
      if (!inDq && char === "'") {
        // シングルクォート処理（ダブルクォート内でない前提）
        inSq = !inSq;
        i++;
        continue
      }
      if (!inDq && !inSq && char === '>') {
        // クォート外の > を発見
        outerBeginEnd = i;
        break;
      }
      i++;
    }
    if (outerBeginEnd < 0) {
      // 最初のタグが見つからない場合
      return [];
    }

    i = html.length - 1;
    while (i >= 0) {
      const char = html[i];
      // 終了タグ内にクォートはない前提
      if (char === '<') {
        //  < を発見
        outerEndStart = i;
        break;
      }
      i--;
    }
    if (outerEndStart < 0 || outerEndStart <= outerBeginEnd) {
      // 最初のタグの終了タグが見つからない場合
      return [];
    }

    const tags = [html.substring(0, outerBeginEnd + 1), html.substring(outerBeginEnd + 1, outerEndStart), html.substring(outerEndStart)];
    return tags;
  },

  /**
   * @private
   * 開始タグ解析.<br>
   * <ul>
   *   <li>開始タグからタグ名と属性を抽出する。</li>
   *   <li>HTMLタグ内はブランク区切り、属性は <code>=</code>区切りまたは <code>readonly</code>等の区切り無しを前提とする。</li>
   *   <li><pre>［例］ <code>&lt;tr class="row" style="color:black" hidden&gt;</code>は
   *      <code>['tr', {class: 'row', style: 'color:black', hidden: 'hidden'}]</code>を返す。</pre></li>
   * </ul>
   * @param {string} htmlTag 開始タグ文字列
   * @returns {Array<string, Object>|null} [タグ名, 属性連想配列]（解析できない場合は <code>null</code> を返す）
   */
  _parseHtmlOpenTag: function(htmlTag) {
    if (ValUtil.isBlank(htmlTag)) {
      return null;
    }
    htmlTag = ValUtil.nvl(htmlTag).trim();
    // < と > を除去してブランクで分割
    htmlTag = htmlTag.substring(1, htmlTag.length - 1).trim();
    const tags = htmlTag.split(' ');

    // 最初のブランクまでがタグ名
    const tagName = tags[0].toLowerCase();
    // 属性値群
    const attrs = {};

    // タグ名以外の部分を属性として解析
    for (const tag of tags.slice(1)) {
      const att = tag.trim();
      if (ValUtil.isBlank(att)) {
        continue;
      }

      const eqPos = att.indexOf('=');
      if (eqPos < 0) {
        // readonly 等の値なし属性
        attrs[att] = att;
        continue;
      }

      // 属性名=値 の形式
      const attrName = att.substring(0, eqPos);
      let attrVal = att.substring(eqPos + 1).trim();

      // クォートを除去
      if ((attrVal.startsWith('"') && attrVal.endsWith('"')) ||
        (attrVal.startsWith("'") && attrVal.endsWith("'"))) {
        attrVal = attrVal.substring(1, attrVal.length - 1);
      }

      attrs[attrName] = attrVal;
    }

    return [tagName, attrs];
  },
};


/**
 * セッションストレージユーティリティクラス.<br>
 * <ul>
 *   <li>ブラウザのセッションストレージに下記の単位＋キーで連想配列を格納・取得する。</li>
 *   <ul>
 *     <li>ページ単位（URLの HTMLファイル単位、１ページ内でデータ保持）</li>
 *     <li>機能単位（URLの機能ディレクトリ単位、ページ間でデータ共有）</li>
 *     <li>システム単位（システム全体でデータ共有）</li>
 *   </ul>
 *   <li>クリティカルな処理ではない前提とし、原則として例外エラーとしない。</li>
 * </ul>
 * @class
 */
const StorageUtil = /** @lends StorageUtil */ {

  /** @private ページ単位キープレフィックス */
  _KEY_PREFIX_PAGE: '@page',
  /** @private 機能単位キープレフィックス */
  _KEY_PREFIX_MODULE: '@module',
  /** @private システム共通キープレフィックス */
  _KEY_PREFIX_SYSTEM: '@system',

  /** @private ルートディレクトリ名 */
  _ROOT_DIR_NAME: '[root]',

  /**
   * ページ単位（URLの HTMLファイル単位）データ取得. <br>
   * <ul>
   *   <li>ブラウザのセッションストレージからページ単位＋キーで連想配列を取得する。</li>
   * </ul>
   * @param {string} key 取得キー
   * @param {Object} [notExistsValue] 非存在時戻値（省略可能）
   * @returns {Object|null} 取得データ
   */
  getPageObj: function(key, notExistsValue) {
    if (!StorageUtil._argsValidateObjGet('getPageObj', key, notExistsValue)) {
      return null;
    }
    const pageKey = StorageUtil._createPageKey(key);
    const obj = StorageUtil._getObj(pageKey, notExistsValue);
    return obj;
  },

  /**
   * 機能単位（URLの機能ディレクトリ単位）データ取得. <br>
   * <ul>
   *   <li>ブラウザのセッションストレージから機能単位＋キーで連想配列を取得する。</li>
   * </ul>
   * @param {string} key 取得キー
   * @param {Object} [notExistsValue] 非存在時戻値（省略可能）
   * @returns {Object|null} 取得データ
   */
  getModuleObj: function(key, notExistsValue) {
    if (!StorageUtil._argsValidateObjGet('getModuleObj', key, notExistsValue)) {
      return null;
    }
    const mdlKey = StorageUtil._createModuleKey(key);
    const obj = StorageUtil._getObj(mdlKey, notExistsValue);
    return obj;
  },

  /**
   * システム単位データ取得.<br>
   * <ul>
   *   <li>ブラウザのセッションストレージからキーで連想配列を取得する。</li>
   * </ul>
   * @param {string} key 取得キー
   * @param {Object} [notExistsValue] 非存在時戻値（省略可能）
   * @returns {Object|null} 取得データ
   */
  getSystemObj: function(key, notExistsValue) {
    if (!StorageUtil._argsValidateObjGet('getSystemObj', key, notExistsValue)) {
      return null;
    }
    const sysKey = StorageUtil._createSystemKey(key);
    const obj = StorageUtil._getObj(sysKey, notExistsValue);
    return obj;
  },

  /**
   * ページ単位データ（URLの HTMLファイル単位）格納.<br>
   * <ul>
   *   <li>ブラウザのセッションストレージにページ単位＋キーで連想配列を格納する。</li>
   * </ul>
   * @param {string} key 格納キー
   * @param {Object} obj 格納データ
   * @returns {boolean} 格納成功時は <code>true</code>
   */
  setPageObj: function(key, obj) {
    if (!StorageUtil._argsValidateObjSet('setPageObj', key, obj)) {
      return false;
    }
    const pageKey = StorageUtil._createPageKey(key);
    return StorageUtil._setObj(pageKey, obj);
  },

  /**
   * 機能単位データ（URLの機能ディレクトリ単位）格納.<br>
   * <ul>
   *   <li>ブラウザのセッションストレージに機能単位＋キーで連想配列を格納する。</li>
   * </ul>
   * @param {string} key 格納キー
   * @param {Object} obj 格納データ
   * @returns {boolean} 格納成功時は <code>true</code>
   */
  setModuleObj: function(key, obj) {
    if (!StorageUtil._argsValidateObjSet('setModuleObj', key, obj)) {
      return false;
    }
    const mdlKey = StorageUtil._createModuleKey(key);
    return StorageUtil._setObj(mdlKey, obj);
  },

  /**
   * システム単位データ格納.<br>
   * <ul>
   *   <li>ブラウザのセッションストレージにキーで連想配列を格納する。</li>
   * </ul>
   * @param {string} key 格納キー
   * @param {Object} obj 格納データ
   * @returns {boolean} 格納成功時は <code>true</code>
   */
  setSystemObj: function(key, obj) {
    if (!StorageUtil._argsValidateObjSet('setSystemObj', key, obj)) {
      return false;
    }
    const sysKey = StorageUtil._createSystemKey(key);
    return StorageUtil._setObj(sysKey, obj);
  },

  /**
   * ページ単位データ削除.
   * @param {string} key キー
   * @returns {boolean} 削除成功時は <code>true</code>
   */
  removePage: function(key) {
    if (ValUtil.isBlank(key)) {
      console.error('StorageUtil#removePage: key is required.');
      return false;
    }
    const pageKey = StorageUtil._createPageKey(key);
    return StorageUtil._remove(pageKey);
  },

  /**
   * 機能単位データ削除.
   * @param {string} key キー
   * @returns {boolean} 削除成功時は <code>true</code>
   */
  removeModule: function(key) {
    if (ValUtil.isBlank(key)) {
      console.error('StorageUtil#removeModule: key is required.');
      return false;
    }
    const mdlKey = StorageUtil._createModuleKey(key);
    return StorageUtil._remove(mdlKey);
  },

  /**
   * システム単位データ削除.
   * @param {string} key 削除キー
   * @returns {boolean} 削除成功時は <code>true</code>
   */
  removeSystem: function(key) {
    if (ValUtil.isBlank(key)) {
      console.error('StorageUtil#removeSystem: key is required.');
      return false;
    }
    const sysKey = StorageUtil._KEY_PREFIX_SYSTEM + key;
    return StorageUtil._remove(sysKey);
  },

  /**
   * 全データクリア.<br>
   * <ul>
   *   <li>このユーティリティで格納されたすべてのデータを削除する。</li>
   *   <li>緊急時やデバッグ用途で使用する。</li>
   * </ul>
   * @returns {boolean} クリア成功時は <code>true</code>
   */
  clearAllData: function() {
    const systemResult = StorageUtil.clearSystem();
    const moduleResult = StorageUtil.clearModule();
    const pageResult = StorageUtil.clearPage();
    return systemResult && moduleResult && pageResult;
  },

  /**
   * @private
   * 取得時引数チェック.
   * @param {string} methodName 取得メソッド名
   * @param {string} key 取得キー
   * @param {Object} [notExistsValue] 非存在時戻値（省略可能）
   * @returns {boolean} エラーの場合は <code>false</code>
   */
  _argsValidateObjGet: function(methodName, key, notExistsValue) {
    if (ValUtil.isBlank(key)) {
      console.error(`StorageUtil#${methodName}: key is required.`);
      return false;
    }
    if (!ValUtil.isEmpty(notExistsValue) && !ValUtil.isObj(notExistsValue)) {
      console.error(`StorageUtil#${methodName}: notExistsValue must be an object.`);
      return false;
    }
    return true;
  },

  /**
   * @private
   * 格納時引数チェック.
   * @param {string} methodName 取得メソッド名
   * @param {string} key 取得キー
   * @param {Object} obj 格納データ
   * @returns {boolean} エラーの場合は <code>false</code>
   */
  _argsValidateObjSet: function(methodName, key, obj) {
    if (ValUtil.isBlank(key)) {
      console.error(`StorageUtil#${methodName}: key is required.`);
      return false;
    }
    if (!ValUtil.isObj(obj)) {
      console.error(`StorageUtil#${methodName}: store data must be an object.`);
      return false;
    }
    return true;
  },

  /**
   * @private
   * データ取得.
   * @param {string} key キー
   * @param {Object} [notExistsValue] 非存在時戻値（省略可能）
   * @returns {Object|null} 取得データ
   */
  _getObj: function(key, notExistsValue) {
    try {
      const json = sessionStorage.getItem(key);
      if (ValUtil.isNull(json)) {
        if (!ValUtil.isNull(notExistsValue)) {
          return notExistsValue;
        } else {
          return null;
        }
      }
      return JSON.parse(json);
    } catch (e) {
      console.error(`StorageUtil#_getObj: Failed to parse JSON data. key=${key}`, e);
      // 破損データを削除
      try {
        sessionStorage.removeItem(key);
      } catch (removeError) {
        console.error(`StorageUtil#_getObj: Failed to remove corrupted data. key=${key}`, removeError);
      }
      return null;
    }
  },

  /**
   * @private
   * データ格納.
   * @param {string} key キー
   * @param {Object} obj 格納データ
   * @returns {boolean} 格納成功時は <code>true</code>
   */
  _setObj: function(key, obj) {
    try {
      const json = JSON.stringify(obj);
      sessionStorage.setItem(key, json);
      return true;
    } catch (e) {
      if (e.name === 'QuotaExceededError') {
        console.error(`StorageUtil#_setData: Storage quota exceeded. key=${key}`, e);
      } else {
        console.error(`StorageUtil#_setData: Failed to save data. key=${key}`, e);
      }
      return false;
    }
  },

  /**
   * @private
   * データ削除.
   * @param {string} key キー
   * @returns {boolean} 削除成功時は <code>true</code>
   */
  _remove: function(key) {
    try {
      sessionStorage.removeItem(key);
      return true;
    } catch (e) {
      console.warn(`StorageUtil#_remove: Failed to remove data. key=${key}`, e);
      return false;
    }
  },

  /**
   * @private
   * ページ単位キー生成.
   * @param {string} key キー
   * @returns {string} ページ単位キー
   */
  _createPageKey: function(key) {
    return StorageUtil._createPageKeyPrefixByLocation() + key;
  },

  /**
   * @private
   * 現在ページのページ単位キープレフィックス生成.
   * @returns {string} ページ単位キープレフィックス
   */
  _createPageKeyPrefixByLocation: function() {
    // locationからページ名を取得
    const paths = location.pathname.split('/');
    // ページ名を取得
    let pageName = paths.pop();
    if (ValUtil.isBlank(pageName)) {
      pageName = 'index';
    } else {
      // 拡張子を除去
      const dotPos = pageName.lastIndexOf('.');
      if (dotPos > 0) {
        pageName = pageName.substring(0, dotPos);
      }
    }
    // ディレクトリ名を取得
    let mdlName = paths.pop();
    if (ValUtil.isBlank(mdlName)) {
      // ルートディレクトリの場合
      mdlName = StorageUtil._ROOT_DIR_NAME;
    }
    return `${StorageUtil._KEY_PREFIX_PAGE}/${mdlName}/${pageName}/`;
  },

  /**
   * @private
   * 機能単位キー生成.
   * @param {string} key キー
   * @returns {string} 機能単位キー
   */
  _createModuleKey: function(key) {
    return StorageUtil._createModuleKeyPrefixByLocation() + key;
  },

  /**
   * @private
   * 現在機能の機能単位キープレフィックス生成.
   * @returns {string} 機能単位キープレフィックス
   */
  _createModuleKeyPrefixByLocation: function() {
    // locationから機能名を取得
    const paths = location.pathname.split('/');
    // ファイル名を除去
    paths.pop();
    // ディレクトリ名を取得
    let mdlName = paths.pop();
    if (ValUtil.isBlank(mdlName)) {
      // ルートディレクトリの場合
      mdlName = StorageUtil._ROOT_DIR_NAME;
    }
    return `${StorageUtil._KEY_PREFIX_MODULE}/${mdlName}/`;
  },

  /**
   * @private
   * システム単位キー生成.
   * @param {string} key キー
   * @returns {string} システム単位キー
   */
  _createSystemKey: function(key) {
    return `${StorageUtil._KEY_PREFIX_SYSTEM}/${key}`;
  },

  /**
   * ページ単位データ全削除.<br>
   * <ul>
   *   <li>現在のページで格納されたすべてのデータを削除する。</li>
   * </ul>
   * @returns {boolean} クリア成功時は <code>true</code>
   */
  clearPage: function() {
    const prefix = StorageUtil._createPageKeyPrefixByLocation();
    return StorageUtil._clear(prefix);
  },

  /**
   * 機能単位データ全削除.<br>
   * <ul>
   *   <li>現在の機能で格納されたすべてのデータを削除する。</li>
   * </ul>
   * @returns {boolean} クリア成功時は <code>true</code>
   */
  clearModule: function() {
    const prefix = StorageUtil._createModuleKeyPrefixByLocation();
    return StorageUtil._clear(prefix);
  },

  /**
   * システム単位データクリア.<br>
   * <ul>
   *   <li>システム共有で格納されたすべてのデータを削除する。</li>
   * </ul>
   * @returns {boolean} クリア成功時は <code>true</code>
   */
  clearSystem: function() {
    const prefix = StorageUtil._KEY_PREFIX_SYSTEM;
    return StorageUtil._clear(prefix);
  },

  /**
   * @private
   * プレフィックス指定データ全削除.
   * @param {string} prefix プレフィックス
   * @returns {boolean} クリア成功時は <code>true</code>
   */
  _clear: function(prefix) {
    let count = 0;
    try {
      // sessionStorageの全キーを確認
      for (let i = (sessionStorage.length - 1); i >= 0; i--) {
        const key = sessionStorage.key(i);
        if (!ValUtil.isNull(key) && key.startsWith(prefix)) {
          sessionStorage.removeItem(key);
          count++;
        }
      }
      console.info(`StorageUtil#_clear: Cleared ${count} items.`);
      return true;
    } catch (e) {
      console.error('StorageUtil#_clear: Failed to clear data.', e);
      return false;
    }
  },

  /**
   * @private
   * デバッグ用：格納データ全件表示.<br>
   * <ul>
   *   <li>現在格納されているすべてのデータをコンソールに表示する。</li>
   *   <li>開発・デバッグ用途でのみ使用する。</li>
   * </ul>
   */
  _debugAllData: function() {
    console.group('StorageUtil#_debugAllData: All Data');
    
    try {
      const sysObj = {};
      const mdlObj = {};
      const pageObj = {};
      const otherObj = {};
      
      for (let i = 0; i < sessionStorage.length; i++) {
        const key = sessionStorage.key(i);
        if (ValUtil.isNull(key)) {
          continue;
        }
        const value = sessionStorage.getItem(key);
        if (key.startsWith(StorageUtil._KEY_PREFIX_SYSTEM)) {
          const orgKey = key.substring(StorageUtil._KEY_PREFIX_SYSTEM.length);
          sysObj[orgKey] = value;
        } else if (key.startsWith(StorageUtil._KEY_PREFIX_MODULE)) {
          const orgKey = key.substring(StorageUtil._KEY_PREFIX_MODULE.length);
          mdlObj[orgKey] = value;
        } else if (key.startsWith(StorageUtil._KEY_PREFIX_PAGE)) {
          const orgKey = key.substring(StorageUtil._KEY_PREFIX_PAGE.length);
          pageObj[orgKey] = value;
        } else {
          otherObj[key] = value;
        }
      }
      console.log(StorageUtil._KEY_PREFIX_SYSTEM + ':', sysObj);
      console.log(StorageUtil._KEY_PREFIX_MODULE + ':', mdlObj);
      console.log(StorageUtil._KEY_PREFIX_PAGE + ':', pageObj);
      console.log('Other:', otherObj);
    } catch (e) {
      console.error('StorageUtil#_debugAllData: Failed to show data.', e);
    }
    
    console.groupEnd();
  }
};

