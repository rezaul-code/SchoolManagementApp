package com.management.school.repository;

import com.management.school.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    Optional<Teacher> findByEmail(String email);
    
    Optional<Teacher> findByEmployeeId(String employeeId);
    
    List<Teacher> findBySubject(String subject);
    
    List<Teacher> findByActive(boolean active);
    
    List<Teacher> findByNameContainingAndActiveTrue(String name);
    
    /**
     * Finds active teachers by subject and the year of their joining date.
     */
    @Query("SELECT t FROM Teacher t WHERE t.subject = :subject AND YEAR(t.joiningDate) = :year AND t.active = true")
    List<Teacher> findBySubjectAndJoiningYear(
            @Param("subject") String subject,
            @Param("year") String year
    );
}