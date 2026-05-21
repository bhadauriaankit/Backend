package com.ankit.elearning.repository;

import com.ankit.elearning.entity.Module;
import com.ankit.elearning.entity.ModuleProgress;
import com.ankit.elearning.entity.User;
import com.ankit.elearning.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ModuleProgressRepository extends JpaRepository<ModuleProgress, Long> {

    Optional<ModuleProgress> findByUserAndModule(User user, Module module);

    List<ModuleProgress> findByUser(User user);

    @Query("SELECT COUNT(mp) FROM ModuleProgress mp " +
            "WHERE mp.user = :user AND mp.module.test = :test AND mp.completed = true")
    long countByUserAndTestAndCompleted(@Param("user") User user,
                                        @Param("test") Test test);

    @Query("SELECT MAX(mp.completedAt) FROM ModuleProgress mp " +
            "WHERE mp.user = :user AND mp.module.test = :test AND mp.completed = true")
    LocalDateTime findLastCompletedAt(@Param("user") User user,
                                      @Param("test") Test test);

    /** Cascade-delete all progress for a user (called before deleting user) */
    @Modifying
    @Transactional
    @Query("DELETE FROM ModuleProgress mp WHERE mp.user = :user")
    void deleteByUser(@Param("user") User user);

    /** Cascade-delete all progress for a module (called before deleting module) */
    @Modifying
    @Transactional
    @Query("DELETE FROM ModuleProgress mp WHERE mp.module = :module")
    void deleteByModule(@Param("module") Module module);
}