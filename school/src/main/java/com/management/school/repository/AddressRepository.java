package com.management.school.repository;

import com.management.school.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // Spring Data JPA will automatically create methods like:
    // - save(Address address)
    // - findById(Long id)
    // - findAll()
    // - deleteById(Long id)
    // ...and many more!
}