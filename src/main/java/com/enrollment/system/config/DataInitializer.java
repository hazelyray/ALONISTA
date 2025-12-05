package com.enrollment.system.config;

import com.enrollment.system.model.Section;
import com.enrollment.system.model.Student;
import com.enrollment.system.model.Strand;
import com.enrollment.system.model.User;
import com.enrollment.system.repository.SectionRepository;
import com.enrollment.system.repository.StudentRepository;
import com.enrollment.system.repository.StrandRepository;
import com.enrollment.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private SectionRepository sectionRepository;
    
    @Autowired
    private StrandRepository strandRepository;
    
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
        
        // Initialize strands first
        try {
            initializeStrands();
        } catch (Exception e) {
            System.err.println("⚠ Warning: Failed to initialize strands: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Initialize sections (3 per strand for Grade 11 and Grade 12)
        try {
            initializeSections();
        } catch (Exception e) {
            System.err.println("⚠ Warning: Failed to initialize sections: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Initialize test student data (only if no students exist)
        try {
            initializeTestStudents();
        } catch (Exception e) {
            System.err.println("⚠ Warning: Failed to initialize test student data: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=".repeat(60));
        System.out.println("Seguinon Stand Alone Senior High School");
        System.out.println("Enrollment System Started Successfully");
        System.out.println("=".repeat(60));
    }
    
    private void initializeStrands() {
        String[] strandNames = {"ABM", "HUMSS", "STEM", "GAS", "TVL"};
        String[] descriptions = {
            "Accountancy, Business and Management",
            "Humanities and Social Sciences",
            "Science, Technology, Engineering and Mathematics",
            "General Academic Strand",
            "Technical-Vocational-Livelihood"
        };
        
        int strandsCreated = 0;
        for (int i = 0; i < strandNames.length; i++) {
            if (!strandRepository.existsByName(strandNames[i])) {
                Strand strand = new Strand();
                strand.setName(strandNames[i]);
                strand.setDescription(descriptions[i]);
                strand.setIsActive(true);
                
                strandRepository.save(strand);
                strandsCreated++;
            }
        }
        
        if (strandsCreated > 0) {
            System.out.println("✓ Created " + strandsCreated + " default strands");
        } else {
            System.out.println("✓ Strands already exist in database");
        }
    }
    
    private void initializeSections() {
        String[] strands = {"ABM", "HUMSS", "STEM", "GAS", "TVL"};
        Integer[] gradeLevels = {11, 12};
        String[] sectionNames = {"A", "B", "C"};
        
        int sectionsCreated = 0;
        for (String strand : strands) {
            for (Integer gradeLevel : gradeLevels) {
                for (String sectionName : sectionNames) {
                    String fullSectionName = strand + "-" + gradeLevel + sectionName;
                    
                    // Check if section already exists
                    if (!sectionRepository.existsByNameAndStrandAndGradeLevel(fullSectionName, strand, gradeLevel)) {
                        Section section = new Section();
                        section.setName(fullSectionName);
                        section.setStrand(strand);
                        section.setGradeLevel(gradeLevel);
                        section.setIsActive(true);
                        section.setCapacity(40); // Default capacity
                        
                        sectionRepository.save(section);
                        sectionsCreated++;
                    }
                }
            }
        }
        
        if (sectionsCreated > 0) {
            System.out.println("✓ Created " + sectionsCreated + " sections (3 per strand for Grade 11 and 12)");
        } else {
            System.out.println("✓ Sections already exist in database");
        }
    }
    
    private void initializeTestStudents() {
        long existingCount = studentRepository.count();
        
        // Check if we need to add test students
        if (existingCount >= 30) {
            System.out.println("✓ Database already has " + existingCount + " students. Skipping test data initialization.");
            return;
        }
        
        // Calculate how many students to add
        int studentsToAdd = 30 - (int)existingCount;
        System.out.println("✓ Initializing " + studentsToAdd + " test student records (existing: " + existingCount + ")...");
        Random random = new Random();
        
        // Filipino first names
        String[] firstNames = {
            "Maria", "Juan", "Jose", "Ana", "Carlos", "Rosa", "Pedro", "Carmen", "Miguel", "Elena",
            "Antonio", "Francisco", "Teresa", "Manuel", "Isabel", "Ricardo", "Dolores", "Fernando", "Patricia",
            "Roberto", "Gloria", "Alberto", "Mercedes", "Eduardo", "Concepcion", "Ramon", "Esperanza", "Alfredo", "Rosario", "Cristina"
        };
        
        // Filipino last names
        String[] lastNames = {
            "Santos", "Reyes", "Cruz", "Bautista", "Villanueva", "Fernandez", "Ramos", "Torres", "Garcia", "Lopez",
            "Martinez", "Rodriguez", "Gonzalez", "Perez", "Sanchez", "Rivera", "Morales", "Ortiz", "Gutierrez", "Castillo",
            "Dela Cruz", "Mendoza", "Aquino", "Castro", "Romero", "Vargas", "Flores", "Herrera", "Jimenez", "Moreno"
        };
        
        // Middle names
        String[] middleNames = {
            "Cruz", "Reyes", "Santos", "Garcia", "Lopez", "Martinez", "Rodriguez", "Gonzalez", "Perez", "Sanchez"
        };
        
        // Strands
        String[] strands = {"ABM", "HUMSS", "STEM", "GAS", "TVL"};
        
        // Enrollment statuses
        String[] enrollmentStatuses = {"Enrolled", "Pending", "Transferred", "Graduated"};
        
        // Parent relationships
        String[] relationships = {"Father", "Mother", "Guardian"};
        
        // Previous schools
        String[] previousSchools = {
            "Seguinon Elementary School", "San Jose Elementary School", "Rizal Elementary School",
            "Bonifacio Elementary School", "Aguinaldo Elementary School", "Mabini Elementary School",
            "Quezon Elementary School", "Osmena Elementary School", "Roxas Elementary School", "Magsaysay Elementary School"
        };
        
        // Addresses
        String[] addresses = {
            "Barangay Poblacion, Seguinon", "Barangay San Jose, Seguinon", "Barangay Rizal, Seguinon",
            "Barangay Bonifacio, Seguinon", "Barangay Aguinaldo, Seguinon", "Barangay Mabini, Seguinon",
            "Barangay Quezon, Seguinon", "Barangay Osmena, Seguinon", "Barangay Roxas, Seguinon", "Barangay Magsaysay, Seguinon"
        };
        
        int successCount = 0;
        for (int i = 0; i < studentsToAdd; i++) {
            try {
                Student student = new Student();
                
                // Generate name (required field)
                String firstName = firstNames[random.nextInt(firstNames.length)];
                String lastName = lastNames[random.nextInt(lastNames.length)];
                String middleName = middleNames[random.nextInt(middleNames.length)];
                student.setName(firstName + " " + middleName + " " + lastName);
                
                // Generate birthdate (age between 15-18)
                int age = 15 + random.nextInt(4); // 15-18 years old
                LocalDate birthdate = LocalDate.now().minusYears(age).minusDays(random.nextInt(365));
                student.setBirthdate(birthdate);
                student.setAge(age);
                
                // Sex (required field)
                student.setSex(random.nextBoolean() ? "Male" : "Female");
                
                // Address
                student.setAddress(addresses[random.nextInt(addresses.length)]);
                
                // Contact number (Philippine format: 09XXXXXXXXX)
                String contact = "09" + String.format("%09d", random.nextInt(1000000000));
                student.setContactNumber(contact);
                
                // Parent/Guardian information
                String parentFirstName = firstNames[random.nextInt(firstNames.length)];
                String parentLastName = lastNames[random.nextInt(lastNames.length)];
                student.setParentGuardianName(parentFirstName + " " + parentLastName);
                String parentContact = "09" + String.format("%09d", random.nextInt(1000000000));
                student.setParentGuardianContact(parentContact);
                student.setParentGuardianRelationship(relationships[random.nextInt(relationships.length)]);
                
                // Grade level (required field - 11 or 12)
                int gradeLevel = random.nextBoolean() ? 11 : 12;
                student.setGradeLevel(gradeLevel);
                
                // Strand
                student.setStrand(strands[random.nextInt(strands.length)]);
                
                // Previous school
                student.setPreviousSchool(previousSchools[random.nextInt(previousSchools.length)]);
                
                // GWA (between 75.0 and 98.0)
                double gwa = 75.0 + (random.nextDouble() * 23.0);
                student.setGwa(Math.round(gwa * 100.0) / 100.0);
                
                // LRN (optional - 12 digits, some students may not have it)
                if (random.nextDouble() > 0.2) { // 80% chance of having LRN
                    String lrn = String.format("%012d", random.nextInt(1000000000));
                    student.setLrn(lrn);
                }
                
                // Enrollment status (required field)
                student.setEnrollmentStatus(enrollmentStatuses[random.nextInt(enrollmentStatuses.length)]);
                
                studentRepository.save(student);
                successCount++;
            } catch (Exception e) {
                System.err.println("⚠ Error creating student " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        System.out.println("✓ Successfully created " + successCount + " test student records");
    }
}