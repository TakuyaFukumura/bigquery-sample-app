package com.example.myapplication.controller

import com.example.myapplication.service.IndexService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * IndexControllerのSpockテスト
 * MockMvcを使用してHTTPリクエストをテストする
 */
class IndexControllerSpec extends Specification {

    def indexService = Mock(IndexService)
    def indexController = new IndexController(indexService)
    def mockMvc = MockMvcBuilders.standaloneSetup(indexController).build()

    def "GETリクエストでindexページが正しく表示されること"() {
        given: "サービスからのメッセージ"
        indexService.getMessage() >> "Hello from Service!"

        when: "ルートパスにGETリクエストを送信"
        def result = mockMvc.perform(get("/"))

        then: "ステータスが200でindexビューが返される"
        result.andExpect(status().isOk())
              .andExpect(view().name("index"))
              .andExpect(model().attribute("message", "Hello from Service!"))
    }

    def "サービスのgetMessage()が呼び出されること"() {
        given: "サービスの設定"
        indexService.getMessage() >> "Test Message"

        when: "ルートパスにGETリクエストを送信"
        mockMvc.perform(get("/"))

        then: "サービスのgetMessageが1回呼び出される"
        1 * indexService.getMessage()
    }

    def "異なるメッセージでも正しくモデルに設定されること"() {
        given: "サービスからの別のメッセージ"
        def testMessage = "Different Message"
        indexService.getMessage() >> testMessage

        when: "ルートパスにGETリクエストを送信"
        def result = mockMvc.perform(get("/"))

        then: "メッセージがモデルに正しく設定される"
        result.andExpect(model().attribute("message", testMessage))
    }
}
