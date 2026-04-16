package com.ankit.elearning.controller;

import com.ankit.elearning.entity.Test;
import com.ankit.elearning.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    @Autowired private TestService testService;

    @PostMapping
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> create(
            @RequestBody Test test,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails
    ) {
        return ResponseEntity.ok(testService.createTest(test, userDetails.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getPublished() {
        return ResponseEntity.ok(testService.getPublishedTests());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(testService.getAllTests());
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> publish(@PathVariable Long id) {
        return ResponseEntity.ok(testService.publishTest(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        testService.deleteTest(id);
        return ResponseEntity.ok(Map.of("message", "Test deleted"));
    }
}