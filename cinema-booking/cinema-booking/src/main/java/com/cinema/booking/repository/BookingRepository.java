package com.cinema.booking.repository;

import com.cinema.booking.model.Booking;
import com.cinema.booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT b FROM Booking b WHERE " +
           "(:keyword IS NULL OR b.bookingCode LIKE %:keyword%) AND " +
           "(:status IS NULL OR b.status = :status) " +
           "ORDER BY b.createdAt DESC")
    List<Booking> search(@Param("keyword") String keyword,
                         @Param("status") Booking.BookingStatus status);

    @Query("SELECT FUNCTION('MONTH', b.createdAt), COUNT(b.id), SUM(b.totalAmount), SUM(b.discountAmount) " +
    	       "FROM Booking b " +
    	       "WHERE FUNCTION('YEAR', b.createdAt) = :year AND b.status <> 'cancelled' " +
    	       "GROUP BY FUNCTION('MONTH', b.createdAt) " +
    	       "ORDER BY FUNCTION('MONTH', b.createdAt)")
    	List<Object[]> getRevenueByMonth(@Param("year") int year);

    // Doanh thu theo ngày trong khoảng thời gian
    @Query("SELECT FUNCTION('DATE', b.createdAt), " +
           "COUNT(b), SUM(b.totalAmount), SUM(b.discountAmount) " +
           "FROM Booking b " +
           "WHERE b.createdAt BETWEEN :from AND :to " +
           "AND b.status != 'cancelled' " +
           "GROUP BY FUNCTION('DATE', b.createdAt) " +
           "ORDER BY FUNCTION('DATE', b.createdAt)")
    List<Object[]> getRevenueByDay(@Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);
}