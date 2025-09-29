package com.example.myapplication.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;

/**
 * AI機能を提供するサービスクラス
 * Gemini APIを使用して豆知識を取得する
 */
@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.gemini.api-key:}")
    private String apiKey;

    @Value("${app.ai.gemini.model:gemini-2.5-flash-lite}")
    private String model;

    public AiService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Gemini APIを使用して豆知識を取得する
     *
     * @return 豆知識の文字列
     * @throws RuntimeException API呼び出しに失敗した場合
     */
    public String getTrivia() {
        // APIキーが設定されていない場合は例外をスローする
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("Gemini APIキーが設定されていません");
            throw new IllegalStateException("APIキーが設定されていません。");
        }

        try {
            Map<String, Object> requestBody = createGeminiRequestBody();

            String response = webClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent")
                    .header("Content-Type", "application/json")
                    .header("X-Goog-Api-Key", apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return parseGeminiResponse(response);

        } catch (WebClientResponseException e) {
            logger.error("Gemini API呼び出しでHTTPエラーが発生", e);
            throw new RuntimeException("API呼び出しに失敗しました。");
        } catch (Exception e) {
            logger.error("Gemini API呼び出しで予期しないエラーが発生", e);
            throw new RuntimeException("API呼び出しで予期しないエラーが発生しました。");
        }
    }

    /**
     * Gemini API用のリクエストボディを生成する
     *
     * @return Gemini APIに送信するリクエストボディ（Map形式）
     */
    private static Map<String, Object> createGeminiRequestBody() {
        String prompt = "100文字程度の日本語で豆知識を教えてください。";

        // Gemini API リクエストボディの構築
        return Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                },
                "generationConfig", Map.of(
                        "temperature", 2.0, // 値が大きいとAI応答がランダムになる：設定幅 0.0 ~ 2.0
                        "maxOutputTokens", 200
                )
        );
    }

    /**
     * Gemini APIのレスポンスから豆知識テキストを抽出する
     *
     * @param response Gemini APIからのJSONレスポンス
     * @return 抽出された豆知識テキスト
     */
    private String parseGeminiResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.get("candidates");

            if (candidates != null && candidates.isArray() && !candidates.isEmpty()) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.get("content");

                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && !parts.isEmpty()) {
                        JsonNode text = parts.get(0).get("text");
                        if (text != null) {
                            return text.asText().trim();
                        }
                    }
                }
            }

            logger.warn("Gemini APIレスポンスの解析に失敗しました。期待される形式ではありません: {}", response);
            throw new RuntimeException("AIからの応答を解析できませんでした。期待される形式ではありません。");

        } catch (Exception e) {
            logger.error("Gemini APIレスポンスの解析中にエラーが発生", e);
            throw new RuntimeException("AIからの応答を解析できませんでした。");
        }
    }
}
