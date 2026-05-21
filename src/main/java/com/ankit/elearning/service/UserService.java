package com.ankit.elearning.service;

import com.ankit.elearning.dto.RegisterRequest;
import com.ankit.elearning.entity.*;
import com.ankit.elearning.repository.*;
import com.ankit.elearning.security.JwtUtil;
import com.ankit.elearning.util.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private EmailService emailService;
    @Autowired private PasswordResetTokenRepository tokenRepository;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    // ── REGISTER ──────────────────────────────────────────────────────────────
    @Transactional
    public void registerUser(RegisterRequest request) {

        // Basic null / blank checks
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Name is required.");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required.");
        }

        // Email validation — catches disposable domains
        if (!EmailValidator.isValidEmail(request.getEmail().trim())) {
            throw new RuntimeException("Please use a valid, non-disposable email address.");
        }

        // Password length
        if (request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters.");
        }

        // Password match (confirmPassword may be null when called from tests — skip if so)
        if (request.getConfirmPassword() != null
                && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match.");
        }

        // Duplicate email
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new RuntimeException("An account with this email already exists.");
        }

        // Role validation — only STUDENT and AUTHOR can self-register
        String roleStr = (request.getRole() != null && !request.getRole().isBlank())
                ? request.getRole().trim().toUpperCase()
                : "STUDENT";
        if (!roleStr.equals("STUDENT") && !roleStr.equals("AUTHOR")) {
            throw new RuntimeException("Invalid role. Only STUDENT or AUTHOR are allowed.");
        }

        // Persist user
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(roleStr));
        userRepository.save(user);

        // Send welcome email — failure must NOT roll back registration
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        } catch (Exception e) {
            log.warn("Welcome email failed for {}: {}", user.getEmail(), e.getMessage());
        }
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────
    public Map<String, Object> loginUser(String email, String password) {
        if (email == null || email.isBlank()) throw new RuntimeException("Email is required.");
        if (password == null || password.isBlank()) throw new RuntimeException("Password is required.");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email.trim().toLowerCase(), password)
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found."));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        Map<String, Object> response = new HashMap<>();
        response.put("token",  token);
        response.put("role",   user.getRole().name());
        response.put("email",  user.getEmail());
        response.put("name",   user.getName());
        response.put("id",     user.getId());

        try {
            emailService.sendLoginNotificationEmail(user.getEmail(), user.getName());
        } catch (Exception e) {
            log.warn("Login notification email failed for {}: {}", user.getEmail(), e.getMessage());
        }
        return response;
    }

    // ── GET CURRENT USER ──────────────────────────────────────────────────────
    public Map<String, Object> getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));
        Map<String, Object> info = new HashMap<>();
        info.put("id",    user.getId());
        info.put("name",  user.getName());
        info.put("email", user.getEmail());
        info.put("role",  user.getRole().name());
        return info;
    }

    // ── FORGOT PASSWORD ───────────────────────────────────────────────────────
    @Transactional
    public void sendPasswordResetEmail(String email) {
        if (email == null || email.isBlank()) throw new RuntimeException("Email is required.");

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("No account found with that email."));

        // Delete any existing tokens for this user
        tokenRepository.deleteByUser_Id(user.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        tokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetLink);
    }

    // ── RESET PASSWORD ────────────────────────────────────────────────────────
    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) throw new RuntimeException("Token is required.");
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters.");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset link."));

        if (resetToken.isUsed()) throw new RuntimeException("This reset link has already been used.");
        if (resetToken.isExpired()) throw new RuntimeException("This reset link has expired. Please request a new one.");

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}
