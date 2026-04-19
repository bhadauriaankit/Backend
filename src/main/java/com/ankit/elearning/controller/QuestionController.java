package com.ankit.elearning.controller;

import com.ankit.elearning.entity.Question;
import com.ankit.elearning.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    @Autowired private QuestionService questionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> create(@RequestBody Question question, @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        return ResponseEntity.ok(questionService.createQuestion(question, userDetails.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN','STUDENT')")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    @GetMapping("/my-questions")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> getMyQuestions(@AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        return ResponseEntity.ok(questionService.getMyQuestions(userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AUTHOR')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(Map.of("message", "Question deleted"));
    }
}