package com.enrollment.system.repository;

import com.enrollment.system.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    
    List<Subject> findByGradeLevel(Integer gradeLevel);
    
    List<Subject> findByGradeLevelAndIsActiveTrue(Integer gradeLevel);
    
    List<Subject> findByIsActiveTrue();
    
    Optional<Subject> findByNameAndGradeLevel(String name, Integer gradeLevel);
    
    boolean existsByNameAndGradeLevel(String name, Integer gradeLevel);
}

