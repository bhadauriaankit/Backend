package com.ankit.elearning.controller;

import com.ankit.elearning.entity.Role;
import com.ankit.elearning.entity.User;
import com.ankit.elearning.repository.UserRepository;
import com.ankit.elearning.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private TestService testService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> changeRole(
            @PathVariable Long id,
            @RequestParam String role
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(Role.valueOf(role.toUpperCase()));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Role updated"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    @GetMapping("/tests")
    public ResponseEntity<?> getAllTests() {
        return ResponseEntity.ok(testService.getAllTests());
    }

    @PutMapping("/tests/{id}/publish")
    public ResponseEntity<?> publishTest(@PathVariable Long id) {
        return ResponseEntity.ok(testService.publishTest(id));
    }

    @DeleteMapping("/tests/{id}")
    public ResponseEntity<?> deleteTest(@PathVariable Long id) {
        testService.deleteTest(id);
        return ResponseEntity.ok(Map.of("message", "Test deleted"));
    }
}