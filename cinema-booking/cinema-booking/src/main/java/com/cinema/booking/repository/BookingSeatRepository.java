package com.cinema.booking.repository;

import com.cinema.booking.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Integer> {

    // Lấy seatId đã bị đặt cho 1 showtime (chỉ tính booking chưa bị hủy/hoàn tiền)
    @Query("SELECT bs.seat.id FROM BookingSeat bs " +
           "WHERE bs.booking.showtime.id = :showtimeId " +
           "AND bs.booking.status IN ('pending', 'confirmed')")
    List<Integer> findTakenSeatIdsByShowtimeId(@Param("showtimeId") Integer showtimeId);
}