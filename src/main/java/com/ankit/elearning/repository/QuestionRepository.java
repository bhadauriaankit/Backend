package com.ankit.elearning.repository;

import com.ankit.elearning.entity.Question;
import com.ankit.elearning.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCreatedBy(User author);
}