package com.example.system.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_balance")
public class StudentBalance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String studentId;
    
    @Column(nullable = false)
    private Double totalTuitionFee;
    
    @Column(nullable = false)
    private Double amountPaid;
    
    @Column(nullable = false)
    private Double remainingBalance;
    
    @Column(nullable = false)
    private String semester;
    
    @Column(nullable = false)
    private String schoolYear;
    
    // Link to User
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    // Constructors
    public StudentBalance() {}
    
    public StudentBalance(String studentId, Double totalTuitionFee, Double amountPaid, 
                         Double remainingBalance, String semester, String schoolYear) {
        this.studentId = studentId;
        this.totalTuitionFee = totalTuitionFee;
        this.amountPaid = amountPaid;
        this.remainingBalance = remainingBalance;
        this.semester = semester;
        this.schoolYear = schoolYear;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public Double getTotalTuitionFee() {
        return totalTuitionFee;
    }
    
    public void setTotalTuitionFee(Double totalTuitionFee) {
        this.totalTuitionFee = totalTuitionFee;
    }
    
    public Double getAmountPaid() {
        return amountPaid;
    }
    
    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }
    
    public Double getRemainingBalance() {
        return remainingBalance;
    }
    
    public void setRemainingBalance(Double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public String getSchoolYear() {
        return schoolYear;
    }
    
    public void setSchoolYear(String schoolYear) {
        this.schoolYear = schoolYear;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
}