package com.onepg.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * ファイル操作ユーティリティクラス.
 */
public final class FileUtil {

  /** 日時フォーマッター：タイムスタンプ ファイル用. */
  private static final DateTimeFormatter DTF_FILE_TIMESTAMP =
      DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss").withResolverStyle(ResolverStyle.STRICT);

  /**
   * コンストラクタ.
   */
  private FileUtil() {
    // 処理なし
  }

  /**
   * OS一時ディレクトリパス取得.<br>
   * <ul>
   * <li>具体的には Java の java.io.tmpdir システムプロパティのパスを返す。</li>
   * </ul>
   *
   * @return OS一時ディレクトリパス
   */
  public static String getOsTemporaryPath() {
    final String tempDir = System.getProperty("java.io.tmpdir");
    return tempDir;
  }

  /**
   * ファイルパス結合.<br>
   * <ul>
   * <li>引数の最後にブランクを渡すと / または \ 終わりのパスを返す。</li>
   * <li>区切文字は OS に沿った文字となる。</li>
   * </ul>
   *
   * @param paths ファイルパス（複数指定）
   * @return ファイルパス
   */
  public static String joinPath(final String... paths) {
    if (ValUtil.isEmpty(paths)) {
      return ValUtil.BLANK;
    }
    
    String ret = new String(paths[0]);
    if (PropertiesUtil.isWindowsOs() && ret.length() == 2 && ret.toString().endsWith(":")) {
      ret += File.separator;
    }
    for (int i = 1; i < paths.length; i++) {
      ret = (new File(ret, paths[i])).getPath();
    }
    if (paths.length > 1 && ValUtil.isBlank(paths[paths.length - 1])) {
      ret += File.separator;
    }
    return ret.toString();
  }

  /**
   * OSパス変換.<br>
   * <ul>
   * <li>区切文字が OS に沿った文字に置換される。</li>
   * </ul>
   *
   * @param path ファイルパス
   * @return ファイルパス
   */
  public static String convOsPath(final String path) {
    return (new File(path)).getPath();
  }

  /**
   * 絶対パス変換.<br>
   * <ul>
   * <li>相対パスがあれば絶対パスに変換される。</li>
   * <li>区切文字が OS に沿った文字に置換される。</li>
   * </ul>
   *
   * @param path 相対パス
   * @return 絶対パス
   */
  public static String convAbsolutePath(final String path) {
    final String[] paths = ValUtil.split(convOsPath(path), File.separator);
    String ret = new String(paths[0]);
    if (PropertiesUtil.isWindowsOs() && ret.length() == 2 && ret.endsWith(":")) {
      ret += File.separator;
    }
    for (int i = 1; i < paths.length; i++) {
      if (".".equals(paths[i])) {
        continue;
      }
      if ("..".equals(paths[i])) {
        if (i == 1) {
          // ルートの次の場合は親が無いのでスキップ
          continue;
        }
        ret = (new File(ret)).getParentFile().getPath();
        continue;
      }
      ret = (new File(ret, paths[i])).getPath();
    }
    return ret;
  }

  /**
   * ファイル存在確認（ディレクトリも可）.
   *
   * @param checkPath 確認パス
   * @return 存在する場合は <code>true</code>
   */
  public static boolean exists(final String checkPath) {
    final Path path = Paths.get(checkPath);
    return Files.exists(path);
  }

  /**
   * 親ディレクトリ存在確認（ディレクトリも可）.
   *
   * @param checkPath 確認パス
   * @return 存在する場合は <code>true</code>
   */
  public static boolean existsParent(final String checkPath) {
    if (ValUtil.isBlank(checkPath)) {
      return false;
    }
    final String parentPath = getParentPath(checkPath);
    return (!ValUtil.isNull(parentPath)) && exists(parentPath);
  }

  /**
   * フルパスからファイル名取得（ディレクトリも可）.
   *
   * @param fullPath フルパス
   * @return ファイル名のみ
   */
  public static String getFileName(final String fullPath) {
    return Paths.get(fullPath).getFileName().toString();
  }

  /**
   * フルパスから親ディレクトリパス取得（ディレクトリも可）.
   *
   * @param fullPath フルパス
   * @return 親ディレクトリパス
   */
  public static String getParentPath(final String fullPath) {
    return Paths.get(fullPath).getParent().toString();
  }

  /**
   * ファイル更新日時取得.
   *
   * @param fullPath フルパス
   * @return ファイル更新日時（yyyyMMddHHmmss）
   */
  public static String getFileModifiedDateTime(final String fullPath) {
    final File file = new File(fullPath);
    final long lastModified = file.lastModified();
    final Instant lastInst = Instant.ofEpochMilli(lastModified);
    final LocalDateTime ldt = LocalDateTime.ofInstant(lastInst, ZoneId.systemDefault());
    final String ret = DTF_FILE_TIMESTAMP.format(ldt);
    return ret;
  }

