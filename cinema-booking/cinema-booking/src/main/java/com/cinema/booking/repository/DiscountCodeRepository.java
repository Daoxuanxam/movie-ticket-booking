package com.cinema.booking.repository;

import com.cinema.booking.model.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Integer> {

    @Query("SELECT d FROM DiscountCode d WHERE d.code = :code")
    Optional<DiscountCode> findByCode(@Param("code") String code);
}