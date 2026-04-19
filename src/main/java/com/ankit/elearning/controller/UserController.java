package com.ankit.elearning.controller;

import com.ankit.elearning.dto.LoginRequest;
import com.ankit.elearning.dto.RegisterRequest;
import com.ankit.elearning.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    @Autowired private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok(Map.of("message", "Registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.loginUser(request.getEmail(), request.getPassword()));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT','AUTHOR','ADMIN')")
    public ResponseEntity<?> me(@AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        return ResponseEntity.ok(userService.getCurrentUser(userDetails.getUsername()));
    }
}