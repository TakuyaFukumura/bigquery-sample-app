package com.example.myapplication.controller

import com.example.myapplication.service.AiService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * AiControllerのSpockテスト
 * MockMvcを使用してHTTPリクエストをテストする
 */
class AiControllerSpec extends Specification {

    def aiService = Mock(AiService)
    def aiController = new AiController(aiService)
    def mockMvc = MockMvcBuilders.standaloneSetup(aiController).build()

    def "GETリクエストでAI機能サンプルページが正しく表示されること"() {
        when: "/aiパスにGETリクエストを送信"
        def result = mockMvc.perform(get("/ai"))

        then: "ステータスが200でai-sampleビューが返される"
        result.andExpect(status().isOk())
              .andExpect(view().name("ai-sample"))
    }

    def "POSTリクエストで豆知識が正常に取得されること"() {
        given: "サービスからの豆知識"
        def triviaText = "これは豆知識です。"
        aiService.getTrivia() >> triviaText

        when: "/ai/triviaパスにPOSTリクエストを送信"
        def result = mockMvc.perform(post("/ai/trivia"))

        then: "ステータスが200でai-sampleビューが返され、豆知識とsuccess=trueがモデルに設定される"
        result.andExpect(status().isOk())
              .andExpect(view().name("ai-sample"))
              .andExpect(model().attribute("trivia", triviaText))
              .andExpect(model().attribute("success", true))
    }

    def "POSTリクエストでサービスからエラーが発生した場合の処理"() {
        given: "サービスからの例外"
        def errorMessage = "API呼び出しエラー"
        aiService.getTrivia() >> { throw new RuntimeException(errorMessage) }

        when: "/ai/triviaパスにPOSTリクエストを送信"
        def result = mockMvc.perform(post("/ai/trivia"))

        then: "ステータスが200でai-sampleビューが返され、エラーメッセージとsuccess=falseがモデルに設定される"
        result.andExpect(status().isOk())
              .andExpect(view().name("ai-sample"))
              .andExpect(model().attribute("error", "豆知識の取得に失敗しました: " + errorMessage))
              .andExpect(model().attribute("success", false))
    }

    def "サービスのgetTrivia()が呼び出されること"() {
        when: "/ai/triviaパスにPOSTリクエストを送信"
        mockMvc.perform(post("/ai/trivia"))

        then: "サービスのgetTriviaが1回呼び出される"
        1 * aiService.getTrivia()
    }
}
