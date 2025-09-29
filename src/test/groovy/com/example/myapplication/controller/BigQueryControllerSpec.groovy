package com.example.myapplication.controller

import com.example.myapplication.service.BigQueryService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * BigQueryControllerのSpockテスト
 * MockMvcを使用してHTTPリクエストをテストする
 */
class BigQueryControllerSpec extends Specification {

    def bigQueryService = Mock(BigQueryService)
    def bigQueryController = new BigQueryController(bigQueryService)
    def mockMvc = MockMvcBuilders.standaloneSetup(bigQueryController).build()
    def objectMapper = new ObjectMapper()

    def "GET /bigquery/query でクエリが正常に実行されること"() {
        given: "サービスからのレスポンス"
        def testData = [
                ["id": 1, "name": "テストユーザー"]
        ]

        when: "クエリエンドポイントにGETリクエストを送信"
        def result = mockMvc.perform(get("/bigquery/query")
                .param("sql", "SELECT * FROM test_table"))

        then: "サービスのrunQueryが1回呼び出される"
        1 * bigQueryService.runQuery("SELECT * FROM test_table") >> testData

        and: "ステータスが200で正しいレスポンスが返される"
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.success').value(true))
              .andExpect(jsonPath('$.rowCount').value(1))
              .andExpect(jsonPath('$.data[0].id').value(1))
              .andExpect(jsonPath('$.data[0].name').value("テストユーザー"))
    }

    def "GET /bigquery/query で無効なクエリパラメータを渡すとBadRequestが返されること"() {
        when: "空のクエリでリクエストを送信"
        def result = mockMvc.perform(get("/bigquery/query")
                .param("sql", ""))

        then: "サービスから例外が発生"
        1 * bigQueryService.runQuery("") >> { throw new IllegalArgumentException("SQLクエリが空です") }

        and: "ステータスが400でエラーレスポンスが返される"
        result.andExpect(status().isBadRequest())
              .andExpect(jsonPath('$.success').value(false))
              .andExpect(jsonPath('$.error').value("SQLクエリが空です"))
    }

    def "POST /bigquery/table/{tableName} でテーブルが正常に作成されること"() {
        when: "テーブル作成エンドポイントにPOSTリクエストを送信"
        def result = mockMvc.perform(post("/bigquery/table/test_table"))

        then: "サービスのcreateTableが1回呼び出される"
        1 * bigQueryService.createTable("test_table", _)

        and: "ステータスが200で正しいレスポンスが返される"
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.success').value(true))
              .andExpect(jsonPath('$.message').value("テーブルが正常に作成されました: test_table"))
    }

    def "POST /bigquery/table/{tableName}/data でデータが正常に挿入されること"() {
        given: "挿入するテストデータ"
        def testData = [
                ["id": 1, "name": "テストユーザー1"],
                ["id": 2, "name": "テストユーザー2"]
        ]

        when: "データ挿入エンドポイントにPOSTリクエストを送信"
        def result = mockMvc.perform(post("/bigquery/table/test_table/data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData)))

        then: "サービスのinsertDataが1回呼び出される"
        1 * bigQueryService.insertData("test_table", testData)

        and: "ステータスが200で正しいレスポンスが返される"
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.success').value(true))
              .andExpect(jsonPath('$.message').value("2 件のデータが正常に挿入されました"))
    }

    def "DELETE /bigquery/table/{tableName} でテーブルが正常に削除されること"() {
        when: "テーブル削除エンドポイントにDELETEリクエストを送信"
        def result = mockMvc.perform(delete("/bigquery/table/test_table"))

        then: "サービスのdeleteTableが1回呼び出される"
        1 * bigQueryService.deleteTable("test_table")

        and: "ステータスが200で正しいレスポンスが返される"
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.success').value(true))
              .andExpect(jsonPath('$.message').value("テーブルが正常に削除されました: test_table"))
    }

    def "GET /bigquery/tables でテーブル一覧が正常に取得されること"() {
        given: "サービスからのテーブル一覧"
        def tableList = ["table1", "table2", "table3"]

        when: "テーブル一覧エンドポイントにGETリクエストを送信"
        def result = mockMvc.perform(get("/bigquery/tables"))

        then: "サービスのlistTablesが1回呼び出される"
        1 * bigQueryService.listTables() >> tableList

        and: "ステータスが200で正しいレスポンスが返される"
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.success').value(true))
              .andExpect(jsonPath('$.tableCount').value(3))
              .andExpect(jsonPath('$.tables[0]').value("table1"))
              .andExpect(jsonPath('$.tables[1]').value("table2"))
              .andExpect(jsonPath('$.tables[2]').value("table3"))
    }

    def "GET /bigquery/health でヘルスチェックが正常に実行されること"() {
        given: "サービスからの正常なレスポンス"
        def healthData = [["health_check": 1]]

        when: "ヘルスチェックエンドポイントにGETリクエストを送信"
        def result = mockMvc.perform(get("/bigquery/health"))

        then: "サービスのrunQueryが1回呼び出される"
        1 * bigQueryService.runQuery("SELECT 1 as health_check") >> healthData

        and: "ステータスが200で正しいレスポンスが返される"
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.success').value(true))
              .andExpect(jsonPath('$.status').value("BigQuery connection is healthy"))
    }

    def "GET /bigquery/health でBigQuery接続エラーが発生した場合Internal Server Errorが返されること"() {
        when: "ヘルスチェックエンドポイントにGETリクエストを送信"
        def result = mockMvc.perform(get("/bigquery/health"))

        then: "サービスから例外が発生"
        1 * bigQueryService.runQuery("SELECT 1 as health_check") >> { throw new RuntimeException("接続失敗") }

        and: "ステータスが500でエラーレスポンスが返される"
        result.andExpect(status().isInternalServerError())
              .andExpect(jsonPath('$.success').value(false))
              .andExpect(jsonPath('$.status').value("BigQuery connection failed"))
              .andExpect(jsonPath('$.error').value("接続失敗"))
    }
}