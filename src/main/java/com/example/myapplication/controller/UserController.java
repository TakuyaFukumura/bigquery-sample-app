package com.example.myapplication.controller;

import com.example.myapplication.dto.UserRegistrationDto;
import com.example.myapplication.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ユーザー登録処理を担当するコントローラー
 * 登録フォームの表示と登録処理を行います
 */
@Controller
@RequestMapping("/register")
public class UserController {

    private static final String REGISTER_VIEW = "register";

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * ユーザー登録フォームを表示
     *
     * @param model Thymeleafモデル
     * @return 登録フォームテンプレート名
     */
    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("userRegistration", new UserRegistrationDto());
        return REGISTER_VIEW;
    }

    /**
     * ユーザー登録処理
     *
     * @param userRegistration   登録フォームデータ
     * @param result             バリデーション結果
     * @param model              Thymeleafモデル
     * @param redirectAttributes リダイレクト時の属性
     * @return リダイレクト先またはフォーム表示
     */
    @PostMapping
    public String registerUser(@Valid @ModelAttribute("userRegistration") UserRegistrationDto userRegistration,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // パスワード一致チェック
        if (!userRegistration.isPasswordMatching()) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "パスワードが一致しません");
        }

        // バリデーションエラーがある場合はフォームに戻る
        if (result.hasErrors()) {
            return REGISTER_VIEW;
        }

        try {
            // ユーザー作成
            userService.createUser(userRegistration);

            // 成功メッセージを設定してログイン画面にリダイレクト
            redirectAttributes.addFlashAttribute("successMessage",
                    "ユーザー登録が完了しました。ログインしてください。");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            // ユーザー名重複エラー
            result.rejectValue("username", "error.username", e.getMessage());
            return REGISTER_VIEW;

        } catch (Exception e) {
            // その他のエラー
            model.addAttribute("errorMessage", "登録中にエラーが発生しました。再度お試しください。");
            return REGISTER_VIEW;
        }
    }
}
