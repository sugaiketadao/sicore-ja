## WSL上で Docker インストール

***すべてWSL上の作業となります***

1. インストールシェルを取得して実行する。
```Command
$ curl -fsSL https://get.docker.com -o get-docker.sh
$ sudo sh get-docker.sh
ユーザー作成時に設定したパスワードを入力する。
```

2. systemd が有効化されているか確認する。
```Command
$ sudo vi /etc/wsl.conf

下記の内容が記載されていれば設定完了です。
--------------------
[boot]
systemd=true
--------------------

ファイルが存在しない、または上記内容が記載されていない場合：
  i キーで編集モードに入り、上記内容を入力する。
  ESC キーで編集モードを終了し、:wq と入力して Enter で保存する。

上記内容が既に記載されている場合：
  :q と入力して Enter で vi を終了する。
```

3. Docker を root ユーザー以外から使用できるよう権限設定する。
```Command
$ sudo usermod -aG docker ${USER}
```

4. WSL を再起動する。
    - WSL 停止方法：スタートメニューからコマンドプロンプトを実行し、`wsl --shutdown` コマンドを実行する。
    - WSL 起動方法：スタートメニューから Ubuntu を実行する。

5. Docker をサービス起動する。
```Command
$ sudo service docker start
ユーザー作成時に設定したパスワードを入力する。
```

6. Docker をテスト実行する。
```Command
$ docker run hello-world

下記が含まれるメッセージが表示される：
Hello from Docker!
This message shows that your installation appears to be working correctly.
```

---
- Docker 停止コマンド
```Command
$ docker compose down
```

- Docker 起動コマンド
```Command
$ docker compose up -d
```
