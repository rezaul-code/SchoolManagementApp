package com.management.school.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teachers")
public class Teacher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String phone;
    
    @Column(nullable = false)
    private LocalDate dateOfBirth;
    
    @Column(unique = true, nullable = false)
    private String employeeId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
    
    @Column(nullable = false)
    private LocalDate joiningDate;
    
    @Column(nullable = false)
    private String qualification;
    
    private String specialization;
    
    @ElementCollection
    @CollectionTable(name = "teacher_subjects", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "subject")
    private Set<String> subjects = new HashSet<>();
    
    @Column(nullable = false)
    private Double salary;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;
    
    private String emergencyContactName;
    private String emergencyContactPhone;
    
    @Column(nullable = false)
    private boolean active = true;
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
    
    // Constructors
    public Teacher() {}
    
    public Teacher(String name, String email, String phone,
                   LocalDate dateOfBirth, String employeeId, Gender gender,
                   LocalDate joiningDate, String qualification, Double salary) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.employeeId = employeeId;
        this.gender = gender;
        this.joiningDate = joiningDate;
        this.qualification = qualification;
        this.salary = salary;
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
    
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public Set<String> getSubjects() { return subjects; }
    public void setSubjects(Set<String> subjects) { this.subjects = subjects; }
    
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
    
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { 
        this.emergencyContactName = emergencyContactName; 
    }
    
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { 
        this.emergencyContactPhone = emergencyContactPhone; 
    }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}