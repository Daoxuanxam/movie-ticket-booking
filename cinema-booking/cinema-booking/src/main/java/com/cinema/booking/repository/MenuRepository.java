package com.cinema.booking.repository;

import com.cinema.booking.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByActiveTrueOrderBySortOrderAsc();
}