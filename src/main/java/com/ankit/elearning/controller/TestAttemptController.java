package com.ankit.elearning.controller;

import com.ankit.elearning.service.TestAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/attempts")
public class TestAttemptController {

    @Autowired private TestAttemptService testAttemptService;

    @PostMapping("/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> startTest(@RequestParam Long testId,
                                       @AuthenticationPrincipal UserDetails user) {
        try {
            return ResponseEntity.ok(
                    testAttemptService.startTest(testId, user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{attemptId}/answer")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> saveAnswer(@PathVariable Long attemptId,
                                        @RequestParam Long questionId,
                                        @RequestParam String option,
                                        @AuthenticationPrincipal UserDetails user) {
        try {
            testAttemptService.saveAnswer(attemptId, questionId, option, user.getUsername());
            return ResponseEntity.ok(Map.of("message", "Answer saved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{attemptId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submit(@PathVariable Long attemptId,
                                    @AuthenticationPrincipal UserDetails user) {
        try {
            return ResponseEntity.ok(
                    testAttemptService.submitTest(attemptId, user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{attemptId}/result")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getResult(@PathVariable Long attemptId,
                                       @AuthenticationPrincipal UserDetails user) {
        try {
            return ResponseEntity.ok(
                    testAttemptService.getResult(attemptId, user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-attempts")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyAttempts(@AuthenticationPrincipal UserDetails user) {
        try {
            return ResponseEntity.ok(
                    testAttemptService.getUserAttempts(user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** Returns whether the student has already passed a specific test */
    @GetMapping("/has-passed/{testId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> hasPassed(@PathVariable Long testId,
                                       @AuthenticationPrincipal UserDetails user) {
        try {
            boolean passed = testAttemptService.hasUserPassedTest(testId, user.getUsername());
            return ResponseEntity.ok(Map.of("passed", passed));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{attemptId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> deleteAttempt(@PathVariable Long attemptId,
                                           @AuthenticationPrincipal UserDetails user) {
        try {
            testAttemptService.deleteAttempt(attemptId, user.getUsername());
            return ResponseEntity.ok(Map.of("message", "Attempt discarded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}