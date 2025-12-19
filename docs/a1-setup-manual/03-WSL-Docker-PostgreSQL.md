## WSL 上の Docker に PostgreSQL コンテナ作成

1. Windows 上で Docker設定ファイルを作成する。
    - 下記テキストをファイル名 `docker-compose.yml` で保存してください。（エンコード：UTF-8 BOM なし、改行コード：LF）
    - `image: postgres:` の後のバージョン `17.2` は任意のバージョンを指定できます。
    - `POSTGRES_PASSWORD:` の後のパスワード `dcpgpass` はコンテナOS上の postgres ユーザーのパスワードで任意の値を指定できます。
    - `/tmp/share_docker:/tmp/share_host` で WSL上の `/tmp/share_host` と Docker上の `/tmp/share_docker` がリンクします。

```docker-compose
services:
  postgres:
    image: postgres:17.2
    environment:
      POSTGRES_PASSWORD: dcpgpass
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
      - /tmp/share_docker:/tmp/share_host
volumes:
  pgdata:
```

2. Docker設定ファイルを WSL 上の Ubuntu ディレクトリへコピーする。
    - エクスプローラーで `\\wsl.localhost\Ubuntu\home\user01` に docker-compose.yml をコピーする。（`user01` は WSL インストール時に作成したユーザー名）

3. WSL を起動する。WSL 起動済の場合は再起動する。
    - WSL 停止方法：スタートメニューからコマンドプロンプトを実行し、`wsl --shutdown` コマンドを実行する。
    - WSL 起動方法：スタートメニューから Ubuntu を実行する。

***以下はすべてWSL上の作業となります***

4. Docker を起動する。
```Command
$ docker compose up -d
初回実行時は Docker イメージのダウンロードが行われるため、時間がかかります。
```

5. Docker上の Ubuntuにログインする。
```Command
$ docker ps

結果からコンテナ名（例：user01-postgres-1）をコピーして下記コマンドでログインする。
$ docker exec -it コンテナ名 bash
（例：docker exec -it user01-postgres-1 bash）
```

6. Docker-Ubuntu 上で PostgreSQL にログインする。
```Command
# su - postgres
$ psql -h localhost -p 5432 -U postgres
```

7. ロール（ユーザー）と DB を作成する。（ロール名 `dbuser01`・パスワード `dbpass01`・DB名 `db01` は任意の値を指定できます）
```SQL
# CREATE ROLE dbuser01 WITH LOGIN PASSWORD 'dbpass01';
# CREATE DATABASE db01 OWNER dbuser01;
# GRANT ALL PRIVILEGES ON DATABASE db01 TO dbuser01;
# quit
```

8. 作成した DB にログインする。
```Command
$ psql -h localhost -p 5432 -d db01 -U postgres
```

9. スキーマを作成する。（スキーマ名 `schema01` は任意の値を指定できます）　***publicスキーマのみ使用する場合はこの手順をスキップする***
```SQL
# CREATE SCHEMA schema01 AUTHORIZATION dbuser01;
# GRANT ALL PRIVILEGES ON SCHEMA schema01 TO dbuser01;

作成した DB のスキーマ優先順位を変更する。（`public` より前に作成したスキーマを指定）
# ALTER DATABASE db01 SET search_path TO schema01, public;
# quit
```

10. カレントスキーマを確認する。　***publicスキーマのみ使用する場合はこの手順をスキップする***
```Command
作成したユーザーで DB にログインする。
$ psql -h localhost -p 5432 -d db01 -U dbuser01
```

```SQL
> SELECT current_schema();
`schema01` と表示されます。
> quit
```

11. Docker-Ubuntu からログアウトする。
```Command
$ exit
「su - postgres」から exit した状態になります。
# exit
Docker-Ubuntu から exit した状態になります。
```

12. 母艦 Windows から下記情報で DB接続する。（A5 や psqledit で接続）
    - HOST: localhost
    - PORT: 5432
    - DB: db01
    - USER: dbuser01
    - PASS: dbpass01


13. Docker を停止する。
```Command
$ docker compose down
```


## Docker イメージ再作成コマンド

***通常不要な手順です。***

```Command
Docker を停止する。
$ docker compose down

イメージ名を確認して削除する。
$ docker images
$ docker rmi postgres:17.2

ボリュームを確認して削除する。
$ docker volume ls
$ docker volume rm user01_pgdata

Docker を再起動する。
$ docker compose up -d
```

