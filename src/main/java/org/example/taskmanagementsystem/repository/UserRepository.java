package org.example.taskmanagementsystem.repository;

import org.example.taskmanagementsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.isVerified = false AND u.codeSentAt < :expirationTime")
    void deleteExpiredUnverifiedUsers(@Param("expirationTime") LocalDateTime expirationTime);
}
