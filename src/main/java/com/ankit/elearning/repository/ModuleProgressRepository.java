package com.ankit.elearning.repository;

import com.ankit.elearning.entity.Module;
import com.ankit.elearning.entity.ModuleProgress;
import com.ankit.elearning.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ModuleProgressRepository extends JpaRepository<ModuleProgress, Long> {
    Optional<ModuleProgress> findByUserAndModule(User user, Module module);
    List<ModuleProgress> findByUser(User user);
    long countByUserAndCompletedTrue(User user);
    long countByUserAndModuleInAndCompletedTrue(User user, List<Module> modules);
}