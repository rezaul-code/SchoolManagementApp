package com.management.school.controller;

import com.management.school.model.Admin;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class ProfileDialogController {

    @FXML private Label nameLabel, usernameLabel, roleLabel;
    @FXML private Button closeButton;

    @FXML
    public void initialize() {
        closeButton.setId("cancelButton"); // Use a common style if available
        closeButton.setOnAction(e -> ((Stage) closeButton.getScene().getWindow()).close());
    }

    /**
     * Populates the dialog with the current admin's information.
     * @param admin The logged-in Admin object
     */
    public void setAdmin(Admin admin) {
        if (admin == null) return;
        
        nameLabel.setText(admin.getFullName());
        usernameLabel.setText(admin.getUsername());
        roleLabel.setText(admin.getRole());
    }
}