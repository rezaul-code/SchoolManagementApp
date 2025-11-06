package com.management.school.service;

import com.management.school.model.Address;
import com.management.school.model.Teacher;
import com.management.school.repository.AddressRepository;
import com.management.school.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TeacherService {
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Transactional
    public Teacher createTeacher(Teacher teacher) {
        // Check for duplicate email
        if (teacherRepository.findByEmail(teacher.getEmail()).isPresent()) {
            throw new IllegalArgumentException("A teacher with this email already exists");
        }
        
        // Check for duplicate employee ID
        if (teacherRepository.findByEmployeeId(teacher.getEmployeeId()).isPresent()) {
            throw new IllegalArgumentException("A teacher with this employee ID already exists");
        }
        
        // Ensure new teachers are active
        teacher.setActive(true);
        
        // Handle address separately for SQLite compatibility
        if (teacher.getAddress() != null) {
            Address address = addressRepository.save(teacher.getAddress());
            teacher.setAddressId(address.getId());
            teacher.setAddress(null); // Clear transient field before saving
        }
        
        Teacher savedTeacher = teacherRepository.save(teacher);
        
        // Reload address for return object
        if (savedTeacher.getAddressId() != null) {
            savedTeacher.setAddress(addressRepository.findById(savedTeacher.getAddressId()).orElse(null));
        }
        
        return savedTeacher;
    }
    
    @Transactional
    public Teacher updateTeacher(Long id, Teacher teacherDetails) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Teacher not found with id: " + id));
        
        // Check email uniqueness (exclude current teacher)
        teacherRepository.findByEmail(teacherDetails.getEmail())
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("A teacher with this email already exists");
                }
            });
        
        // Check employee ID uniqueness (exclude current teacher)
        teacherRepository.findByEmployeeId(teacherDetails.getEmployeeId())
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("A teacher with this employee ID already exists");
                }
            });
        
        // Update fields
        teacher.setName(teacherDetails.getName());
        teacher.setEmail(teacherDetails.getEmail());
        teacher.setPhone(teacherDetails.getPhone());
        teacher.setDateOfBirth(teacherDetails.getDateOfBirth());
        teacher.setEmployeeId(teacherDetails.getEmployeeId());
        teacher.setSubject(teacherDetails.getSubject());
        teacher.setQualification(teacherDetails.getQualification());
        teacher.setGender(teacherDetails.getGender());
        teacher.setJoiningDate(teacherDetails.getJoiningDate());
        teacher.setDesignation(teacherDetails.getDesignation());
        teacher.setEmergencyContactName(teacherDetails.getEmergencyContactName());
        teacher.setEmergencyContactPhone(teacherDetails.getEmergencyContactPhone());
        
        // Handle address update
        if (teacherDetails.getAddress() != null) {
            if (teacher.getAddressId() != null) {
                // Update existing address
                Address existingAddress = addressRepository.findById(teacher.getAddressId())
                        .orElse(new Address());
                existingAddress.setStreet(teacherDetails.getAddress().getStreet());
                existingAddress.setCity(teacherDetails.getAddress().getCity());
                existingAddress.setState(teacherDetails.getAddress().getState());
                existingAddress.setPinCode(teacherDetails.getAddress().getPinCode());
                existingAddress.setCountry(teacherDetails.getAddress().getCountry());
                addressRepository.save(existingAddress);
            } else {
                // Create new address
                Address newAddress = addressRepository.save(teacherDetails.getAddress());
                teacher.setAddressId(newAddress.getId());
            }
        }
        
        Teacher updatedTeacher = teacherRepository.save(teacher);
        
        // Reload address for return object
        if (updatedTeacher.getAddressId() != null) {
            updatedTeacher.setAddress(addressRepository.findById(updatedTeacher.getAddressId()).orElse(null));
        }
        
        return updatedTeacher;
    }
    
    @Transactional(readOnly = true)
    public Teacher getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Teacher not found with id: " + id));
        
        // Load address if exists
        if (teacher.getAddressId() != null) {
            teacher.setAddress(addressRepository.findById(teacher.getAddressId()).orElse(null));
        }
        
        return teacher;
    }
    
    @Transactional(readOnly = true) 
    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = teacherRepository.findByActive(true);
        loadAddressesForTeachers(teachers);
        return teachers;
    }
    
    @Transactional(readOnly = true)
    public List<Teacher> getTeachersByFilter(String subject, int year) {
        String yearString = String.valueOf(year);
        List<Teacher> teachers = teacherRepository.findBySubjectAndJoiningYear(subject, yearString);
        loadAddressesForTeachers(teachers);
        return teachers;
    }
    
    @Transactional(readOnly = true)
    public List<Teacher> searchTeachers(String searchTerm) {
        List<Teacher> teachers = teacherRepository.findByNameContainingAndActiveTrue(searchTerm);
        loadAddressesForTeachers(teachers);
        return teachers;
    }
    
    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Teacher not found with id: " + id));
        teacher.setActive(false);
        teacherRepository.save(teacher);
    }
    
    // Helper method to load addresses for a list of teachers
    private void loadAddressesForTeachers(List<Teacher> teachers) {
        for (Teacher teacher : teachers) {
            if (teacher.getAddressId() != null) {
                teacher.setAddress(addressRepository.findById(teacher.getAddressId()).orElse(null));
            }
        }
    }
}