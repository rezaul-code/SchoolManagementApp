package com.management.school.controller;

import com.management.school.core.AdminSession;
import com.management.school.core.SchoolSpringFXMLLoader;
import com.management.school.model.Admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Modality; // Import Modality

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DashboardController {

    // ===== Tabs =====
    @FXML private TabPane tabPane;
    @FXML private Tab homeTab, studentTab, teacherTab, classTab, reportsTab, backupsTab;

    // ===== User Menu =====
    @FXML private MenuButton userMenu;
    @FXML private MenuItem profileMenuItem, changePasswordMenuItem, logoutMenuItem;
    
    @FXML private Label userNameLabel;
    
    @Autowired
    private SchoolSpringFXMLLoader loader;

    @Autowired
    private AdminSession adminSession;

    @FXML
    public void initialize() {
        // Load Home tab immediately
        loadTabContent(homeTab, "/fxml/home.fxml", "Home tab loaded.");

        // Set logged-in user
        Admin currentAdmin = adminSession.getCurrentAdmin();
        if (currentAdmin != null) {
            userNameLabel.setText(currentAdmin.getFullName().toUpperCase());
        } else {
            System.out.println("⚠ No active session, redirecting to login...");
            handleLogout();
            return;
        }
        
        // Listen to popup open/close for styling
        userMenu.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (isNowShowing) {
                if (!userMenu.getStyleClass().contains("popup-open")) {
                    userMenu.getStyleClass().add("popup-open");
                }
            } else {
                userMenu.getStyleClass().remove("popup-open");
            }
        });

        // Setup menu actions
        profileMenuItem.setOnAction(e -> handleProfile()); // <-- This is now active
        changePasswordMenuItem.setOnAction(e -> handleChangePassword());
        logoutMenuItem.setOnAction(e -> handleLogout());

        // Lazy-load other tabs
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            try {
                if (newTab == studentTab) {
                    loadTabContent(studentTab, "/fxml/student_management.fxml", "Student tab loaded.");
                } else if (newTab == teacherTab) {
                    loadTabContent(teacherTab, "/fxml/teacher-management.fxml", "Teacher tab loaded.");
                } else if (newTab == classTab) {
                    loadTabContent(classTab, "/fxml/class-management.fxml", "Class tab loaded.");
                } else if (newTab == reportsTab) {
                    loadTabContent(reportsTab, "/fxml/reports.fxml", "Reports tab loaded.");
                } else if (newTab == backupsTab) {
                    loadTabContent(backupsTab, "/fxml/backups.fxml", "backups tab loaded.");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ===== Load Tab Helper =====
    private void loadTabContent(Tab tab, String fxmlPath, String logMessage) {
        try {
            if (tab.getContent() == null) {
                Parent content = loader.load(fxmlPath);
                tab.setContent(content);
                System.out.println("📂 " + logMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            tab.setContent(new Label("Error loading view: " + fxmlPath));
        }
    }

    // ===== User Menu Actions =====
    
    /**
     * Handles opening the Profile dialog.
     * This is the NEWLY IMPLEMENTED method.
     */
    private void handleProfile() {
        Admin admin = adminSession.getCurrentAdmin();
        if (admin == null) return;

        try {
            // Load the FXML and its controller
            SchoolSpringFXMLLoader.FXMLView<ProfileDialogController> view = 
                    loader.loadWithController("/fxml/profile_dialog.fxml");
            
            Parent root = view.getRoot();
            
            // Pass the admin data to the dialog controller
            view.getController().setAdmin(admin); 

            // Create and show the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("User Profile");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(userMenu.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleChangePassword() {
        // TODO: Create and load /fxml/change_password.fxml
        System.out.println("Change Password clicked. Implement dialog.");
        showAlert("Not Implemented", "Change Password view is not yet implemented.");
    }

    private void handleLogout() {
        try {
            adminSession.logout();

            Parent loginRoot = loader.load("/fxml/login.fxml");
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.setTitle("Login - School Management");
            loginStage.setMaximized(true);
            loginStage.show();

            Stage dashboardStage = (Stage) userMenu.getScene().getWindow();
            dashboardStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}