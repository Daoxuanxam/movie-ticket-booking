package com.cinema.booking.controller;

import com.cinema.booking.model.Booking;
import com.cinema.booking.model.User;
import com.cinema.booking.repository.BookingRepository;
import com.cinema.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class BookingHistoryController {

    @Autowired private UserRepository userRepository;
    @Autowired private BookingRepository bookingRepository;

    @GetMapping("/booking/history")
    public String bookingHistory(Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        List<Booking> bookings = bookingRepository.findByUserOrderByCreatedAtDesc(user);

        // Tính tổng ở Java, không tính trong Thymeleaf
        long confirmedCount = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.confirmed)
                .count();
        BigDecimal totalSpent = bookings.stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("totalSpent", totalSpent);
        return "booking-history";
    }
}