package com.ankit.elearning.repository;

import com.ankit.elearning.entity.TestAttempt;
import com.ankit.elearning.entity.User;
import com.ankit.elearning.entity.Test;
import com.ankit.elearning.entity.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {
    List<TestAttempt> findByUser(User user);
    List<TestAttempt> findByUserOrderByStartTimeDesc(User user);
    Optional<TestAttempt> findByUserAndTestAndStatus(User user, Test test, AttemptStatus status);
    Optional<TestAttempt> findByUserAndTestAndStatusOrderByEndTimeDesc(User user, Test test, AttemptStatus status);

    @Query("SELECT COUNT(t) FROM TestAttempt t WHERE t.user = :user AND t.test = :test AND t.startTime >= :startOfDay")
    long countByUserAndTestAndStartTimeAfter(@Param("user") User user, @Param("test") Test test, @Param("startOfDay") LocalDateTime startOfDay);
}