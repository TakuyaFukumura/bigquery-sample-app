package com.example.myapplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ユーザー登録用のデータ転送オブジェクト（DTO）
 * フォームからのデータを受け取るためのクラス
 */
@Data
public class UserRegistrationDto {

    @NotBlank(message = "ユーザー名は必須です")
    @Size(min = 3, max = 50, message = "ユーザー名は3文字以上50文字以下で入力してください")
    private String username;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 6, message = "パスワードは6文字以上で入力してください")
    private String password;

    @NotBlank(message = "パスワード確認は必須です")
    private String confirmPassword;

    /**
     * パスワードと確認パスワードが一致するかチェック
     *
     * @return パスワードが一致する場合true
     */
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
