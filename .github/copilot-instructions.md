# BigQuery Sample App

このプロジェクトはSpring BootアプリケーションとGoogle BigQueryを連携させるサンプルアプリケーションです。Spring Boot 3.5.6、Java 17、Maven、Thymeleafテンプレートエンジン、Google Cloud BigQuery 2.38.0、Spring Security（認証・認可）、Spring Data JPA（H2インメモリDB）、Gemini API（AI機能）、Spring Boot Actuatorによる監視機能を利用しています。

必ず最初にこれらの手順を参照し、ここに記載されていない情報や予期しない事象に遭遇した場合のみ検索やbashコマンドを利用してください。

## 効率的な作業のために

### 初期設定とビルド
- 開始前にJava 17以上（OpenJDK推奨）をインストールしてください。
- アプリケーションのビルド：
  - `./mvnw clean package` -- 初回ビルドは依存関係のダウンロードで4～5分かかります。絶対にキャンセルしないでください。タイムアウトは600秒以上に設定。
  - 2回目以降のビルドはキャッシュ利用で約5秒です。
- テスト実行：
  - `./mvnw test` -- 2～4秒で完了します。こちらもキャンセル禁止（高速ですが）。安全のためタイムアウトは60秒以上に設定。

### アプリケーションの起動
- **必ずビルド後**にアプリケーションを起動してください。
- 開発モード（認証無効）：`./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` -- 約4秒で起動。起動中は絶対にキャンセルしないでください。
- 本番モード（認証有効）：`./mvnw spring-boot:run` または `java -jar target/myproject.jar` -- ビルド成功後、約5～6秒で起動。
- アプリケーションは http://localhost:8080 で動作します。
- 本番モードではログインページが表示されます（Spring Security有効）。

### Docker（注意：制限環境では動作しない場合あり）
- Dockerビルド：`docker compose build` -- サンドボックス環境ではSSL証明書の問題で失敗する場合があります。その場合「この環境ではSSL証明書制限によりDockerビルドが失敗する」と記録してください。
- Docker起動：`docker compose up` -- ビルド成功時のみ使用。

## 検証

### 手動テスト要件
- **コード変更後は必ず動作確認**を行ってください：
  1. 開発モードでアプリケーションをビルド＆起動：`./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
  2. ホームページテスト：`curl -s http://localhost:8080` で正常にHTMLが返ること（"Hello World!" メッセージが含まれること）
  3. ヘルスエンドポイントテスト：`curl -s http://localhost:8080/actuator/health` で `{"status":"UP"}` が返ること
  4. BigQueryエンドポイントテスト：`curl -s http://localhost:8080/bigquery/health` で接続状態が確認できること
  5. AI機能エンドポイントアクセス確認：http://localhost:8080/ai にアクセス可能なこと
  6. H2コンソールが http://localhost:8080/h2-console でアクセス可能なこと（開発モードのみ）
  7. 続行前にアプリケーションを停止（Ctrl+C）

### 常に実施するテストシナリオ
- **DB連携**：アプリはH2インメモリDBをschema.sql/data.sqlで初期化します。ホームページに「Hello World!」が表示されること＝DB接続確認。
- **Webレイヤ**：Thymeleafテンプレートのレンダリングが正しく行われ、HTMLレスポンスにBootstrap CSSとメッセージ表示が含まれること。
- **BigQuery連携**：BigQueryServiceが正しく初期化され、開発モードではサンプルデータを返し、本番モードでは実際のBigQueryに接続できること。
- **AI機能**：Gemini APIを利用した豆知識取得機能が動作すること（環境変数GEMINI_API_KEYが設定されていない場合はサンプルデータを返す）。
- **認証・認可**：本番モードではSpring Securityによる認証が有効で、開発モードでは無効化されること。
- **Spring Boot機能**：Actuatorのヘルスチェックが動作し、H2コンソールが開発モードで利用できること。

## よくある作業

