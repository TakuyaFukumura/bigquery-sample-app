package com.example.myapplication

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

/**
 * Spring Boot統合テスト
 * アプリケーション全体のコンテキストを使用したテスト
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class MyApplicationSpec extends Specification {

    def "Spring Bootアプリケーションが正常に起動すること"() {
        expect: "アプリケーションコンテキストが正常にロードされる"
        true // アプリケーションが起動すればテスト成功
    }
}
