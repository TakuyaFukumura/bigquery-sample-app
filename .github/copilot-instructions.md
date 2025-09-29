# Basic Spring Boot Application

このプロジェクトはSpring Boot開発の基礎となるサンプルWebアプリケーションです。Spring Boot 3.5.4、Java 17、Maven、Thymeleafテンプレートエンジン、Spring Data JPA（H2インメモリDB）、Spring Boot Actuatorによる監視機能を利用しています。

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
- 開発モード：`./mvnw spring-boot:run` -- 約4秒で起動。起動中は絶対にキャンセルしないでください。
- 本番モード：`java -jar target/myproject.jar` -- ビルド成功後、約5～6秒で起動。
- アプリケーションは http://localhost:8080 で動作します。

### Docker（注意：制限環境では動作しない場合あり）
- Dockerビルド：`docker compose build` -- サンドボックス環境ではSSL証明書の問題で失敗する場合があります。その場合「この環境ではSSL証明書制限によりDockerビルドが失敗する」と記録してください。
- Docker起動：`docker compose up` -- ビルド成功時のみ使用。

## 検証

### 手動テスト要件
- **コード変更後は必ず動作確認**を行ってください：
  1. 上記のいずれかのコマンドでアプリケーションをビルド＆起動
  2. ホームページテスト：`curl -s http://localhost:8080 | grep -i "hello"` で `<span>Hello World!</span>` が返ること
  3. ヘルスエンドポイントテスト：`curl -s http://localhost:8080/actuator/health` で `{"status":"UP"}` が返ること
  4. H2コンソールが http://localhost:8080/h2-console でアクセス可能なこと（ブラウザで確認）
  5. 続行前にアプリケーションを停止（Ctrl+C）

### 常に実施するテストシナリオ
- **DB連携**：アプリはH2インメモリDBをschema.sql/data.sqlで初期化します。ホームページに「Hello World!」が表示されること＝DB接続確認。
- **Webレイヤ**：Thymeleafテンプレートのレンダリングが正しく行われ、HTMLレスポンスにBootstrap CSSとメッセージ表示が含まれること。
- **Spring Boot機能**：Actuatorのヘルスチェックが動作し、H2コンソールが開発用に利用できること。

## よくある作業

### ファイル構成概要
```
src/
├── main/
│   ├── java/com/example/myapplication/
│   │   ├── MyApplication.java          # Main Spring Boot application class
│   │   ├── controller/IndexController.java  # Web controller handling requests
│   │   ├── service/IndexService.java   # Business logic service layer
│   │   ├── entity/Message.java         # JPA entity for database
│   │   └── repository/MessageRepository.java  # Data access layer
│   └── resources/
│       ├── application.properties      # Main configuration
│       ├── schema.sql                 # Database schema initialization
│       ├── data.sql                   # Database data initialization  
│       └── templates/index.html       # Thymeleaf template
└── test/
    └── groovy/com/example/myapplication/  # Spock tests written in Groovy
```

### 主要な設定ファイル
- **pom.xml**: Spring Boot 3.5.4、Java 17、Spockテストフレームワークを利用したMavenプロジェクト設定
- **application.properties**: H2データベース設定、JPA設定、Actuatorエンドポイント
- **Dockerfile**: マルチステージDockerビルド（制限環境では動作しない場合あり）
- **docker-compose.yml**: 開発用プロファイルを含むDocker Compose設定

### 開発ワークフロー
- Mavenのバージョンを統一するため、必ず`./mvnw`（Maven Wrapper）を使用してください（`mvn`は使わない）
- このアプリケーションはLombokを利用しています。IDEでLombokプラグインを有効にしてください
- テストはGroovy＋Spockフレームワークで記述されています（JUnitより表現力が高い）
- アプリ起動時、DBに「Hello World!」メッセージが1件自動登録されます

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
