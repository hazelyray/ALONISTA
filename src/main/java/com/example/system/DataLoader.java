package com.example.system;

import com.example.system.models.User;
import com.example.system.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Create test users if database is empty
        if (userRepository.count() == 0) {
            
            // Test Student User
            User student = new User();
            student.setUsername("student");
            student.setPassword(passwordEncoder.encode("password123"));
            student.setFullName("John Doe");
            student.setEmail("john.doe@example.com");
            student.setRole("STUDENT");
            student.setEnabled(true);
            userRepository.save(student);
            
            // Test Admin User
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Admin User");
            admin.setEmail("admin@example.com");
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            userRepository.save(admin);
            
            System.out.println("==============================================");
            System.out.println("Test users created:");
            System.out.println("Student - username: student, password: password123");
            System.out.println("Admin - username: admin, password: admin123");
            System.out.println("==============================================");
        }
    }
}