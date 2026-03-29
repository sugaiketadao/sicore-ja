## WSL上の Docker に PostgreSQL コンテナ作成

1. Windows 上で Docker設定ファイルを作成する。
    - 下記テキストをファイル名 `docker-compose.yml` で保存してください。（エンコード：UTF-8 BOM なし、改行コード：LF）
    - `image: postgres:` の後のバージョン `17.7` は任意のバージョンを指定できます。
    - `POSTGRES_PASSWORD:` の後のパスワード `dcpgpass` は Docker上の postgres ユーザーのパスワードで任意の値を指定できます。
    - `/tmp/share_docker:/tmp/share_host` で WSL上の `/tmp/share_docker` と Docker上の `/tmp/share_host` がリンクされ、WSL上の Ubuntu と Docker上の Ubuntu（postgres コンテナ）の共有ディレクトリとなります。

```docker-compose
services:
  postgres:
    image: postgres:17.7
    container_name: postgres
    environment:
      POSTGRES_PASSWORD: "dcpgpass"
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
      - /tmp/share_docker:/tmp/share_host
volumes:
  pgdata:
```

2. Docker設定ファイルを WSL上の Ubuntu ディレクトリへコピーする。
    - WSLインストール時に作成したユーザー（例）`user01` の HOMEディレクトリ `\\wsl.localhost\Ubuntu\home\user01` へエクスプローラーで docker-compose.yml をコピーする。
    - 既に docker-compose.yml が存在する場合は、上記 `services:` 配下の内容と `volumes:` 配下の内容をそれぞれ既存ファイルに追記する。

3. WSL を起動する。WSL 起動済の場合は再起動する。
    - WSL 停止方法：スタートメニューからコマンドプロンプトを実行し、`wsl --shutdown` コマンドを実行する。
    - WSL 起動方法：スタートメニューから Ubuntu を実行する。

***以下はすべてWSL上の作業となります***

4. Docker を起動する。
```Command
$ docker compose up -d
初回実行時は Docker イメージのダウンロードが行われるため、時間がかかります。
```

5. コンテナの起動を確認する。
```Command
$ docker compose ps
postgres コンテナの STATUS が Up または running で表示されれば起動完了です。
```

6. Docker上の Ubuntu（postgres コンテナ）にログインする。
```Command
`docker exec -it` の後ろは docker-compose.yml の container_name で指定したコンテナ名
$ docker exec -it postgres bash
```

7. Docker-Ubuntu 上で PostgreSQL にログインする。
```Command
postgres OSユーザーに切り替える。
# su - postgres

$ psql -h localhost -p 5432 -U postgres
```

8. ロール（ユーザー）と DB を作成する。（ロール名 `dbuser01`・パスワード `dbpass01`・DB名 `db01` は任意の値を指定できます）
```SQL
# CREATE ROLE dbuser01 WITH LOGIN PASSWORD 'dbpass01';
# CREATE DATABASE db01 OWNER dbuser01;
# GRANT ALL PRIVILEGES ON DATABASE db01 TO dbuser01;
# quit
```

9. 作成した DB にログインする。
```Command
$ psql -h localhost -p 5432 -d db01 -U postgres
```

10. スキーマを作成する。（スキーマ名 `schema01` は任意の値を指定できます）　***publicスキーマのみ使用する場合はこの手順をスキップする***
```SQL
# CREATE SCHEMA schema01 AUTHORIZATION dbuser01;
# GRANT ALL PRIVILEGES ON SCHEMA schema01 TO dbuser01;

作成した DB のスキーマ優先順位を変更する。（`public` より前に作成したスキーマを指定）
# ALTER DATABASE db01 SET search_path TO schema01, public;
# quit

CREATE SCHEMA で権限エラーになる場合は作成したユーザーでログインしてスキーマを作成する（上記SQLを実行する）
```

11. カレントスキーマを確認する。　***publicスキーマのみ使用する場合はこの手順をスキップする***
```Command
作成したユーザーで DB にログインする。
$ psql -h localhost -p 5432 -d db01 -U dbuser01
```

```SQL
=> SELECT current_schema();
`schema01` と表示されます。
=> \q
```

12. Docker-Ubuntu からログアウトする。
```Command
$ exit
「su - postgres」から exit した状態です。
# exit
Docker-Ubuntu からログアウトします。
```

13. ホスト Windows から下記情報で DB接続する。（A5 や psqledit で接続）
    - HOST: localhost
    - PORT: 5432
    - DB: db01
    - USER: dbuser01
    - PASS: dbpass01


14. `db.properties` の DB接続情報は下記のとおり。
```properties
default.conn.url=jdbc:postgresql://localhost:5432/db01
default.conn.user=dbuser01
default.conn.pass=dbpass01
```

---
### Docker上の psql で SQLファイルを実行する手順

1. WSL上で共有ディレクトリに書き込み権限を付与する。

```Command
$ sudo chmod 777 /tmp/share_docker/
```

2. 対象 SQLファイルを WSL上の Ubuntu ディレクトリへコピーする。
    - エクスプローラーで `\\wsl.localhost\Ubuntu\tmp\share_docker` へSQLファイルをコピーする。
    - WSL上の /tmp/share_docker/ は Docker上では /tmp/share_host/ としてマウントされる。（前出 docker-compose 参照）

3. Docker上の Ubuntu（postgres コンテナ）で DB にログインして SQLファイルを実行する。
```Command
`docker exec -it` の後ろは docker-compose.yml の container_name で指定したコンテナ名
$ docker exec -it postgres bash

postgres OSユーザーに切り替える。
# su - postgres

DB にログインする。
$ psql -h localhost -p 5432 -d db01 -U dbuser01
```

```SQL
［例］クライアント文字コードを変更、ディレクトリに移動してファイルを実行
=> \encoding UTF8
=> show client_encoding;
=> \cd /tmp/share_host
=> \i example_data_create.sql
```

---
## コマンド集

### Docker 停止
```Command
$ docker compose down
```

### Docker 起動
```Command
$ docker compose up -d
```

### Docker上の Ubuntu（postgres コンテナ）で DB にログイン
```Command
`docker exec -it` の後ろは docker-compose.yml の container_name で指定したコンテナ名
$ docker exec -it postgres bash

postgres OSユーザーに切り替える。
# su - postgres

DB にログインする。
$ psql -h localhost -p 5432 -d db01 -U dbuser01
```

### Docker イメージ再作成

***通常不要な手順です。***

```Command
Docker を停止する。
$ docker compose down

イメージ名を確認して削除する。
$ docker images
$ docker rmi postgres:17.7

ボリュームを確認して削除する。
$ docker volume ls
$ docker volume rm user01_pgdata

Docker を再起動する。
$ docker compose up -d
```

