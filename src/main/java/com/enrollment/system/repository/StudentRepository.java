package com.enrollment.system.repository;

import com.enrollment.system.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    List<Student> findAllByOrderByNameAsc();
    
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.section ORDER BY s.name ASC")
    List<Student> findAllWithSectionByOrderByNameAsc();
    
    Optional<Student> findByLrn(String lrn);
    
    boolean existsByLrn(String lrn);
    
    List<Student> findByGradeLevel(Integer gradeLevel);
    
    List<Student> findByStrand(String strand);
    
    List<Student> findByEnrollmentStatus(String enrollmentStatus);
}
