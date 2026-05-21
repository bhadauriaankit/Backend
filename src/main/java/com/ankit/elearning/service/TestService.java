package com.ankit.elearning.service;

import com.ankit.elearning.dto.TestRequest;
import com.ankit.elearning.entity.*;
import com.ankit.elearning.entity.Module;
import com.ankit.elearning.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class TestService {

    @Autowired private TestRepository     testRepository;
    @Autowired private UserRepository     userRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private CertificateRepository certificateRepository;
    @Autowired private TestAttemptRepository attemptRepository;
    @Autowired private AttemptAnswerRepository answerRepository;
    @Autowired private ModuleProgressRepository progressRepository;
    @Autowired private ModuleRepository moduleRepository;

    // ── CREATE ────────────────────────────────────────────────────────────────
    @Transactional
    public Test createTest(TestRequest req, String authorEmail) {
        if (req.getTitle() == null || req.getTitle().isBlank())
            throw new RuntimeException("Title is required.");
        if (req.getDuration() == null || req.getDuration() <= 0)
            throw new RuntimeException("Duration must be a positive number.");

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Test test = new Test();
        test.setTitle(req.getTitle().trim());
        test.setDescription(req.getDescription() != null ? req.getDescription().trim() : "");
        test.setDuration(req.getDuration());
        test.setAuthor(author);
        test.setPublished(false);
        test.setApprovalStatus(author.getRole() == Role.ADMIN
                ? ApprovalStatus.APPROVED
                : ApprovalStatus.PENDING_APPROVAL);

        if (req.getQuestionIds() != null && !req.getQuestionIds().isEmpty()) {
            test.setQuestions(questionRepository.findAllById(req.getQuestionIds()));
        }
        return testRepository.save(test);
    }

    // ── PUBLISHED TESTS (student view) — safe DTO map ─────────────────────────
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPublishedTestsWithDetails() {
        List<Test> tests = testRepository.findByPublishedTrue();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Test t : tests) {
            Map<String, Object> m = new HashMap<>();
            m.put("id",            t.getId());
            m.put("title",         t.getTitle());
            m.put("description",   t.getDescription());
            m.put("duration",      t.getDuration());
            m.put("published",     t.isPublished());
            m.put("approvalStatus", t.getApprovalStatus().name());
            m.put("questionCount", t.getQuestions() != null ? t.getQuestions().size() : 0);
            m.put("moduleCount",   t.getModules()   != null ? t.getModules().size()   : 0);
            result.add(m);
        }
        return result;
    }

    // ── ALL TESTS (author "my-tests" view) — safe DTO map ────────────────────
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllTestsAsDtos() {
        return testsToDto(testRepository.findAll());
    }

    // ── ADMIN view — safe DTO map ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllTestsForAdmin() {
        List<Test> tests = testRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Test t : tests) {
            Map<String, Object> m = new HashMap<>();
            m.put("id",            t.getId());
            m.put("title",         t.getTitle());
            m.put("description",   t.getDescription());
            m.put("duration",      t.getDuration());
            m.put("published",     t.isPublished());
            m.put("approvalStatus", t.getApprovalStatus().name());
            m.put("rejectionReason", t.getRejectionReason());
            m.put("questionCount", t.getQuestions() != null ? t.getQuestions().size() : 0);
            // Safe author info — author is now EAGER so this is fine
            if (t.getAuthor() != null) {
                Map<String, Object> authorMap = new HashMap<>();
                authorMap.put("id",    t.getAuthor().getId());
                authorMap.put("name",  t.getAuthor().getName());
                authorMap.put("email", t.getAuthor().getEmail());
                m.put("author", authorMap);
            }
            result.add(m);
        }
        return result;
    }

    // ── TESTS BY AUTHOR — safe DTO map ────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTestsByAuthorDto(String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("Author not found"));
        return testsToDto(testRepository.findByAuthor(author));
    }

    // ── GET BY ID ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Test getById(Long id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found"));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getByIdDto(Long id, String currentUserEmail) {
        Test test = getById(id);
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() == Role.STUDENT && !test.isPublished()) {
            throw new RuntimeException("Test not found");
        }
        if (currentUser.getRole() == Role.AUTHOR
                && (test.getAuthor() == null
                || !test.getAuthor().getEmail().equals(currentUserEmail))) {
            throw new RuntimeException("Access denied");
        }

        boolean includeAnswers = currentUser.getRole() == Role.ADMIN
                || currentUser.getRole() == Role.AUTHOR;
        Map<String, Object> m = testToDto(test);
        m.put("questions", test.getQuestions() == null
                ? List.of()
                : test.getQuestions().stream()
                .map(q -> questionToDto(q, includeAnswers))
                .toList());
        return m;
    }

    // ── PUBLISH ───────────────────────────────────────────────────────────────
    @Transactional
    public Map<String, Object> publishTest(Long id) {
        Test test = getById(id);
        test.setPublished(true);
        test.setApprovalStatus(ApprovalStatus.APPROVED);
        test.setRejectionReason(null);
        testRepository.save(test);
        Map<String, Object> m = new HashMap<>();
        m.put("id",        test.getId());
        m.put("title",     test.getTitle());
        m.put("published", true);
        m.put("approvalStatus", test.getApprovalStatus().name());
        return m;
    }

    @Transactional
    public Map<String, Object> rejectTest(Long id, String reason) {
        Test test = getById(id);
        test.setPublished(false);
        test.setApprovalStatus(ApprovalStatus.REJECTED);
        test.setRejectionReason(reason != null && !reason.isBlank() ? reason.trim() : "Rejected by admin");
        testRepository.save(test);
        Map<String, Object> m = new HashMap<>();
        m.put("id", test.getId());
        m.put("title", test.getTitle());
        m.put("published", false);
        m.put("approvalStatus", test.getApprovalStatus().name());
        m.put("rejectionReason", test.getRejectionReason());
        return m;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @Transactional
    public void deleteTest(Long id) {
        Test test = getById(id);
        for (TestAttempt attempt : attemptRepository.findByTest(test)) {
            certificateRepository.findByAttempt(attempt).ifPresent(certificateRepository::delete);
            answerRepository.deleteByAttempt(attempt);
        }
        attemptRepository.deleteAll(attemptRepository.findByTest(test));
        enrollmentRepository.deleteByTest(test);
        for (Module module : moduleRepository.findByTestOrderByOrderIndexAsc(test)) {
            progressRepository.deleteByModule(module);
        }
        testRepository.delete(test);
    }

    // ── CSV UPLOAD ────────────────────────────────────────────────────────────
    @Transactional
    public void uploadQuestionsFromCsv(Long testId, MultipartFile file, String authorEmail) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (author.getRole() == Role.AUTHOR
                && (test.getAuthor() == null || !test.getAuthor().getId().equals(author.getId())))
            throw new RuntimeException("You can only add questions to your own test.");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            List<Question> newQs = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.trim().isEmpty()) continue;
                String[] c = line.split(",", -1);
                if (c.length < 6) throw new RuntimeException("Invalid CSV row: " + line);
                Question q = new Question();
                q.setQuestionText(c[0].trim());
                q.setOptionA(c[1].trim());
                q.setOptionB(c[2].trim());
                q.setOptionC(c[3].trim());
                q.setOptionD(c[4].trim());
                q.setCorrectAnswer(c[5].trim().toUpperCase());
                q.setMarks(c.length > 6 && !c[6].isBlank() ? Integer.parseInt(c[6].trim()) : 1);
                q.setCreatedBy(author);
                newQs.add(questionRepository.save(q));
            }
            List<Question> existing = test.getQuestions();
            if (existing == null) existing = new ArrayList<>();
            existing.addAll(newQs);
            test.setQuestions(existing);
            testRepository.save(test);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV: " + e.getMessage());
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
    private List<Map<String, Object>> testsToDto(List<Test> tests) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Test t : tests) {
            Map<String, Object> m = new HashMap<>();
            m.put("id",            t.getId());
            m.put("title",         t.getTitle());
            m.put("description",   t.getDescription());
            m.put("duration",      t.getDuration());
            m.put("published",     t.isPublished());
            m.put("approvalStatus", t.getApprovalStatus().name());
            m.put("questionCount", t.getQuestions() != null ? t.getQuestions().size() : 0);
            result.add(m);
        }
        return result;
    }

    private Map<String, Object> testToDto(Test t) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", t.getId());
        m.put("title", t.getTitle());
        m.put("description", t.getDescription());
        m.put("duration", t.getDuration());
        m.put("published", t.isPublished());
        m.put("approvalStatus", t.getApprovalStatus().name());
        m.put("rejectionReason", t.getRejectionReason());
        m.put("questionCount", t.getQuestions() != null ? t.getQuestions().size() : 0);
        m.put("moduleCount", t.getModules() != null ? t.getModules().size() : 0);
        return m;
    }

    private Map<String, Object> questionToDto(Question q, boolean includeAnswers) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", q.getId());
        m.put("questionText", q.getQuestionText());
        m.put("optionA", q.getOptionA());
        m.put("optionB", q.getOptionB());
        m.put("optionC", q.getOptionC());
        m.put("optionD", q.getOptionD());
        m.put("marks", q.getMarks());
        if (includeAnswers) {
            m.put("correctAnswer", q.getCorrectAnswer());
        }
        return m;
    }

    // Keep for backward compatibility
    public List<Test> getAllTests() {
        return testRepository.findAll();
    }
}
