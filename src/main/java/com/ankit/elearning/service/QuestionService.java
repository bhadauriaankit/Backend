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

    public Question createQuestion(Question question, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // validate correct answer
        if (!List.of("A","B","C","D").contains(question.getCorrectAnswer())) {
            throw new RuntimeException("correctAnswer must be A, B, C or D");
        }

        question.setCreatedBy(user); // now actually used
        return questionRepository.save(question);
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }
}