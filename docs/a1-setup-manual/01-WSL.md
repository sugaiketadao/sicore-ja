## WSL（Windows Subsystem for Linux）インストール
1. スタートメニューから Windows PowerShell を右クリックメニューの管理者権限で実行し、`wsl --install` コマンドを実行する。
    - インストール後に `Create a default Unix user account:` が表示された場合は OS再起動をスキップして手順4へ進む。

2. インストール後 OS再起動する。

3. OS再起動後にスタートメニューからコマンドプロンプトを実行し、`wsl` コマンドを実行する。
    - 何も表示されない場合は後出の [WSL が実行できない場合](#wslが実行できない場合) を参照してください。

4. WSL上のユーザーを作成する。（ユーザー名・パスワードは任意の値を指定できます）
    - `Create a default Unix user account:` の後にユーザー名を入力する。
    - `New password:` の後にパスワードを入力する。
    - `Retype new password:` の後に再度パスワードを入力する。

5. ホームディレクトリを移動する。
    - `cd ~` を入力する。

---
- WSL 停止方法
    - スタートメニューからコマンドプロンプトを実行し、`wsl --shutdown` コマンドを実行する。

- WSL 起動方法
    - スタートメニューから Ubuntu を実行する。

---
## WSL が実行できない場合
1. スタートメニューからコントロールパネルを開き、その中の「プログラム」>「Windowsの機能の有効化または無効化」を選択する。

2. 下記がチェックOFFならチェックONに変更してください。
    - 「Linux用 Windows サブシステム（Windows Subsystem for Linux）」
    - 「仮想マシン プラットフォーム（Virtual Machine Platform）」

3. OS再起動する。

4. スタートメニューからコマンドプロンプトを実行し、`wsl` コマンドを実行する。

5. 「Linux 用 Windows サブシステムにインストールされているディストリビューションはありません。」 と表示された場合はスタートメニューから Windows PowerShell を右クリックメニューの管理者権限で実行し、`wsl --install Ubuntu` コマンドを実行する。

6. ユーザーの作成手順に戻ってください。
