package com.enrollment.system.service;

import com.enrollment.system.dto.UserDto;
import com.enrollment.system.model.User;
import com.enrollment.system.model.Subject;
import com.enrollment.system.model.Section;
import com.enrollment.system.repository.UserRepository;
import com.enrollment.system.repository.SubjectRepository;
import com.enrollment.system.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private SectionRepository sectionRepository;
    
    @Transactional(readOnly = true)
    public List<UserDto> getAllTeachers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.UserRole.TEACHER)
                .map(UserDto::fromUser)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UserDto getTeacherById(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getRole() == User.UserRole.TEACHER)
                .map(UserDto::fromUser)
                .orElse(null);
    }
    
    @Transactional
    public UserDto createTeacher(UserDto teacherDto) {
        // Validate username uniqueness
        if (userRepository.existsByUsername(teacherDto.getUsername())) {
            throw new RuntimeException("Username already exists: " + teacherDto.getUsername());
        }
        
        // Validate email uniqueness if provided
        if (teacherDto.getEmail() != null && !teacherDto.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(teacherDto.getEmail())) {
                throw new RuntimeException("Email already exists: " + teacherDto.getEmail());
            }
        }
        
        // Validate required fields
        if (teacherDto.getUsername() == null || teacherDto.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        
        if (teacherDto.getFullName() == null || teacherDto.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        
        if (teacherDto.getPassword() == null || teacherDto.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        
        // Create new teacher user
        User teacher = new User();
        teacher.setUsername(teacherDto.getUsername().trim());
        teacher.setPassword(passwordEncoder.encode(teacherDto.getPassword()));
        teacher.setFullName(teacherDto.getFullName().trim());
        teacher.setEmail(teacherDto.getEmail() != null && !teacherDto.getEmail().trim().isEmpty() ? teacherDto.getEmail().trim() : null);
        teacher.setRole(User.UserRole.TEACHER);
        teacher.setIsActive(teacherDto.getIsActive() != null ? teacherDto.getIsActive() : true);
        // createdAt and updatedAt will be set by @PrePersist lifecycle callback
        
        User savedTeacher = userRepository.save(teacher);
        return UserDto.fromUser(savedTeacher);
    }
    
    @Transactional
    public UserDto updateTeacher(Long id, UserDto teacherDto) {
        User teacher = userRepository.findById(id)
                .filter(user -> user.getRole() == User.UserRole.TEACHER)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));
        
        // Validate username uniqueness if changed
        String newUsername = teacherDto.getUsername() != null ? teacherDto.getUsername().trim() : null;
        if (newUsername != null && !newUsername.equals(teacher.getUsername())) {
            if (userRepository.existsByUsername(newUsername)) {
                throw new RuntimeException("Username already exists: " + newUsername);
            }
            teacher.setUsername(newUsername);
        }
        
        // Validate email uniqueness if changed
        String newEmail = teacherDto.getEmail() != null ? teacherDto.getEmail().trim() : null;
        if (newEmail != null && !newEmail.equals(teacher.getEmail() != null ? teacher.getEmail() : "")) {
            if (!newEmail.isEmpty() && userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Email already exists: " + newEmail);
            }
            teacher.setEmail(newEmail.isEmpty() ? null : newEmail);
        }
        
        // Update full name
        if (teacherDto.getFullName() != null && !teacherDto.getFullName().trim().isEmpty()) {
            teacher.setFullName(teacherDto.getFullName().trim());
        }
        
        // Update password if provided
        if (teacherDto.getPassword() != null && !teacherDto.getPassword().trim().isEmpty()) {
            teacher.setPassword(passwordEncoder.encode(teacherDto.getPassword()));
        }
        
        // Update active status
        if (teacherDto.getIsActive() != null) {
            teacher.setIsActive(teacherDto.getIsActive());
        }
        
        teacher.setUpdatedAt(LocalDateTime.now());
        
        User updatedTeacher = userRepository.save(teacher);
        return UserDto.fromUser(updatedTeacher);
    }
    
    @Transactional
    public void deleteTeacher(Long id) {
        User teacher = userRepository.findById(id)
                .filter(user -> user.getRole() == User.UserRole.TEACHER)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));
        
        userRepository.delete(teacher);
    }
    
    @Transactional
    public void assignSubjects(Long teacherId, List<Long> subjectIds) {
        User teacher = userRepository.findById(teacherId)
                .filter(user -> user.getRole() == User.UserRole.TEACHER)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));
        
        // Validate maximum 8 subjects
        if (subjectIds != null && subjectIds.size() > 8) {
            throw new RuntimeException("A teacher can have a maximum of 8 subjects");
        }
        
        // Clear existing subjects
        teacher.getSubjects().clear();
        
        // Add new subjects
        if (subjectIds != null && !subjectIds.isEmpty()) {
            List<Subject> subjects = subjectRepository.findAllById(subjectIds);
            if (subjects.size() != subjectIds.size()) {
                throw new RuntimeException("One or more subjects not found");
            }
            teacher.getSubjects().addAll(subjects);
        }
        
        userRepository.save(teacher);
    }
    
    @Transactional
    public void assignSections(Long teacherId, List<Long> sectionIds) {
        User teacher = userRepository.findById(teacherId)
                .filter(user -> user.getRole() == User.UserRole.TEACHER)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));
        
        // Clear existing sections
        teacher.getSections().clear();
        
        // Add new sections
        if (sectionIds != null && !sectionIds.isEmpty()) {
            List<Section> sections = sectionRepository.findAllById(sectionIds);
            if (sections.size() != sectionIds.size()) {
                throw new RuntimeException("One or more sections not found");
            }
            teacher.getSections().addAll(sections);
        }
        
        userRepository.save(teacher);
    }
    
    @Transactional(readOnly = true)
    public List<Subject> getTeacherSubjects(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .filter(user -> user.getRole() == User.UserRole.TEACHER)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));
        
        // Force initialization of lazy collection
        java.util.List<Subject> subjects = new java.util.ArrayList<>(teacher.getSubjects());
        // Access the collection to trigger lazy loading
        teacher.getSubjects().size();
        
        return subjects;
    }
    
    @Transactional(readOnly = true)
    public List<Section> getTeacherSections(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .filter(user -> user.getRole() == User.UserRole.TEACHER)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));
        
        // Force initialization of lazy collection
        java.util.List<Section> sections = new java.util.ArrayList<>(teacher.getSections());
        // Access the collection to trigger lazy loading
        teacher.getSections().size();
        
        return sections;
    }
}

