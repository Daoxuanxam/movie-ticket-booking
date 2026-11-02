package com.cinema.booking.controller;

import com.cinema.booking.model.User;
import com.cinema.booking.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    // Hiển thị danh sách user
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/user/list";
    }

    // Hiển thị form thêm
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/user/form";
    }

    // Hiển thị form sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable @NonNull Integer id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        model.addAttribute("user", user);
        return "admin/user/form";
    }

    // Hiển thị chi tiết user
    @GetMapping("/details/{id}")
    public String showUserDetails(@PathVariable @NonNull Integer id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        model.addAttribute("user", user);
        return "admin/user/details";
    }

    // Xử lý thêm/sửa
    @PostMapping("/save")
    public String saveUser(
            @ModelAttribute @NonNull User user,
            @RequestParam(value = "role", required = false) String roleStr,
            @RequestParam(value = "isActive", required = false) String isActiveStr,
            Model model) {

        // Bind role
        if (roleStr != null && !roleStr.isEmpty()) {
            try {
                user.setRole(User.Role.valueOf(roleStr));
            } catch (IllegalArgumentException e) {
                user.setRole(User.Role.customer);
            }
        }

        // Bind isActive
        user.setIsActive(isActiveStr != null && isActiveStr.equals("true"));

        // Nếu sửa: giữ lại createdAt cũ
        if (user.getId() != null) {
            userRepository.findById(user.getId()).ifPresent(existing ->
                user.setCreatedAt(existing.getCreatedAt())
            );
        }

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // Email bị trùng hoặc vi phạm constraint khác
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", "Email \"" + user.getEmail() + "\" đã tồn tại. Vui lòng dùng email khác.");
            return "admin/user/form";
        }

        return "redirect:/admin/users";
    }

    // Xoá user (soft delete)
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable @NonNull Integer id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setIsActive(false);
            userRepository.save(user);
        });
        return "redirect:/admin/users";
    }

    // API JSON
    @GetMapping("/api")
    @ResponseBody
    public List<User> apiGetAllUsers() {
        return userRepository.findAll();
    }
}