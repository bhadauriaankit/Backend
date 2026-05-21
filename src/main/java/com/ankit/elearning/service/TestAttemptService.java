package com.ankit.elearning.service;

import com.ankit.elearning.entity.*;
import com.ankit.elearning.entity.Module;
import com.ankit.elearning.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestAttemptService {

    private static final Logger  log            = LoggerFactory.getLogger(TestAttemptService.class);
    private static final int     MAX_ATTEMPTS   = 3;
    private static final int     COOLDOWN_MINS  = 30;
    public  static final double  PASS_THRESHOLD = 33.0;

    @Autowired private TestAttemptRepository    attemptRepo;
    @Autowired private TestRepository           testRepo;
    @Autowired private UserRepository           userRepo;
    @Autowired private QuestionRepository       questionRepo;
    @Autowired private AttemptAnswerRepository  answerRepo;
    @Autowired private ModuleProgressRepository progressRepo;
    @Autowired private ModuleRepository         moduleRepo;
    @Autowired private EmailService             emailService;
    @Autowired private EnrollmentRepository     enrollmentRepo;
    @Autowired private CertificateService       certificateService;
    @Autowired private EnrollmentService        enrollmentService;

    // ── START TEST ────────────────────────────────────────────────────────────
    @Transactional
    public Map<String, Object> startTest(Long testId, String userEmail) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Test test = testRepo.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        if (!test.isPublished())
            throw new RuntimeException("Test is not published yet.");
        if (test.getApprovalStatus() != ApprovalStatus.APPROVED)
            throw new RuntimeException("Course is not approved yet.");
        if (test.getQuestions() == null || test.getQuestions().isEmpty())
            throw new RuntimeException("This test has no questions.");
        if (!enrollmentRepo.existsByUserAndTest(user, test))
            throw new RuntimeException("Please enroll in this course before attempting the test.");
        if (!attemptRepo.findByUserAndTestAndStatus(user, test, AttemptStatus.IN_PROGRESS).isEmpty()) {
            throw new RuntimeException("You already have an in-progress attempt for this test.");
        }

        // Check user has not already PASSED this test
        List<TestAttempt> completedAttempts =
                attemptRepo.findCompletedByUserAndTestOrderByEndTimeDesc(user, test);
        boolean alreadyPassed = completedAttempts.stream()
                .anyMatch(a -> a.getPercentage() != null
                        && a.getPercentage() >= PASS_THRESHOLD);
        if (alreadyPassed) {
            throw new RuntimeException(
                    "You have already passed this test! " +
                            "Check your email for your certificate of completion.");
        }

        // Must complete all modules first
        List<Module> modules = moduleRepo.findByTestOrderByOrderIndexAsc(test);
        if (!modules.isEmpty()) {
            long done = progressRepo.countByUserAndTestAndCompleted(user, test);
            if (done < modules.size())
                throw new RuntimeException(
                        "Please complete all course modules before attempting the test.");
        }

        if (completedAttempts.size() >= MAX_ATTEMPTS)
            throw new RuntimeException(
                    "You have reached the maximum of " + MAX_ATTEMPTS +
                            " attempts for this test.");

        // Cooldown between attempts
        if (!completedAttempts.isEmpty()) {
            LocalDateTime lastEnd = completedAttempts.get(0).getEndTime();
            if (lastEnd != null
                    && lastEnd.plusMinutes(COOLDOWN_MINS).isAfter(LocalDateTime.now())) {
                long minsLeft = java.time.Duration.between(
                                LocalDateTime.now(), lastEnd.plusMinutes(COOLDOWN_MINS))
                        .toMinutes() + 1;
                throw new RuntimeException(
                        "Please wait " + minsLeft + " more minute(s) before retrying.");
            }
        }

        TestAttempt attempt = new TestAttempt();
        attempt.setUser(user);
        attempt.setTest(test);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setStartTime(LocalDateTime.now());
        attempt = attemptRepo.save(attempt);

        Map<String, Object> resp = new HashMap<>();
        resp.put("attemptId",  attempt.getId());
        resp.put("testId",     test.getId());
        resp.put("testTitle",  test.getTitle());
        resp.put("duration",   test.getDuration());
        resp.put("questions",  test.getQuestions() != null
                ? test.getQuestions().stream().map(this::questionForStudent).collect(Collectors.toList())
                : new ArrayList<>());
        return resp;
    }

    // ── SAVE ANSWER ───────────────────────────────────────────────────────────
    @Transactional
    public void saveAnswer(Long attemptId, Long questionId,
                           String selected, String userEmail) {
        if (selected == null || selected.isBlank()) {
            throw new RuntimeException("Selected option is required.");
        }
        TestAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        if (!attempt.getUser().getEmail().equals(userEmail))
            throw new RuntimeException("Unauthorized");
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS)
            throw new RuntimeException("Test already submitted");

        Question q = questionRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        boolean belongsToTest = attempt.getTest().getQuestions().stream()
                .anyMatch(question -> question.getId().equals(questionId));
        if (!belongsToTest) {
            throw new RuntimeException("Question does not belong to this test.");
        }

        AttemptAnswer ans = answerRepo
                .findByAttemptAndQuestion(attempt, q)
                .orElse(new AttemptAnswer());
        ans.setAttempt(attempt);
        ans.setQuestion(q);
        ans.setSelectedOption(selected.trim().toUpperCase());

        Set<String> correct  = new HashSet<>(
                Arrays.asList(q.getCorrectAnswer().split(",")));
        Set<String> selSet   = new HashSet<>(
                Arrays.asList(selected.trim().toUpperCase().split(",")));
        ans.setCorrect(correct.equals(selSet));
        answerRepo.save(ans);
    }

    // ── SUBMIT TEST ───────────────────────────────────────────────────────────
    @Transactional
    public Map<String, Object> submitTest(Long attemptId, String userEmail) {
        TestAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        if (!attempt.getUser().getEmail().equals(userEmail))
            throw new RuntimeException("Unauthorized");
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS)
            throw new RuntimeException("Already submitted");

        List<AttemptAnswer> answers = answerRepo.findByAttempt(attempt);
        int total   = attempt.getTest().getQuestions().size();
        int correct = (int) answers.stream().filter(AttemptAnswer::isCorrect).count();
        double pct  = total > 0 ? (correct * 100.0 / total) : 0;
        boolean passed = pct >= PASS_THRESHOLD;

        attempt.setScore(correct);
        attempt.setTotalQuestions(total);
        attempt.setPercentage(pct);
        attempt.setPassed(passed);
        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setEndTime(LocalDateTime.now());
        attemptRepo.save(attempt);

        Certificate certificate = null;
        if (passed) {
            certificate = certificateService.recordCertificate(attempt);
            enrollmentService.markCompleted(attempt.getUser(), attempt.getTest());
        }

        // Send result email (includes certificate PDF if passed)
        try {
            emailService.sendTestResultEmail(
                    attempt.getUser().getEmail(),
                    attempt.getUser().getName(),
                    attempt.getTest().getTitle(),
                    correct, total, pct, passed);
        } catch (Exception e) {
            log.warn("Result email failed: {}", e.getMessage());
        }

        Map<String, Object> res = new HashMap<>();
        res.put("attemptId",      attempt.getId());
        res.put("score",          correct);
        res.put("totalQuestions", total);
        res.put("percentage",     pct);
        res.put("passed",         passed);
        res.put("passThreshold",  PASS_THRESHOLD);
        res.put("canRetry",       !passed);
        if (certificate != null) {
            res.put("certificate", certificateService.toDto(certificate));
        }
        return res;
    }

    // ── GET RESULT ────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Map<String, Object> getResult(Long attemptId, String userEmail) {
        TestAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        if (!attempt.getUser().getEmail().equals(userEmail))
            throw new RuntimeException("Unauthorized");
        if (attempt.getStatus() != AttemptStatus.COMPLETED)
            throw new RuntimeException("Test not completed yet");

        List<AttemptAnswer> answers = answerRepo.findByAttempt(attempt);
        boolean passed = attempt.getPercentage() != null
                && attempt.getPercentage() >= PASS_THRESHOLD;

        Map<String, Object> res = new HashMap<>();
        res.put("attemptId",      attempt.getId());
        res.put("testId",         attempt.getTest().getId());
        res.put("testTitle",      attempt.getTest().getTitle());
        res.put("score",          attempt.getScore());
        res.put("totalQuestions", attempt.getTotalQuestions());
        res.put("percentage",     attempt.getPercentage());
        res.put("passed",         passed);
        res.put("passThreshold",  PASS_THRESHOLD);
        res.put("canRetry",       !passed);
        res.put("startTime",      attempt.getStartTime());
        res.put("endTime",        attempt.getEndTime());
        Map<String, Object> certificate = certificateService.getCertificateForAttempt(attempt);
        if (certificate != null) {
            res.put("certificate", certificate);
        }

        List<Map<String, Object>> details = answers.stream().map(a -> {
            Map<String, Object> d = new HashMap<>();
            d.put("questionText",   a.getQuestion().getQuestionText());
            d.put("optionA",        a.getQuestion().getOptionA());
            d.put("optionB",        a.getQuestion().getOptionB());
            d.put("optionC",        a.getQuestion().getOptionC());
            d.put("optionD",        a.getQuestion().getOptionD());
            d.put("selectedOption", a.getSelectedOption());
            d.put("correctOption",  a.getQuestion().getCorrectAnswer());
            d.put("isCorrect",      a.isCorrect());
            return d;
        }).collect(Collectors.toList());
        res.put("answers", details);
        return res;
    }

    // ── GET USER ATTEMPTS ─────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserAttempts(String userEmail) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return attemptRepo.findByUserOrderByStartTimeDesc(user).stream().map(a -> {
            boolean passed = a.getPercentage() != null
                    && a.getPercentage() >= PASS_THRESHOLD;
            Map<String, Object> m = new HashMap<>();
            m.put("attemptId",      a.getId());
            m.put("testId",         a.getTest().getId());
            m.put("testTitle",      a.getTest().getTitle());
            m.put("status",         a.getStatus().toString());
            m.put("score",          a.getScore());
            m.put("totalQuestions", a.getTotalQuestions());
            m.put("percentage",     a.getPercentage());
            m.put("passed",         passed);
            m.put("canRetry",       !passed && a.getStatus() == AttemptStatus.COMPLETED);
            Map<String, Object> certificate = certificateService.getCertificateForAttempt(a);
            if (certificate != null) {
                m.put("certificate", certificate);
            }
            m.put("startedAt",      a.getStartTime());
            m.put("completedAt",    a.getEndTime());
            return m;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> questionForStudent(Question q) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", q.getId());
        m.put("questionText", q.getQuestionText());
        m.put("optionA", q.getOptionA());
        m.put("optionB", q.getOptionB());
        m.put("optionC", q.getOptionC());
        m.put("optionD", q.getOptionD());
        m.put("marks", q.getMarks());
        return m;
    }

    // ── CHECK IF USER PASSED A TEST ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public boolean hasUserPassedTest(Long testId, String userEmail) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Test test = testRepo.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        return attemptRepo.findCompletedByUserAndTestOrderByEndTimeDesc(user, test)
                .stream()
                .anyMatch(a -> a.getPercentage() != null
                        && a.getPercentage() >= PASS_THRESHOLD);
    }

    // ── DISCARD IN-PROGRESS ATTEMPT ───────────────────────────────────────────
    @Transactional
    public void deleteAttempt(Long attemptId, String userEmail) {
        TestAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        if (!attempt.getUser().getEmail().equals(userEmail))
            throw new RuntimeException("Unauthorized");
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS)
            throw new RuntimeException("Only in-progress attempts can be discarded");
        answerRepo.deleteByAttempt(attempt);
        attemptRepo.delete(attempt);
    }
}
