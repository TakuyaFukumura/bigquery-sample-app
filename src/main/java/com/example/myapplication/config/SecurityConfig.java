package com.example.myapplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security設定クラス
 * 認証・認可の設定を行います（データベースベース認証）
 * 注意：'dev'プロファイルが有効な場合は、DevSecurityConfigが優先されます
 */
@Configuration
@EnableWebSecurity
@Profile("!dev")
public class SecurityConfig {

    /**
     * セキュリティフィルターチェーンの設定
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 認可設定
                .authorizeHttpRequests(authz -> authz
                        // H2コンソールは開発用のため認証不要
                        .requestMatchers("/h2-console/**").permitAll()
                        // Actuatorヘルスエンドポイントは認証不要
                        .requestMatchers("/actuator/health").permitAll()
                        // ログインページは認証不要
                        .requestMatchers("/login").permitAll()
                        // ユーザー登録関連のエンドポイントは認証不要
                        .requestMatchers("/register", "/register/**").permitAll()
                        // その他のすべてのリクエストは認証が必要
                        .anyRequest().authenticated()
                )
                // ログイン設定
                .formLogin(form -> form
                        .loginPage("/login")  // カスタムログインページ
                        .defaultSuccessUrl("/", true) // ログイン成功時のリダイレクト先
                        .permitAll()
                )
                // ログアウト設定
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout") // ログアウト成功時のリダイレクト先
                        .permitAll()
                )
                // H2コンソール用の設定（フレームとCSRFを無効化）
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                );

        return http.build();
    }
}
