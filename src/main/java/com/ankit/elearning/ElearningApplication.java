package com.ankit.elearning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ElearningApplication {
	public static void main(String[] args) {
		SpringApplication.run(ElearningApplication.class, args);
		System.out.println("🚀 E-Learning Platform Started Successfully!");
		System.out.println("📧 Email service configured with Brevo");
		System.out.println("💾 Database: PostgreSQL (data persists across restarts)");
	}
}