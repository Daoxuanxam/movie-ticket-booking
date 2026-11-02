package com.cinema.booking.repository;

import com.cinema.booking.model.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Integer> {

    @Query("SELECT c FROM Cinema c WHERE c.isActive = true")
    List<Cinema> findByIsActiveTrue();

    // Lấy các rạp đang active và đang có suất chiếu (status active) cho 1 phim cụ thể
    @Query("SELECT DISTINCT c FROM Cinema c " +
           "JOIN Hall h ON h.cinema.id = c.id " +
           "JOIN Showtime s ON s.hall.id = h.id " +
           "WHERE c.isActive = true AND s.movie.id = :movieId AND s.status = 'active' " +
           "ORDER BY c.name")
    List<Cinema> findCinemasByMovieId(@Param("movieId") Integer movieId);
 // Lấy các rạp đang active và có ít nhất 1 suất chiếu đang active (không lọc theo phim cụ thể)
    @Query("SELECT DISTINCT c FROM Cinema c " +
           "JOIN Hall h ON h.cinema.id = c.id " +
           "JOIN Showtime s ON s.hall.id = h.id " +
           "WHERE c.isActive = true AND s.status = 'active' " +
           "ORDER BY c.name")
    List<Cinema> findCinemasWithShowtimes();
}