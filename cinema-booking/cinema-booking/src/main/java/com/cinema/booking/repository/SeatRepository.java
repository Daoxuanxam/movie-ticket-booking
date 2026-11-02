package com.cinema.booking.repository;

import com.cinema.booking.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {

    @Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.isActive = true ORDER BY s.rowLabel, s.seatNumber")
    List<Seat> findByHallId(@Param("hallId") Integer hallId);
}