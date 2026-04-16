package com.ankit.elearning.controller;

import com.ankit.elearning.service.TestAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/attempts")
public class TestAttemptController {

    @Autowired private TestAttemptService service;

    @PostMapping("/start")
    @PreAuthorize("hasRole('STUDENT')") // ✅ FIXED: uncommented
    public ResponseEntity<?> startTest(
            @RequestParam Long testId,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails
    ) {
        return ResponseEntity.ok(service.startTest(testId, userDetails.getUsername()));
    }

    @PostMapping("/{attemptId}/answer")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> saveAnswer(
            @PathVariable Long attemptId,
            @RequestParam Long questionId,
            @RequestParam String option,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails
    ) {
        service.saveAnswer(attemptId, questionId, option, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Answer saved"));
    }

    @PostMapping("/{attemptId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submit(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails
    ) {
        return ResponseEntity.ok(service.submitTest(attemptId, userDetails.getUsername()));
    }

    @GetMapping("/{attemptId}/result")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getResult(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails
    ) {
        return ResponseEntity.ok(service.getResult(attemptId, userDetails.getUsername()));
    }

    @GetMapping("/my-attempts")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> myAttempts(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails
    ) {
        return ResponseEntity.ok(service.getUserAttempts(userDetails.getUsername()));
    }
}