package com.example.system.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.system.models.StudentBalance;

@Repository
public interface StudentBalanceRepository extends JpaRepository<StudentBalance, Long> {
    
    Optional<StudentBalance> findByStudentId(String studentId);
    
    Optional<StudentBalance> findByUserId(Long userId);
}