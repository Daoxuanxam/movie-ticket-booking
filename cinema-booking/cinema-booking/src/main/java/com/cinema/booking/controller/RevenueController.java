package com.cinema.booking.controller;

import com.cinema.booking.repository.BookingRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/revenue")
public class RevenueController {

    private final BookingRepository bookingRepository;

    public RevenueController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "day") String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
            @RequestParam(required = false) Integer year,
            Model model
    ) {
        // Giá trị mặc định
        LocalDate today = LocalDate.now();
        if (from == null) from = today.withDayOfMonth(1);
        if (to   == null) to   = today;
        if (year == null) year = today.getYear();

        List<Object[]> revenueList;
        
        if ("month".equals(type)) {
            // Lấy dữ liệu số tháng từ DB (Ví dụ: row[0] là số 6)
            List<Object[]> rawList = bookingRepository.getRevenueByMonth(year);
            
            // Map lại dữ liệu để chuyển số thành chữ "Tháng X", giữ nguyên giao diện Thymeleaf
            revenueList = rawList.stream().map(row -> new Object[]{
                    "Tháng " + row[0], // Chuyển số 6 thành "Tháng 6"
                    row[1],            // totalBookings
                    row[2],            // totalRevenue
                    row[3]             // totalDiscount
            }).collect(Collectors.toList());
            
        } else {
            LocalDateTime fromDT = from.atStartOfDay();
            LocalDateTime toDT   = to.atTime(23, 59, 59);
            revenueList = bookingRepository.getRevenueByDay(fromDT, toDT);
        }

        // Tính tổng doanh thu cho summary card
        BigDecimal totalRevenue = revenueList.stream()
                .map(row -> row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalBookings = revenueList.stream()
                .mapToLong(row -> row[1] != null ? ((Number) row[1]).longValue() : 0L)
                .sum();

        // Chuỗi JSON cho Chart.js
        String chartLabels = revenueList.stream()
                .map(row -> "\"" + row[0] + "\"")
                .collect(Collectors.joining(","));

        String chartValues = revenueList.stream()
                .map(row -> row[2] != null ? ((BigDecimal) row[2]).toPlainString() : "0")
                .collect(Collectors.joining(","));

        model.addAttribute("type",          type);
        model.addAttribute("from",          from);
        model.addAttribute("to",            to);
        model.addAttribute("year",          year);
        model.addAttribute("revenueList",   revenueList);
        model.addAttribute("totalRevenue",  totalRevenue);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("chartLabels",   chartLabels);
        model.addAttribute("chartValues",   chartValues);

        return "admin/revenue/revenue";
    }

    @GetMapping("/export")
    public void exportCsv(
            @RequestParam(defaultValue = "day") String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
            @RequestParam(required = false) Integer year,
            jakarta.servlet.http.HttpServletResponse response
    ) throws java.io.IOException {
        // Giá trị mặc định
        LocalDate today = LocalDate.now();
        if (from == null) from = today.withDayOfMonth(1);
        if (to   == null) to   = today;
        if (year == null) year = today.getYear();

        List<Object[]> revenueList;
        String fileName;

        if ("month".equals(type)) {
            List<Object[]> rawList = bookingRepository.getRevenueByMonth(year);
            revenueList = rawList.stream().map(row -> new Object[]{
                    "Tháng " + row[0],
                    row[1],
                    row[2],
                    row[3]
            }).collect(Collectors.toList());
            fileName = "bao-cao-doanh-thu-theo-thang-" + year + ".csv";
        } else {
            LocalDateTime fromDT = from.atStartOfDay();
            LocalDateTime toDT   = to.atTime(23, 59, 59);
            revenueList = bookingRepository.getRevenueByDay(fromDT, toDT);
            fileName = "bao-cao-doanh-thu-theo-ngay-" + from + "-den-" + to + ".csv";
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // Write UTF-8 BOM so Excel opens it with correct encoding
        response.getOutputStream().write(new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF });

        java.io.PrintWriter writer = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(response.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8)
        );

        // Header
        writer.println("Thời gian,Số lượng đơn,Doanh thu (₫),Giảm giá (₫)");

        for (Object[] row : revenueList) {
            String time = row[0] != null ? row[0].toString() : "";
            String bookings = row[1] != null ? row[1].toString() : "0";
            String revenue = row[2] != null ? ((BigDecimal) row[2]).toPlainString() : "0";
            String discount = row[3] != null ? ((BigDecimal) row[3]).toPlainString() : "0";

            writer.println(time + "," + bookings + "," + revenue + "," + discount);
        }

        writer.flush();
    }
}