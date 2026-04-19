package com.ankit.elearning.repository;

import com.ankit.elearning.entity.AttemptAnswer;
import com.ankit.elearning.entity.Question;
import com.ankit.elearning.entity.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {
    List<AttemptAnswer> findByAttempt(TestAttempt attempt);
    Optional<AttemptAnswer> findByAttemptAndQuestion(TestAttempt attempt, Question question);
}