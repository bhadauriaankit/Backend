package com.ankit.elearning.service;

import com.ankit.elearning.entity.Test;
import com.ankit.elearning.entity.User;
import com.ankit.elearning.repository.TestRepository;
import com.ankit.elearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {

    @Autowired private TestRepository testRepository;
    @Autowired private UserRepository userRepository;

    public Test createTest(Test test, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        test.setCreatedBy(user);
        test.setPublished(false); // needs admin approval
        return testRepository.save(test);
    }

    // students only see published tests
    public List<Test> getPublishedTests() {
        return testRepository.findByPublishedTrue();
    }

    // admin sees all
    public List<Test> getAllTests() {
        return testRepository.findAll();
    }

    public Test publishTest(Long testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        test.setPublished(true);
        return testRepository.save(test);
    }

    public void deleteTest(Long testId) {
        testRepository.deleteById(testId);
    }
}