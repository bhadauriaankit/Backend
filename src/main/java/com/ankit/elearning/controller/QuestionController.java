// ===== QuestionController.java =====
package com.ankit.elearning.controller;

import com.ankit.elearning.entity.Question;
import com.ankit.elearning.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired private QuestionService questionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody Question question,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(questionService.createQuestionDto(question, userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(questionService.getAllQuestionDtos());
    }

    @GetMapping("/my-questions")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> getMyQuestions(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(questionService.getMyQuestionDtos(userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AUTHOR')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            questionService.deleteQuestion(id);
            return ResponseEntity.ok(Map.of("message", "Question deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
