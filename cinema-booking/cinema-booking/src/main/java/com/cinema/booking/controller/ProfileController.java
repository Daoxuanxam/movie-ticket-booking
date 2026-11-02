package com.cinema.booking.controller;

import com.cinema.booking.model.User;
import com.cinema.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping({"/profile", "/account"})
    public String showProfile(Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "profile";
    }

    // ── THÊM: Hiển thị form chỉnh sửa ─────────────────────────────────────
    @GetMapping({"/profile/edit", "/account/edit"})
    public String showEditForm(Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "profile-form"; // → templates/profile-form.html
    }

    @PostMapping({"/profile/update", "/account/update"})
    public String updateProfile(
            Authentication auth,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            RedirectAttributes ra) {

        if (auth == null) return "redirect:/login";
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        if (fullName == null || fullName.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Họ tên không được để trống!");
            return "redirect:/profile/edit";
        }

        user.setFullName(fullName.trim());
        user.setPhone(phone != null ? phone.trim() : "");
        userRepository.save(user);

        ra.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        return "redirect:/profile";
    }

    @PostMapping({"/profile/change-password", "/account/change-password"})
    public String changePassword(
            Authentication auth,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes ra) {

        if (auth == null) return "redirect:/login";
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            ra.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
            return "redirect:/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu mới không khớp!");
            return "redirect:/profile";
        }

        if (newPassword.length() < 6) {
            ra.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự!");
            return "redirect:/profile";
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        ra.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        return "redirect:/profile";
    }
}