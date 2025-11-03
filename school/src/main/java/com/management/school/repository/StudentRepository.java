package com.management.school.repository;

import com.management.school.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    Optional<Student> findByRollNumber(String rollNumber);
    List<Student> findByGradeAndSection(String grade, String section);
    List<Student> findByActive(boolean active);
    List<Student> findByNameContaining(String name);
    
    // ----- NEW METHOD -----
    /**
     * Finds active students by grade, section, and the year of their admission.
     */
    @Query("SELECT s FROM Student s WHERE s.grade = :grade AND s.section = :section AND YEAR(s.admissionDate) = :year AND s.active = true")
    List<Student> findByGradeSectionAndAdmissionYear(
            @Param("grade") String grade, 
            @Param("section") String section, 
            @Param("year") String year
    );

    // --- (Optional) A better search method that only finds active students ---
    List<Student> findByNameContainingAndActiveTrue(String name);
}