package com.enrollment.system.service;

import com.enrollment.system.model.Strand;
import com.enrollment.system.repository.StrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StrandService {
    
    @Autowired
    private StrandRepository strandRepository;
    
    public List<Strand> getAllStrands() {
        return strandRepository.findAllByOrderByNameAsc();
    }
    
    public List<Strand> getActiveStrands() {
        return strandRepository.findByIsActiveTrue();
    }
    
    public Optional<Strand> getStrandById(Long id) {
        return strandRepository.findById(id);
    }
    
    public Optional<Strand> getStrandByName(String name) {
        return strandRepository.findByName(name);
    }
    
    @Transactional
    public Strand createStrand(String name, String description) {
        // Check if strand already exists
        if (strandRepository.existsByName(name)) {
            throw new IllegalArgumentException("Strand " + name + " already exists");
        }
        
        Strand strand = new Strand(name, description);
        return strandRepository.save(strand);
    }
    
    @Transactional
    public Strand updateStrand(Long id, String name, String description) {
        Strand strand = strandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Strand not found with id: " + id));
        
        // Check if new name conflicts with existing strand
        Optional<Strand> existing = strandRepository.findByName(name);
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new IllegalArgumentException("Strand " + name + " already exists");
        }
        
        strand.setName(name);
        strand.setDescription(description);
        
        return strandRepository.save(strand);
    }
    
    @Transactional
    public void deleteStrand(Long id) {
        Strand strand = strandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Strand not found with id: " + id));
        
        // Soft delete by setting isActive to false
        strand.setIsActive(false);
        strandRepository.save(strand);
    }
}

