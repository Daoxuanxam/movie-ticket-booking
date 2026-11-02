package com.cinema.booking.repository;

import com.cinema.booking.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {

    // Lấy tất cả showtimes theo movie + cinema (qua hall)
    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId AND s.hall.cinema.id = :cinemaId AND s.status = 'active' ORDER BY s.startTime")
    List<Showtime> findByMovieIdAndCinemaId(@Param("movieId") Integer movieId,
                                            @Param("cinemaId") Integer cinemaId);

    // Lấy showtimes theo movie + cinema + ngày cụ thể
    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId AND s.hall.cinema.id = :cinemaId AND FUNCTION('DATE', s.startTime) = :date AND s.status = 'active' ORDER BY s.startTime")
    List<Showtime> findByMovieIdAndCinemaIdAndDate(@Param("movieId") Integer movieId,
                                                   @Param("cinemaId") Integer cinemaId,
                                                   @Param("date") LocalDate date);

    // Lấy danh sách ngày có suất chiếu theo movie + cinema
    @Query("SELECT DISTINCT FUNCTION('DATE', s.startTime) FROM Showtime s WHERE s.movie.id = :movieId AND s.hall.cinema.id = :cinemaId AND s.status = 'active' ORDER BY FUNCTION('DATE', s.startTime)")
    List<java.sql.Date> findAvailableDatesByMovieAndCinema(@Param("movieId") Integer movieId,
                                                           @Param("cinemaId") Integer cinemaId);
    @Query("SELECT DISTINCT s.movie.id FROM Showtime s WHERE s.hall.cinema.id = :cinemaId AND s.status = 'active'")
    List<Integer> findMovieIdsByCinemaId(@Param("cinemaId") Integer cinemaId);

    List<Showtime> findByMovieId(Integer movieId);
}