package com.ankit.elearning.controller;

import com.ankit.elearning.entity.*;
import com.ankit.elearning.repository.*;
import com.ankit.elearning.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private UserRepository               userRepository;
    @Autowired private TestService                  testService;
    @Autowired private TestRepository               testRepository;
    @Autowired private ModuleProgressRepository     moduleProgressRepo;
    @Autowired private ModuleRepository             moduleRepository;
    @Autowired private TestAttemptRepository        attemptRepository;
    @Autowired private AttemptAnswerRepository      answerRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private QuestionRepository           questionRepository;
    @Autowired private EnrollmentRepository         enrollmentRepository;
    @Autowired private CertificateRepository        certificateRepository;

    // ── GET ALL USERS ─────────────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            users.forEach(u -> u.setPassword(null));
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ── CHANGE ROLE ───────────────────────────────────────────────────────────
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long id, @RequestParam String role) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setRole(Role.valueOf(role.toUpperCase()));
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Role updated to " + role.toUpperCase()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── DELETE USER — full cascade ────────────────────────────────────────────
    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 1. Delete password reset tokens
            tokenRepository.deleteByUser_Id(id);

            // 2. Delete attempt answers then attempts (student activity)
            List<TestAttempt> attempts = attemptRepository.findByUser(user);
            for (TestAttempt attempt : attempts) {
                certificateRepository.findByAttempt(attempt).ifPresent(certificateRepository::delete);
                answerRepository.deleteByAttempt(attempt);
            }
            attemptRepository.deleteAll(attempts);

            // 3. Delete module progress (student activity)
            moduleProgressRepo.deleteByUser(user);
            enrollmentRepository.deleteByUser(user);

            // 4. If user is an AUTHOR: nullify createdBy on their questions
            //    (set to null rather than deleting — questions may be linked to other tests)
            List<Question> authoredQuestions = questionRepository.findByCreatedBy(user);
            for (Question q : authoredQuestions) {
                q.setCreatedBy(null);
                questionRepository.save(q);
            }

            // 5. Keep courses/tests but detach the deleted author account
            List<Test> authoredTests = testRepository.findByAuthor(user);
            for (Test t : authoredTests) {
                t.setAuthor(null);
                testRepository.save(t);
            }

            // 6. Handle modules created by this author
            //    Delete progress on those modules first, then the modules themselves
            List<com.ankit.elearning.entity.Module> authoredModules =
                    moduleRepository.findByCreatedBy(user);
            for (com.ankit.elearning.entity.Module m : authoredModules) {
                moduleProgressRepo.deleteByModule(m);
            }
            moduleRepository.deleteAll(authoredModules);

            // 7. Now safe to delete the user
            userRepository.delete(user);

            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET ALL TESTS ─────────────────────────────────────────────────────────
    @GetMapping("/tests")
    public ResponseEntity<?> getAllTests() {
        try {
            return ResponseEntity.ok(testService.getAllTestsForAdmin());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ── PUBLISH TEST ──────────────────────────────────────────────────────────
    @PutMapping("/tests/{id}/publish")
    public ResponseEntity<?> publishTest(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(testService.publishTest(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── REJECT TEST / COURSE ─────────────────────────────────────────────────
    @PutMapping("/tests/{id}/reject")
    public ResponseEntity<?> rejectTest(@PathVariable Long id,
                                        @RequestParam(required = false) String reason) {
        try {
            return ResponseEntity.ok(testService.rejectTest(id, reason));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── DELETE TEST ───────────────────────────────────────────────────────────
    @DeleteMapping("/tests/{id}")
    public ResponseEntity<?> deleteTest(@PathVariable Long id) {
        try {
            testService.deleteTest(id);
            return ResponseEntity.ok(Map.of("message", "Test deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
