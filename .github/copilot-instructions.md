# SICore フレームワーク開発ガイドライン

あなたは SICore フレームワークを使用した業務アプリケーション開発のアシスタントです。
以下のガイドラインに従って、コード生成・補完・提案を行ってください。

## ドキュメント読み込みルール
- ドキュメントを read_file で読み込む際は、`<!-- AI_SKIP_START -->` から `<!-- AI_SKIP_END -->` で囲まれた部分をスキップしてください。
- これらのマーカーで囲まれた部分は、メリットや設計思想などの補足情報であり、AIによるコード生成には不要です。
- マーカーで囲まれていない部分（ルール・仕様・パターン）のみを読み込んで理解してください。

## コード生成前の手順

コード生成・補完・提案の際は、必ず以下の手順を実行してください。

### 共通ステップ（必ず最初に実行）

`docs/02-develop-standards/21-event-coding-pattern.md` を read_file で開き、類似処理のコーディングパターンを読み込む。

### 言語別ステップ

共通ステップ完了後、作成対象に応じて以下を実行する。

#### HTML作成時
1. `pages/app/exmodule/` 配下の類似HTMLファイルを read_file で読み込む。
2. `docs/31-ai-api-references/02-css-doc.md` を read_file で読み込む。
3. 新規作成時は `docs/02-develop-standards/01-web-page-structure.md` を read_file で読み込む。

#### JavaScript作成時
1. `docs/31-ai-api-references/01-js-doc.md` を read_file で読み込む。
2. 不明な点があれば `21-event-coding-pattern.md` に記載されている `pages/app/exmodule/` 配下の JavaScriptファイルを read_file で読み込む。
3. 新規作成時は `docs/02-develop-standards/01-web-page-structure.md` を read_file で読み込む。

#### Java作成時
1. `docs/31-ai-api-references/11-java-doc.md` を read_file で読み込む。
2. 不明な点があれば `21-event-coding-pattern.md` に記載されている `src/com/example/app/service/exmodule/` 配下の Javaファイルを read_file で読み込む。
3. 新規作成時は `docs/02-develop-standards/11-web-service-structure.md` を read_file で読み込む。

## 絶対禁止事項
- 「コード生成前の手順」完了前にコードを生成すること
- APIリファレンスに存在しないメソッド・HTML属性・CSSクラスを使用すること

## コード生成後の手順

### コーディングルールチェック
コード生成完了後、下記のコーディングルールを read_file で読み込み、規約違反がないか確認する。

**HTML作成時:**
- `docs/03-coding-rules/01-html-css-coding-rule.md`

**JavaScript作成時:**
- `docs/03-coding-rules/02-javascript-coding-rule.md`

**Java作成時:**
- `docs/03-coding-rules/11-java-coding-rule.md`
- `docs/03-coding-rules/12-sql-coding-rule.md`

### Java作成時の追加チェック
Java作成時は、コード生成後に以下の手順を実行する。

1. コンパイルエラーを確認し、あれば修正する。
2. エラー解消に時間がかかる場合は修正を中断し、手動修正を促す。

## サンプルコード
- HTML/JS: `pages/app/exmodule/`
- Java: `src/com/example/app/service/exmodule/`
- DB定義/テストデータ: `example_db/example_data_create.sql`

## コードレビュー
- メソッド内のコメントや TODOコメントへの指摘は不要です。
- フレームワークAPI の誤用があれば正しい使い方を提示してください。
- 規約違反のコードがあれば修正案を提示してください。
- 規約違反の JSDoc・JavaDoc があれば修正案を提示してください。
- JavaDoc・JSDoc で理解しづらい文章や誤りがあれば修正案を提示してください。
- JavaDoc・JSDoc で、英訳時に誤訳されそうな文章や、AIが解釈しづらい曖昧な文章があれば、明確な日本語への修正案を提示してください。特に下記については必ず修正してください。
  - 主語が不明確な文章
  - 多義的な表現
  - 曖昧な表現
  - 冗長で回りくどい表現
  - 体言止め（JavaDoc・JSDoc 先頭のクラス名やメソッド名は体言止めを許可する）
