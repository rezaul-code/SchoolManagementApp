package com.management.school.controller;

import com.management.school.model.Address;
import com.management.school.model.Student;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class StudentFormController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker dobPicker;
    @FXML private TextField rollNumberField;
    @FXML private ComboBox<String> gradeField;
    @FXML private ComboBox<String> sectionField;
    @FXML private ComboBox<Student.Gender> genderComboBox;
    @FXML private DatePicker admissionDatePicker;
    @FXML private TextField guardianNameField;
    @FXML private TextField guardianPhoneField;
    @FXML private TextField guardianEmailField;
    @FXML private TextField streetField;
    @FXML private TextField cityField;
    @FXML private TextField stateField;
    @FXML private TextField zipCodeField;
    @FXML private TextField countryField;

    private final List<String> classLevels = List.of(
            "Nursery", "KG", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X");
    private final List<String> sections = List.of("A", "B");

    @FXML
    public void initialize() {
        // Populate the dropdowns
        gradeField.setItems(FXCollections.observableArrayList(classLevels));
        sectionField.setItems(FXCollections.observableArrayList(sections));
        genderComboBox.setItems(FXCollections.observableArrayList(Student.Gender.values()));
    }

    /**
     * Populates the form fields with data from an existing student (for editing).
     */
    public void setStudent(Student student) {
        if (student == null) return;

        nameField.setText(student.getName());
        emailField.setText(student.getEmail());
        phoneField.setText(student.getPhone());
        dobPicker.setValue(student.getDateOfBirth());
        rollNumberField.setText(student.getRollNumber());
        gradeField.setValue(student.getGrade());
        sectionField.setValue(student.getSection());
        genderComboBox.setValue(student.getGender());
        admissionDatePicker.setValue(student.getAdmissionDate());
        guardianNameField.setText(student.getGuardianName());
        guardianPhoneField.setText(student.getGuardianPhone());
        guardianEmailField.setText(student.getGuardianEmail());

        if (student.getAddress() != null) {
            streetField.setText(student.getAddress().getStreet());
            cityField.setText(student.getAddress().getCity());
            stateField.setText(student.getAddress().getState());
            zipCodeField.setText(student.getAddress().getPinCode());
            countryField.setText(student.getAddress().getCountry());
        }
    }

    /**
     * Reads data from the form and creates/updates the Student object.
     */
    public Student getStudentData(Student studentToSave) {
        if (studentToSave == null) {
            studentToSave = new Student();
        }

        studentToSave.setName(nameField.getText().trim());
        studentToSave.setEmail(emailField.getText().trim());
        studentToSave.setPhone(phoneField.getText().trim());
        studentToSave.setDateOfBirth(dobPicker.getValue());
        studentToSave.setRollNumber(rollNumberField.getText().trim());
        studentToSave.setGrade(gradeField.getValue());
        studentToSave.setSection(sectionField.getValue());
        studentToSave.setGender(genderComboBox.getValue());
        studentToSave.setAdmissionDate(admissionDatePicker.getValue());
        studentToSave.setGuardianName(guardianNameField.getText().trim());
        studentToSave.setGuardianPhone(guardianPhoneField.getText().trim());
        studentToSave.setGuardianEmail(guardianEmailField.getText().trim());

        // Only create address if at least one field is filled
        if (!streetField.getText().trim().isEmpty() || 
            !cityField.getText().trim().isEmpty() ||
            !stateField.getText().trim().isEmpty() || 
            !zipCodeField.getText().trim().isEmpty() ||
            !countryField.getText().trim().isEmpty()) {
            
            Address address = studentToSave.getAddress() != null ? studentToSave.getAddress() : new Address();
            address.setStreet(streetField.getText().trim());
            address.setCity(cityField.getText().trim());
            address.setState(stateField.getText().trim());
            address.setPinCode(zipCodeField.getText().trim());
            address.setCountry(countryField.getText().trim());
            studentToSave.setAddress(address);
        }

        return studentToSave;
    }

    /**
     * Validates the required fields in the form.
     */
    public boolean validateInputs() {
        // Check required fields
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Name is required", Alert.AlertType.WARNING);
            return false;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Email is required", Alert.AlertType.WARNING);
            return false;
        }
        
        // Validate email format
        String email = emailField.getText().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Validation Error", "Please enter a valid email address", Alert.AlertType.WARNING);
            return false;
        }
        
        if (rollNumberField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Roll Number is required", Alert.AlertType.WARNING);
            return false;
        }
        
        if (gradeField.getValue() == null) {
            showAlert("Validation Error", "Grade is required", Alert.AlertType.WARNING);
            return false;
        }
        
        if (sectionField.getValue() == null) {
            showAlert("Validation Error", "Section is required", Alert.AlertType.WARNING);
            return false;
        }
        
        if (genderComboBox.getValue() == null) {
            showAlert("Validation Error", "Gender is required", Alert.AlertType.WARNING);
            return false;
        }
        
        if (admissionDatePicker.getValue() == null) {
            showAlert("Validation Error", "Admission Date is required", Alert.AlertType.WARNING);
            return false;
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Phone is required", Alert.AlertType.WARNING);
            return false;
        }
        
        // Validate admission date is not in the future
        if (admissionDatePicker.getValue().isAfter(LocalDate.now())) {
            showAlert("Validation Error", "Admission date cannot be in the future", Alert.AlertType.WARNING);
            return false;
        }
        
        // Validate date of birth if provided
        if (dobPicker.getValue() != null) {
            if (dobPicker.getValue().isAfter(LocalDate.now())) {
                showAlert("Validation Error", "Date of Birth cannot be in the future", Alert.AlertType.WARNING);
                return false;
            }
            
            // Check if age is reasonable (between 2 and 25 years for school students)
            int age = LocalDate.now().getYear() - dobPicker.getValue().getYear();
            if (age < 2 || age > 25) {
                showAlert("Validation Error", "Please enter a valid Date of Birth for a school student", Alert.AlertType.WARNING);
                return false;
            }
        }
        
        if (!phoneField.getText().trim().isEmpty()) {
            String phone = phoneField.getText().trim();
            if (!phone.matches("^[0-9+\\-\\s()]{10,15}$")) {
                showAlert("Validation Error", "Please enter a valid phone number (10-15 digits)", Alert.AlertType.WARNING);
                return false;
            }
        }
        
        // Validate guardian email format if provided
        if (!guardianEmailField.getText().trim().isEmpty()) {
            String guardianEmail = guardianEmailField.getText().trim();
            if (!guardianEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showAlert("Validation Error", "Please enter a valid guardian email address", Alert.AlertType.WARNING);
                return false;
            }
        }
        
        // Validate guardian phone format if provided
        if (!guardianPhoneField.getText().trim().isEmpty()) {
            String guardianPhone = guardianPhoneField.getText().trim();
            if (!guardianPhone.matches("^[0-9+\\-\\s()]{10,15}$")) {
                showAlert("Validation Error", "Please enter a valid guardian phone number (10-15 digits)", Alert.AlertType.WARNING);
                return false;
            }
        }
        
        return true;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}