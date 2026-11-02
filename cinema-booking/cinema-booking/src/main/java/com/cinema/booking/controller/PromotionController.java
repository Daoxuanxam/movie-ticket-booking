package com.cinema.booking.controller;

import com.cinema.booking.model.DiscountCode;
import com.cinema.booking.repository.DiscountCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class PromotionController {

    @Autowired private DiscountCodeRepository discountCodeRepository;

    @GetMapping("/promotions")
    public String promotions(Model model) {
        LocalDateTime now = LocalDateTime.now();

        List<DiscountCode> promotions = discountCodeRepository.findAll().stream()
                .filter(d -> d.getIsActive())
                .filter(d -> !now.isBefore(d.getValidFrom()) && !now.isAfter(d.getValidUntil()))
                .filter(d -> d.getMaxUses() == null || d.getUsedCount() < d.getMaxUses())
                .toList();

        model.addAttribute("promotions", promotions);
        return "promotions";
    }
}