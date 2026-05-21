// ===== QuestionService.java =====
package com.ankit.elearning.service;

import com.ankit.elearning.entity.Question;
import com.ankit.elearning.entity.User;
import com.ankit.elearning.repository.QuestionRepository;
import com.ankit.elearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired private QuestionRepository questionRepository;
    @Autowired private UserRepository userRepository;

    @Transactional
    public Question createQuestion(Question question, String userEmail) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        question.setCreatedBy(author);
        // Normalize correct answer
        if (question.getCorrectAnswer() != null) {
            question.setCorrectAnswer(question.getCorrectAnswer().toUpperCase().trim());
        }
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

    public Map<String, Object> createQuestionDto(Question question, String userEmail) {
        return toDto(createQuestion(question, userEmail), true);
    }

    public List<Map<String, Object>> getAllQuestionDtos() {
        return questionRepository.findAll().stream()
                .map(q -> toDto(q, true))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMyQuestionDtos(String userEmail) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return questionRepository.findByCreatedBy(author).stream()
                .map(q -> toDto(q, true))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    private Map<String, Object> toDto(Question q, boolean includeCorrectAnswer) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", q.getId());
        m.put("questionText", q.getQuestionText());
        m.put("optionA", q.getOptionA());
        m.put("optionB", q.getOptionB());
        m.put("optionC", q.getOptionC());
        m.put("optionD", q.getOptionD());
        m.put("marks", q.getMarks());
        if (includeCorrectAnswer) {
            m.put("correctAnswer", q.getCorrectAnswer());
        }
        return m;
    }
}
 
