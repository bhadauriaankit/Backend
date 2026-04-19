package com.ankit.elearning.service;

import com.ankit.elearning.entity.Question;
import com.ankit.elearning.entity.User;
import com.ankit.elearning.repository.QuestionRepository;
import com.ankit.elearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class QuestionService {
    @Autowired private QuestionRepository questionRepository;
    @Autowired private UserRepository userRepository;

    public Question createQuestion(Question question, String userEmail) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        question.setCreatedBy(author);
        return questionRepository.save(question);
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public List<Question> getMyQuestions(String userEmail) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return questionRepository.findByCreatedBy(author);
    }

    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }
}