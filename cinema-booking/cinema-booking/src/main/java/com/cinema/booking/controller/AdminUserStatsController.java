package com.cinema.booking.controller;

import com.cinema.booking.model.User;
import com.cinema.booking.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users/stats")
public class AdminUserStatsController {

    private final UserRepository userRepository;

    public AdminUserStatsController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String getStats(@RequestParam(required = false) Integer year, Model model) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long inactiveUsers = userRepository.countByIsActiveFalse();
        long adminUsers = userRepository.countByRole(User.Role.admin);
        long customerUsers = userRepository.countByRole(User.Role.customer);

        List<Object[]> registrationsList = userRepository.getUserRegistrationsByMonth(year);

        String chartLabels = registrationsList.stream()
                .map(row -> "\"" + row[0] + "\"")
                .collect(Collectors.joining(","));

        String chartValues = registrationsList.stream()
                .map(row -> row[1].toString())
                .collect(Collectors.joining(","));

        model.addAttribute("year", year);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("inactiveUsers", inactiveUsers);
        model.addAttribute("adminUsers", adminUsers);
        model.addAttribute("customerUsers", customerUsers);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartValues", chartValues);

        return "admin/user/stats";
    }
}
