package com.example.myapplication.controller


import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.servlet.view.InternalResourceViewResolver
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

/**
 * LoginControllerのSpockテスト
 * カスタムログインページの表示をテストします
 */
class LoginControllerSpec extends Specification {

    def loginController = new LoginController()
    def mockMvc = MockMvcBuilders.standaloneSetup(loginController)
            .setViewResolvers(new InternalResourceViewResolver("/templates/", ".html"))
            .build()

    def "GET /loginでログインページが表示されること"() {
        when: "ログインページにGETリクエストを送信"
        def result = mockMvc.perform(get("/login"))

        then: "ステータスが200でログインページが返される"
        result.andExpect(status().isOk())
                .andExpect(view().name("login"))
    }
}
