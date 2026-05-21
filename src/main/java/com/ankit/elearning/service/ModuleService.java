package com.ankit.elearning.service;

import com.ankit.elearning.entity.*;
import com.ankit.elearning.entity.Module;
import com.ankit.elearning.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModuleService {

    @Autowired private ModuleRepository         moduleRepository;
    @Autowired private TestRepository           testRepository;
    @Autowired private UserRepository           userRepository;
    @Autowired private ModuleProgressRepository progressRepository;
    @Autowired private EnrollmentRepository     enrollmentRepository;

    // ── CREATE MODULE ─────────────────────────────────────────────────────────
    @Transactional
    public Module createModule(Long testId, Module module, String userEmail) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        // Authors can only add modules to their own tests
        // test.getAuthor() is now EAGER so this is always safe
        if (author.getRole() == Role.AUTHOR
                && (test.getAuthor() == null || !test.getAuthor().getEmail().equals(userEmail))) {
            throw new RuntimeException("You can only add modules to your own courses.");
        }

        module.setTest(test);
        module.setCreatedBy(author);

        if (module.getOrderIndex() == null) {
            List<Module> existing = moduleRepository.findByTestOrderByOrderIndexAsc(test);
            module.setOrderIndex(existing.size());
        }

        return moduleRepository.save(module);
    }

    // ── GET MODULES BY TEST ───────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<Module> getModulesByTest(Long testId, String currentUserEmail) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // AUTHOR can only see modules of their own test
        // test.getAuthor() is EAGER — always safe
        if (currentUser.getRole() == Role.AUTHOR) {
            if (test.getAuthor() == null || !test.getAuthor().getEmail().equals(currentUserEmail)) {
                throw new RuntimeException("Access denied");
            }
        }
        if (currentUser.getRole() == Role.STUDENT) {
            if (!test.isPublished() || test.getApprovalStatus() != ApprovalStatus.APPROVED) {
                throw new RuntimeException("Course is not available.");
            }
            if (!enrollmentRepository.existsByUserAndTest(currentUser, test)) {
                throw new RuntimeException("Please enroll in this course first.");
            }
        }
        // ADMIN can see any course's modules
        return moduleRepository.findByTestOrderByOrderIndexAsc(test);
    }

    // ── DELETE MODULE ─────────────────────────────────────────────────────────
    @Transactional
    public void deleteModule(Long moduleId) {
        // Delete progress records referencing this module first
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        progressRepository.deleteByModule(module);
        moduleRepository.delete(module);
    }

    // ── MARK MODULE COMPLETE ──────────────────────────────────────────────────
    @Transactional
    public void markModuleComplete(Long moduleId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        if (!module.getTest().isPublished()
                || module.getTest().getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new RuntimeException("Course is not available.");
        }
        if (!enrollmentRepository.existsByUserAndTest(user, module.getTest())) {
            throw new RuntimeException("Please enroll in this course first.");
        }

        ModuleProgress progress = progressRepository
                .findByUserAndModule(user, module)
                .orElse(new ModuleProgress());
        progress.setUser(user);
        progress.setModule(module);
        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }

    // ── GET COMPLETED MODULE IDs FOR USER ─────────────────────────────────────
    @Transactional(readOnly = true)
    public List<Long> getUserCompletedModuleIds(String userEmail, Long testId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // module.getTest() is EAGER — safe to use getId() without session
        return progressRepository.findByUser(user).stream()
                .filter(mp -> mp.isCompleted()
                        && mp.getModule().getTest().getId().equals(testId))
                .map(mp -> mp.getModule().getId())
                .collect(Collectors.toList());
    }
}
