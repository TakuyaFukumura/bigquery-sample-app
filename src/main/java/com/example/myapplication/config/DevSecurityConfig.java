package com.example.myapplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 開発用のSpring Security設定クラス
 * 'dev'プロファイルが有効な場合に認証を無効化します
 */
@Configuration
@EnableWebSecurity
@Profile("dev")
public class DevSecurityConfig {

    /**
     * 開発用セキュリティフィルターチェーンの設定
     * すべてのリクエストに対して認証を無効化します
     */
    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
                // 全てのリクエストを認証なしで許可
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()
                )
                // H2コンソール用の設定（フレームとCSRFを無効化）
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                // CSRF保護を完全に無効化（開発用）
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
