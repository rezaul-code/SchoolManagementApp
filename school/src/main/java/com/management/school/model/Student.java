package com.management.school.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Student {
    
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String phone;
    
    private LocalDate dateOfBirth;
    
    @Column(unique = true, nullable = false)
    private String rollNumber;
    
    @Column(nullable = false)
    private String grade;
    
    @Column(nullable = false)
    private String section;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
    
    @Column(nullable = false)
    private LocalDate admissionDate;
    
    private String guardianName;
    private String guardianPhone;
    private String guardianEmail;
    
    // Changed: Use nullable foreign key instead of OneToOne with cascade
    @Column(name = "address_id")
    private Long addressId;
    
    // Transient field to hold address object temporarily
    @Transient
    private Address address;
    
    @Column(nullable = false)
    private boolean active = true;
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
    
    // Constructors
    public Student() {}
    
    public Student(String name, String email, String phone, 
                   LocalDate dateOfBirth, String rollNumber, String grade, String section,
                   Gender gender, LocalDate admissionDate) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.rollNumber = rollNumber;
        this.grade = grade;
        this.section = section;
        this.gender = gender;
        this.admissionDate = admissionDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    
    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }
    
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    
    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }
    
    public String getGuardianEmail() { return guardianEmail; }
    public void setGuardianEmail(String guardianEmail) { this.guardianEmail = guardianEmail; }
    
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}