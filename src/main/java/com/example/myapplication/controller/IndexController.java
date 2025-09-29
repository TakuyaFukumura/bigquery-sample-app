package com.example.myapplication.controller;

import com.example.myapplication.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/") // URLとの関連付け http://localhost:8080/ の時に呼ばれる
public class IndexController {

    private final IndexService service;

    @Autowired
    public IndexController(IndexService service) {
        this.service = service; // IndexServiceのインスタンスを使えるようにしている
    }

    @GetMapping // Getされた時の処理 Postは別
    public String index(Model model) {
        model.addAttribute("message", service.getMessage());
        return "index";
    }
}
