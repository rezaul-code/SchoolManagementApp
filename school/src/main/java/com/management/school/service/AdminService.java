package com.management.school.service;

import com.management.school.core.AdminSession;
import com.management.school.model.Admin;
import com.management.school.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminSession adminSession;

    /**
     * Handles the logic for changing a user's password.
     * This method is transactional, ensuring the change is committed.
     */
    @Transactional
    public void changePassword(String currentPassword, String newPassword, String retypePassword) 
            throws IllegalArgumentException {
        
        // 1. Get the logged-in admin from session
        Admin currentAdmin = adminSession.getCurrentAdmin();
        if (currentAdmin == null) {
            throw new IllegalArgumentException("No admin is logged in!");
        }

        // 2. Verify current password
        if (!passwordEncoder.matches(currentPassword, currentAdmin.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        // 3. Check if new passwords match
        if (!newPassword.equals(retypePassword)) {
            throw new IllegalArgumentException("New passwords do not match.");
        }

        // 4. Optional: password strength check
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }

        // 5. Encode and save new password
        currentAdmin.setPasswordHash(passwordEncoder.encode(newPassword));
        adminRepository.save(currentAdmin);
        
        // The @Transactional annotation will ensure this save is committed
    }
}