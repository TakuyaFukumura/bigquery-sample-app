package com.example.myapplication.controller;

import com.example.myapplication.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * AI機能サンプル画面を制御するコントローラ
 * Gemini APIを使用した豆知識取得機能を提供
 */
@Controller
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    @Autowired
    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * AI機能サンプル画面を表示
     *
     * @return AI機能サンプルテンプレート名
     */
    @GetMapping
    public String aiSample() {
        return "ai-sample";
    }

    /**
     * 豆知識を取得してAI機能サンプル画面に表示
     *
     * @param model ビューに渡すデータモデル
     * @return AI機能サンプルテンプレート名
     */
    @PostMapping("/trivia")
    public String getTrivia(Model model) {
        try {
            String trivia = aiService.getTrivia();
            model.addAttribute("trivia", trivia);
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("error", "豆知識の取得に失敗しました: " + e.getMessage());
            model.addAttribute("success", false);
        }
        return "ai-sample";
    }
}
