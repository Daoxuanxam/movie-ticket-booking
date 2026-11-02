package com.cinema.booking.controller;

import com.cinema.booking.model.User;
import com.cinema.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ===== ĐĂNG KÝ =====
    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            jakarta.servlet.http.HttpSession session,
            Model model) {

        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email này đã được đăng ký.");
            return "register";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp.");
            return "register";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
            return "register";
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(User.Role.customer);
        user.setIsActive(true);
        userRepository.save(user);

        session.setAttribute("registerSuccessMessage", "Đăng ký thành công! Hãy đăng nhập.");
        return "redirect:/login";
    }

    // ===== ĐĂNG NHẬP =====
    @GetMapping("/login")
    public String showLogin(
            jakarta.servlet.http.HttpServletRequest request,
            Model model) {

        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            String loginErrorMessage = (String) session.getAttribute("loginErrorMessage");
            if (loginErrorMessage != null) {
                model.addAttribute("error", loginErrorMessage);
                session.removeAttribute("loginErrorMessage");
            }

            String registerSuccessMessage = (String) session.getAttribute("registerSuccessMessage");
            if (registerSuccessMessage != null) {
                model.addAttribute("success", registerSuccessMessage);
                session.removeAttribute("registerSuccessMessage");
            }

            String logoutSuccessMessage = (String) session.getAttribute("logoutSuccessMessage");
            if (logoutSuccessMessage != null) {
                model.addAttribute("success", logoutSuccessMessage);
                session.removeAttribute("logoutSuccessMessage");
            }
        }
        return "login";
    }
}