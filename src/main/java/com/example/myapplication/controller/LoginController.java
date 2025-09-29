package com.example.myapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ログイン関連の処理を担当するコントローラー
 * カスタムログインページの表示を行います
 */
@Controller
public class LoginController {

    /**
     * カスタムログインページを表示
     *
     * @return ログインページテンプレート名
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
