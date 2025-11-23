package com.example.system;

import com.example.system.models.StudentBalance;
import com.example.system.models.User;
import com.example.system.repositories.StudentBalanceRepository;
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
    private StudentBalanceRepository studentBalanceRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Create test users if database is empty
        if (userRepository.count() == 0) {
            
            // Test Student User 1
            User student1 = new User();
            student1.setUsername("student");
            student1.setPassword(passwordEncoder.encode("password123"));
            student1.setFullName("John Doe");
            student1.setEmail("john.doe@sti.edu");
            student1.setStudentId("2021-000001");
            student1.setRole("STUDENT");
            student1.setEnabled(true);
            student1 = userRepository.save(student1);
            
            // Create tuition balance for student1
            StudentBalance balance1 = new StudentBalance();
            balance1.setStudentId("2021-000001");
            balance1.setTotalTuitionFee(50000.00);
            balance1.setAmountPaid(20000.00);
            balance1.setRemainingBalance(30000.00);
            balance1.setSemester("1st Semester");
            balance1.setSchoolYear("2024-2025");
            balance1.setUser(student1);
            studentBalanceRepository.save(balance1);
            
            // Test Student User 2
            User student2 = new User();
            student2.setUsername("maria");
            student2.setPassword(passwordEncoder.encode("maria123"));
            student2.setFullName("Maria Santos");
            student2.setEmail("maria.santos@sti.edu");
            student2.setStudentId("2021-000002");
            student2.setRole("STUDENT");
            student2.setEnabled(true);
            student2 = userRepository.save(student2);
            
            // Create tuition balance for student2
            StudentBalance balance2 = new StudentBalance();
            balance2.setStudentId("2021-000002");
            balance2.setTotalTuitionFee(55000.00);
            balance2.setAmountPaid(55000.00);
            balance2.setRemainingBalance(0.00);
            balance2.setSemester("1st Semester");
            balance2.setSchoolYear("2024-2025");
            balance2.setUser(student2);
            studentBalanceRepository.save(balance2);
            
            // Test Student User 3
            User student3 = new User();
            student3.setUsername("pedro");
            student3.setPassword(passwordEncoder.encode("pedro123"));
            student3.setFullName("Pedro Cruz");
            student3.setEmail("pedro.cruz@sti.edu");
            student3.setStudentId("2021-000003");
            student3.setRole("STUDENT");
            student3.setEnabled(true);
            student3 = userRepository.save(student3);
            
            // Create tuition balance for student3
            StudentBalance balance3 = new StudentBalance();
            balance3.setStudentId("2021-000003");
            balance3.setTotalTuitionFee(48000.00);
            balance3.setAmountPaid(10000.00);
            balance3.setRemainingBalance(38000.00);
            balance3.setSemester("1st Semester");
            balance3.setSchoolYear("2024-2025");
            balance3.setUser(student3);
            studentBalanceRepository.save(balance3);
            
            // Test Admin User
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Admin User");
            admin.setEmail("admin@sti.edu");
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            userRepository.save(admin);
            
            System.out.println("==============================================");
            System.out.println("Test accounts created:");
            System.out.println("1. Username: student | Password: password123 | Balance: ₱30,000");
            System.out.println("2. Username: maria   | Password: maria123   | Balance: ₱0 (Paid)");
            System.out.println("3. Username: pedro   | Password: pedro123   | Balance: ₱38,000");
            System.out.println("4. Username: admin   | Password: admin123   | Role: Admin");
            System.out.println("==============================================");
        }
    }
}