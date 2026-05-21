package com.ankit.elearning.repository;

import com.ankit.elearning.entity.Module;
import com.ankit.elearning.entity.Test;
import com.ankit.elearning.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByTestOrderByOrderIndexAsc(Test test);

    /** Used when deleting an author — find all modules they created */
    List<Module> findByCreatedBy(User createdBy);
}