package com.ankit.elearning.config;

import com.ankit.elearning.entity.Role;
import com.ankit.elearning.entity.User;
import com.ankit.elearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {
    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Create Admin
            if (userRepository.findByEmail("admin@elearning.com").isEmpty()) {
                User admin = new User();
                admin.setName("Super Admin");
                admin.setEmail("admin@elearning.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
                System.out.println("✅ Admin created: admin@elearning.com / admin123");
            }

            // Create Author
            if (userRepository.findByEmail("author@elearning.com").isEmpty()) {
                User author = new User();
                author.setName("John Author");
                author.setEmail("author@elearning.com");
                author.setPassword(passwordEncoder.encode("author123"));
                author.setRole(Role.AUTHOR);
                userRepository.save(author);
                System.out.println("✅ Author created: author@elearning.com / author123");
            }

            // Create Student
            if (userRepository.findByEmail("student@elearning.com").isEmpty()) {
                User student = new User();
                student.setName("Jane Student");
                student.setEmail("student@elearning.com");
                student.setPassword(passwordEncoder.encode("student123"));
                student.setRole(Role.STUDENT);
                userRepository.save(student);
                System.out.println("✅ Student created: student@elearning.com / student123");
            }

            System.out.println("📊 Database initialized with sample users");
        };
    }
}