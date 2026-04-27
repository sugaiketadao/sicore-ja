# SIcore Framework

[English](https://github.com/sugaiketadao/sicore) | Japanese

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

SIcoreフレームワークは、**GitHub Copilot などの AI コーディングツールでの開発に最適化された**軽量Javaフレームワークです。

アノテーションや複雑な設定を排除したシンプルな設計により、AIが高精度なコードを生成でき、プログラミングビギナーでも理解しやすいアーキテクチャを実現しています。

> ⚠️ **注意**: 本プロジェクトは開発中です。一部未完成な部分がありますが、基本的な機能はお試しいただけます。

## 🚀 特徴

### 1. AIネイティブ開発
GitHub Copilot などのAIが高品質なコードを生成しやすく、プログラミング初心者でも理解しやすい設計を両立しています。

- **標準化されたパターン**: 統一された実装パターンでAI生成精度が向上
- **トークン最適化**: シンプルな設計でAIが読み込むコード量を削減
- **追跡しやすいコード**: アノテーション等の「魔法」を排除し、処理フローが明確
- **AI向けガイドライン**: `.github/copilot-instructions.md` でAIに規約を理解させる

### 2. シンプル＆軽量＆明快
外部依存を極力排除したバニラ設計により、AIも人間も理解しやすいコードを実現。

- **ライブラリレス**: JDK標準機能のみで動作（Tomcat不要）
- **アノテーションレス**: 処理フローが追跡しやすく、AIが推論しやすい
- **JSON中心設計**: ブラウザ⇔サーバー間はJSON限定。HTMLは静的ファイル
- **URL = クラス名**: ルーティング設定不要で直感的
  - URL: `/services/exmodule/ExampleListSearch`
  - Class: `com.example.app.service.exmodule.ExampleListSearch`

### 3. モックアップ = 実装コード
Webデザイナーが作成したHTMLモックアップに name属性を追加するだけで、そのまま実装コードとして機能します。

- **HTMLの再利用**: 設計フェーズのモックアップを本番コードに
- **独自CSSフレームワーク**: 約400行でレスポンシブデザインを実現

### 4. 型安全なデータ処理
Entity/Bean不要。Map拡張の「Io」クラスで全データを安全に扱います。

- **Ioクラス**: NULL安全・型安全なデータ操作
- **バグ防止**: キー重複チェック・存在チェックで単純ミスを防止

### 📦 追加機能
- **LDAP認証**: `web.properties` の設定1行でLDAPサーバー（Active Directory等）と連携したサインイン認証を有効化できます。認証後はJWTトークンでステートレスに認証状態を維持します。
- **バッチ処理**: Webアプリケーションと同じ設計思想で構築されたバッチ処理のひな型を提供しています。コマンドライン引数・プロパティファイルからパラメータを受け取り、統一されたパターンで実装できます。

## 📂 ディレクトリ構成

```
[project root]/
├── docs/                      # ドキュメント
│   ├── 01-introductions/     # 概要説明
│   ├── 02-develop-standards/ # 開発標準・パターン
│   ├── 03-coding-rules/      # コーディング規約
│   ├── 11-api-references/    # APIリファレンス
│   ├── 21-ai-guides/         # AI指示ガイド
│   └── 31-ai-api-references/ # AI用APIリファレンス
├── pages/                     # フロントエンド (HTML/JavaScript)
│   ├── app/                  # サンプル画面
│   └── lib/                  # フレームワーク本体 (JavaScript/CSS)
├── src/                       # バックエンド (Java)
│   ├── com/example/app/      # サンプルコード
│   └── com/onpg/             # フレームワーク本体 (Java)
├── script/                    # バッチ処理用サンプルシェル（bash）
├── script-win/                # バッチ処理用サンプルバッチ（Windows bat）
└── ai-test-prompts/           # テスト用AI指示ガイド
```

## 📖 ドキュメント

開発を始める前に、以下のドキュメントを参照してください。

### 導入・概要
- [プログラマー向け紹介](docs/01-introductions/01-programmer-introduction.md)
- [マネージャー向け紹介](docs/01-introductions/02-manager-introduction.md)

### 開発標準
- [Webページ構成標準 (HTML/JavaScript/CSS)](docs/02-develop-standards/01-web-page-structure.md)
- [Webサービス構成標準 (Java)](docs/02-develop-standards/11-web-service-structure.md)
- [バッチ処理構成標準 (Java)](docs/02-develop-standards/12-batch-processing-structure.md)
- [イベント別コーディングパターン](docs/02-develop-standards/21-event-coding-pattern.md)
- [バッチ処理別コーディングパターン](docs/02-develop-standards/22-batch-coding-pattern.md)

### コーディング規約
- [HTML/CSS コーディング規約](docs/03-coding-rules/01-html-css-coding-rule.md)
- [JavaScript コーディング規約](docs/03-coding-rules/02-javascript-coding-rule.md)
- [Java コーディング規約](docs/03-coding-rules/11-java-coding-rule.md)
- [SQL コーディング規約](docs/03-coding-rules/12-sql-coding-rule.md)

### APIリファレンス
- JSDoc: `docs/11-api-references/01-jsdoc/`
- CSSDoc: `docs/11-api-references/02-cssdoc/`
- JavaDoc: `docs/11-api-references/11-javadoc/`

### AI 指示ガイド
- [AI指示ガイド（業務画面作成用）](docs/21-ai-guides/01-ai-prompt-guide.md)
- [AI指示ガイド（デバッグ・修正用）](docs/21-ai-guides/02-ai-debug-guide.md)

---

## 🖥️ サンプル画面の確認方法 - VS Code
⚠️以下の手順は、VS Code と Java 11 以上がインストールされている環境を前提としています。

### 1. プロジェクトのダウンロード

GitHubからプロジェクトをダウンロードします。

1. GitHubリポジトリページで「Code」ボタンをクリックする。
2. 「Download ZIP」を選択する。
3. ダウンロードした ZIPファイルを任意のフォルダに解凍する。

### 2. VS Codeでプロジェクトを開く

1. VS Code を起動する。
2. 「ファイル」→「フォルダーを開く」で ZIPファイルを解凍したフォルダを選択する。
3. VS Code の「作成者を信頼しますか？」ダイアログボックスが表示されたら「はい」を選択する。

### 3. サーバーの起動

1. `src/com/onepg/web/StandaloneServerStarter.java` を選択する。
2. `F5` キーを押す、または右クリックメニューから「Debug Java」を選択する。
3. コンソールに起動完了メッセージが表示されるまで待機する。

### 4. サンプル画面へアクセス

ブラウザで以下のURLにアクセスします。

```
http://localhost:8000/pages/
```

- サンプル画面の一覧が表示されます。各画面のリンクをクリックして動作を確認できます。
- サーバーを停止する場合は `src/com/onepg/web/StandaloneServerStopper.java` を実行する。

## 5. サンプルコード
- HTML/JavaScript: `pages/app/exmodule/`
- Java: `src/com/example/app/service/exmodule/`
- DB定義/テストデータ: `example_db/example_data_create.sql`, `example_db/data/example.dbf`

---

## 🤖 AI開発の始め方

GitHub Copilot などのAIツールを使用した開発手順は下記のとおりです。

1. **要件作成**: 作成したい機能の要件を mdファイルに記述します。
2. **AI指示**: mdファイルを指定して AI にコーディングを指示します。AI は `.github/copilot-instructions.md` に従い、必要なドキュメントを読み込んでから要件に沿ってコードを生成します。
3. **動作確認**: 生成されたコードの動作を確認し、不具合があればその内容をAIに伝えます。AIは原因を特定してコードを修正します。

### 🧪 すぐに試してみる

リポジトリに含まれるサンプル要件を使って、実際に AI によるコーディングを体験できます。
プロジェクトを VS Code で開き（未実施の場合は上記「サンプル画面の確認方法」を参照）、Copilot チャットに以下のプロンプトを入力してください。

> `ai-test-prompts/order-prompt.md` の要件で画面機能を生成してください。

- AI が要件定義書を読み込み、必要なHTML、JavaScript、Javaコードを自動生成します。
- 生成後は [AI指示ガイド（デバッグ・修正用）](docs/21-ai-guides/02-ai-debug-guide.md) を参照してください。
- ⚠️ 指示を厳密に遵守する AIモデルを使用してください。（2025/12現在は Claude Opus 4.5推奨です）
- 🚫 創造性が高い AIモデルは、この作業には適していない可能性があります。

---
## 💬 コントリビューション

現在、本プロジェクトは開発中のため、**プルリクエストは受け付けておりません**。ただし、バグ報告やご意見・ご要望は受け付けています！ [Issue](../../issues) から作成してください。

## 💖 スポンサー

このプロジェクトを気に入っていただけましたら、[GitHub Sponsors](https://github.com/sponsors/sugaiketadao) でのご支援をご検討ください。いただいたご支援は、コーディング・ドキュメント作成の時間確保や、開発環境・AI ツールの維持に活用させていただきます。

⭐ スターを付けていただくだけでも、大きな励みになります！

[![Sponsor](https://img.shields.io/static/v1?label=Sponsor&message=%E2%9D%A4&logo=GitHub&color=%23fe8e86)](https://github.com/sponsors/sugaiketadao)

---
## 📜 ライセンス

### 同梱ソフトウェア

本プロジェクトには以下のサードパーティソフトウェアが含まれています。

| ソフトウェア | ライセンス | 説明 |
|-|-|-|
| [SQLite](https://www.sqlite.org/) (`sqlite3.exe`) | Public Domain | SQLiteデータベースエンジン |
| [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc) (`sqlite-jdbc-3.50.2.0.jar`) | Apache License 2.0 | SQLite用JDBCドライバ |

SQLiteはパブリックドメインであり、使用・改変・再配布に制限はありません。
SQLite JDBC Driver は Apache License 2.0 の下で配布されています。ライセンス全文は `licenses/` フォルダを参照してください。

---
© 2025 sugaiketadao (onepg.com)

