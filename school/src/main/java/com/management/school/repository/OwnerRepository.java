package com.management.school.repository;

import com.management.school.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    Optional<Owner> findByEmail(String email);
    List<Owner> findByActive(boolean active);
    List<Owner> findByNameContaining(String name);
}