  /**
   * ファイル名またはフルパスを拡張子とそれ以外に分割.
   *
   * @param fileName ファイル名またはフルパス
   * @return 文字配列｛拡張子より前の部分、拡張子｝（いずれもドットは含まない）
   */
  public static String[] splitFileTypeMark(final String fileName) {
    final int markIdx = fileName.lastIndexOf(".");
    final String[] ret = new String[2];

    if (markIdx <= 0) {
      // 拡張子が無い、またはドット始まりのファイル名は拡張子無しで返す。
      ret[0] = fileName;
      ret[1] = ValUtil.BLANK;
      return ret;
    }
    ret[0] = fileName.substring(0, markIdx);
    ret[1] = fileName.substring(markIdx + 1);
    return ret;
  }

  /**
   * ファイルパス（絶対パス）リスト取得.<br>
   * <ul>
   * <li>ファイル名、拡張子の検索文字は大文字小文字を区別しない。</li>
   * </ul>
   *
   * @param dirPath     対象ディレクトリパス
   * @param typeMark    検索拡張子（省略可能）省略した場合は <code>null</code> ※ドット文字不要
   * @param prefixMatch 検索ファイル名 前方一致（省略可能）省略した場合は <code>null</code>
   * @param middleMatch 検索ファイル名 中間一致（省略可能）省略した場合は <code>null</code>
   * @param suffixMatch 検索ファイル名 後方一致（省略可能）省略した場合は <code>null</code>
   * @return ファイルパス（絶対パス）リスト
   * @throws RuntimeException ディレクトリが存在しない場合
   */
  public static List<String> getFileList(final String dirPath, final String typeMark,
      final String prefixMatch, final String middleMatch, final String suffixMatch) {
    final File parentDir = new File(dirPath);
    if (!parentDir.exists()) {
      throw new RuntimeException("Directory does not exist. " + LogUtil.joinKeyVal("path", dirPath));
    }

    final File[] files;
    if (ValUtil.isBlank(typeMark) && ValUtil.isBlank(prefixMatch) && ValUtil.isBlank(middleMatch)
        && ValUtil.isBlank(suffixMatch)) {
      // 条件無し
      files = parentDir.listFiles();
    } else {
      // 条件有り
      final String typeMarkL = ValUtil.nvl(typeMark).toLowerCase();
      final String prefixMatchL = ValUtil.nvl(prefixMatch).toLowerCase();
      final String middleMatchL = ValUtil.nvl(middleMatch).toLowerCase();
      final String suffixMatchL = ValUtil.nvl(suffixMatch).toLowerCase();
      final FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
          final String[] names = FileUtil.splitFileTypeMark(name);
          names[0] = names[0].toLowerCase();
          names[1] = names[1].toLowerCase();
          if (!ValUtil.isBlank(typeMarkL) && !names[1].equals(typeMarkL)) {
            // 拡張子 一致しない
            return false;
          }
          if (!ValUtil.isBlank(prefixMatchL) && !names[0].startsWith(prefixMatchL)) {
            // ファイル名 前方一致しない
            return false;
          }
          if (!ValUtil.isBlank(middleMatchL) && !names[0].contains(middleMatchL)) {
            // ファイル名 中間一致しない
            return false;
          }
          if (!ValUtil.isBlank(suffixMatchL) && !names[0].endsWith(suffixMatchL)) {
            // ファイル名 後方一致しない
            return false;
          }
          // 指定された条件に一致する
          return true;
        }
      };
      files = parentDir.listFiles(filter);
    }

    final List<String> retList = new ArrayList<>();
    if (ValUtil.isNull(files)) {
      return retList;
    }
    for (final File file : files) {
      retList.add(file.getAbsolutePath());
    }
    return retList;
  }

  /**
   * ファイル移動.
   *
   * @see #move(File, File)
   * @param srcFilePath 移動元ファイルパス
   * @param destFilePath 移動先ファイルパス（ディレクトリ指定可）
   * @return 移動先ファイルオブジェクト
   */
  public static File move(final String srcFilePath, final String destFilePath) {
    return move(new File(srcFilePath), new File(destFilePath));
  }

  /**
   * ファイル移動.<br>
   * <ul>
   * <li>移動先がディレクトリ指定の場合、ファイル名は移動元と同じになる。</li>
   * </ul>
   *
   * @param srcFile 移動元ファイル
   * @param destFile 移動先ファイル（ディレクトリ指定可）
   * @return 移動先ファイルオブジェクト
   * @throws IllegalArgumentException 移動先ファイルが既に存在する場合
   * @throws IllegalStateException ファイル移動に失敗した場合
   */
  public static File move(final File srcFile, final File destFile) {
    final Path srcPath = Paths.get(srcFile.getAbsolutePath());
    final Path destPath = resolveDestinationPath(srcFile, destFile);
    
    if (destPath.toFile().exists()) {
      throw new RuntimeException("Destination file already exists. "
                              + LogUtil.joinKeyVal("path", destPath.toString()));
    }
    try {
      Files.move(srcPath, destPath);
    } catch (IOException e) {
      throw new RuntimeException("Exception error occurred in file move. " + LogUtil.joinKeyVal("src",
          srcFile.getAbsolutePath(), "dest", destFile.getAbsolutePath()), e);
    }
    return destPath.toFile();
  }

  /**
   * ファイルコピー.
   *
   * @see #copy(File, File)
   * @param srcFilePath コピー元ファイルパス
   * @param destFilePath コピー先ファイルパス（ディレクトリ指定可）
   * @return コピー先ファイルオブジェクト
   */
  public static File copy(final String srcFilePath, final String destFilePath) {
    return copy(new File(srcFilePath), new File(destFilePath));
  }

  /**
   * ファイルコピー.<br>
   * <ul>
   * <li>コピー先がディレクトリ指定の場合、ファイル名はコピー元と同じになる。</li>
   * </ul>
   *
   * @param srcFile コピー元ファイル
   * @param destFile コピー先ファイル（ディレクトリ指定可）
   * @return コピー先ファイルオブジェクト
   * @throws IllegalArgumentException コピー先ファイルが既に存在する場合
   * @throws IllegalStateException ファイルコピーに失敗した場合
   */
  public static File copy(final File srcFile, final File destFile) {
    final Path srcPath = Paths.get(srcFile.getAbsolutePath());
    final Path destPath = resolveDestinationPath(srcFile, destFile);
    
    if (destPath.toFile().exists()) {
      throw new RuntimeException("Destination file already exists. "
                               + LogUtil.joinKeyVal("path", destPath.toString()));
    }
    try {
      Files.copy(srcPath, destPath);
    } catch (IOException e) {
      throw new RuntimeException("Exception error occurred in file copy. " + LogUtil.joinKeyVal("src",
          srcFile.getAbsolutePath(), "dest", destFile.getAbsolutePath()), e);
    }
    return destPath.toFile();
  }

  /**
   * 移動・コピー先のパスを解決.
   */
  private static Path resolveDestinationPath(final File srcFile, final File destFile) {
    if (destFile.isDirectory()) {
      return Paths.get(FileUtil.joinPath(destFile.getAbsolutePath(), srcFile.getName()));
    } else {
      return Paths.get(destFile.getAbsolutePath());
    }
  }

  /**
   * ファイル削除.<br>
   * <ul>
   * <li>ファイルが無ければ <code>false</code> を返す。</li>
   * </ul>
   *
   * @param deleteFilePath 削除ファイルパス
   * @return ファイルが無い場合は <code>false</code>
   * @throws IllegalStateException ファイル削除に失敗した場合
   */
  public static boolean delete(final String deleteFilePath) {
    final Path deletePath = Paths.get(deleteFilePath);
    if (!deletePath.toFile().exists()) {
      return false;
    }
    try {
      Files.delete(deletePath);
    } catch (IOException e) {
      throw new RuntimeException("Exception error occurred in file deletion. "
                                + LogUtil.joinKeyVal("path", deleteFilePath), e);
    }
    return true;
  }

  /**
   * ファイル削除.
   *
   * @see #delete(String)
   * @param deleteFile 削除ファイル
   * @return ファイルが無い場合は <code>false</code>
   */
  public static boolean delete(final File deleteFile) {
    return delete(deleteFile.getAbsolutePath());
  }

  /**
   * ディレクトリ作成.
   *
   * @param dirPath ディレクトリパス
   * @return 既に存在する場合は <code>false</code>
   * @throws IllegalStateException ディレクトリ作成に失敗した場合
   */
  public static boolean makeDir(final String dirPath) {
    final Path path = Paths.get(dirPath);
    if (Files.exists(path)) {
      return false;
    }
    try {
      // 無ければ親ディレクトリも作成する
      Files.createDirectories(path);
    } catch (IOException e) {
      throw new RuntimeException("Exception error occurred in directory creation. " + LogUtil.joinKeyVal("path", dirPath),
          e);
    }
    return true;
  }

}
