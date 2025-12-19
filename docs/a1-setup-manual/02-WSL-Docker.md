## WSL 上で Docker インストール

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

下記の内容であれば設定完了です。
--------------------
[boot]
systemd=true
--------------------
:q で vi を終了する
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

下記が表示される（他にも色々表示される）
Hello from Docker!
This message shows that your installation appears to be working correctly.
```
