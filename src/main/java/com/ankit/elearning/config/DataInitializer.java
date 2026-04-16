package com.ankit.elearning.config;

import com.ankit.elearning.entity.Role;
import com.ankit.elearning.entity.User;
import com.ankit.elearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Bean
    public org.springframework.boot.CommandLineRunner initAdmin() {
        return args -> {

            String adminEmail = "ad@gmail.com";

            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                User admin = new User();
                admin.setName("Super Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123")); // change later
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);

                System.out.println("🔥 ADMIN CREATED: admin@gmail.com / admin123");
            } else {
                System.out.println("✅ Admin already exists");
            }
        };
    }
}