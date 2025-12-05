package com.enrollment.system.service;

import com.enrollment.system.model.Section;
import com.enrollment.system.model.Student;
import com.enrollment.system.repository.SectionRepository;
import com.enrollment.system.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SectionService {
    
    @Autowired
    private SectionRepository sectionRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }
    
    public List<Section> getActiveSections() {
        return sectionRepository.findByIsActiveTrue();
    }
    
    public List<Section> getSectionsByStrand(String strand) {
        return sectionRepository.findByStrand(strand);
    }
    
    public List<Section> getActiveSectionsByStrand(String strand) {
        return sectionRepository.findByStrandAndIsActiveTrue(strand);
    }
    
    public List<Section> getSectionsByStrandAndGradeLevel(String strand, Integer gradeLevel) {
        return sectionRepository.findByStrandAndGradeLevel(strand, gradeLevel);
    }
    
    public List<Section> getActiveSectionsByStrandAndGradeLevel(String strand, Integer gradeLevel) {
        return sectionRepository.findByStrandAndGradeLevelAndIsActiveTrue(strand, gradeLevel);
    }
    
    public Optional<Section> getSectionById(Long id) {
        return sectionRepository.findById(id);
    }
    
    @Transactional
    public Section saveSection(Section section) {
        return sectionRepository.save(section);
    }
    
    @Transactional
    public Section createSection(String name, String strand, Integer gradeLevel) {
        return createSection(name, strand, gradeLevel, null);
    }
    
    @Transactional
    public Section createSection(String name, String strand, Integer gradeLevel, Integer capacity) {
        // Check if section already exists
        if (sectionRepository.existsByNameAndStrandAndGradeLevel(name, strand, gradeLevel)) {
            throw new IllegalArgumentException("Section " + name + " already exists for " + strand + " Grade " + gradeLevel);
        }
        
        Section section = new Section(name, strand, gradeLevel);
        if (capacity != null) {
            section.setCapacity(capacity);
        }
        return sectionRepository.save(section);
    }
    
    @Transactional
    public void deleteSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Section not found with id: " + id));
        
        // Check if there are enrolled students in this section
        List<Student> enrolledStudents = studentRepository.findAll().stream()
                .filter(student -> {
                    Boolean archived = student.getIsArchived();
                    if (archived != null && archived) {
                        return false;
                    }
                    if (!"Enrolled".equals(student.getEnrollmentStatus())) {
                        return false;
                    }
                    if (student.getSection() == null) {
                        return false;
                    }
                    return student.getSection().getId().equals(id);
                })
                .toList();
        
        if (!enrolledStudents.isEmpty()) {
            throw new IllegalStateException("Cannot delete section " + section.getName() + 
                    " because it has " + enrolledStudents.size() + " enrolled student(s). " +
                    "Please reassign or archive these students first.");
        }
        
        // Soft delete by setting isActive to false
        section.setIsActive(false);
        sectionRepository.save(section);
    }
    
    @Transactional
    public Section updateSection(Long id, String name, String strand, Integer gradeLevel, Integer capacity) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Section not found with id: " + id));
        
        // Check if new name conflicts with existing section
        Optional<Section> existing = sectionRepository.findByNameAndStrandAndGradeLevel(name, strand, gradeLevel);
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new IllegalArgumentException("Section " + name + " already exists for " + strand + " Grade " + gradeLevel);
        }
        
        section.setName(name);
        section.setStrand(strand);
        section.setGradeLevel(gradeLevel);
        if (capacity != null) {
            section.setCapacity(capacity);
        }
        
        return sectionRepository.save(section);
    }
    
    @Transactional
    public void activateSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Section not found with id: " + id));
        section.setIsActive(true);
        sectionRepository.save(section);
    }
    
    @Transactional
    public void deletePermanently(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Section not found with id: " + id));
        
        // Check if there are enrolled students in this section
        List<Student> enrolledStudents = studentRepository.findAll().stream()
                .filter(student -> {
                    Boolean archived = student.getIsArchived();
                    if (archived != null && archived) {
                        return false;
                    }
                    if (!"Enrolled".equals(student.getEnrollmentStatus())) {
                        return false;
                    }
                    if (student.getSection() == null) {
                        return false;
                    }
                    return student.getSection().getId().equals(id);
                })
                .toList();
        
        if (!enrolledStudents.isEmpty()) {
            throw new IllegalStateException("Cannot delete permanently section " + section.getName() + 
                    " because it has " + enrolledStudents.size() + " enrolled student(s). " +
                    "Please reassign or archive these students first.");
        }
        
        // Permanent delete - actually remove from database
        sectionRepository.delete(section);
    }
}

