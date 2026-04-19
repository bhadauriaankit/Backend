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

    @Autowired
    private TestAttemptRepository testAttemptRepository;

    @Autowired
    private AttemptAnswerRepository attemptAnswerRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private static final int MAX_ATTEMPTS_PER_DAY = 4;
    private static final int COOLDOWN_MINUTES = 30;

    @Transactional
    public Map<String, Object> startTest(Long testId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.STUDENT) {
            throw new RuntimeException("Only students can take tests");
        }

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        if (!test.isPublished()) {
            throw new RuntimeException("Test is not published yet");
        }

        // Check daily attempt limit for this specific user and test
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayAttempts = testAttemptRepository.countByUserAndTestAndStartTimeAfter(user, test, startOfDay);

        if (todayAttempts >= MAX_ATTEMPTS_PER_DAY) {
            throw new RuntimeException("You have exceeded the daily limit of " + MAX_ATTEMPTS_PER_DAY + " attempts");
        }

        // Check cooldown period for this specific user
        Optional<TestAttempt> lastAttempt = testAttemptRepository
                .findByUserAndTestAndStatusOrderByEndTimeDesc(user, test, AttemptStatus.COMPLETED);
        if (lastAttempt.isPresent() && lastAttempt.get().getEndTime() != null) {
            LocalDateTime nextAllowedTime = lastAttempt.get().getEndTime().plusMinutes(COOLDOWN_MINUTES);
            if (LocalDateTime.now().isBefore(nextAllowedTime)) {
                long minutesLeft = java.time.Duration.between(LocalDateTime.now(), nextAllowedTime).toMinutes();
                throw new RuntimeException("Please wait " + minutesLeft + " more minutes before your next attempt");
            }
        }

        // Check for incomplete attempt for this specific user
        Optional<TestAttempt> incompleteAttempt = testAttemptRepository
                .findByUserAndTestAndStatus(user, test, AttemptStatus.IN_PROGRESS);
        if (incompleteAttempt.isPresent()) {
            throw new RuntimeException("You have an incomplete attempt. Please submit it first.");
        }

        // Create new attempt for this specific user
        TestAttempt attempt = new TestAttempt();
        attempt.setUser(user);
        attempt.setTest(test);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setStartTime(LocalDateTime.now());

        TestAttempt savedAttempt = testAttemptRepository.save(attempt);

        Map<String, Object> response = new HashMap<>();
        response.put("attemptId", savedAttempt.getId());
        response.put("testId", test.getId());
        response.put("testTitle", test.getTitle());
        response.put("duration", test.getDuration());

        List<Map<String, Object>> questionList = new ArrayList<>();
        if (test.getQuestions() != null) {
            questionList = test.getQuestions().stream().map(q -> {
                Map<String, Object> qMap = new HashMap<>();
                qMap.put("id", q.getId());
                qMap.put("questionText", q.getQuestionText());
                qMap.put("optionA", q.getOptionA());
                qMap.put("optionB", q.getOptionB());
                qMap.put("optionC", q.getOptionC());
                qMap.put("optionD", q.getOptionD());
                return qMap;
            }).collect(Collectors.toList());
        }
        response.put("questions", questionList);

        System.out.println("📝 Test started: User=" + userEmail + ", Test=" + test.getTitle());
        return response;
    }

    @Transactional
    public void saveAnswer(Long attemptId, Long questionId, String selectedOption, String userEmail) {
        TestAttempt attempt = validateAttempt(attemptId, userEmail);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("Test is already submitted");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Optional<AttemptAnswer> existingAnswer = attemptAnswerRepository.findByAttemptAndQuestion(attempt, question);
        AttemptAnswer answer = existingAnswer.orElse(new AttemptAnswer());
        answer.setAttempt(attempt);
        answer.setQuestion(question);
        answer.setSelectedOption(selectedOption);
        answer.setCorrect(question.getCorrectAnswer().equalsIgnoreCase(selectedOption));
        attemptAnswerRepository.save(answer);
    }

    @Transactional
    public Map<String, Object> submitTest(Long attemptId, String userEmail) {
        TestAttempt attempt = validateAttempt(attemptId, userEmail);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("Test already submitted");
        }

        List<AttemptAnswer> answers = attemptAnswerRepository.findByAttempt(attempt);
        int totalQuestions = attempt.getTest().getQuestions().size();
        int correctAnswers = (int) answers.stream().filter(AttemptAnswer::isCorrect).count();
        double percentage = (correctAnswers * 100.0) / totalQuestions;

        attempt.setScore(correctAnswers);
        attempt.setTotalQuestions(totalQuestions);
        attempt.setPercentage(percentage);
        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setEndTime(LocalDateTime.now());
        testAttemptRepository.save(attempt);

        Map<String, Object> result = new HashMap<>();
        result.put("attemptId", attempt.getId());
        result.put("score", correctAnswers);
        result.put("totalQuestions", totalQuestions);
        result.put("percentage", percentage);

        System.out.println("✅ Test submitted: User=" + userEmail + ", Score=" + correctAnswers + "/" + totalQuestions);
        return result;
    }

    public Map<String, Object> getResult(Long attemptId, String userEmail) {
        TestAttempt attempt = validateAttempt(attemptId, userEmail);
        if (attempt.getStatus() != AttemptStatus.COMPLETED) {
            throw new RuntimeException("Test not completed yet");
        }

        List<AttemptAnswer> answers = attemptAnswerRepository.findByAttempt(attempt);
        Map<String, Object> result = new HashMap<>();
        result.put("attemptId", attempt.getId());
        result.put("score", attempt.getScore());
        result.put("totalQuestions", attempt.getTotalQuestions());
        result.put("percentage", attempt.getPercentage());
        result.put("startTime", attempt.getStartTime());
        result.put("endTime", attempt.getEndTime());

        List<Map<String, Object>> answerDetails = answers.stream().map(answer -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("questionText", answer.getQuestion().getQuestionText());
            detail.put("selectedOption", answer.getSelectedOption());
            detail.put("correctOption", answer.getQuestion().getCorrectAnswer());
            detail.put("isCorrect", answer.isCorrect());
            return detail;
        }).collect(Collectors.toList());
        result.put("answers", answerDetails);
        return result;
    }

    public List<Map<String, Object>> getUserAttempts(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<TestAttempt> attempts = testAttemptRepository.findByUserOrderByStartTimeDesc(user);
        return attempts.stream().map(attempt -> {
            Map<String, Object> map = new HashMap<>();
            map.put("attemptId", attempt.getId());
            map.put("testTitle", attempt.getTest().getTitle());
            map.put("status", attempt.getStatus().toString());
            map.put("score", attempt.getScore());
            map.put("totalQuestions", attempt.getTotalQuestions());
            map.put("percentage", attempt.getPercentage());
            map.put("startedAt", attempt.getStartTime());
            map.put("completedAt", attempt.getEndTime());
            return map;
        }).collect(Collectors.toList());
    }

    private TestAttempt validateAttempt(Long attemptId, String userEmail) {
        TestAttempt attempt = testAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        if (!attempt.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized access to this attempt");
        }
        return attempt;
    }
}