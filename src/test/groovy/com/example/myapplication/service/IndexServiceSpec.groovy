package com.example.myapplication.service

import com.example.myapplication.entity.Message
import com.example.myapplication.repository.MessageRepository
import spock.lang.Specification

/**
 * IndexServiceのSpockテスト
 * Spockの仕様記述形式でテストケースを記述する
 */
class IndexServiceSpec extends Specification {

    def messageRepository = Mock(MessageRepository)
    def indexService = new IndexService(messageRepository)

    def "getMessage()が正常なメッセージを返すこと"() {
        given: "リポジトリに存在するメッセージ"
        def message = new Message(id: 1L, text: "Hello, World!")
        messageRepository.findById(1L) >> Optional.of(message)

        when: "getMessageを呼び出す"
        def result = indexService.getMessage()

        then: "正しいメッセージが返される"
        result == "Hello, World!"
    }

    def "getMessage()でメッセージが見つからない場合はエラーメッセージを返すこと"() {
        given: "リポジトリにメッセージが存在しない"
        messageRepository.findById(1L) >> Optional.empty()

        when: "getMessageを呼び出す"
        def result = indexService.getMessage()

        then: "エラーメッセージが返される"
        result == "Error!"
    }

    def "getMessage()でリポジトリが呼び出されること"() {
        when: "getMessageを呼び出す"
        indexService.getMessage()

        then: "リポジトリのfindByIdが1回呼び出される"
        1 * messageRepository.findById(1L) >> Optional.empty()
    }
}
