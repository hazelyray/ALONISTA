package com.enrollment.system.service;

import com.enrollment.system.dto.UserDto;
import com.enrollment.system.model.User;
import com.enrollment.system.model.Subject;
import com.enrollment.system.model.Section;
import com.enrollment.system.model.TeacherAssignment;
import com.enrollment.system.repository.UserRepository;
import com.enrollment.system.repository.SubjectRepository;
import com.enrollment.system.repository.SectionRepository;
import com.enrollment.system.repository.TeacherAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
    
    @Autowired
    private TeacherAssignmentRepository teacherAssignmentRepository;
    
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
    
    @Transactional(readOnly = true)
    public long getUniqueSubjectCount(Long teacherId) {
        return teacherAssignmentRepository.countDistinctSubjectsByTeacherId(teacherId);
    }
    
    @Transactional(readOnly = true)
    public Map<Long, List<Section>> getSubjectSectionMap(Long teacherId) {
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByTeacherId(teacherId);
        
        Map<Long, List<Section>> subjectSectionMap = new HashMap<>();
        
        for (TeacherAssignment assignment : assignments) {
            // Access entities to trigger lazy loading within transaction
            Subject subject = assignment.getSubject();
            Section section = assignment.getSection();
            
            if (subject != null && subject.getId() != null && section != null) {
                Long subjectId = subject.getId();
                subjectSectionMap.computeIfAbsent(subjectId, k -> new java.util.ArrayList<>()).add(section);
            }
        }
        
        return subjectSectionMap;
    }
    
    @Transactional
    public void saveTeacherAssignments(Long teacherId, List<com.enrollment.system.controller.AssignTeacherSubjectsSectionsController.AssignmentRecord> assignments) {
        // Validate teacher exists
        User teacher = userRepository.findById(teacherId)
                .filter(user -> user.getRole() == User.UserRole.TEACHER)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));
        
        // Delete all existing assignments for this teacher first
        teacherAssignmentRepository.deleteByTeacherId(teacherId);
        
        // If no assignments to save, we're done
        if (assignments == null || assignments.isEmpty()) {
            return;
        }
        
        // Extract and validate IDs
        List<Long> subjectIds = assignments.stream()
                .map(a -> {
                    if (a.getSubject() == null || a.getSubject().getId() == null) {
                        throw new RuntimeException("Invalid assignment: subject is null or has no ID");
                    }
                    return a.getSubject().getId();
                })
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> sectionIds = assignments.stream()
                .map(a -> {
                    if (a.getSection() == null || a.getSection().getId() == null) {
                        throw new RuntimeException("Invalid assignment: section is null or has no ID");
                    }
                    return a.getSection().getId();
                })
                .distinct()
                .collect(Collectors.toList());
        
        // Validate maximum 8 unique subjects
        if (subjectIds.size() > 8) {
            throw new RuntimeException("A teacher can have a maximum of 8 unique subjects. Found: " + subjectIds.size());
        }
        
        // Fetch all subjects and sections fresh from database
        List<Subject> subjects = subjectRepository.findAllById(subjectIds);
        if (subjects.size() != subjectIds.size()) {
            throw new RuntimeException("One or more subjects not found in database");
        }
        
        List<Section> sections = sectionRepository.findAllById(sectionIds);
        if (sections.size() != sectionIds.size()) {
            throw new RuntimeException("One or more sections not found in database");
        }
        
        // Create a map for quick lookup
        Map<Long, Subject> subjectMap = subjects.stream()
                .collect(Collectors.toMap(Subject::getId, s -> s));
        Map<Long, Section> sectionMap = sections.stream()
                .collect(Collectors.toMap(Section::getId, s -> s));
        
        // Create and save new assignments using fresh entities from database
        for (com.enrollment.system.controller.AssignTeacherSubjectsSectionsController.AssignmentRecord assignmentRecord : assignments) {
            Long subjectId = assignmentRecord.getSubject().getId();
            Long sectionId = assignmentRecord.getSection().getId();
            
            // Get fresh entities from database
            Subject subject = subjectMap.get(subjectId);
            Section section = sectionMap.get(sectionId);
            
            if (subject == null) {
                throw new RuntimeException("Subject not found with id: " + subjectId);
            }
            if (section == null) {
                throw new RuntimeException("Section not found with id: " + sectionId);
            }
            
            // Create new assignment with fresh managed entities
            TeacherAssignment assignment = new TeacherAssignment(teacher, subject, section);
            teacherAssignmentRepository.save(assignment);
        }
    }
    
    @Transactional
    public void clearAllTeacherAssignments() {
        teacherAssignmentRepository.deleteAll();
    }
    
    @Transactional
    public void clearTeacherAssignments(Long teacherId) {
        teacherAssignmentRepository.deleteByTeacherId(teacherId);
    }
    
    @Transactional(readOnly = true)
    public List<Section> getSectionsForSubject(Long teacherId, Long subjectId) {
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByTeacherIdAndSubjectId(teacherId, subjectId);
        
        return assignments.stream()
                .map(TeacherAssignment::getSection)
                .filter(section -> section != null) // Filter out null sections
                .collect(Collectors.toList());
    }
}

