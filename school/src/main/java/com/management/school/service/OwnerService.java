package com.management.school.service;

import com.management.school.model.Owner;
import com.management.school.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OwnerService {
    
    @Autowired
    private OwnerRepository ownerRepository;
    
    public Owner createOwner(Owner owner) {
        if (ownerRepository.findByEmail(owner.getEmail()).isPresent()) {
            throw new RuntimeException("Owner with email already exists");
        }
        return ownerRepository.save(owner);
    }
    
    public Owner updateOwner(Long id, Owner ownerDetails) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Owner not found with id: " + id));
        
        owner.setName(ownerDetails.getName());
        owner.setEmail(ownerDetails.getEmail());
        owner.setPhone(ownerDetails.getPhone());
        owner.setDateOfBirth(ownerDetails.getDateOfBirth());
        owner.setGender(ownerDetails.getGender());
        owner.setDesignation(ownerDetails.getDesignation());
        owner.setOwnershipPercentage(ownerDetails.getOwnershipPercentage());
        owner.setAddress(ownerDetails.getAddress());
        owner.setNationalId(ownerDetails.getNationalId());
        owner.setTaxId(ownerDetails.getTaxId());
        owner.setActive(ownerDetails.isActive());
        
        return ownerRepository.save(owner);
    }
    
    public Optional<Owner> getOwnerById(Long id) {
        return ownerRepository.findById(id);
    }
    
    public Optional<Owner> getOwnerByEmail(String email) {
        return ownerRepository.findByEmail(email);
    }
    
    public List<Owner> getAllOwners() {
        return ownerRepository.findAll();
    }
    
    public List<Owner> getActiveOwners() {
        return ownerRepository.findByActive(true);
    }
    
    public List<Owner> searchOwners(String searchTerm) {
        return ownerRepository.findByNameContaining(searchTerm);
    }
    
    public void deleteOwner(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Owner not found with id: " + id));
        owner.setActive(false);
        ownerRepository.save(owner);
    }
    
    public void permanentlyDeleteOwner(Long id) {
        ownerRepository.deleteById(id);
    }
    
    public long getTotalOwnerCount() {
        return ownerRepository.count();
    }
    
    public long getActiveOwnerCount() {
        return ownerRepository.findByActive(true).size();
    }
}