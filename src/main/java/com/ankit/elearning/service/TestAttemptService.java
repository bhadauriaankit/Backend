package com.ankit.elearning.service;

import com.ankit.elearning.entity.*;
import com.ankit.elearning.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestAttemptService {

    @Autowired private TestAttemptRepository attemptRepo;
    @Autowired private TestRepository testRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private QuestionRepository questionRepo;
    @Autowired private AttemptAnswerRepository answerRepo;
    @Autowired
    private EmailService emailService;

    @Transactional
    public Map<String, Object> startTest(Long testId, String userEmail) {
        System.out.println("=== startTest called ===");
        System.out.println("testId: " + testId + ", userEmail: " + userEmail);

        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        System.out.println("User found: " + user.getEmail());

        Test test = testRepo.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found: " + testId));
        System.out.println("Test found: " + test.getTitle() + ", published: " + test.isPublished());

        if (!test.isPublished()) {
            throw new RuntimeException("Test is not published yet");
        }

        // Create new attempt
        TestAttempt attempt = new TestAttempt();
        attempt.setUser(user);
        attempt.setTest(test);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setStartTime(LocalDateTime.now());
        TestAttempt saved = attemptRepo.save(attempt);
        System.out.println("Attempt saved with ID: " + saved.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("attemptId", saved.getId());
        response.put("testId", test.getId());
        response.put("testTitle", test.getTitle());
        response.put("duration", test.getDuration());

        List<Map<String, Object>> questions = new ArrayList<>();
        if (test.getQuestions() != null) {
            questions = test.getQuestions().stream().map(q -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", q.getId());
                map.put("questionText", q.getQuestionText());
                map.put("optionA", q.getOptionA());
                map.put("optionB", q.getOptionB());
                map.put("optionC", q.getOptionC());
                map.put("optionD", q.getOptionD());
                return map;
            }).collect(Collectors.toList());
        }
        response.put("questions", questions);
        System.out.println("Returning response with " + questions.size() + " questions");
        return response;
    }

    @Transactional
    public void saveAnswer(Long attemptId, Long questionId, String selectedOption, String userEmail) {
        TestAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        if (!attempt.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("Test already submitted");
        }
        Question question = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Optional<AttemptAnswer> existing = answerRepo.findByAttemptAndQuestion(attempt, question);
        AttemptAnswer answer = existing.orElse(new AttemptAnswer());
        answer.setAttempt(attempt);
        answer.setQuestion(question);
        answer.setSelectedOption(selectedOption);

        // Check correctness
        Set<String> correctSet = new HashSet<>(Arrays.asList(question.getCorrectAnswer().split(",")));
        Set<String> selectedSet = new HashSet<>(Arrays.asList(selectedOption.split(",")));
        answer.setCorrect(correctSet.equals(selectedSet));

        answerRepo.save(answer);
    }

    @Transactional
    public Map<String, Object> submitTest(Long attemptId, String userEmail) {
        TestAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        if (!attempt.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("Test already submitted");
        }

        List<AttemptAnswer> answers = answerRepo.findByAttempt(attempt);
        int total = attempt.getTest().getQuestions().size();
        int correct = (int) answers.stream().filter(AttemptAnswer::isCorrect).count();
        double percentage = total > 0 ? (correct * 100.0 / total) : 0;

        attempt.setScore(correct);
        attempt.setTotalQuestions(total);
        attempt.setPercentage(percentage);
        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setEndTime(LocalDateTime.now());
        attemptRepo.save(attempt);

        boolean passed = percentage >= 60;
        emailService.sendTestResultEmail(
                attempt.getUser().getEmail(),
                attempt.getUser().getName(),
                attempt.getTest().getTitle(),
                correct,
                total,
                percentage,
                passed
        );

        Map<String, Object> result = new HashMap<>();
        result.put("attemptId", attempt.getId());
        result.put("score", correct);
        result.put("totalQuestions", total);
        result.put("percentage", percentage);
        return result;
    }

    public Map<String, Object> getResult(Long attemptId, String userEmail) {
        TestAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        if (!attempt.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        if (attempt.getStatus() != AttemptStatus.COMPLETED) {
            throw new RuntimeException("Test not completed yet");
        }
        List<AttemptAnswer> answers = answerRepo.findByAttempt(attempt);
        Map<String, Object> result = new HashMap<>();
        result.put("attemptId", attempt.getId());
        result.put("score", attempt.getScore());
        result.put("totalQuestions", attempt.getTotalQuestions());
        result.put("percentage", attempt.getPercentage());
        result.put("startTime", attempt.getStartTime());
        result.put("endTime", attempt.getEndTime());

        List<Map<String, Object>> details = answers.stream().map(a -> {
            Map<String, Object> d = new HashMap<>();
            d.put("questionText", a.getQuestion().getQuestionText());
            d.put("selectedOption", a.getSelectedOption());
            d.put("correctOption", a.getQuestion().getCorrectAnswer());
            d.put("isCorrect", a.isCorrect());
            return d;
        }).collect(Collectors.toList());
        result.put("answers", details);
        return result;
    }

    public List<Map<String, Object>> getUserAttempts(String userEmail) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return attemptRepo.findByUserOrderByStartTimeDesc(user).stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("attemptId", a.getId());
            m.put("testTitle", a.getTest().getTitle());
            m.put("status", a.getStatus().toString());
            m.put("score", a.getScore());
            m.put("totalQuestions", a.getTotalQuestions());
            m.put("percentage", a.getPercentage());
            m.put("startedAt", a.getStartTime());
            m.put("completedAt", a.getEndTime());
            return m;
        }).collect(Collectors.toList());
    }
}