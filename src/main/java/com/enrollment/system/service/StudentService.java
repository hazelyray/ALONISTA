package com.enrollment.system.service;

import com.enrollment.system.dto.StudentDto;
import com.enrollment.system.model.Section;
import com.enrollment.system.model.Student;
import com.enrollment.system.repository.SectionRepository;
import com.enrollment.system.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private SectionRepository sectionRepository;
    
    @Transactional
    public StudentDto saveStudent(StudentDto studentDto) {
        Student student = new Student();
        student.setName(studentDto.getName());
        student.setBirthdate(studentDto.getBirthdate());
        
        // Calculate age from birthdate if provided
        if (studentDto.getBirthdate() != null) {
            student.setAge(calculateAge(studentDto.getBirthdate()));
        } else {
            student.setAge(studentDto.getAge());
        }
        
        student.setSex(studentDto.getSex());
        student.setAddress(studentDto.getAddress());
        student.setContactNumber(studentDto.getContactNumber());
        student.setParentGuardianName(studentDto.getParentGuardianName());
        student.setParentGuardianContact(studentDto.getParentGuardianContact());
        student.setParentGuardianRelationship(studentDto.getParentGuardianRelationship());
        student.setGradeLevel(studentDto.getGradeLevel());
        student.setStrand(studentDto.getStrand());
        student.setPreviousSchool(studentDto.getPreviousSchool());
        student.setGwa(studentDto.getGwa());
        student.setLrn(studentDto.getLrn());
        student.setEnrollmentStatus(studentDto.getEnrollmentStatus());
        
        // Handle section assignment with validation
        if (studentDto.getSectionId() != null) {
            // Validate: Only enrolled students can be assigned to a section
            if (!"Enrolled".equals(studentDto.getEnrollmentStatus())) {
                throw new RuntimeException("Sections can only be assigned to enrolled students. Please change the enrollment status to 'Enrolled' or remove the section assignment.");
            }
            Section section = sectionRepository.findById(studentDto.getSectionId())
                    .orElseThrow(() -> new RuntimeException("Section not found with id: " + studentDto.getSectionId()));
            student.setSection(section);
        } else {
            // Validate: Enrolled students must have a section
            if ("Enrolled".equals(studentDto.getEnrollmentStatus())) {
                throw new RuntimeException("Enrolled students must be assigned to a section.");
            }
            student.setSection(null);
        }
        
        // Ensure new students are not archived
        student.setIsArchived(false);
        
        Student savedStudent = studentRepository.save(student);
        return StudentDto.fromStudent(savedStudent);
    }
    
    @Transactional(readOnly = true)
    public List<StudentDto> getAllStudents() {
        return studentRepository.findAllWithSectionByOrderByNameAsc()
                .stream()
                .filter(student -> {
                    Boolean archived = student.getIsArchived();
                    return archived == null || !archived;
                })
                .map(StudentDto::fromStudent)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<StudentDto> getArchivedStudents() {
        return studentRepository.findAllWithSectionByOrderByNameAsc()
                .stream()
                .filter(student -> {
                    Boolean archived = student.getIsArchived();
                    return archived != null && archived;
                })
                .map(StudentDto::fromStudent)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public StudentDto getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(student -> {
                    // Force initialization of lazy-loaded section within transaction
                    if (student.getSection() != null) {
                        student.getSection().getName(); // Trigger lazy load
                    }
                    return StudentDto.fromStudent(student);
                })
                .orElse(null);
    }
    
    @Transactional
    public StudentDto updateStudent(Long id, StudentDto studentDto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
        
        student.setName(studentDto.getName());
        student.setBirthdate(studentDto.getBirthdate());
        
        // Calculate age from birthdate if provided
        if (studentDto.getBirthdate() != null) {
            student.setAge(calculateAge(studentDto.getBirthdate()));
        } else {
            student.setAge(studentDto.getAge());
        }
        
        student.setSex(studentDto.getSex());
        student.setAddress(studentDto.getAddress());
        student.setContactNumber(studentDto.getContactNumber());
        student.setParentGuardianName(studentDto.getParentGuardianName());
        student.setParentGuardianContact(studentDto.getParentGuardianContact());
        student.setParentGuardianRelationship(studentDto.getParentGuardianRelationship());
        student.setGradeLevel(studentDto.getGradeLevel());
        student.setStrand(studentDto.getStrand());
        student.setPreviousSchool(studentDto.getPreviousSchool());
        student.setGwa(studentDto.getGwa());
        student.setEnrollmentStatus(studentDto.getEnrollmentStatus());
        
        // Handle section assignment with validation
        if (studentDto.getSectionId() != null) {
            // Validate: Only enrolled students can be assigned to a section
            if (!"Enrolled".equals(studentDto.getEnrollmentStatus())) {
                throw new RuntimeException("Sections can only be assigned to enrolled students. Please change the enrollment status to 'Enrolled' or remove the section assignment.");
            }
            Section section = sectionRepository.findById(studentDto.getSectionId())
                    .orElseThrow(() -> new RuntimeException("Section not found with id: " + studentDto.getSectionId()));
            student.setSection(section);
        } else {
            // Validate: Enrolled students must have a section
            if ("Enrolled".equals(studentDto.getEnrollmentStatus())) {
                throw new RuntimeException("Enrolled students must be assigned to a section.");
            }
            student.setSection(null);
        }
        
        // Handle LRN update - check for uniqueness if LRN is being changed
        String newLrn = studentDto.getLrn() != null ? studentDto.getLrn().trim() : null;
        String currentLrn = student.getLrn();
        
        // Only update LRN if it's different and not empty
        if (newLrn != null && !newLrn.isEmpty()) {
            // Check if LRN is being changed
            if (!newLrn.equals(currentLrn)) {
                // Check if another student already has this LRN
                if (studentRepository.existsByLrn(newLrn)) {
                    // Check if it's the same student (shouldn't happen, but just in case)
                    java.util.Optional<Student> existingStudent = studentRepository.findByLrn(newLrn);
                    if (existingStudent.isPresent() && !existingStudent.get().getId().equals(id)) {
                        throw new RuntimeException("LRN " + newLrn + " already exists for another student.");
                    }
                }
                student.setLrn(newLrn);
            }
            // If LRN is the same, no need to update
        } else {
            // If new LRN is empty/null, clear it
            student.setLrn(null);
        }
        
        Student updatedStudent = studentRepository.save(student);
        return StudentDto.fromStudent(updatedStudent);
    }
    
    @Transactional
    public void archiveStudent(Long id, String archiveReason) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
        
        student.setIsArchived(true);
        student.setArchiveReason(archiveReason);
        student.setArchivedAt(LocalDateTime.now());
        
        studentRepository.save(student);
    }
    
    @Transactional
    public void restoreStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
        
        student.setIsArchived(false);
        student.setArchiveReason(null);
        student.setArchivedAt(null);
        
        studentRepository.save(student);
    }
    
    @Transactional
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }
    
    private int calculateAge(LocalDate birthdate) {
        if (birthdate == null) {
            return 0;
        }
        return Period.between(birthdate, LocalDate.now()).getYears();
    }
}
