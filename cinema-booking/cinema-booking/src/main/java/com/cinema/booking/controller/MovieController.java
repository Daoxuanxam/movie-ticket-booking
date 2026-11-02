package com.cinema.booking.controller;

import com.cinema.booking.model.Cinema;
import com.cinema.booking.model.Movie;
import com.cinema.booking.repository.CinemaRepository;
import com.cinema.booking.repository.MovieRepository;
import com.cinema.booking.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ShowtimeRepository showtimeRepository;
    @Autowired
    private CinemaRepository cinemaRepository;

    // ===== DANH SÁCH TẤT CẢ PHIM =====
    @GetMapping("/movies")
    public String getAllMovies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer cinemaId,
            Model model) {

        List<Movie> movies = movieRepository.findAll().stream()
            .filter(m -> search == null || search.isEmpty() ||
                    m.getTitle().toLowerCase().contains(search.toLowerCase()))
            .filter(m -> status == null || status.isEmpty() ||
                    m.getStatus().name().equals(status))
            .collect(Collectors.toList());

        // Lọc theo rạp nếu có cinemaId
        String cinemaName = null;
        if (cinemaId != null) {
            List<Integer> movieIds = showtimeRepository.findMovieIdsByCinemaId(cinemaId);
            movies = movies.stream()
                .filter(m -> movieIds.contains(m.getId()))
                .collect(Collectors.toList());

            cinemaName = cinemaRepository.findById(cinemaId)
                    .map(Cinema::getName).orElse(null);
        }

        List<Movie> nowShowing = movies.stream()
            .filter(m -> m.getStatus() == Movie.Status.now_showing)
            .collect(Collectors.toList());
        List<Movie> comingSoon = movies.stream()
            .filter(m -> m.getStatus() == Movie.Status.coming_soon)
            .collect(Collectors.toList());

        model.addAttribute("movies", movies);
        model.addAttribute("nowShowing", nowShowing);
        model.addAttribute("comingSoon", comingSoon);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("status", status != null ? status : "");
        model.addAttribute("cinemaId", cinemaId);
        model.addAttribute("cinemaName", cinemaName);
        model.addAttribute("pageTitle", cinemaName != null
                ? "Phim tại " + cinemaName
                : "Tất cả phim");
        model.addAttribute("activeTab", "all");
        return "movies";
    }

    // ===== PHIM ĐANG CHIẾU =====
    @GetMapping("/movies/now-showing")
    public String nowShowing(Model model) {
        List<Movie> movies = movieRepository.findAll().stream()
            .filter(m -> m.getStatus() == Movie.Status.now_showing)
            .collect(Collectors.toList());

        model.addAttribute("movies", movies);
        model.addAttribute("nowShowing", movies);
        model.addAttribute("comingSoon", List.of());
        model.addAttribute("search", "");
        model.addAttribute("status", "now_showing");
        model.addAttribute("cinemaId", null);
        model.addAttribute("cinemaName", null);
        model.addAttribute("pageTitle", "Phim đang chiếu");
        model.addAttribute("activeTab", "now_showing");
        return "movies";
    }

    // ===== PHIM SẮP CHIẾU =====
    @GetMapping("/movies/coming-soon")
    public String comingSoon(Model model) {
        List<Movie> movies = movieRepository.findAll().stream()
            .filter(m -> m.getStatus() == Movie.Status.coming_soon)
            .collect(Collectors.toList());

        model.addAttribute("movies", movies);
        model.addAttribute("nowShowing", List.of());
        model.addAttribute("comingSoon", movies);
        model.addAttribute("search", "");
        model.addAttribute("status", "coming_soon");
        model.addAttribute("cinemaId", null);
        model.addAttribute("cinemaName", null);
        model.addAttribute("pageTitle", "Phim sắp chiếu");
        model.addAttribute("activeTab", "coming_soon");
        return "movies";
    }

    // ===== TÌM KIẾM PHIM =====
    @GetMapping("/movies/search")
    public String searchMovies(
            @RequestParam(required = false) String q,
            Model model) {

        List<Movie> movies = movieRepository.findAll().stream()
            .filter(m -> q == null || q.isEmpty() ||
                    m.getTitle().toLowerCase().contains(q.toLowerCase()) ||
                    (m.getActors() != null && m.getActors().toLowerCase().contains(q.toLowerCase())))
            .collect(Collectors.toList());

        List<Movie> nowShowing = movies.stream()
            .filter(m -> m.getStatus() == Movie.Status.now_showing)
            .collect(Collectors.toList());
        List<Movie> comingSoon = movies.stream()
            .filter(m -> m.getStatus() == Movie.Status.coming_soon)
            .collect(Collectors.toList());

        model.addAttribute("movies", movies);
        model.addAttribute("nowShowing", nowShowing);
        model.addAttribute("comingSoon", comingSoon);
        model.addAttribute("search", q != null ? q : "");
        model.addAttribute("status", "");
        model.addAttribute("cinemaId", null);
        model.addAttribute("cinemaName", null);
        model.addAttribute("pageTitle", "Kết quả tìm kiếm: " + (q != null ? q : ""));
        model.addAttribute("activeTab", "all");
        return "movies";
    }

    // ===== CHI TIẾT PHIM =====
    @GetMapping("/movies/{id}")
    public String movieDetail(@PathVariable @NonNull Integer id, Model model) {
        Movie movie = movieRepository.findById(id).orElse(null);
        if (movie == null) return "redirect:/movies";

        List<Movie> related = movieRepository.findAll().stream()
            .filter(m -> !m.getId().equals(id))
            .filter(m -> m.getStatus() == movie.getStatus())
            .limit(4)
            .collect(Collectors.toList());

        model.addAttribute("movie", movie);
        model.addAttribute("related", related);
        return "movie-detail";
    }
}