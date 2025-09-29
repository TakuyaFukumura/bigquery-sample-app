package com.example.myapplication.service;

import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BigQuery機能を提供するサービスクラス
 * Google Cloud BigQueryを使用してデータの読み書きを行う
 */
@Slf4j
@Service
public class BigQueryService {

    private final BigQuery bigQuery;
    private final String projectId;
    private final String datasetId;

    public BigQueryService(@Value("${app.bigquery.project-id}") String projectId,
                          @Value("${app.bigquery.dataset-id}") String datasetId) {
        this(projectId, datasetId, BigQueryOptions.getDefaultInstance().getService());
    }

    // テスト用のコンストラクタ
    public BigQueryService(String projectId, String datasetId, BigQuery bigQuery) {
        this.projectId = projectId;
        this.datasetId = datasetId;
        this.bigQuery = bigQuery;
        try {
            log.info("BigQueryService initialized with project: {}, dataset: {}", projectId, datasetId);
        } catch (Exception e) {
            log.error("BigQuery初期化に失敗しました", e);
            throw new RuntimeException("BigQueryの初期化に失敗しました", e);
        }
    }

    /**
     * SQLクエリを実行して結果を取得する
     *
     * @param sql 実行するSQLクエリ
     * @return クエリ結果のリスト（Map形式）
     * @throws RuntimeException クエリ実行に失敗した場合
     */
    public List<Map<String, Object>> runQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQLクエリが空です");
        }

        try {
            log.info("BigQueryクエリを実行: {}", sql);
            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
            TableResult result = bigQuery.query(queryConfig);

            List<Map<String, Object>> rows = new ArrayList<>();
            for (FieldValueList row : result.iterateAll()) {
                Map<String, Object> rowMap = new HashMap<>();
                for (Field field : result.getSchema().getFields()) {
                    String fieldName = field.getName();
                    FieldValue fieldValue = row.get(fieldName);
                    rowMap.put(fieldName, fieldValue.isNull() ? null : fieldValue.getValue());
                }
                rows.add(rowMap);
            }

            log.info("BigQueryクエリ完了: {} 件の結果を取得", rows.size());
            return rows;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("BigQueryクエリが中断されました", e);
            throw new RuntimeException("クエリが中断されました", e);
        } catch (Exception e) {
            log.error("BigQueryクエリ実行に失敗", e);
            throw new RuntimeException("クエリの実行に失敗しました: " + e.getMessage(), e);
        }
    }

    /**
     * テーブルを作成する
     *
     * @param tableName テーブル名
     * @param schema    テーブルスキーマ
     * @throws RuntimeException テーブル作成に失敗した場合
     */
    public void createTable(String tableName, Schema schema) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("テーブル名が空です");
        }
        if (schema == null) {
            throw new IllegalArgumentException("スキーマが指定されていません");
        }

        try {
            log.info("BigQueryテーブルを作成: {}.{}.{}", projectId, datasetId, tableName);
            TableId tableId = TableId.of(projectId, datasetId, tableName);
            TableDefinition tableDefinition = StandardTableDefinition.of(schema);
            TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();

            Table table = bigQuery.create(tableInfo);
            log.info("BigQueryテーブル作成完了: {}", table.getTableId());

        } catch (BigQueryException e) {
            if (e.getCode() == 409) {
                log.warn("テーブルは既に存在します: {}.{}.{}", projectId, datasetId, tableName);
            } else {
                log.error("BigQueryテーブル作成に失敗", e);
                throw new RuntimeException("テーブルの作成に失敗しました: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("BigQueryテーブル作成中に予期しないエラーが発生", e);
            throw new RuntimeException("テーブル作成で予期しないエラーが発生しました", e);
        }
    }

    /**
     * データをテーブルに挿入する
     *
     * @param tableName テーブル名
     * @param rows      挿入するデータ行のリスト
     * @throws RuntimeException データ挿入に失敗した場合
     */
    public void insertData(String tableName, List<Map<String, Object>> rows) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("テーブル名が空です");
        }
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("挿入するデータが空です");
        }

        try {
            log.info("BigQueryテーブルにデータを挿入: {}.{}.{}, {} 件", projectId, datasetId, tableName, rows.size());
            TableId tableId = TableId.of(projectId, datasetId, tableName);

            List<InsertAllRequest.RowToInsert> rowsToInsert = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                rowsToInsert.add(InsertAllRequest.RowToInsert.of(row));
            }

            InsertAllRequest insertRequest = InsertAllRequest.newBuilder(tableId)
                    .setRows(rowsToInsert)
                    .build();

            InsertAllResponse response = bigQuery.insertAll(insertRequest);
            if (response.hasErrors()) {
                log.error("データ挿入でエラーが発生: {}", response.getInsertErrors());
                throw new RuntimeException("データ挿入でエラーが発生しました");
            }

            log.info("BigQueryデータ挿入完了: {} 件", rows.size());

        } catch (Exception e) {
            log.error("BigQueryデータ挿入に失敗", e);
            throw new RuntimeException("データ挿入に失敗しました: " + e.getMessage(), e);
        }
    }

    /**
     * テーブルを削除する
     *
     * @param tableName 削除するテーブル名
     * @throws RuntimeException テーブル削除に失敗した場合
     */
    public void deleteTable(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("テーブル名が空です");
        }

        try {
            log.info("BigQueryテーブルを削除: {}.{}.{}", projectId, datasetId, tableName);
            TableId tableId = TableId.of(projectId, datasetId, tableName);
            boolean deleted = bigQuery.delete(tableId);

            if (deleted) {
                log.info("BigQueryテーブル削除完了: {}", tableId);
            } else {
                log.warn("テーブルが見つかりませんでした: {}", tableId);
            }

        } catch (Exception e) {
            log.error("BigQueryテーブル削除に失敗", e);
            throw new RuntimeException("テーブル削除に失敗しました: " + e.getMessage(), e);
        }
    }

    /**
     * データセット内のテーブル一覧を取得する
     *
     * @return テーブル名のリスト
     * @throws RuntimeException テーブル一覧取得に失敗した場合
     */
    public List<String> listTables() {
        try {
            log.info("BigQueryテーブル一覧を取得: {}.{}", projectId, datasetId);
            DatasetId datasetIdObj = DatasetId.of(projectId, datasetId);

            List<String> tableNames = new ArrayList<>();
            Page<Table> tables = bigQuery.listTables(datasetIdObj);
            for (Table table : tables.iterateAll()) {
                tableNames.add(table.getTableId().getTable());
            }

            log.info("BigQueryテーブル一覧取得完了: {} 件", tableNames.size());
            return tableNames;

        } catch (Exception e) {
            log.error("BigQueryテーブル一覧取得に失敗", e);
            throw new RuntimeException("テーブル一覧の取得に失敗しました: " + e.getMessage(), e);
        }
    }
}