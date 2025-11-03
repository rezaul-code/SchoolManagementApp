package com.management.school.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.management.school.core.AdminSession;
import com.management.school.core.SchoolSpringFXMLLoader;
import com.management.school.model.Admin;
import com.management.school.repository.AdminRepository;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Component
public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    
    @Autowired private AdminRepository adminRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AdminSession adminSession;
    @Autowired private SchoolSpringFXMLLoader fxmlLoader;
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password are required!");
            return;
        }
        
        try {
            Optional<Admin> adminOpt = adminRepository.findByAnyIdentifier(username);
            
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                if (passwordEncoder.matches(password, admin.getPasswordHash())) {
                    // Store logged-in admin in session
                    adminSession.setCurrentAdmin(admin);
                    openDashboard();
                    return;
                }
            }
            
            errorLabel.setText("Invalid Username or Password!");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error during login. Please try again.");
        }
    }
    
    private void openDashboard() {
        try {
            // Load dashboard via Spring loader
            Parent root = fxmlLoader.load("/fxml/dashboard.fxml");
            
            Stage dashboardStage = new Stage();
            Scene scene = new Scene(root);
            dashboardStage.setScene(scene);
            dashboardStage.setTitle("School Management - Dashboard");
            dashboardStage.setMaximized(true);
            dashboardStage.setResizable(true);
            dashboardStage.setMinWidth(900);
            dashboardStage.setMinHeight(600);
            dashboardStage.show();
            
            // Close login window
            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading dashboard.");
        }
    }
}