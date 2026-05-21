package com.ankit.elearning.controller;

import com.ankit.elearning.entity.Module;
import com.ankit.elearning.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    @Autowired private ModuleService moduleService;

    @PostMapping("/test/{testId}")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> create(@PathVariable Long testId,
                                    @Valid @RequestBody Module module,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(
                    moduleService.createModule(testId, module, userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test/{testId}")
    @PreAuthorize("hasAnyRole('STUDENT','AUTHOR','ADMIN')")
    public ResponseEntity<?> getByTest(@PathVariable Long testId,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(
                    moduleService.getModulesByTest(testId, userDetails.getUsername()));
        } catch (Exception e) {
            // 403 if access denied, 500 for other errors
            String msg = e.getMessage();
            int status = (msg != null && msg.contains("Access denied")) ? 403 : 500;
            return ResponseEntity.status(status).body(Map.of("error", msg));
        }
    }

    @DeleteMapping("/{moduleId}")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long moduleId) {
        try {
            moduleService.deleteModule(moduleId);
            return ResponseEntity.ok(Map.of("message", "Module deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{moduleId}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> markComplete(@PathVariable Long moduleId,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        try {
            moduleService.markModuleComplete(moduleId, userDetails.getUsername());
            return ResponseEntity.ok(Map.of("message", "Module marked complete"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test/{testId}/progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getProgress(@PathVariable Long testId,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(
                    moduleService.getUserCompletedModuleIds(
                            userDetails.getUsername(), testId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
