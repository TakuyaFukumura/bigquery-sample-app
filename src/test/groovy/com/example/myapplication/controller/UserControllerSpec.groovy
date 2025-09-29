package com.example.myapplication.controller

import com.example.myapplication.dto.UserRegistrationDto
import com.example.myapplication.entity.User
import com.example.myapplication.service.UserService
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.servlet.view.InternalResourceViewResolver
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * UserControllerのSpockテスト
 * ユーザー登録機能のテストを行います
 */
class UserControllerSpec extends Specification {

    def userService = Mock(UserService)
    def userController = new UserController(userService)
    def mockMvc = MockMvcBuilders.standaloneSetup(userController)
            .setViewResolvers(new InternalResourceViewResolver("/templates/", ".html"))
            .setValidator(new LocalValidatorFactoryBean())
            .build()

    def "GET /registerでユーザー登録フォームが表示されること"() {
        when: "登録ページにGETリクエストを送信"
        def result = mockMvc.perform(get("/register"))

        then: "ステータスが200で登録フォームが返される"
        result.andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("userRegistration"))
    }

    def "POST /registerで有効なデータの場合ユーザーが作成されること"() {
        given: "有効な登録データ"
        def testUser = new User("testuser", "encodedPassword", "USER")
        userService.createUser(_) >> testUser

        when: "有効な登録データでPOSTリクエストを送信"
        def result = mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("password", "password123")
                .param("confirmPassword", "password123"))

        then: "ユーザーが作成されログインページにリダイレクトされる"
        1 * userService.createUser({ UserRegistrationDto dto ->
            dto.username == "testuser" &&
                    dto.password == "password123" &&
                    dto.confirmPassword == "password123"
        })
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
    }

    def "POST /registerでパスワードが一致しない場合エラーになること"() {
        when: "パスワードが一致しない登録データでPOSTリクエストを送信"
        def result = mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("password", "password123")
                .param("confirmPassword", "differentpassword"))

        then: "ユーザー作成は行われずフォームに戻る"
        0 * userService.createUser(_)
        result.andExpect(status().isOk())
                .andExpect(view().name("register"))
    }

    def "POST /registerで短いパスワードの場合バリデーションエラーになること"() {
        when: "短いパスワードでPOSTリクエストを送信"
        def result = mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("password", "123")
                .param("confirmPassword", "123"))

        then: "バリデーションエラーでユーザー作成は行われずフォームに戻る"
        0 * userService.createUser(_)
        result.andExpect(status().isOk())
                .andExpect(view().name("register"))
    }
}