### ファイル構成概要
```
src/
├── main/
│   ├── java/com/example/myapplication/
│   │   ├── MyApplication.java                   # Main Spring Boot application class
│   │   ├── controller/
│   │   │   ├── IndexController.java            # ホームページコントローラー
│   │   │   ├── BigQueryController.java         # BigQuery REST APIコントローラー
│   │   │   ├── AiController.java               # AI機能（Gemini）コントローラー
│   │   │   ├── UserController.java             # ユーザー管理コントローラー
│   │   │   └── LoginController.java            # ログインコントローラー
│   │   ├── service/
│   │   │   ├── IndexService.java               # ホームページビジネスロジック
│   │   │   ├── BigQueryService.java            # BigQuery操作ビジネスロジック
│   │   │   ├── AiService.java                  # AI機能（Gemini API連携）
│   │   │   └── UserService.java                # ユーザー管理ビジネスロジック
│   │   ├── entity/                             # JPA entities
│   │   ├── repository/                         # Data access layer
│   │   ├── config/                             # Spring Security等の設定
│   │   └── dto/                                # Data Transfer Objects
│   └── resources/
│       ├── application.properties              # Main configuration
│       ├── schema.sql                          # Database schema initialization
│       ├── data.sql                            # Database data initialization
│       └── templates/
│           ├── index.html                      # ホームページ
│           ├── bigquery.html                   # BigQuery操作ページ
│           ├── ai-sample.html                  # AI機能サンプルページ
│           ├── login.html                      # ログインページ
│           └── fragments/                      # 共通フラグメント（ヘッダー、フッター等）
└── test/
    └── groovy/com/example/myapplication/       # Spock tests written in Groovy
        ├── controller/
        │   ├── BigQueryControllerSpec.groovy   # BigQueryコントローラーテスト
        │   └── AiControllerSpec.groovy         # AIコントローラーテスト
        └── service/
            ├── BigQueryServiceSpec.groovy      # BigQueryサービステスト
            └── AiServiceSpec.groovy            # AIサービステスト
```

### 主要な設定ファイル
- **pom.xml**: Spring Boot 3.5.6、Java 17、Google Cloud BigQuery 2.38.0、Spring Security、Spockテストフレームワークを利用したMavenプロジェクト設定
- **application.properties**: H2データベース設定、JPA設定、BigQuery設定、Gemini API設定、Actuatorエンドポイント
- **Dockerfile**: マルチステージDockerビルド（制限環境では動作しない場合あり）
- **docker-compose.yml**: 開発用プロファイルを含むDocker Compose設定

### 環境変数
以下の環境変数を設定することで、本番環境での動作を制御できます：
- **GOOGLE_APPLICATION_CREDENTIALS**: BigQueryサービスアカウントキーファイルのパス（本番モードで必須）
- **BIGQUERY_PROJECT_ID**: BigQueryプロジェクトID（デフォルト: sample-project）
- **BIGQUERY_DATASET_ID**: BigQueryデータセットID（デフォルト: sample_dataset）
- **GEMINI_API_KEY**: Gemini APIキー（設定されていない場合はサンプルデータを返す）

### 開発ワークフロー
- Mavenのバージョンを統一するため、必ず`./mvnw`（Maven Wrapper）を使用してください（`mvn`は使わない）
- このアプリケーションはLombokを利用しています。IDEでLombokプラグインを有効にしてください
- テストはGroovy＋Spockフレームワークで記述されています（JUnitより表現力が高い）
- アプリ起動時、DBに「Hello World!」メッセージが1件自動登録されます
- 開発時は`dev`プロファイルを使用すると認証が無効化され、BigQueryやGemini APIの代わりにサンプルデータが使用されます
- 本番モードではSpring Securityによる認証が有効になり、実際のBigQueryとGemini APIに接続されます

### 主要機能
1. **BigQuery連携**: テーブル作成・削除、データ挿入・取得、SQLクエリ実行
2. **AI機能（Gemini API）**: 豆知識取得などのAI機能サンプル
3. **認証・認可（Spring Security）**: ログイン機能、ユーザー管理
4. **データベース管理**: H2インメモリDBによる開発用データ管理
5. **REST API**: BigQuery操作用のRESTful API

### ビルドとCI情報
- CIビルドは`mvn clean package`を実行（ローカル開発と同じ）
- 追加のリンティングツールは未導入。Mavenのコンパイラ警告を参照してください
- `.github/workflows/build.yml`のGitHub Actionsワークフローで、pushごとにビルドが実行されます
- ビルド成果物：`target/myproject.jar`（Spring Boot fat JAR、約60MB）

### トラブルシューティング
- Mavenの依存解決でビルド失敗時：`~/.m2/repository`を削除し再ビルド
- アプリが起動しない場合：`lsof -i :8080`で8080番ポートの使用状況を確認
- テスト失敗時：`./mvnw clean test`でクリーンな状態から再実行
- Dockerビルド失敗時：従来のMavenビルド手順を利用してください（制限環境ではDockerが動作しない場合があります）
- BigQuery接続エラー：`GOOGLE_APPLICATION_CREDENTIALS`環境変数が正しく設定されているか確認、または開発モード（`dev`プロファイル）で起動してサンプルデータを使用
- 認証エラー：開発時は`dev`プロファイルで起動すると認証が無効化されます
- Gemini APIエラー：`GEMINI_API_KEY`環境変数が設定されていない場合はサンプルデータが返されます

### セキュリティ注意事項
- **サービスアカウントキーファイルは絶対にGit等のバージョン管理に含めないでください**
- **GEMINI_API_KEYなどのAPIキーも同様にバージョン管理に含めないでください**
- 開発モード（`dev`プロファイル）は本番環境で絶対に使用しないでください（認証が無効化されます）
- 本番環境では適切な環境変数管理システム（AWS Secrets Manager、Google Secret Manager等）を使用してください
