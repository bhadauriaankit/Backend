package com.ankit.elearning.repository;

import com.ankit.elearning.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}