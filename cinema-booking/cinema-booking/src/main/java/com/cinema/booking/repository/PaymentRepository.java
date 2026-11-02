package com.cinema.booking.repository;

import com.cinema.booking.model.Booking;
import com.cinema.booking.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByBooking(Booking booking);
    Optional<Payment> findByPaymentCode(String paymentCode);
    List<Payment> findByStatus(Payment.PaymentStatus status);
}
