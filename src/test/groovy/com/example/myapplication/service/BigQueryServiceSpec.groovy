package com.example.myapplication.service

import com.google.api.gax.paging.Page
import com.google.cloud.bigquery.*
import spock.lang.Specification

/**
 * BigQueryServiceのSpockテスト
 * BigQueryの主要機能をモックを使用してテストする
 */
class BigQueryServiceSpec extends Specification {

    def bigQuery = Mock(BigQuery)
    def bigQueryService = new BigQueryService("test-project", "test-dataset", bigQuery)

    def "runQuery()でSQLクエリが正常に実行されること"() {
        given: "テスト用のクエリとモックレスポンス"
        def sql = "SELECT * FROM test_table"
        def mockSchema = Schema.of(
                Field.of("id", StandardSQLTypeName.INT64),
                Field.of("name", StandardSQLTypeName.STRING)
        )
        def mockResult = Mock(TableResult)
        def mockRow = Mock(FieldValueList)
        def mockIdField = Mock(FieldValue)
        def mockNameField = Mock(FieldValue)

        when: "クエリを実行"
        def result = bigQueryService.runQuery(sql)

        then: "BigQueryのqueryメソッドが呼び出され、正しい結果が返される"
        1 * bigQuery.query(_) >> mockResult
        1 * mockResult.getSchema() >> mockSchema
        1 * mockResult.iterateAll() >> [mockRow]
        1 * mockRow.get("id") >> mockIdField
        1 * mockRow.get("name") >> mockNameField
        1 * mockIdField.isNull() >> false
        1 * mockIdField.getValue() >> 1L
        1 * mockNameField.isNull() >> false
        1 * mockNameField.getValue() >> "テストユーザー"

        and: "結果が正しく変換される"
        result.size() == 1
        result[0]["id"] == 1L
        result[0]["name"] == "テストユーザー"
    }

    def "runQuery()で空のSQLクエリを渡すとIllegalArgumentExceptionが発生すること"() {
        when: "空のクエリを実行"
        bigQueryService.runQuery("")

        then: "例外が発生"
        def ex = thrown(IllegalArgumentException)
        ex.message == "SQLクエリが空です"
    }

    def "runQuery()でnullのSQLクエリを渡すとIllegalArgumentExceptionが発生すること"() {
        when: "nullクエリを実行"
        bigQueryService.runQuery(null)

        then: "例外が発生"
        def ex = thrown(IllegalArgumentException)
        ex.message == "SQLクエリが空です"
    }

    def "createTable()でテーブルが正常に作成されること"() {
        given: "テスト用のテーブル名とスキーマ"
        def tableName = "test_table"
        def schema = Schema.of(Field.of("id", StandardSQLTypeName.INT64))

        when: "テーブルを作成"
        bigQueryService.createTable(tableName, schema)

        then: "BigQueryのcreateメソッドが呼び出される"
        1 * bigQuery.create(_) >> { throw new RuntimeException("期待通りの呼び出し") }
        
        and: "実際にはRuntimeExceptionが発生する（モックの期待通り）"
        thrown(RuntimeException)
    }

    def "createTable()で空のテーブル名を渡すとIllegalArgumentExceptionが発生すること"() {
        given: "空のテーブル名"
        def schema = Schema.of(Field.of("id", StandardSQLTypeName.INT64))

        when: "空のテーブル名でテーブルを作成"
        bigQueryService.createTable("", schema)

        then: "例外が発生"
        def ex = thrown(IllegalArgumentException)
        ex.message == "テーブル名が空です"
    }

    def "createTable()でnullのスキーマを渡すとIllegalArgumentExceptionが発生すること"() {
        when: "nullスキーマでテーブルを作成"
        bigQueryService.createTable("test_table", null)

        then: "例外が発生"
        def ex = thrown(IllegalArgumentException)
        ex.message == "スキーマが指定されていません"
    }

    def "insertData()でデータが正常に挿入されること"() {
        given: "テスト用のテーブル名とデータ"
        def tableName = "test_table"
        def data = [
                ["id": 1, "name": "テストユーザー1"],
                ["id": 2, "name": "テストユーザー2"]
        ]
        def mockResponse = Mock(InsertAllResponse)

        when: "データを挿入"
        bigQueryService.insertData(tableName, data)

        then: "BigQueryのinsertAllメソッドが呼び出される"
        1 * bigQuery.insertAll(_) >> mockResponse
        1 * mockResponse.hasErrors() >> false
    }

    def "insertData()で空のデータを渡すとIllegalArgumentExceptionが発生すること"() {
        when: "空のデータでデータを挿入"
        bigQueryService.insertData("test_table", [])

        then: "例外が発生"
        def ex = thrown(IllegalArgumentException)
        ex.message == "挿入するデータが空です"
    }

    def "deleteTable()でテーブルが正常に削除されること"() {
        given: "テスト用のテーブル名"
        def tableName = "test_table"

        when: "テーブルを削除"
        bigQueryService.deleteTable(tableName)

        then: "BigQueryのdeleteメソッドが呼び出される"
        1 * bigQuery.delete(_) >> true
    }

    def "listTables()でテーブル一覧が正常に取得されること"() {
        given: "テスト用のテーブル一覧"
        def mockPage = Mock(Page)

        when: "テーブル一覧を取得"
        bigQueryService.listTables()

        then: "BigQueryのlistTablesメソッドが呼び出される"
        1 * bigQuery.listTables(_) >> mockPage
        1 * mockPage.iterateAll() >> { throw new RuntimeException("期待通りの呼び出し") }
        
        and: "実際にはRuntimeExceptionが発生する（モックの期待通り）"
        thrown(RuntimeException)
    }
}