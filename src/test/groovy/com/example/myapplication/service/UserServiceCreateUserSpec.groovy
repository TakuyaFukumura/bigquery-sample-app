package com.example.myapplication.service

import com.example.myapplication.dto.UserRegistrationDto
import com.example.myapplication.entity.User
import com.example.myapplication.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

/**
 * UserServiceの拡張機能（ユーザー作成）のSpockテスト
 */
class UserServiceCreateUserSpec extends Specification {

    def userRepository = Mock(UserRepository)
    def passwordEncoder = Mock(PasswordEncoder)
    def userService = new UserService(userRepository, passwordEncoder)

    def "新規ユーザーが正常に作成されること"() {
        given: "新規ユーザー登録データ"
        def registrationDto = new UserRegistrationDto()
        registrationDto.username = "newuser"
        registrationDto.password = "password123"
        registrationDto.confirmPassword = "password123"

        def encodedPassword = "encoded-password"
        def savedUser = new User("newuser", encodedPassword, "USER")
        savedUser.id = 1L

        when: "ユーザーを作成"
        def result = userService.createUser(registrationDto)

        then: "ユーザー名の重複チェックが行われる"
        1 * userRepository.findByUsername("newuser") >> Optional.empty()

        and: "パスワードがエンコードされる"
        1 * passwordEncoder.encode("password123") >> encodedPassword

        and: "新規ユーザーが保存される"
        1 * userRepository.save({ User user ->
            user.username == "newuser" &&
                    user.password == encodedPassword &&
                    user.roles == "USER" &&
                    user.enabled == true
        }) >> savedUser

        and: "保存されたユーザーが返される"
        result == savedUser
    }

    def "既存ユーザー名で登録しようとした場合例外が発生すること"() {
        given: "既存ユーザーと同じユーザー名での登録データ"
        def registrationDto = new UserRegistrationDto()
        registrationDto.username = "existinguser"
        registrationDto.password = "password123"
        registrationDto.confirmPassword = "password123"

        def existingUser = new User("existinguser", "old-password", "USER")

        when: "既存ユーザー名でユーザー作成を試行"
        userService.createUser(registrationDto)

        then: "ユーザー名の重複チェックで既存ユーザーが見つかる"
        1 * userRepository.findByUsername("existinguser") >> Optional.of(existingUser)

        and: "IllegalArgumentExceptionが発生する"
        def exception = thrown(IllegalArgumentException)
        exception.message == "ユーザー名 'existinguser' は既に使用されています"

        and: "パスワードエンコードや保存は行われない"
        0 * passwordEncoder.encode(_)
        0 * userRepository.save(_)
    }

    def "ユーザー名存在チェックが正常に動作すること"() {
        given: "存在するユーザー名と存在しないユーザー名"
        def existingUser = new User("existinguser", "password", "USER")

        when: "存在するユーザー名をチェック"
        def existsResult = userService.isUsernameExists("existinguser")

        and: "存在しないユーザー名をチェック"
        def notExistsResult = userService.isUsernameExists("newuser")

        then: "適切な結果が返される"
        1 * userRepository.findByUsername("existinguser") >> Optional.of(existingUser)
        1 * userRepository.findByUsername("newuser") >> Optional.empty()

        existsResult == true
        notExistsResult == false
    }
}
