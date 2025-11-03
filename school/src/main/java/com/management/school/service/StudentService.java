package com.management.school.service;

import com.management.school.model.Address;
import com.management.school.model.Student;
import com.management.school.repository.AddressRepository;
import com.management.school.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class StudentService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Transactional
    public Student createStudent(Student student) {
        // Check for duplicate email
        if (studentRepository.findByEmail(student.getEmail()).isPresent()) {
            throw new IllegalArgumentException("A student with this email already exists");
        }
        
        // Check for duplicate roll number
        if (studentRepository.findByRollNumber(student.getRollNumber()).isPresent()) {
            throw new IllegalArgumentException("A student with this roll number already exists");
        }
        
        // Ensure new students are active
        student.setActive(true);
        
        // Handle address separately for SQLite compatibility
        if (student.getAddress() != null) {
            Address address = addressRepository.save(student.getAddress());
            student.setAddressId(address.getId());
            student.setAddress(null); // Clear transient field before saving
        }
        
        Student savedStudent = studentRepository.save(student);
        
        // Reload address for return object
        if (savedStudent.getAddressId() != null) {
            savedStudent.setAddress(addressRepository.findById(savedStudent.getAddressId()).orElse(null));
        }
        
        return savedStudent;
    }
    
    @Transactional
    public Student updateStudent(Long id, Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Student not found with id: " + id));
        
        // Check email uniqueness (exclude current student)
        studentRepository.findByEmail(studentDetails.getEmail())
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("A student with this email already exists");
                }
            });
        
        // Check roll number uniqueness (exclude current student)
        studentRepository.findByRollNumber(studentDetails.getRollNumber())
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("A student with this roll number already exists");
                }
            });
        
        // Update fields
        student.setName(studentDetails.getName());
        student.setEmail(studentDetails.getEmail());
        student.setPhone(studentDetails.getPhone());
        student.setDateOfBirth(studentDetails.getDateOfBirth());
        student.setRollNumber(studentDetails.getRollNumber());
        student.setGrade(studentDetails.getGrade());
        student.setSection(studentDetails.getSection());
        student.setGender(studentDetails.getGender());
        student.setAdmissionDate(studentDetails.getAdmissionDate());
        student.setGuardianName(studentDetails.getGuardianName());
        student.setGuardianPhone(studentDetails.getGuardianPhone());
        student.setGuardianEmail(studentDetails.getGuardianEmail());
        
        // Handle address update
        if (studentDetails.getAddress() != null) {
            if (student.getAddressId() != null) {
                // Update existing address
                Address existingAddress = addressRepository.findById(student.getAddressId())
                        .orElse(new Address());
                existingAddress.setStreet(studentDetails.getAddress().getStreet());
                existingAddress.setCity(studentDetails.getAddress().getCity());
                existingAddress.setState(studentDetails.getAddress().getState());
                existingAddress.setPinCode(studentDetails.getAddress().getPinCode());
                existingAddress.setCountry(studentDetails.getAddress().getCountry());
                addressRepository.save(existingAddress);
            } else {
                // Create new address
                Address newAddress = addressRepository.save(studentDetails.getAddress());
                student.setAddressId(newAddress.getId());
            }
        }
        
        Student updatedStudent = studentRepository.save(student);
        
        // Reload address for return object
        if (updatedStudent.getAddressId() != null) {
            updatedStudent.setAddress(addressRepository.findById(updatedStudent.getAddressId()).orElse(null));
        }
        
        return updatedStudent;
    }
    
    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Student not found with id: " + id));
        
        // Load address if exists
        if (student.getAddressId() != null) {
            student.setAddress(addressRepository.findById(student.getAddressId()).orElse(null));
        }
        
        return student;
    }
    
    @Transactional(readOnly = true) 
    public List<Student> getAllStudents() {
        List<Student> students = studentRepository.findByActive(true);
        loadAddressesForStudents(students);
        return students;
    }
    
    @Transactional(readOnly = true)
    public List<Student> getStudentsByFilter(String grade, String section, int year) {
        String yearString = String.valueOf(year);
        List<Student> students = studentRepository.findByGradeSectionAndAdmissionYear(grade, section, yearString);
        loadAddressesForStudents(students);
        return students;
    }
    
    @Transactional(readOnly = true)
    public List<Student> searchStudents(String searchTerm) {
        List<Student> students = studentRepository.findByNameContainingAndActiveTrue(searchTerm);
        loadAddressesForStudents(students);
        return students;
    }
    
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Student not found with id: " + id));
        student.setActive(false);
        studentRepository.save(student);
    }
    
    // Helper method to load addresses for a list of students
    private void loadAddressesForStudents(List<Student> students) {
        for (Student student : students) {
            if (student.getAddressId() != null) {
                student.setAddress(addressRepository.findById(student.getAddressId()).orElse(null));
            }
        }
    }
}