package com.ankit.elearning.controller;

import com.ankit.elearning.dto.TestRequest;
import com.ankit.elearning.entity.Test;
import com.ankit.elearning.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    @Autowired
    private TestService testService;

    @PostMapping
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> createTest(
            @RequestBody TestRequest req,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails
    ) {
        try {
            System.out.println("=== CREATE TEST REQUEST ===");
            System.out.println("Author email: " + userDetails.getUsername());
            System.out.println("Test title: " + req.getTitle());
            System.out.println("Duration: " + req.getDuration());
            System.out.println("Question IDs: " + req.getQuestionIds());

            Test test = testService.createTest(req, userDetails.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test created successfully");
            response.put("testId", test.getId());
            response.put("title", test.getTitle());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error creating test: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','AUTHOR','ADMIN')")
    public ResponseEntity<?> getPublishedTests() {
        try {
            List<Map<String, Object>> tests = testService.getPublishedTestsWithDetails();
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> getAllTests() {
        try {
            List<Test> tests = testService.getAllTests();
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/my-tests")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> getMyTests(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails
    ) {
        try {
            List<Test> tests = testService.getTestsByAuthor(userDetails.getUsername());
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','AUTHOR','ADMIN')")
    public ResponseEntity<?> getTestById(@PathVariable Long id) {
        try {
            Test test = testService.getById(id);
            return ResponseEntity.ok(test);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(404).body(error);
        }
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> publishTest(@PathVariable Long id) {
        try {
            Test test = testService.publishTest(id);
            return ResponseEntity.ok(test);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AUTHOR')")
    public ResponseEntity<?> deleteTest(@PathVariable Long id) {
        try {
            testService.deleteTest(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Test deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}