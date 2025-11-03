package com.management.school.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "owners")
public class Owner {
    
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
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
    
    @Column(nullable = false)
    private String designation;
    
    @Column(nullable = false)
    private Double ownershipPercentage;
    
    @Column(nullable = false)
    private LocalDate appointmentDate;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;
    
    private String nationalId; // Aadhar, SSN, etc.
    private String taxId;
    
    @Column(nullable = false)
    private boolean active = true;
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
    
    // Constructors
    public Owner() {}
    
    public Owner(String name, String email, String phone,
                 LocalDate dateOfBirth, Gender gender, String designation,
                 Double ownershipPercentage, LocalDate appointmentDate) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.designation = designation;
        this.ownershipPercentage = ownershipPercentage;
        this.appointmentDate = appointmentDate;
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
    
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    
    public Double getOwnershipPercentage() { return ownershipPercentage; }
    public void setOwnershipPercentage(Double ownershipPercentage) { 
        this.ownershipPercentage = ownershipPercentage; 
    }
    
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { 
        this.appointmentDate = appointmentDate; 
    }
    
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}