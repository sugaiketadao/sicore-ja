package com.example.app.service.exmodule;

import com.onepg.db.SqlUtil;
import com.onepg.util.Io;
import com.onepg.web.AbstractDbAccessWebService;

/**
 * 一覧初期処理 Webサービスクラス.
 */
public class ExampleListInit extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    final String today = SqlUtil.getToday(getDbConn());
    // 初期値セット
    io.put("birth_dt", today);
  }
}
 