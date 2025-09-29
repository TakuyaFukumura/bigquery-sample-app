package com.example.myapplication.service;

import com.example.myapplication.dto.UserRegistrationDto;
import com.example.myapplication.entity.User;
import com.example.myapplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * データベースベースのユーザー認証サービス
 * Spring SecurityのUserDetailsServiceを実装してデータベースからユーザー情報を取得
 * ユーザー登録機能も提供
 */
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<SimpleGrantedAuthority> authorities = Arrays.stream(user.getRoles().split(","))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                .toList();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!user.getEnabled())
                .build();
    }

    /**
     * 新規ユーザーを作成する
     *
     * @param registrationDto 登録フォームからのデータ
     * @return 作成されたユーザーエンティティ
     * @throws IllegalArgumentException ユーザー名が既に存在する場合
     */
    public User createUser(UserRegistrationDto registrationDto) {
        // ユーザー名の重複チェック
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("ユーザー名 '" + registrationDto.getUsername() + "' は既に使用されています");
        }

        // パスワードをエンコード
        String encodedPassword = passwordEncoder.encode(registrationDto.getPassword());

        // 新規ユーザー作成（デフォルトでUSERロール、有効状態）
        User newUser = new User(
                registrationDto.getUsername(),
                encodedPassword,
                "USER"
        );

        return userRepository.save(newUser);
    }

    /**
     * ユーザー名の存在チェック
     *
     * @param username チェックするユーザー名
     * @return ユーザー名が既に存在する場合true
     */
    public boolean isUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
