# BigQuery Sample App

[![Build](https://github.com/TakuyaFukumura/bigquery-sample-app/workflows/Build/badge.svg)](https://github.com/TakuyaFukumura/bigquery-sample-app/actions?query=branch%3Amain)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6.3-blue)](https://maven.apache.org/)

Spring BootアプリケーションとGoogle BigQueryを連携させるサンプルアプリケーションです。

## 概要

このアプリケーションでは以下の機能を提供します：
- Google BigQueryへの接続とクエリ実行
- テーブルの作成・削除
- データの挿入・取得
- BigQuery API をラップしたREST API
- 認証はサービスアカウントキー（JSONファイル）方式を採用

## 技術スタック

- **Spring Boot 3.5.6** - メインフレームワーク
- **Java 17** - プログラミング言語
- **Maven** - ビルドツール
- **Google Cloud BigQuery 2.38.0** - データウェアハウス
- **Spring Security** - 認証・認可
- **Thymeleaf** - テンプレートエンジン
- **H2 Database** - 開発用データベース
- **Spock Framework** - テストフレームワーク

## 前提条件

1. **Java 17以上（OpenJDK推奨）**
2. **Google Cloud Platform アカウント**
3. **BigQueryが利用可能なGCPプロジェクト**
4. **サービスアカウントキー（JSONファイル）**

## セットアップ

### 1. リポジトリのクローン
```bash
git clone https://github.com/TakuyaFukumura/bigquery-sample-app.git
cd bigquery-sample-app
```

### 2. GCPサービスアカウントの設定

#### 2.1 サービスアカウントの作成
1. [GCPコンソール](https://console.cloud.google.com/)にアクセス
2. 「IAM と管理」→「サービスアカウント」
3. 「サービスアカウントを作成」をクリック
4. サービスアカウント名を入力（例：`bigquery-sample-app`）

#### 2.2 権限の付与
サービスアカウントに以下のロールを付与：
- **BigQuery データ編集者** - テーブル作成・データ挿入用
- **BigQuery ジョブユーザー** - クエリ実行用

#### 2.3 キーファイルのダウンロード
1. 作成されたサービスアカウントをクリック
2. 「キー」タブ→「キーを追加」→「新しいキーを作成」
3. キータイプ「JSON」を選択してダウンロード

### 3. 環境変数の設定

```bash
# サービスアカウントキーファイルのパスを設定
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/service-account-key.json

# BigQueryプロジェクトIDとデータセットIDを設定
export BIGQUERY_PROJECT_ID=your-gcp-project-id
export BIGQUERY_DATASET_ID=your-dataset-id
```

### 4. BigQueryデータセットの作成

[BigQueryコンソール](https://console.cloud.google.com/bigquery)でデータセットを作成：
1. プロジェクトを選択
2. 「データセットを作成」をクリック
3. データセットID（例：`sample_dataset`）を入力
4. 場所を選択（例：`asia-northeast1`）
5. 「データセットを作成」をクリック

## アプリケーションの起動

### 開発モード（認証無効）
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 本番モード（認証有効）
```bash
./mvnw spring-boot:run
```

### ビルド＆実行
```bash
./mvnw clean package
java -jar target/myproject.jar
```

## API エンドポイント

アプリケーション起動後、以下のエンドポイントが利用可能です：

### BigQuery Web UI

#### BigQuery操作画面（ホームページ）
```
GET /
```
BigQueryのテーブル操作とクエリ実行を行うWebインターフェース

### BigQuery REST API

#### ヘルスチェック
```
GET /bigquery/api/health
```

#### クエリ実行
```
GET /bigquery/api/query?sql=SELECT 1 as test
```

#### テーブル一覧取得
```
GET /bigquery/api/tables
```

#### テーブル作成（サンプルスキーマ）
```
POST /bigquery/api/table/{tableName}
```

#### データ挿入
```
POST /bigquery/api/table/{tableName}/data
Content-Type: application/json

[
  {"id": 1, "name": "ユーザー1", "email": "user1@example.com", "created_at": "2023-01-01T00:00:00Z"},
  {"id": 2, "name": "ユーザー2", "email": "user2@example.com", "created_at": "2023-01-02T00:00:00Z"}
]
```

#### テーブル削除
```
DELETE /bigquery/api/table/{tableName}
```

### その他のエンドポイント

#### ヘルスチェック
```
GET /actuator/health
```

#### H2コンソール（開発モードのみ）
```
GET /h2-console
```

## 使用例

### 1. 接続確認
```bash
curl http://localhost:8080/bigquery/api/health
```

### 2. テーブル作成
```bash
curl -X POST http://localhost:8080/bigquery/api/table/users
```

### 3. データ挿入
```bash
curl -X POST http://localhost:8080/bigquery/api/table/users/data \
  -H "Content-Type: application/json" \
  -d '[{"id": 1, "name": "田中太郎", "email": "tanaka@example.com", "created_at": "2023-01-01T00:00:00Z"}]'
```

### 4. データ取得
```bash
curl "http://localhost:8080/bigquery/api/query?sql=SELECT * FROM users LIMIT 10"
```

### 5. テーブル削除
```bash
curl -X DELETE http://localhost:8080/bigquery/api/table/users
```

## 設定

### application.properties

BigQuery関連の設定：
```properties
# BigQuery設定
app.bigquery.project-id=${BIGQUERY_PROJECT_ID:sample-project}
app.bigquery.dataset-id=${BIGQUERY_DATASET_ID:sample_dataset}
```

サービスアカウントキーのパスは環境変数 `GOOGLE_APPLICATION_CREDENTIALS` で設定。

## テスト

### 全テスト実行
```bash
./mvnw test
```

### BigQuery関連テストのみ実行
```bash
./mvnw test -Dtest="*BigQuery*"
```

## 認証設定

### 本番モード（認証有効）
通常の起動では認証が有効になります：
```bash
./mvnw spring-boot:run
```
- ホームページ（`http://localhost:8080`）にアクセスするとログインページにリダイレクトされます
- ログイン後、BigQuery操作画面が表示されます
- デフォルトユーザーでログインできます（詳細はデータベース初期化ファイルを参照）

### 開発モード（認証無効）
開発時の利便性のため、`dev`プロファイルでは認証を無効化できます：
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
- 認証なしで全てのページにアクセス可能（トップページはBigQuery操作画面）
- BigQuery REST APIにも認証なしでアクセス可能
- H2コンソール（`http://localhost:8080/h2-console`）にもアクセス可能

### 注意事項
- 開発モードは **本番環境では絶対に使用しないでください**
- 開発モードではCSRF保護も無効化されます
- 本番デプロイ前には必ず認証有効モードでテストしてください

## セキュリティ考慮事項

- **サービスアカウントキーは絶対にGit等のバージョン管理に含めないでください**
- サービスアカウントには最小権限の原則で必要な権限のみ付与してください
- 本番環境では適切な環境変数管理システムを使用してください
- 定期的にサービスアカウントキーをローテーションしてください

## トラブルシューティング

### 1. BigQuery接続エラー
```
RuntimeException: BigQueryの初期化に失敗しました
```
**解決方法：**
- `GOOGLE_APPLICATION_CREDENTIALS` 環境変数が正しく設定されているか確認
- サービスアカウントキーファイルが存在し、読み取り可能か確認
- サービスアカウントに適切な権限が付与されているか確認

### 2. データセットが見つからないエラー
```
Not found: Dataset project:dataset
```
**解決方法：**
- BigQueryコンソールでデータセットが作成されているか確認
- `BIGQUERY_PROJECT_ID` と `BIGQUERY_DATASET_ID` 環境変数が正しく設定されているか確認

### 3. 権限エラー
```
Access Denied: BigQuery BigQuery: Permission denied
```
**解決方法：**
- サービスアカウントに「BigQuery データ編集者」と「BigQuery ジョブユーザー」ロールが付与されているか確認

### 4. テスト失敗
```
Tests run: X, Failures: X, Errors: X
```
**解決方法：**
- `./mvnw clean test` でクリーンな状態から再実行
- Java 17以上が使用されているか確認

## 開発情報

### プロジェクト構成
```
src/
├── main/
│   ├── java/com/example/myapplication/
│   │   ├── controller/BigQueryController.java  # BigQuery REST API
│   │   ├── service/BigQueryService.java        # BigQuery ビジネスロジック
│   │   └── ...
│   └── resources/
│       ├── application.properties              # アプリケーション設定
│       └── ...
└── test/
    └── groovy/com/example/myapplication/
        ├── controller/BigQueryControllerSpec.groovy  # Controller テスト
        ├── service/BigQueryServiceSpec.groovy        # Service テスト
        └── ...
```

### ライセンス
このプロジェクトはサンプル用途のため特別なライセンスはありません。

### 貢献
プルリクエストやイシューの報告を歓迎します。
