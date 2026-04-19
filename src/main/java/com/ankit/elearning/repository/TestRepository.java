package com.ankit.elearning.repository;

import com.ankit.elearning.entity.Test;
import com.ankit.elearning.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByPublishedTrue();
    List<Test> findByAuthor(User author);
}