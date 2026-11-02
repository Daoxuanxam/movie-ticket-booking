package com.cinema.booking.repository;

import com.cinema.booking.model.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HallRepository extends JpaRepository<Hall, Integer> {
    List<Hall> findByCinemaIdAndIsActiveTrue(Integer cinemaId);
}
