package com.ankit.elearning.service;

import com.ankit.elearning.dto.AttemptResponse;
import com.ankit.elearning.dto.ResultDetail;
import com.ankit.elearning.entity.*;
import com.ankit.elearning.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TestAttemptService {

    @Autowired private TestAttemptRepository attemptRepository;
    @Autowired private AttemptAnswerRepository answerRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private TestRepository testRepository;
    @Autowired private UserRepository userRepository;

    public TestAttempt startTest(Long testId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        if (!test.isPublished()) {
            throw new RuntimeException("Test not available");
        }

        TestAttempt attempt = new TestAttempt();
        attempt.setUser(user);
        attempt.setTest(test);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setEndTime(LocalDateTime.now().plusMinutes(test.getDuration()));
        attempt.setStatus(Status.STARTED);
        attempt.setScore(0);

        return attemptRepository.save(attempt);
    }

    public void saveAnswer(Long attemptId, Long questionId, String option, String email) {
        TestAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (!attempt.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
        if (attempt.getStatus() == Status.SUBMITTED) {
            throw new RuntimeException("Test already submitted");
        }
        if (LocalDateTime.now().isAfter(attempt.getEndTime())) {
            throw new RuntimeException("Time over");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (!attempt.getTest().getQuestions().contains(question)) {
            throw new RuntimeException("Invalid question for this test");
        }
        if (!List.of("A","B","C","D").contains(option)) {
            throw new RuntimeException("Invalid option");
        }

        AttemptAnswer ans = answerRepository
                .findByAttemptAndQuestion(attempt, question)
                .orElse(new AttemptAnswer());

        ans.setAttempt(attempt);
        ans.setQuestion(question);
        ans.setSelectedOption(option);
        answerRepository.save(ans);
    }

    public AttemptResponse submitTest(Long attemptId, String email) {
        TestAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (!attempt.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
        if (attempt.getStatus() == Status.SUBMITTED) {
            throw new RuntimeException("Already submitted");
        }

        // ✅ FIXED: time check added
        if (LocalDateTime.now().isAfter(attempt.getEndTime())) {
            attempt.setStatus(Status.SUBMITTED);
            attemptRepository.save(attempt);
            throw new RuntimeException("Time over — test auto-submitted");
        }

        List<Question> questions = attempt.getTest().getQuestions();
        List<AttemptAnswer> answers = answerRepository.findByAttempt(attempt);

        int score = 0, correct = 0, wrong = 0, skipped = 0;

        for (Question q : questions) {
            AttemptAnswer ans = answers.stream()
                    .filter(a -> a.getQuestion().getId().equals(q.getId()))
                    .findFirst().orElse(null);

            if (ans == null || ans.getSelectedOption() == null) {
                skipped++;
            } else if (ans.getSelectedOption().equals(q.getCorrectAnswer())) {
                score += q.getMarks();
                correct++;
            } else {
                score -= q.getNegativeMarks();
                wrong++;
            }
        }

        score = Math.max(score, 0);
        attempt.setScore(score);
        attempt.setStatus(Status.SUBMITTED);
        attempt.setEndTime(LocalDateTime.now());
        attemptRepository.save(attempt);

        return new AttemptResponse(score, correct, wrong, skipped);
    }

    public List<ResultDetail> getResult(Long attemptId, String email) {
        TestAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (!attempt.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        List<Question> questions = attempt.getTest().getQuestions();
        List<AttemptAnswer> answers = answerRepository.findByAttempt(attempt);

        return questions.stream().map(q -> {
            AttemptAnswer ans = answers.stream()
                    .filter(a -> a.getQuestion().getId().equals(q.getId()))
                    .findFirst().orElse(null);

            String selected = (ans != null) ? ans.getSelectedOption() : null;

            return new ResultDetail(
                    q.getQuestionText(),
                    selected,
                    q.getCorrectAnswer(),
                    selected != null && selected.equals(q.getCorrectAnswer())
            );
        }).toList();
    }

    public List<TestAttempt> getUserAttempts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return attemptRepository.findByUser(user);
    }
}