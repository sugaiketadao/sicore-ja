## WSL上の Docker に PostgreSQL コンテナ作成

1. Windows 上で Docker設定ファイルを作成する。
    - 下記テキストをファイル名 `docker-compose.yml` で保存してください。（エンコード：UTF-8 BOM なし、改行コード：LF）
    - `image: postgres:` の後のバージョン `17.2` は任意のバージョンを指定できます。
    - `POSTGRES_PASSWORD:` の後のパスワード `dcpgpass` は Docker上の postgres ユーザーのパスワードで任意の値を指定できます。
    - `/tmp/share_docker:/tmp/share_host` で WSL上の `/tmp/share_docker` と Docker上の `/tmp/share_host` がリンクされ、WSL上の Ubuntu と Docker上の Ubuntu の共有ディレクトリとなります。

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

2. Docker設定ファイルを WSL上の Ubuntu ディレクトリへコピーする。
    - インストール時に作成したユーザー `user01` の HOMEディレクトリ `\\wsl.localhost\Ubuntu\home\user01` へエクスプローラーで docker-compose.yml をコピーする。

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
postgres OSユーザーに切り替え
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
=> SELECT current_schema();
`schema01` と表示されます。
=> \q
```

11. Docker-Ubuntu からログアウトする。
```Command
$ exit
「su - postgres」から exit した状態です。
# exit
Docker-Ubuntu からログアウトします。
```

12. ホスト Windows から下記情報で DB接続する。（A5 や psqledit で接続）
    - HOST: localhost
    - PORT: 5432
    - DB: db01
    - USER: dbuser01
    - PASS: dbpass01

---
- Docker 停止コマンド
```Command
$ docker compose down
```

- Docker 起動コマンド
```Command
$ docker compose up -d
```


- Docker上の Ubuntu で DB にログインする。
```Command
$ docker ps

結果からコンテナ名（例：user01-postgres-1）をコピーして下記コマンドで Docker上の Ubuntu にログインする。
$ docker exec -it コンテナ名 bash
（例：docker exec -it user01-postgres-1 bash）

postgres OSユーザーに切り替える。
# su - postgres

DB にログインする。
$ psql -h localhost -p 5432 -d db01 -U dbuser01
```

---
## Docker上の psql で SQLファイルを実行する手順

1. WSL上で共有ディレクトリに書き込み権限を付与する。

```Command
$ sudo chmod 777 /tmp/share_docker/
```

2. 対象 SQLファイルを WSL上の Ubuntu ディレクトリへコピーする。
    - エクスプローラーで `\\wsl.localhost\Ubuntu\tmp\share_docker` へSQLファイルをコピーする。

3. Docker上の Ubuntu で DB にログインして SQLファイルを実行する。

```SQL
［例］
=> \i /tmp/share_host/example_data_create.sql
```

---
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

