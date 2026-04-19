package com.ankit.elearning.controller;

import com.ankit.elearning.entity.Module;
import com.ankit.elearning.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {
    @Autowired private ModuleService moduleService;

    @PostMapping("/test/{testId}")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> create(@PathVariable Long testId, @RequestBody Module module, @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        return ResponseEntity.ok(moduleService.createModule(testId, module, userDetails.getUsername()));
    }

    @GetMapping("/test/{testId}")
    @PreAuthorize("hasAnyRole('STUDENT','AUTHOR','ADMIN')")
    public ResponseEntity<?> getByTest(@PathVariable Long testId) {
        return ResponseEntity.ok(moduleService.getModulesByTest(testId));
    }

    @DeleteMapping("/{moduleId}")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long moduleId) {
        moduleService.deleteModule(moduleId);
        return ResponseEntity.ok(Map.of("message", "Module deleted"));
    }
}