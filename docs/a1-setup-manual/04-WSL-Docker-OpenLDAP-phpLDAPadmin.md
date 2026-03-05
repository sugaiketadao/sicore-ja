## WSL上の Docker に OpenLDAP・phpLDAPadmin コンテナ作成

1. Windows 上で Docker設定ファイルを作成する。
    - 下記テキストをファイル名 `docker-compose.yml` で保存してください。（エンコード：UTF-8 BOM なし、改行コード：LF）
    - `LDAP_ORGANISATION:` の後の組織名 `Example Inc` は任意の値を指定できます。
    - `LDAP_DOMAIN:` の後のドメイン `example.com` は任意の値を指定できます。（`dc=example,dc=com` に対応します）
    - `LDAP_ADMIN_PASSWORD:` の後のパスワード `dcldappass` は OpenLDAP の管理者パスワードで任意の値を指定できます。（手順内の ldapadd・ldapsearch コマンドおよび phpLDAPadmin へのログインで使用します）
    - `PHPLDAPADMIN_HTTPS: "false"` は phpLDAPadmin の HTTPS を無効にする設定です。デフォルトは HTTPS 有効のためポート `80` でアクセスするために指定が必要です。

```docker-compose
services:
  openldap:
    image: osixia/openldap:1.5.0
    container_name: openldap
    environment:
      LDAP_ORGANISATION: "Example Inc"
      LDAP_DOMAIN: "example.com"
      LDAP_ADMIN_PASSWORD: "dcldappass"
    ports:
      - "389:389"
    volumes:
      - ldapdata:/var/lib/ldap
      - ldapconfig:/etc/ldap/slapd.d
  phpldapadmin:
    image: osixia/phpldapadmin:0.9.0
    container_name: phpldapadmin
    environment:
      PHPLDAPADMIN_LDAP_HOSTS: "openldap"
      PHPLDAPADMIN_HTTPS: "false"
    ports:
      - "8080:80"
    depends_on:
      - openldap
volumes:
  ldapdata:
  ldapconfig:
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
openldap・phpldapadmin 2コンテナの STATUS が Up または running で表示されれば起動完了です。
```

6. Docker上の Ubuntu（openldap コンテナ）にログインする。
```Command
`docker exec -it` の後ろは docker-compose.yml の container_name で指定したコンテナ名
$ docker exec -it openldap bash
```

7. OU（組織単位）を作成する。（OU名 `users` は任意の値を指定できます）
```Command
下記内容を ou.ldif として作成する。

# cat << 'EOF' > /tmp/ou.ldif
dn: ou=users,dc=example,dc=com
objectClass: organizationalUnit
ou: users
EOF

ldapadd コマンドで OU を追加する。
# ldapadd -x -H ldap://localhost -D "cn=admin,dc=example,dc=com" -w dcldappass -f /tmp/ou.ldif
```

8. テストユーザーを作成する。（ユーザーID `U001`・パスワード `P001` は任意の値を指定できます）
```Command
パスワードのハッシュ値を生成する。
# slappasswd -s P001
{SSHA}xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
生成されたハッシュ値をコピーする。

下記内容を U001.ldif として作成する。{SSHA}... の部分は上記で生成したハッシュ値に置き換える。
# cat << 'EOF' > /tmp/U001.ldif
dn: uid=U001,ou=users,dc=example,dc=com
objectClass: inetOrgPerson
uid: U001
cn: マイク・デイビス
sn: デイビス
userPassword: {SSHA}xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
EOF

ldapadd コマンドでユーザーを追加する。
# ldapadd -x -H ldap://localhost -D "cn=admin,dc=example,dc=com" -w dcldappass -f /tmp/U001.ldif

U002 を追加する場合は同様に U002.ldif を作成して ldapadd を実行する。
# slappasswd -s P002
{SSHA}yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy

# cat << 'EOF' > /tmp/U002.ldif
dn: uid=U002,ou=users,dc=example,dc=com
objectClass: inetOrgPerson
uid: U002
cn: 池田健
sn: 池田
userPassword: {SSHA}yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy
EOF

# ldapadd -x -H ldap://localhost -D "cn=admin,dc=example,dc=com" -w dcldappass -f /tmp/U002.ldif
```

9. ユーザーの作成を確認する。
```Command
# ldapsearch -x -H ldap://localhost -D "cn=admin,dc=example,dc=com" -w dcldappass -b "ou=users,dc=example,dc=com"

作成したユーザー情報が表示されれば成功です。
cn や sn に日本語を設定した場合、ldapsearch の出力では `cn::` のようにコロンが2つ表示され、値がBase64エンコードされた英数字で表示されます。これは文字化けではなく、非ASCII文字を表示するための仕様です。
```

10. Docker-Ubuntu からログアウトする。
```Command
# exit
```

11. Windows 上のブラウザで phpLDAPadmin にアクセスする。
    - 画面左側のツリーパネルにある `login` をクリックしてログイン画面を開く。
        - URL: `http://localhost:8080`
        - Login DN: `cn=admin,dc=example,dc=com`
        - Password: `dcldappass`
    - 画面左側のツリーパネルにある `dc=example,dc=com` から作成したユーザーを確認する。

12. `web.properties` の LDAP接続情報は下記のとおり。
```properties
ldap.url=ldap://localhost:389
ldap.user.dn.fmt=uid=%s,ou=users,dc=example,dc=com
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

### Docker上の Ubuntu（openldap コンテナ）でユーザー一覧確認
```Command
`docker exec -it` の後ろは docker-compose.yml の container_name で指定したコンテナ名
$ docker exec -it openldap bash

LDAP 検索でユーザー一覧を表示する。
# ldapsearch -x -H ldap://localhost -D "cn=admin,dc=example,dc=com" -w dcldappass -b "ou=users,dc=example,dc=com"
# exit
```

### Docker イメージ再作成

***通常不要な手順です。***

```Command
Docker を停止する。
$ docker compose down

イメージ名を確認して削除する。
$ docker images
$ docker rmi osixia/openldap:1.5.0
$ docker rmi osixia/phpldapadmin:0.9.0

ボリュームを確認して削除する。
$ docker volume ls
$ docker volume rm user01_ldapdata
$ docker volume rm user01_ldapconfig

Docker を再起動する。
$ docker compose up -d
```

