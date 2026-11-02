package com.cinema.booking.controller;

import com.cinema.booking.model.Booking;
import com.cinema.booking.repository.BookingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    @Autowired
    private BookingRepository bookingRepository;

    // 10.1 Danh sách
    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Model model
    ) {
        Booking.BookingStatus statusEnum = null;

        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = Booking.BookingStatus.valueOf(status);
            } catch (Exception ignored) {}
        }

        List<Booking> bookings = bookingRepository.search(keyword, statusEnum);

        model.addAttribute("bookings", bookings);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("statuses", Booking.BookingStatus.values());

        return "admin/booking/list";
    }

    // Chi tiết
    @GetMapping("/{id}")
    public String detail(@PathVariable @NonNull Integer id, Model model) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return "redirect:/admin/bookings";

        model.addAttribute("booking", booking);
        return "admin/booking/detail";
    }

    // 10.2 Hủy vé
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable @NonNull Integer id, RedirectAttributes ra) {
        Booking b = bookingRepository.findById(id).orElse(null);

        if (b == null) {
            ra.addFlashAttribute("error", "Không tìm thấy vé");
            return "redirect:/admin/bookings";
        }

        if (b.getStatus() == Booking.BookingStatus.cancelled) {
            ra.addFlashAttribute("error", "Vé đã bị hủy");
            return "redirect:/admin/bookings/" + id;
        }

        b.setStatus(Booking.BookingStatus.cancelled);
        b.setCancelledAt(LocalDateTime.now());
        b.setCancelReason("Admin hủy");

        bookingRepository.save(b);

        ra.addFlashAttribute("success", "Hủy vé thành công");
        return "redirect:/admin/bookings/" + id;
    }

    @PostMapping("/{id}/refund")
    public String refund(@PathVariable @NonNull Integer id, RedirectAttributes ra) {
        Booking b = bookingRepository.findById(id).orElse(null);

        if (b == null) {
            ra.addFlashAttribute("error", "Không tìm thấy vé");
            return "redirect:/admin/bookings";
        }

        if (b.getPaymentStatus() != Booking.PaymentStatus.paid) {
            ra.addFlashAttribute("error", "Chưa thanh toán");
            return "redirect:/admin/bookings/" + id;
        }

        if (b.getPaymentStatus() == Booking.PaymentStatus.refunded) {
            ra.addFlashAttribute("error", "Đã hoàn tiền");
            return "redirect:/admin/bookings/" + id;
        }

        b.setStatus(Booking.BookingStatus.refunded);
        b.setPaymentStatus(Booking.PaymentStatus.refunded);
        b.setCancelledAt(LocalDateTime.now());

        bookingRepository.save(b);

        ra.addFlashAttribute("success", "Hoàn tiền thành công");
        return "redirect:/admin/bookings/" + id;
    }
}