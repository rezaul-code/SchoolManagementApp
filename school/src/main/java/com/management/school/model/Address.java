package com.management.school.model;

import jakarta.persistence.*;

@Entity
@Table(name = "addresses")
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String street;
    private String city;
    private String state;
    private String pinCode;
    private String country;
    
    // Constructors
    public Address() {}
    
    public Address(String street, String city, String state, String pinCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.pinCode = pinCode;
        this.country = country;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getPinCode() {
        return pinCode;
    }
    
    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
}