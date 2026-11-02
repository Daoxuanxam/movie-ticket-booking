package com.cinema.booking.repository;

import com.cinema.booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByIsActiveTrue();
    long countByIsActiveFalse();
    long countByRole(User.Role role);

    @Query("SELECT CONCAT('Tháng ', MONTH(u.createdAt)), COUNT(u) " +
           "FROM User u " +
           "WHERE YEAR(u.createdAt) = :year " +
           "GROUP BY MONTH(u.createdAt) " +
           "ORDER BY MONTH(u.createdAt)")
    List<Object[]> getUserRegistrationsByMonth(@Param("year") Integer year);
}