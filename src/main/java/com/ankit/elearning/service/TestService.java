package com.ankit.elearning.service;

import com.ankit.elearning.dto.TestRequest;
import com.ankit.elearning.entity.*;
import com.ankit.elearning.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TestService {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private TestAttemptRepository testAttemptRepository;

    @Autowired
    private AttemptAnswerRepository attemptAnswerRepository;

    @Transactional
    public Test createTest(TestRequest req, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Test test = new Test();
        test.setTitle(req.getTitle());
        test.setDescription(req.getDescription());
        test.setDuration(req.getDuration());
        test.setAuthor(author);
        test.setPublished(false);

        if (req.getQuestionIds() != null && !req.getQuestionIds().isEmpty()) {
            List<Question> questions = questionRepository.findAllById(req.getQuestionIds());
            test.setQuestions(questions);
        } else {
            test.setQuestions(new ArrayList<>());
        }

        return testRepository.save(test);
    }

    public List<Map<String, Object>> getPublishedTestsWithDetails() {
        List<Test> tests = testRepository.findByPublishedTrue();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Test test : tests) {
            Map<String, Object> testMap = new HashMap<>();
            testMap.put("id", test.getId());
            testMap.put("title", test.getTitle());
            testMap.put("description", test.getDescription());
            testMap.put("duration", test.getDuration());
            testMap.put("published", test.isPublished());
            testMap.put("questionCount", test.getQuestions() != null ? test.getQuestions().size() : 0);
            result.add(testMap);
        }
        return result;
    }

    public List<Test> getPublishedTests() {
        return testRepository.findByPublishedTrue();
    }

    public List<Test> getAllTests() {
        return testRepository.findAll();
    }

    public List<Test> getTestsByAuthor(String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("Author not found"));
        return testRepository.findByAuthor(author);
    }

    public Test getById(Long id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found"));
    }

    @Transactional
    public Test publishTest(Long id) {
        Test test = getById(id);
        test.setPublished(true);
        return testRepository.save(test);
    }

    @Transactional
    public void deleteTest(Long id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        // 1. Delete all answers of attempts belonging to this test
        List<TestAttempt> attempts = testAttemptRepository.findByTest(test);
        for (TestAttempt attempt : attempts) {
            List<AttemptAnswer> answers = attemptAnswerRepository.findByAttempt(attempt);
            if (!answers.isEmpty()) {
                attemptAnswerRepository.deleteAll(answers);
            }
        }

        // 2. Delete all test attempts
        if (!attempts.isEmpty()) {
            testAttemptRepository.deleteAll(attempts);
        }

        // 3. Delete all modules of this test – use fully qualified name to avoid conflict with java.lang.Module
        List<com.ankit.elearning.entity.Module> modules = moduleRepository.findByTestOrderByOrderIndexAsc(test);
        if (!modules.isEmpty()) {
            moduleRepository.deleteAll(modules);
        }

        // 4. Clear the questions relationship to avoid foreign key issues
        test.setQuestions(null);
        testRepository.save(test);

        // 5. Finally delete the test itself
        testRepository.delete(test);
    }
}
