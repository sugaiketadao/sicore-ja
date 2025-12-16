# SICore Framework

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

SICoreフレームワークは、**「プログラミングビギナー」** と **「AIによるコード生成」** をサポートするために設計された軽量Javaフレームワークです。

多機能なフレームワークとは対照的に、アノテーションや複雑な設定を排除し、シンプルで理解しやすいアーキテクチャを採用しています。

> ⚠️ **注意**: 本プロジェクトは開発中です。一部未完成な部分がありますが、基本的な機能はお試しいただけます。

## 🚀 特徴

### 1. シンプル＆軽量＆明快
- **JSON中心設計**: ブラウザとサーバー間の通信には、JSONのみを使用します。テンプレートエンジンは使用せず、HTMLは静的ファイルとして扱います。
- **ライブラリレス**: 外部ライブラリへの依存を極力排除しています。Tomcatも不要です。（JDK標準機能のみで動作）
- **URL = クラス名**: ルーティング設定は不要です。URLパスがJavaクラス名に直接マッピングされます。
  - URL: `/services/ordermng/OrderListSearch`
  - Class: `com.example.app.service.ordermng.OrderListSearch`
- **アノテーションレス**: コードの処理内容を不明瞭にしがちなアノテーションを排除しています。これにより、コードの実行フローを追跡しやすくしています。

### 2. 堅牢なデータ処理
- **Ioクラス**: `Map<String, String>` を拡張した `Io` クラスにより、NULL安全・型安全なデータ操作を実現します。
- **バグ防止**: キーの重複チェックや存在チェック機能により、単純なミスを未然に防ぎます。

### 3. プロトタイプ駆動
- **HTMLの再利用**: 開発者は、Webデザイナーが作成したHTMLモックアップをそのまま本番コードとして使用できます。
- **独自CSSフレームワーク**: 独自のCSSフレームワークを提供しており、最小限のCSSクラス指定でレスポンシブデザインを実装できます。

### 4. AIネイティブ開発
GitHub Copilot などのAIコーディングアシスタントが、高品質なコードを生成しやすいように設計されています。
- **AI向けガイドライン**: `.github/copilot-instructions.md` により、AIにフレームワークの規約を正確に理解させます。
- **トークン最適化**: 人間向けドキュメントに加えて、AI専用の簡潔なドキュメントを提供しています。また、人間と共用するドキュメントでは `<!-- AI_SKIP_START -->` マーカーでAI不要部分を囲むことで、AIが読み込むトークン量を削減できます。
- **標準化されたパターン**: UI実装パターンとビジネスロジック実装パターンが統一されており、AIが高精度なコードを生成できます。

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
- [イベント別コーディングパターン](docs/02-develop-standards/21-event-coding-pattern.md)

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

サンプル画面の一覧が表示されます。各画面のリンクをクリックして動作を確認できます。

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
Copilot チャットに以下のプロンプトを入力してください。

> `ai-test-prompts/order-prompt.md` の要件で画面機能を生成してください。

- AI が要件定義書を読み込み、必要なHTML、JavaScript、Javaコードを自動生成します。
- ⚠️ 指示を厳密に遵守する AIエージェントを使用してください。（2025/12現在は Claude Opus 4.5推奨です）
- 🚫 創造性が高い AIエージェントは、この作業には適していない可能性があります。

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
© 2025 onepg.com

