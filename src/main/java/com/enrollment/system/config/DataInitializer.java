package com.enrollment.system.config;

import com.enrollment.system.model.User;
import com.enrollment.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Administrator");
            admin.setEmail("admin@seguinonshs.edu.ph");
            admin.setRole(User.UserRole.ADMIN);
            admin.setIsActive(true);
            
            userRepository.save(admin);
            System.out.println("✓ Default admin user created - Username: admin, Password: admin123");
        }
        
        // Create default registrar user if not exists
        if (!userRepository.existsByUsername("registrar")) {
            User registrar = new User();
            registrar.setUsername("registrar");
            registrar.setPassword(passwordEncoder.encode("registrar123"));
            registrar.setFullName("School Registrar");
            registrar.setEmail("registrar@seguinonshs.edu.ph");
            registrar.setRole(User.UserRole.REGISTRAR);
            registrar.setIsActive(true);
            
            userRepository.save(registrar);
            System.out.println("✓ Default registrar user created - Username: registrar, Password: registrar123");
        }
        
        System.out.println("=".repeat(60));
        System.out.println("Seguinon Stand Alone Senior High School");
        System.out.println("Enrollment System Started Successfully");
        System.out.println("=".repeat(60));
    }
}