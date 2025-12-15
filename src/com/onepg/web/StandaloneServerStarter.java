package com.onepg.web;

/**
 * StandaloneHttpServer起動クラス.
 * @hidden
 */
public final class StandaloneServerStarter {
  /**
   * メイン処理.
   * @param args コマンドライン引数
   */
  public static void main(final String[] args) {
     new Thread(() -> StandaloneServer.main(args)).start();
  }
}