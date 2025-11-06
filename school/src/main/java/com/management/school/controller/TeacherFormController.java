package com.management.school.controller;

import com.management.school.model.Address;
import com.management.school.model.Teacher;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;

@Controller
public class TeacherFormController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private DatePicker dobPicker;
    @FXML
    private TextField employeeIdField;
    @FXML
    private ComboBox<String> subjectField;
    @FXML
    private TextField qualificationField;
    @FXML
    private ComboBox<String> genderComboBox;
    @FXML
    private DatePicker joiningDatePicker;
    @FXML
    private TextField designationField;
    @FXML
    private TextField emergencyContactNameField;
    @FXML
    private TextField emergencyContactPhoneField;
    @FXML
    private TextField streetField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField stateField;
    @FXML
    private TextField zipCodeField;
    @FXML
    private TextField countryField;

    private final List<String> subjects = List.of(
        "Mathematics", "Physics", "Chemistry", "Biology", 
        "English", "Hindi", "History", "Geography", 
        "Computer Science", "Physical Education", "Art", "Music"
    );
    
    private final List<String> genders = List.of("Male", "Female", "Other");

    @FXML
    public void initialize() {
        subjectField.setItems(FXCollections.observableArrayList(subjects));
        genderComboBox.setItems(FXCollections.observableArrayList(genders));
    }

    public void setTeacher(Teacher teacher) {
        if (teacher != null) {
            nameField.setText(teacher.getName());
            emailField.setText(teacher.getEmail());
            phoneField.setText(teacher.getPhone());
            dobPicker.setValue(teacher.getDateOfBirth());
            employeeIdField.setText(teacher.getEmployeeId());
            subjectField.setValue(teacher.getSubject());
            qualificationField.setText(teacher.getQualification());
            genderComboBox.setValue(teacher.getGender());
            joiningDatePicker.setValue(teacher.getJoiningDate());
            designationField.setText(teacher.getDesignation());
            emergencyContactNameField.setText(teacher.getEmergencyContactName());
            emergencyContactPhoneField.setText(teacher.getEmergencyContactPhone());

            if (teacher.getAddress() != null) {
                streetField.setText(teacher.getAddress().getStreet());
                cityField.setText(teacher.getAddress().getCity());
                stateField.setText(teacher.getAddress().getState());
                zipCodeField.setText(teacher.getAddress().getPinCode());
                countryField.setText(teacher.getAddress().getCountry());
            }
        }
    }

    public boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errors.append("- Name is required\n");
        }

        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            errors.append("- Email is required\n");
        } else if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.append("- Invalid email format\n");
        }

        if (employeeIdField.getText() == null || employeeIdField.getText().trim().isEmpty()) {
            errors.append("- Employee ID is required\n");
        }

        if (subjectField.getValue() == null) {
            errors.append("- Subject is required\n");
        }

        if (qualificationField.getText() == null || qualificationField.getText().trim().isEmpty()) {
            errors.append("- Qualification is required\n");
        }

        if (genderComboBox.getValue() == null) {
            errors.append("- Gender is required\n");
        }

        if (joiningDatePicker.getValue() == null) {
            errors.append("- Joining date is required\n");
        } else if (joiningDatePicker.getValue().isAfter(LocalDate.now())) {
            errors.append("- Joining date cannot be in the future\n");
        }

        if (errors.length() > 0) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    public Teacher getTeacherData(Teacher existingTeacher) {
        Teacher teacher = existingTeacher != null ? existingTeacher : new Teacher();

        teacher.setName(nameField.getText().trim());
        teacher.setEmail(emailField.getText().trim());
        teacher.setPhone(phoneField.getText() != null ? phoneField.getText().trim() : null);
        teacher.setDateOfBirth(dobPicker.getValue());
        teacher.setEmployeeId(employeeIdField.getText().trim());
        teacher.setSubject(subjectField.getValue());
        teacher.setQualification(qualificationField.getText().trim());
        teacher.setGender(genderComboBox.getValue());
        teacher.setJoiningDate(joiningDatePicker.getValue());
        teacher.setDesignation(designationField.getText() != null ? designationField.getText().trim() : null);
        teacher.setEmergencyContactName(emergencyContactNameField.getText() != null ? emergencyContactNameField.getText().trim() : null);
        teacher.setEmergencyContactPhone(emergencyContactPhoneField.getText() != null ? emergencyContactPhoneField.getText().trim() : null);

        // Build address if any field is filled
        if (isAnyAddressFieldFilled()) {
            Address address = new Address();
            address.setStreet(streetField.getText() != null ? streetField.getText().trim() : null);
            address.setCity(cityField.getText() != null ? cityField.getText().trim() : null);
            address.setState(stateField.getText() != null ? stateField.getText().trim() : null);
            address.setPinCode(zipCodeField.getText() != null ? zipCodeField.getText().trim() : null);
            address.setCountry(countryField.getText() != null ? countryField.getText().trim() : null);
            teacher.setAddress(address);
        }

        return teacher;
    }

    private boolean isAnyAddressFieldFilled() {
        return (streetField.getText() != null && !streetField.getText().trim().isEmpty()) ||
               (cityField.getText() != null && !cityField.getText().trim().isEmpty()) ||
               (stateField.getText() != null && !stateField.getText().trim().isEmpty()) ||
               (zipCodeField.getText() != null && !zipCodeField.getText().trim().isEmpty()) ||
               (countryField.getText() != null && !countryField.getText().trim().isEmpty());
    }
}