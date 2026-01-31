package com.example.app.service.exmodule;

import java.sql.Connection;

import com.onepg.db.SqlUtil;
import com.onepg.util.Io;
import com.onepg.util.IoItems;
import com.onepg.util.IoRows;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;
import com.onepg.util.Io.MsgType;
import com.onepg.web.AbstractDbAccessWebService;

/**
 * 登録・更新 Webサービスクラス.
 */
public class ExampleUpsert extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    // ヘッダーバリデーション
    validateHeader(io);
    if (io.hasErrorMsg()) {
      // バリデーションエラー時は処理を抜ける
      return;
    }
    // 明細バリデーション
    validateDetail(io);
    if (io.hasErrorMsg()) {
      // バリデーションエラー時は処理を抜ける
      return;
    }

    // ヘッダー登録・更新
    upsertHead(io);
    if (io.hasErrorMsg()) {
      // 排他制御エラー時は処理を抜ける
      return;
    }
    // 明細削除登録
    delInsDetail(io);
    // 成功メッセージ設定
    if (ValUtil.isBlank(io.getString("upd_ts"))) {
      // 新規登録時メッセージセット
      io.putMsg(MsgType.INFO, "i0001", new String[] { io.getString("user_id") });
    } else {
      // 更新時メッセージセット
      io.putMsg(MsgType.INFO, "i0002", new String[] { io.getString("user_id") });
    } 
  }

  /**
   * ヘッダーバリデーション.
   * 
   * @param io 引数かつ戻値（リクエストかつレスポンス）
   * @throws Exception バリデーションエラー
   */
  private void validateHeader(final Io io) throws Exception {

    // ユーザーIDチェック
    final String userId = io.getString("user_id");
    if (ValUtil.isBlank(userId)) {
      // 未入力メッセージセット
      io.putMsg(MsgType.ERROR,"ev001", new String[]{"ユーザーID"}, "user_id");
    } else if (!ValUtil.isAlphabetNumber(userId)) {
      // 半角英数字以外メッセージセット
      io.putMsg(MsgType.ERROR, "ev011", new String[] { "ユーザーID" }, "user_id");
    } else if (!ValUtil.checkLength(userId, 4)) {
      // 桁数不正メッセージセット
      io.putMsg(MsgType.ERROR, "ev021", new String[] { "ユーザーID" , "4" }, "user_id");
    }

    // ユーザー名チェック
    final String userNm = io.getString("user_nm");
    if (ValUtil.isBlank(userNm)) {
      // 未入力メッセージセット
      io.putMsg(MsgType.ERROR, "ev001", new String[] { "ユーザー名" }, "user_nm");
    } else if (!ValUtil.checkLength(userNm, 20)) {
      // 桁数不正メッセージセット
      io.putMsg(MsgType.ERROR, "ev021", new String[] { "ユーザー名", "20" }, "user_nm");
    }

    // Emailチェック
    final String email = io.getString("email");
    if (!ValUtil.isBlank(email)) {
      if (!ValUtil.checkLength(email, 50)) {
        // 桁数不正メッセージセット
        io.putMsg(MsgType.ERROR, "ev021", new String[] { "Email", "50" }, "email");
      }
    }

    // 年収チェック
    final String incomeAm = io.getString("income_am");
    if (!ValUtil.isBlank(incomeAm) ) {
      if (!ValUtil.isNumber(incomeAm)) {
        // 数値不正メッセージセット
        io.putMsg(MsgType.ERROR, "ev012", new String[] { "年収" }, "income_am");
      } else if (!ValUtil.checkLengthNumber(incomeAm, 10, 0) ) {
        // 桁数不正メッセージセット
        io.putMsg(MsgType.ERROR, "ev022", new String[] { "年収", "10" }, "income_am");
      }
    }

    // 誕生日チェック
    final String birthDt = io.getString("birth_dt");
    if (!ValUtil.isBlank(birthDt)) {
      if (!ValUtil.isDate(birthDt)) {
        // 日付不正メッセージセット
        io.putMsg(MsgType.ERROR, "ev013", new String[] { "誕生日" }, "birth_dt");
      }
    }
  }

  /**
   * 明細バリデーション.
   * 
   * @param io 引数かつ戻値（リクエストかつレスポンス）
   * @throws Exception バリデーションエラー
   */
  private void validateDetail(final Io io) throws Exception {
    if (!io.containsKeyRows("detail")) {
      // 明細が存在しない場合は明細チェックをスキップ
      return;
    }

    final IoRows detail = io.getRows("detail");

    // 明細行ループ（インデックスを使うので拡張for文は使わない）
    for (int rowIdx = 0; rowIdx < detail.size(); rowIdx++) {
      final IoItems row = detail.get(rowIdx);
      // ペット名チェック
      final String petNm = row.getString("pet_nm");
      if (ValUtil.isBlank(petNm)) {
        // 未入力メッセージセット
        io.putMsg(MsgType.ERROR, "ev001", new String[] { "ペット名" }, "pet_nm", "detail", rowIdx);
      } else if (!ValUtil.checkLength(petNm, 10)) {
        // 桁数不正メッセージセット
        io.putMsg(MsgType.ERROR, "ev021", new String[] { "ペット名", "10" }, "pet_nm", "detail", rowIdx);
      }

      // 体重チェック
      final String weightKg = row.getString("weight_kg");
      if (!ValUtil.isBlank(weightKg)) {
        if (!ValUtil.isNumber(weightKg)) {
          // 数値不正メッセージセット
          io.putMsg(MsgType.ERROR, "ev012", new String[] { "体重" }, "weight_kg", "detail", rowIdx);
        } else if (!ValUtil.checkLengthNumber(weightKg, 3, 1)) {
          // 桁数不正メッセージセット
          io.putMsg(MsgType.ERROR, "ev023", new String[] { "体重", String.valueOf(3 - 1),  "1" }, "weight_kg", "detail", rowIdx);
        }
      }

      // 誕生日チェック
      final String birthDt = row.getString("birth_dt");
      if (!ValUtil.isBlank(birthDt)) {
        if (!ValUtil.isDate(birthDt)) {
          // 日付不正メッセージセット
          io.putMsg(MsgType.ERROR, "ev013", new String[] { "誕生日" }, "birth_dt", "detail", rowIdx);
        }
      }
    }
  }

  /**
   * ヘッダー登録・更新.
   * 
   * @param io 引数かつ戻値（リクエストかつレスポンス）
   */
  private void upsertHead(final Io io) {
    if (ValUtil.isBlank(io.getString("upd_ts"))) {
      // 更新タイムスタンプがブランクの場合は DB１件新規登録
      if (!SqlUtil.insertOne(getDbConn(), "t_user", io, "upd_ts")) {
        io.putMsg(MsgType.ERROR, "e0001", new String[] { io.getString("user_id") }, "user_id");
        super.logger.info("Unique constraint violation occurred during header insert. " + LogUtil.joinKeyVal("user_id", io.getString("user_id")));
      }
      return;
    }

    // DB１件更新
    if (!SqlUtil.updateOne(getDbConn(), "t_user", io, new String[]{"user_id"}, "upd_ts")) {
      io.putMsg(MsgType.ERROR, "e0002", new String[] { io.getString("user_id") }, "user_id");
    }
  }

  /**
   * 明細削除登録.
   * 
   * @param io 引数かつ戻値（リクエストかつレスポンス）
   */
  private void delInsDetail(final Io io) {
    final Connection conn = getDbConn();
    // DB複数件削除
    final int delCnt = SqlUtil.delete(conn, "t_user_pet", io, new String[] { "user_id" });

    if (!io.containsKeyRows("detail")) {
      // 明細が存在しない場合は明細登録をスキップ
      if (super.logger.isDevelopMode()) {
        super.logger.develop(LogUtil.joinKeyVal("deleted count", delCnt, "inserted count", 0));
      }
      return;
    }

    // 明細新規登録
    final IoRows detail = io.getRows("detail");
    final String userId = io.getString("user_id");
    int dno = 0;
    for (final IoItems row : detail) {
      dno++;
      // キー値セット
      row.put("user_id", userId);
      row.put("pet_no", dno);
      // DB１件登録
      SqlUtil.insertOne(conn, "t_user_pet", row);
    }
    if (super.logger.isDevelopMode()) {
      super.logger.develop(LogUtil.joinKeyVal("deleted count", delCnt, "inserted count", dno));
    }
  }
}