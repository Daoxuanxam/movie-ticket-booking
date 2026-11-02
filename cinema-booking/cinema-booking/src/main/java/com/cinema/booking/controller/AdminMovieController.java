package com.cinema.booking.controller;

import com.cinema.booking.model.Movie;
import com.cinema.booking.model.Cinema;
import com.cinema.booking.model.Hall;
import com.cinema.booking.model.Showtime;
import com.cinema.booking.repository.MovieRepository;
import com.cinema.booking.repository.CinemaRepository;
import com.cinema.booking.repository.HallRepository;
import com.cinema.booking.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/movies") // ← prefix /admin/movies cho tất cả
public class AdminMovieController {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    // Danh sách
    @GetMapping
    public String listMovies(Model model) {
        model.addAttribute("movies", movieRepository.findAll());
        return "admin/movie/list";
    }

    // Form thêm
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("movie", new Movie());
        return "admin/movie/form";
    }

    // Form sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable @NonNull Integer id, Model model) {
        Movie movie = movieRepository.findById(id).orElse(new Movie());
        model.addAttribute("movie", movie);
        
        if (movie.getId() != null) {
            model.addAttribute("showtimes", showtimeRepository.findByMovieId(id));
            model.addAttribute("cinemas", cinemaRepository.findByIsActiveTrue());
        }
        return "admin/movie/form";
    }

    // API lấy danh sách phòng chiếu thuộc rạp
    @GetMapping("/halls")
    @ResponseBody
    public List<Hall> getHallsByCinema(@RequestParam Integer cinemaId) {
        return hallRepository.findByCinemaIdAndIsActiveTrue(cinemaId);
    }

    // Thêm suất chiếu mới
    @PostMapping("/{movieId}/showtimes/add")
    public String addShowtime(
            @PathVariable Integer movieId,
            @RequestParam Integer hallId,
            @RequestParam String startTimeStr,
            @RequestParam Showtime.Language language,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (startTimeStr == null || startTimeStr.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Thời gian bắt đầu không được để trống!");
                return "redirect:/admin/movies/edit/" + movieId;
            }
            
            Movie movie = movieRepository.findById(movieId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim với ID: " + movieId));
            Hall hall = hallRepository.findById(hallId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu với ID: " + hallId));
            
            Showtime showtime = new Showtime();
            showtime.setMovie(movie);
            showtime.setHall(hall);
            
            java.time.LocalDateTime startTime = java.time.LocalDateTime.parse(startTimeStr);
            showtime.setStartTime(startTime);
            showtime.setEndTime(startTime.plusMinutes(movie.getDuration()));
            showtime.setLanguage(language);
            showtime.setStatus(Showtime.Status.active);
            
            showtimeRepository.save(showtime);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm suất chiếu mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm suất chiếu: " + e.getMessage());
        }
        
        return "redirect:/admin/movies/edit/" + movieId;
    }

    // Xoá suất chiếu
    @GetMapping("/{movieId}/showtimes/delete/{showtimeId}")
    public String deleteShowtime(
            @PathVariable Integer movieId,
            @PathVariable Integer showtimeId,
            RedirectAttributes redirectAttributes) {
        try {
            showtimeRepository.deleteById(showtimeId);
            redirectAttributes.addFlashAttribute("successMessage", "Xoá suất chiếu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xoá suất chiếu này vì đã có khách hàng đặt vé!");
        }
        return "redirect:/admin/movies/edit/" + movieId;
    }

    // Lưu (thêm + sửa)
    @PostMapping("/save")
    public String saveMovie(@ModelAttribute @NonNull Movie movie) {
        movieRepository.save(movie);
        return "redirect:/admin/movies";
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable @NonNull Integer id, RedirectAttributes redirectAttributes) {
        try {
            movieRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xoá phim thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xoá bộ phim này vì đang có suất chiếu hoặc liên kết dữ liệu khác!");
        }
        return "redirect:/admin/movies";
    }

    // Tìm kiếm
    @GetMapping("/search")
    public String searchMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            Model model) {

        List<Movie> movies = movieRepository.findAll().stream()
                .filter(m -> title == null || title.isEmpty() ||
                        m.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(m -> status == null || status.isEmpty() ||
                        m.getStatus().name().equals(status))
                .collect(Collectors.toList());

        model.addAttribute("movies", movies);
        return "admin/movie/list";
    }
}