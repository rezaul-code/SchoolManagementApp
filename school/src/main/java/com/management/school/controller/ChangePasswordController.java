package com.management.school.controller;

import com.management.school.service.AdminService; // Import the new service
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChangePasswordController {

    private Stage dialogStage;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField retypePasswordField;
    @FXML private Label messageLabel;
    @FXML private Button cancelButton;
    @FXML private Button changeButton;

    // --- NEW ---
    @Autowired
    private AdminService adminService; // Inject the service
    // --- REMOVE OLD REPO, ENCODER, AND SESSION ---

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // ===== THIS METHOD IS NOW MUCH SIMPLER =====
    @FXML
    private void handleChangePassword() {
        String currentPassword = currentPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String retypePassword = retypePasswordField.getText().trim();

        // Check for empty fields
        if (currentPassword.isEmpty() || newPassword.isEmpty() || retypePassword.isEmpty()) {
            messageLabel.setText("All fields are required!");
            return;
        }

        try {
            // Call the service to handle all logic
            adminService.changePassword(currentPassword, newPassword, retypePassword);

            // Success
            messageLabel.setText("Password changed successfully!");

            // Close the dialog
            if (dialogStage != null) {
                dialogStage.close();
            }

        } catch (IllegalArgumentException e) {
            // Display any validation errors from the service
            messageLabel.setText(e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected errors
            messageLabel.setText("An unexpected error occurred.");
            e.printStackTrace();
        }
    }
}