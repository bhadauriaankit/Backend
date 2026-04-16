package com.ankit.elearning.repository;

import com.ankit.elearning.entity.TestAttempt;
import com.ankit.elearning.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {
    List<TestAttempt> findByUser(User user);
}