package com.cinema.booking.controller;

import com.cinema.booking.model.*;
import com.cinema.booking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class BookingController {

    @Autowired private MovieRepository movieRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CinemaRepository cinemaRepository;
    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private DiscountCodeRepository discountCodeRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private BookingSeatRepository bookingSeatRepository;
    @Autowired private PaymentRepository paymentRepository;

    @GetMapping("/booking")
    public String booking(@RequestParam(required = false) Integer movieId,
                          Model model) {
        Movie movie = null;
        if (movieId != null) {
            movie = movieRepository.findById(movieId).orElse(null);
        }
        if (movie == null) {
            movie = movieRepository.findAll().stream().findFirst().orElse(null);
        }
        if (movie == null) {
            movie = new Movie();
            movie.setTitle("Avengers: Doomsday");
            movie.setDuration(149);
            movie.setRated("T18");
            movie.setPosterUrl("");
            movie.setReleaseDate(LocalDate.of(2026, 5, 1));
        }

        // CHỈ lấy rạp đang có suất chiếu cho phim này, thay vì lấy tất cả rạp
        List<Cinema> cinemas = cinemaRepository.findCinemasByMovieId(movie.getId());

        model.addAttribute("movie", movie);
        model.addAttribute("cinemas", cinemas);
        return "booking";
    }

    // API: Lấy danh sách ngày có suất chiếu theo movie + cinema
    @GetMapping("/booking/dates")
    @ResponseBody
    public List<String> getAvailableDates(@RequestParam Integer movieId,
                                          @RequestParam Integer cinemaId) {
        List<java.sql.Date> dates = showtimeRepository
                .findAvailableDatesByMovieAndCinema(movieId, cinemaId);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dates.stream()
                .map(d -> d.toLocalDate().format(fmt))
                .toList();
    }

    // API: Lấy suất chiếu theo movie + cinema + ngày
    @GetMapping("/booking/showtimes")
    @ResponseBody
    public List<ShowtimeDTO> getShowtimes(@RequestParam Integer movieId,
                                          @RequestParam Integer cinemaId,
                                          @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<Showtime> list = showtimeRepository
                .findByMovieIdAndCinemaIdAndDate(movieId, cinemaId, localDate);

        return list.stream().map(s -> {
            ShowtimeDTO dto = new ShowtimeDTO();
            dto.setId(s.getId());
            dto.setTime(s.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            dto.setHallName(s.getHall().getName());
            dto.setHallType(s.getHall().getHallType().getValue());
            dto.setLanguage(s.getLanguage().name());
            return dto;
        }).toList();
    }

    // API: Lấy sơ đồ ghế thật + trạng thái đã bán theo showtimeId
    @GetMapping("/booking/seats")
    @ResponseBody
    public List<SeatDTO> getSeats(@RequestParam Integer showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
        if (showtime == null) return List.of();

        List<Seat> seats = seatRepository.findByHallId(showtime.getHall().getId());
        List<Integer> takenIds = bookingSeatRepository.findTakenSeatIdsByShowtimeId(showtimeId);

        return seats.stream().map(s -> {
            SeatDTO dto = new SeatDTO();
            dto.setId(s.getId());
            dto.setRow(s.getRowLabel());
            dto.setNumber(s.getSeatNumber());
            dto.setType(s.getSeatType());
            dto.setTaken(takenIds.contains(s.getId()));
            return dto;
        }).toList();
    }

    // API: Kiểm tra mã giảm giá
    @GetMapping("/booking/discount")
    @ResponseBody
    public DiscountResponse checkDiscount(@RequestParam String code,
                                           @RequestParam BigDecimal orderTotal) {
        DiscountResponse res = new DiscountResponse();
        var opt = discountCodeRepository.findByCode(code.trim().toUpperCase());

        if (opt.isEmpty()) {
            res.setValid(false);
            res.setMessage("Mã giảm giá không tồn tại");
            return res;
        }

        DiscountCode d = opt.get();
        LocalDateTime now = LocalDateTime.now();

        if (!d.getIsActive()) {
            res.setValid(false);
            res.setMessage("Mã giảm giá đã bị khóa");
            return res;
        }
        if (now.isBefore(d.getValidFrom()) || now.isAfter(d.getValidUntil())) {
            res.setValid(false);
            res.setMessage("Mã giảm giá đã hết hạn");
            return res;
        }
        if (d.getMaxUses() != null && d.getUsedCount() >= d.getMaxUses()) {
            res.setValid(false);
            res.setMessage("Mã giảm giá đã hết lượt sử dụng");
            return res;
        }
        if (orderTotal.compareTo(d.getMinOrder()) < 0) {
            res.setValid(false);
            res.setMessage("Đơn hàng chưa đạt giá trị tối thiểu " +
                    d.getMinOrder().toBigInteger() + "đ");
            return res;
        }

        BigDecimal discountAmount;
        if (d.getDiscountType() == DiscountCode.DiscountType.percent) {
            discountAmount = orderTotal.multiply(d.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
        } else {
            discountAmount = d.getDiscountValue();
            if (discountAmount.compareTo(orderTotal) > 0) {
                discountAmount = orderTotal;
            }
        }

        res.setValid(true);
        res.setDiscountType(d.getDiscountType().name());
        res.setDiscountValue(d.getDiscountValue());
        res.setDiscountAmount(discountAmount);
        res.setMessage("Áp dụng thành công");
        return res;
    }

    // DTO suất chiếu
    public static class ShowtimeDTO {
        private Integer id;
        private String time;
        private String hallName;
        private String hallType;
        private String language;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public String getHallName() { return hallName; }
        public void setHallName(String hallName) { this.hallName = hallName; }
        public String getHallType() { return hallType; }
        public void setHallType(String hallType) { this.hallType = hallType; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    // DTO ghế
    public static class SeatDTO {
        private Integer id;
        private String row;
        private Integer number;
        private String type;
        private boolean taken;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getRow() { return row; }
        public void setRow(String row) { this.row = row; }
        public Integer getNumber() { return number; }
        public void setNumber(Integer number) { this.number = number; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public boolean isTaken() { return taken; }
        public void setTaken(boolean taken) { this.taken = taken; }
    }

    // DTO kết quả kiểm tra mã giảm giá
    public static class DiscountResponse {
        private boolean valid;
        private String message;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal discountAmount;

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getDiscountType() { return discountType; }
        public void setDiscountType(String discountType) { this.discountType = discountType; }
        public BigDecimal getDiscountValue() { return discountValue; }
        public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    }

    // Endpoint lưu booking khi xác nhận thanh toán
    @PostMapping("/booking/confirm")
    public String confirmBooking(
            Authentication auth,
            @RequestParam Integer movieId,
            @RequestParam Integer showtimeId,
            @RequestParam String bookingCode,
            @RequestParam String showDate,
            @RequestParam String showTime,
            @RequestParam String hallName,
            @RequestParam String seats,
            @RequestParam(required = false) List<Integer> seatIds,
            @RequestParam(required = false) List<String> seatTypes,
            @RequestParam(required = false) List<BigDecimal> seatPrices,
            @RequestParam BigDecimal totalAmount,
            @RequestParam(required = false, defaultValue = "0") BigDecimal discountAmount,
            @RequestParam(required = false, defaultValue = "momo") String paymentMethod,
            Model model) {

        if (auth == null) return "redirect:/login";
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
        if (showtime == null) return "redirect:/booking?movieId=" + movieId;

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setBookingCode(bookingCode);
        booking.setSubtotal(totalAmount.add(discountAmount));
        booking.setDiscountAmount(discountAmount);
        booking.setTotalAmount(totalAmount);
        booking.setPaymentMethod(paymentMethod);
        booking.setStatus(Booking.BookingStatus.confirmed);
        booking.setPaymentStatus(Booking.PaymentStatus.paid);

        booking.setMovieTitle(movieRepository.findById(movieId)
                .map(Movie::getTitle).orElse("Phim"));
        booking.setShowDate(showDate);
        booking.setShowTime(showTime);
        booking.setHallName(hallName);
        booking.setSeats(seats);

        Booking saved = bookingRepository.save(booking);

        // Lưu chi tiết ghế vào booking_seats để biết ghế nào đã bị chiếm
        if (seatIds != null) {
            for (int i = 0; i < seatIds.size(); i++) {
                Seat seat = seatRepository.findById(seatIds.get(i)).orElse(null);
                if (seat == null) continue;
                BookingSeat bs = new BookingSeat();
                bs.setBooking(saved);
                bs.setSeat(seat);
                bs.setSeatType(seatTypes != null && seatTypes.size() > i ? seatTypes.get(i) : seat.getSeatType());
                bs.setPrice(seatPrices != null && seatPrices.size() > i ? seatPrices.get(i) : BigDecimal.ZERO);
                bookingSeatRepository.save(bs);
            }
        }

        // Lưu thông tin giao dịch vào bảng payments
        Payment payment = new Payment();
        payment.setBooking(saved);
        payment.setPaymentCode("PAY_" + bookingCode);
        payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "momo");
        payment.setAmount(totalAmount);
        payment.setStatus(Payment.PaymentStatus.success);
        payment.setGatewayTransactionId("TXN_" + System.currentTimeMillis());
        payment.setBankCode("APP");
        payment.setResponseCode("00");
        payment.setPayDate(LocalDateTime.now());
        paymentRepository.save(payment);

        return "redirect:/booking/history";
    }
}