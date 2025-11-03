package com.management.school.repository;

import com.management.school.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    Optional<Admin> findByUsername(String username);
    
    Optional<Admin> findByEmail(String email);
    
    @Query("SELECT a FROM Admin a WHERE a.username = :identifier OR a.email = :identifier")
    Optional<Admin> findByAnyIdentifier(String identifier);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}