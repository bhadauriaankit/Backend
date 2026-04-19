package com.ankit.elearning.service;

import com.ankit.elearning.entity.Module;
import com.ankit.elearning.entity.Test;
import com.ankit.elearning.entity.User;
import com.ankit.elearning.repository.ModuleRepository;
import com.ankit.elearning.repository.TestRepository;
import com.ankit.elearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ModuleService {
    @Autowired private ModuleRepository moduleRepository;
    @Autowired private TestRepository testRepository;
    @Autowired private UserRepository userRepository;

    public Module createModule(Long testId, Module module, String userEmail) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        module.setTest(test);
        module.setCreatedBy(author);
        if (module.getOrderIndex() == null) {
            module.setOrderIndex(moduleRepository.findByTestOrderByOrderIndexAsc(test).size());
        }
        return moduleRepository.save(module);
    }

    public List<Module> getModulesByTest(Long testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        return moduleRepository.findByTestOrderByOrderIndexAsc(test);
    }

    public void deleteModule(Long moduleId) {
        moduleRepository.deleteById(moduleId);
    }
}