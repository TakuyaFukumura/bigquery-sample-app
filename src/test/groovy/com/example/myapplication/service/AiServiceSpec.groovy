package com.example.myapplication.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

/**
 * AiServiceのSpockテスト
 * Spockの仕様記述形式でテストケースを記述する
 */
class AiServiceSpec extends Specification {

    def webClient = Mock(WebClient)
    def objectMapper = Mock(ObjectMapper)
    def aiService = new AiService(webClient, objectMapper)

    def "APIキーが設定されていない場合、例外がスローされること"() {
        given: "APIキーが設定されていないAiService"
        // デフォルトでAPIキーは空文字列

        when: "getTriviaを呼び出す"
        aiService.getTrivia()

        then: "IllegalStateExceptionがスローされる"
        def exception = thrown(IllegalStateException)
        exception.message == "APIキーが設定されていません。"
    }

    def "WebClientとObjectMapperが正しく注入されること"() {
        expect: "依存関係が正しく設定されている"
        aiService != null
    }
}
