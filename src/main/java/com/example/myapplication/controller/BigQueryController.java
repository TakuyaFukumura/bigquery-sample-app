package com.example.myapplication.controller;

import com.example.myapplication.service.BigQueryService;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BigQuery機能を提供するREST API コントローラ
 * BigQueryサービスへのHTTPアクセスを提供
 */
@Slf4j
@RestController
@RequestMapping("/bigquery")
public class BigQueryController {

    private final BigQueryService bigQueryService;

    @Autowired
    public BigQueryController(BigQueryService bigQueryService) {
        this.bigQueryService = bigQueryService;
    }

    /**
     * SQLクエリを実行して結果を取得
     *
     * @param sql 実行するSQLクエリ
     * @return クエリ結果
     */
    @GetMapping("/query")
    public ResponseEntity<?> runQuery(@RequestParam String sql) {
        try {
            log.info("BigQueryクエリリクエスト受信: {}", sql);
            List<Map<String, Object>> result = bigQueryService.runQuery(sql);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "rowCount", result.size(),
                    "data", result
            ));
        } catch (IllegalArgumentException e) {
            log.warn("無効なクエリパラメータ: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("BigQueryクエリ実行エラー", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "クエリの実行に失敗しました: " + e.getMessage()
            ));
        }
    }

    /**
     * サンプルテーブルを作成
     *
     * @param tableName テーブル名
     * @return 作成結果
     */
    @PostMapping("/table/{tableName}")
    public ResponseEntity<?> createSampleTable(@PathVariable String tableName) {
        try {
            log.info("BigQueryサンプルテーブル作成リクエスト受信: {}", tableName);
            
            // サンプルスキーマを定義
            Schema schema = Schema.of(
                    Field.of("id", StandardSQLTypeName.INT64),
                    Field.of("name", StandardSQLTypeName.STRING),
                    Field.of("email", StandardSQLTypeName.STRING),
                    Field.of("created_at", StandardSQLTypeName.TIMESTAMP)
            );
            
            bigQueryService.createTable(tableName, schema);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "テーブルが正常に作成されました: " + tableName
            ));
        } catch (IllegalArgumentException e) {
            log.warn("無効なテーブル名: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("BigQueryテーブル作成エラー", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "テーブルの作成に失敗しました: " + e.getMessage()
            ));
        }
    }

    /**
     * テーブルにサンプルデータを挿入
     *
     * @param tableName テーブル名
     * @param data      挿入するデータ
     * @return 挿入結果
     */
    @PostMapping("/table/{tableName}/data")
    public ResponseEntity<?> insertData(@PathVariable String tableName,
                                       @RequestBody List<Map<String, Object>> data) {
        try {
            log.info("BigQueryデータ挿入リクエスト受信: {} に {} 件", tableName, data.size());
            bigQueryService.insertData(tableName, data);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", data.size() + " 件のデータが正常に挿入されました"
            ));
        } catch (IllegalArgumentException e) {
            log.warn("無効なデータまたはテーブル名: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("BigQueryデータ挿入エラー", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "データの挿入に失敗しました: " + e.getMessage()
            ));
        }
    }

    /**
     * テーブルを削除
     *
     * @param tableName 削除するテーブル名
     * @return 削除結果
     */
    @DeleteMapping("/table/{tableName}")
    public ResponseEntity<?> deleteTable(@PathVariable String tableName) {
        try {
            log.info("BigQueryテーブル削除リクエスト受信: {}", tableName);
            bigQueryService.deleteTable(tableName);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "テーブルが正常に削除されました: " + tableName
            ));
        } catch (IllegalArgumentException e) {
            log.warn("無効なテーブル名: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("BigQueryテーブル削除エラー", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "テーブルの削除に失敗しました: " + e.getMessage()
            ));
        }
    }

    /**
     * データセット内のテーブル一覧を取得
     *
     * @return テーブル一覧
     */
    @GetMapping("/tables")
    public ResponseEntity<?> listTables() {
        try {
            log.info("BigQueryテーブル一覧取得リクエスト受信");
            List<String> tables = bigQueryService.listTables();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "tableCount", tables.size(),
                    "tables", tables
            ));
        } catch (Exception e) {
            log.error("BigQueryテーブル一覧取得エラー", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "テーブル一覧の取得に失敗しました: " + e.getMessage()
            ));
        }
    }

    /**
     * BigQuery接続状態を確認
     *
     * @return 接続状態
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            log.info("BigQueryヘルスチェックリクエスト受信");
            // 簡単なクエリでBigQueryの接続を確認
            bigQueryService.runQuery("SELECT 1 as health_check");
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "status", "BigQuery connection is healthy"
            ));
        } catch (Exception e) {
            log.error("BigQueryヘルスチェックエラー", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "status", "BigQuery connection failed",
                    "error", e.getMessage()
            ));
        }
    }
}