# --- ビルド用ステージ ---
## MavenとJDKが入った公式イメージを使用（Java 21, Alpineベース）
FROM maven:3.9-eclipse-temurin-17-alpine AS build

## 作業ディレクトリを/appに設定
WORKDIR /app

## プロジェクトの全ファイルをコンテナにコピー
COPY . .

## テストをスキップしてMavenでパッケージング（JARファイルを生成）
RUN mvn clean package -DskipTests

# --- 実行用ステージ ---
## 実行用の軽量イメージ
FROM eclipse-temurin:21-jre-alpine

## 非rootユーザーでの実行
RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -s /bin/sh -D spring

USER spring:spring

## 作業ディレクトリの設定
WORKDIR /app

## ビルドされたJARファイルをコピー
COPY --chown=spring:spring --from=build /app/target/*.jar app.jar

## アプリケーションのポート
EXPOSE 8080

## アプリケーションの起動
ENTRYPOINT ["java", "-jar", "app.jar"]
