package com.cinema.booking.controller;
import com.cinema.booking.model.Movie;
import com.cinema.booking.model.Cinema;
import com.cinema.booking.repository.MovieRepository;
import com.cinema.booking.repository.CinemaRepository;
import com.cinema.booking.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.stream.Collectors;
@Controller
public class HomeController {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private MenuService menuService;
    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        List<Movie> nowShowing = movieRepository.findAll().stream()
            .filter(m -> m.getStatus() == Movie.Status.now_showing)
            .limit(3)
            .collect(Collectors.toList());
        List<Cinema> cinemas = cinemaRepository.findCinemasWithShowtimes();
        model.addAttribute("title", "Cinema Booking System");
        model.addAttribute("welcomeMessage", "Khám phá hàng trăm bộ phim hay đang chiếu. Đặt vé nhanh, thanh toán tiện lợi.");
        model.addAttribute("nowShowing", nowShowing);
        model.addAttribute("cinemas", cinemas);
        model.addAttribute("menus", menuService.getMenuTree());   // thêm menu động
        return "home/index";
    }
    @GetMapping("/home")
    public String homePage(Model model, Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        List<Movie> nowShowing = movieRepository.findAll().stream()
            .filter(m -> m.getStatus() == Movie.Status.now_showing)
            .limit(3)
            .collect(Collectors.toList());
        List<Cinema> cinemas = cinemaRepository.findCinemasWithShowtimes();
        model.addAttribute("title", "Home");
        model.addAttribute("nowShowing", nowShowing);
        model.addAttribute("cinemas", cinemas);
        model.addAttribute("menus", menuService.getMenuTree());   // thêm menu động
        return "home/index";
    }
}