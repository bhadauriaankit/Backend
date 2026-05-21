package com.ankit.elearning.controller;

import com.ankit.elearning.dto.TestRequest;
import com.ankit.elearning.service.TestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    @Autowired private TestService testService;

    @PostMapping
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> createTest(@Valid @RequestBody TestRequest req,
                                        @AuthenticationPrincipal UserDetails user) {
        try {
            var test = testService.createTest(req, user.getUsername());
            return ResponseEntity.ok(Map.of(
                "message", "Test created successfully",
                "testId",  test.getId(),
                "title",   test.getTitle()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** Published tests — student/author/admin view, safe DTO */
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','AUTHOR','ADMIN')")
    public ResponseEntity<?> getPublishedTests() {
        try {
            return ResponseEntity.ok(testService.getPublishedTestsWithDetails());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** All tests — author/admin only, safe DTO */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> getAllTests() {
        try {
            return ResponseEntity.ok(testService.getAllTestsAsDtos());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** My tests — author's own tests, safe DTO */
    @GetMapping("/my-tests")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> getMyTests(@AuthenticationPrincipal UserDetails user) {
        try {
            return ResponseEntity.ok(testService.getTestsByAuthorDto(user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','AUTHOR','ADMIN')")
    public ResponseEntity<?> getById(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails user) {
        try {
            return ResponseEntity.ok(testService.getByIdDto(id, user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> publishTest(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(testService.publishTest(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AUTHOR')")
    public ResponseEntity<?> deleteTest(@PathVariable Long id) {
        try {
            testService.deleteTest(id);
            return ResponseEntity.ok(Map.of("message", "Test deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{testId}/upload-questions")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> uploadQuestions(@PathVariable Long testId,
                                             @RequestParam("file") MultipartFile file,
                                             @AuthenticationPrincipal UserDetails user) {
        try {
            testService.uploadQuestionsFromCsv(testId, file, user.getUsername());
            return ResponseEntity.ok(Map.of("message", "Questions uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
