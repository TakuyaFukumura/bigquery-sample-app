package com.example.myapplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 共通のセキュリティ設定クラス
 * 全プロファイルで共通して使用されるBeanを定義します
 */
@Configuration
public class CommonSecurityConfig {

    /**
     * パスワードエンコーダーの設定
     * 全プロファイルで共通して使用されます
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
