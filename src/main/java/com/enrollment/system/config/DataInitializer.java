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
        
        // Initialize test student data - clear and recreate 30 students
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
        // Clear all existing students first
        long existingCount = studentRepository.count();
        if (existingCount > 0) {
            System.out.println("✓ Clearing " + existingCount + " existing student records...");
            studentRepository.deleteAll();
            System.out.println("✓ All existing students cleared.");
        }
        
        System.out.println("✓ Creating 30 new test student records with properly aligned data...");
        Random random = new Random();
        
        // Get active strands and sections for proper assignment
        java.util.List<Strand> activeStrands = strandRepository.findByIsActiveTrue();
        java.util.List<Section> activeSections = sectionRepository.findByIsActiveTrue();
        
        if (activeStrands.isEmpty()) {
            System.err.println("⚠ Warning: No active strands found. Cannot create students.");
            return;
        }
        
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
        
        // Parent relationships
        String[] relationships = {"Father", "Mother", "Guardian", "Other"};
        
        // Previous schools (all spelled out, no abbreviations)
        String[] previousSchools = {
            "Seguinon Elementary School", "San Jose Elementary School", "Rizal Elementary School",
            "Bonifacio Elementary School", "Aguinaldo Elementary School", "Mabini Elementary School",
            "Quezon Elementary School", "Osmena Elementary School", "Roxas Elementary School", 
            "Magsaysay Elementary School", "Lapu-Lapu Elementary School", "Luna Elementary School"
        };
        
        // Addresses
        String[] addresses = {
            "Barangay Poblacion, Seguinon", "Barangay San Jose, Seguinon", "Barangay Rizal, Seguinon",
            "Barangay Bonifacio, Seguinon", "Barangay Aguinaldo, Seguinon", "Barangay Mabini, Seguinon",
            "Barangay Quezon, Seguinon", "Barangay Osmena, Seguinon", "Barangay Roxas, Seguinon", 
            "Barangay Magsaysay, Seguinon"
        };
        
        // Generate unique contact numbers to avoid duplicates
        java.util.Set<String> usedContacts = new java.util.HashSet<>();
        java.util.Set<String> usedLrns = new java.util.HashSet<>();
        
        int successCount = 0;
        int enrolledCount = 0;
        int pendingCount = 0;
        
        for (int i = 0; i < 30; i++) {
            try {
                Student student = new Student();
                
                // Generate name (required field)
                String firstName = firstNames[i % firstNames.length];
                String lastName = lastNames[i % lastNames.length];
                String middleName = middleNames[i % middleNames.length];
                student.setName(firstName + " " + middleName + " " + lastName);
                
                // Generate birthdate (age between 16-18 to meet validation requirements)
                int age = 16 + random.nextInt(3); // 16-18 years old (must be >= 16)
                LocalDate birthdate = LocalDate.now().minusYears(age).minusDays(random.nextInt(365));
                student.setBirthdate(birthdate);
                student.setAge(age);
                
                // Sex (required field)
                student.setSex(i % 2 == 0 ? "Male" : "Female");
                
                // Address (required)
                student.setAddress(addresses[i % addresses.length]);
                
                // Contact number (Philippine format: 09XXXXXXXXX, must be unique)
                String contact;
                do {
                    contact = "09" + String.format("%09d", 100000000 + random.nextInt(900000000));
                } while (usedContacts.contains(contact));
                usedContacts.add(contact);
                student.setContactNumber(contact);
                
                // Parent/Guardian information (all required)
                String parentFirstName = firstNames[(i + 5) % firstNames.length];
                String parentLastName = lastNames[(i + 3) % lastNames.length];
                student.setParentGuardianName(parentFirstName + " " + parentLastName);
                
                // Parent contact (must be different from student contact)
                String parentContact;
                do {
                    parentContact = "09" + String.format("%09d", 100000000 + random.nextInt(900000000));
                } while (usedContacts.contains(parentContact) || parentContact.equals(contact));
                usedContacts.add(parentContact);
                student.setParentGuardianContact(parentContact);
                student.setParentGuardianRelationship(relationships[i % relationships.length]);
                
                // Grade level (required field - 11 or 12)
                int gradeLevel = (i < 15) ? 11 : 12; // First 15 are Grade 11, next 15 are Grade 12
                student.setGradeLevel(gradeLevel);
                
                // Strand (required - use active strands only)
                Strand selectedStrand = activeStrands.get(i % activeStrands.size());
                student.setStrand(selectedStrand.getName());
                
                // Previous school (required, spelled out)
                student.setPreviousSchool(previousSchools[i % previousSchools.length]);
                
                // GWA (required, between 75.0 and 100.0)
                double gwa = 75.0 + (random.nextDouble() * 25.0);
                student.setGwa(Math.round(gwa * 100.0) / 100.0);
                
                // LRN (required, exactly 12 digits, must be unique)
                String lrn;
                do {
                    lrn = String.format("%012d", 100000000000L + random.nextInt(900000000));
                } while (usedLrns.contains(lrn));
                usedLrns.add(lrn);
                student.setLrn(lrn);
                
                // Enrollment status (required - Enrolled or Pending)
                // First 20 students are Enrolled, last 10 are Pending
                String enrollmentStatus = (i < 20) ? "Enrolled" : "Pending";
                student.setEnrollmentStatus(enrollmentStatus);
                
                // Section assignment (required if Enrolled)
                if ("Enrolled".equals(enrollmentStatus)) {
                    // Find all sections matching the strand and grade level
                    java.util.List<Section> matchingSections = activeSections.stream()
                            .filter(s -> selectedStrand.getName().equals(s.getStrand()) 
                                    && gradeLevel == s.getGradeLevel())
                            .collect(java.util.stream.Collectors.toList());
                    
                    if (!matchingSections.isEmpty()) {
                        // Distribute students across available sections
                        Section assignedSection = matchingSections.get(enrolledCount % matchingSections.size());
                        student.setSection(assignedSection);
                        enrolledCount++;
                    } else {
                        // If no matching section found, set status to Pending
                        student.setEnrollmentStatus("Pending");
                        student.setSection(null);
                        pendingCount++;
                    }
                } else {
                    student.setSection(null);
                    pendingCount++;
                }
                
                // Ensure not archived
                student.setIsArchived(false);
                
                studentRepository.save(student);
                successCount++;
            } catch (Exception e) {
                System.err.println("⚠ Error creating student " + (i + 1) + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("✓ Successfully created " + successCount + " test student records");
        System.out.println("  - Enrolled: " + enrolledCount + " students (with sections assigned)");
        System.out.println("  - Pending: " + pendingCount + " students (no section assigned)");
    }
}