package com.management.school.core;

import com.management.school.model.Admin;
import com.management.school.repository.AdminRepository; // Assuming this is the correct path
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * This component runs once when the application starts.
 * It checks if a default admin user exists and creates one if the database is empty.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    // We use @Autowired on the constructor to inject the required services.
    @Autowired
    public DataInitializer(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Check if there are any admins in the database
        if (adminRepository.count() == 0) {
            System.out.println("No admin accounts found. Creating default admin user...");

            Admin defaultAdmin = new Admin();
            
            // NOTE: I am assuming your Admin model has these methods.
            // If your methods are different (e.g., setLoginId), you must change them here.
            defaultAdmin.setUsername("admin"); 
            defaultAdmin.setPasswordHash(passwordEncoder.encode("admin")); // Hash the password
            defaultAdmin.setFullName("Default Admin");
            // Set any other mandatory fields your Admin model might have
            // defaultAdmin.setEmail("admin@school.com");

            adminRepository.save(defaultAdmin);

            // Print the credentials to the console
            System.out.println("==================================================");
            System.out.println("Default admin user created:");
            System.out.println("Username: admin");
            System.out.println("Password: admin");
            System.out.println("==================================================");
        } else {
            System.out.println("Admin user(s) already exist. Skipping default admin creation.");
        }
    }
}
