package com.management.school.repository;

import com.management.school.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByEmail(String email);
    Optional<Teacher> findByEmployeeId(String employeeId);
    List<Teacher> findByActive(boolean active);
    List<Teacher> findByNameContaining(String name);
}