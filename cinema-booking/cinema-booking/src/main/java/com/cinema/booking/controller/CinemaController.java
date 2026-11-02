package com.cinema.booking.controller;
import com.cinema.booking.model.Cinema;
import com.cinema.booking.repository.CinemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Controller
public class CinemaController {
    @Autowired
    private CinemaRepository cinemaRepository;
    @GetMapping("/cinemas")
    public String cinemaList(Model model) {
        List<Cinema> cinemas = cinemaRepository.findCinemasWithShowtimes();
        // Nhóm theo thành phố
        Map<String, List<Cinema>> byCity = cinemas.stream()
                .collect(Collectors.groupingBy(Cinema::getCity));
        model.addAttribute("cinemas", cinemas);
        model.addAttribute("byCity", byCity);
        return "cinemas";
    }
}