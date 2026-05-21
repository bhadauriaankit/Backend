package com.ankit.elearning.repository;
import com.ankit.elearning.entity.AttemptStatus;
import com.ankit.elearning.entity.Test;
import com.ankit.elearning.entity.TestAttempt;
import com.ankit.elearning.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {
    List<TestAttempt> findByUserOrderByStartTimeDesc(User user);
    List<TestAttempt> findByUserAndTestAndStatus(User user, Test test, AttemptStatus status);
    List<TestAttempt> findByTest(Test test);
    @Query("SELECT COUNT(t) FROM TestAttempt t WHERE t.user = :user AND t.test = :test AND t.startTime >= :startOfDay AND t.status = 'COMPLETED'")
    long countCompletedAttemptsToday(@Param("user") User user, @Param("test") Test test, @Param("startOfDay") LocalDateTime startOfDay);
    @Query("SELECT t FROM TestAttempt t WHERE t.user = :user AND t.test = :test AND t.status = 'COMPLETED' ORDER BY t.endTime DESC")
    List<TestAttempt> findCompletedByUserAndTestOrderByEndTimeDesc(@Param("user") User user, @Param("test") Test test);

    List<TestAttempt> findByUser(User user);

}