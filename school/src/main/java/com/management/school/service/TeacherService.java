package com.management.school.service;

import com.management.school.model.Teacher;
import com.management.school.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TeacherService {
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    public Teacher createTeacher(Teacher teacher) {
        if (teacherRepository.findByEmail(teacher.getEmail()).isPresent()) {
            throw new RuntimeException("Teacher with email already exists");
        }
        if (teacherRepository.findByEmployeeId(teacher.getEmployeeId()).isPresent()) {
            throw new RuntimeException("Teacher with employee ID already exists");
        }
        return teacherRepository.save(teacher);
    }
    
    public Teacher updateTeacher(Long id, Teacher teacherDetails) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));
        
        teacher.setName(teacherDetails.getName());
        teacher.setEmail(teacherDetails.getEmail());
        teacher.setPhone(teacherDetails.getPhone());
        teacher.setDateOfBirth(teacherDetails.getDateOfBirth());
        teacher.setGender(teacherDetails.getGender());
        teacher.setQualification(teacherDetails.getQualification());
        teacher.setSpecialization(teacherDetails.getSpecialization());
        teacher.setSubjects(teacherDetails.getSubjects());
        teacher.setSalary(teacherDetails.getSalary());
        teacher.setAddress(teacherDetails.getAddress());
        teacher.setEmergencyContactName(teacherDetails.getEmergencyContactName());
        teacher.setEmergencyContactPhone(teacherDetails.getEmergencyContactPhone());
        teacher.setActive(teacherDetails.isActive());
        
        return teacherRepository.save(teacher);
    }
    
    public Optional<Teacher> getTeacherById(Long id) {
        return teacherRepository.findById(id);
    }
    
    public Optional<Teacher> getTeacherByEmail(String email) {
        return teacherRepository.findByEmail(email);
    }
    
    public Optional<Teacher> getTeacherByEmployeeId(String employeeId) {
        return teacherRepository.findByEmployeeId(employeeId);
    }
    
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }
    
    public List<Teacher> getActiveTeachers() {
        return teacherRepository.findByActive(true);
    }
    
    public List<Teacher> searchTeachers(String searchTerm) {
        return teacherRepository.findByNameContaining(searchTerm);
    }
    
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));
        teacher.setActive(false);
        teacherRepository.save(teacher);
    }
    
    public void permanentlyDeleteTeacher(Long id) {
        teacherRepository.deleteById(id);
    }
    
    public long getTotalTeacherCount() {
        return teacherRepository.count();
    }
    
    public long getActiveTeacherCount() {
        return teacherRepository.findByActive(true).size();
    }
}