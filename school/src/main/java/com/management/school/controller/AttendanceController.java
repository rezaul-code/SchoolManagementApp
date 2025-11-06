package com.management.school.controller;

import com.management.school.core.AdminSession;
import com.management.school.model.Attendance;
import com.management.school.model.Attendance.AttendanceStatus;
import com.management.school.model.Student;
import com.management.school.service.AttendanceService;
import com.management.school.service.StudentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private AdminSession adminSession;

    // Filter controls
    @FXML
    private ComboBox<String> gradeComboBox;
    @FXML
    private ComboBox<String> sectionComboBox;
    @FXML
    private DatePicker attendanceDatePicker;
    @FXML
    private Button loadStudentsButton;
    @FXML
    private Button saveAttendanceButton;
    @FXML
    private Button viewHistoryButton;
    @FXML
    private Label statusLabel;

    // Attendance table
    @FXML
    private TableView<AttendanceRow> attendanceTable;
    @FXML
    private TableColumn<AttendanceRow, String> rollNumberColumn;
    @FXML
    private TableColumn<AttendanceRow, String> nameColumn;
    @FXML
    private TableColumn<AttendanceRow, String> gradeColumn;
    @FXML
    private TableColumn<AttendanceRow, Void> statusColumn;
    @FXML
    private TableColumn<AttendanceRow, String> remarksColumn;

    private final List<String> classLevels = List.of("Nursery", "KG", "I", "II", "III", "IV", "V", 
                                                      "VI", "VII", "VIII", "IX", "X");
    private final List<String> sections = List.of("A", "B");

    @FXML
    public void initialize() {
        setupTableColumns();
        populateFilters();
        
        attendanceTable.setPlaceholder(new Label("Please select grade, section, and date, then click 'Load Students'"));
        
        // Set default date to today
        attendanceDatePicker.setValue(LocalDate.now());
        
        statusLabel.setText("");
    }

    private void setupTableColumns() {
        rollNumberColumn.setCellValueFactory(new PropertyValueFactory<>("rollNumber"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        gradeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getGrade() + "-" + cellData.getValue().getSection())
        );
        
        // Status column with radio buttons
        statusColumn.setCellFactory(column -> new TableCell<AttendanceRow, Void>() {
            private final ToggleGroup toggleGroup = new ToggleGroup();
            private final RadioButton presentBtn = new RadioButton("P");
            private final RadioButton absentBtn = new RadioButton("A");
            private final RadioButton lateBtn = new RadioButton("L");
            private final RadioButton excusedBtn = new RadioButton("E");
            private final HBox statusBox = new HBox(8, presentBtn, absentBtn, lateBtn, excusedBtn);

            {
                presentBtn.setToggleGroup(toggleGroup);
                absentBtn.setToggleGroup(toggleGroup);
                lateBtn.setToggleGroup(toggleGroup);
                excusedBtn.setToggleGroup(toggleGroup);
                
                presentBtn.getStyleClass().add("status-radio");
                absentBtn.getStyleClass().add("status-radio");
                lateBtn.getStyleClass().add("status-radio");
                excusedBtn.getStyleClass().add("status-radio");
                
                statusBox.setAlignment(Pos.CENTER);
                
                // Set default to present
                presentBtn.setSelected(true);
                
                // Update row data when changed
                toggleGroup.selectedToggleProperty().addListener((obs, old, newToggle) -> {
                    AttendanceRow row = getTableView().getItems().get(getIndex());
                    if (newToggle == presentBtn) {
                        row.setStatus(AttendanceStatus.PRESENT);
                    } else if (newToggle == absentBtn) {
                        row.setStatus(AttendanceStatus.ABSENT);
                    } else if (newToggle == lateBtn) {
                        row.setStatus(AttendanceStatus.LATE);
                    } else if (newToggle == excusedBtn) {
                        row.setStatus(AttendanceStatus.EXCUSED);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    AttendanceRow row = getTableView().getItems().get(getIndex());
                    
                    // Set the correct radio button based on existing status
                    switch (row.getStatus()) {
                        case PRESENT -> presentBtn.setSelected(true);
                        case ABSENT -> absentBtn.setSelected(true);
                        case LATE -> lateBtn.setSelected(true);
                        case EXCUSED -> excusedBtn.setSelected(true);
                    }
                    
                    setGraphic(statusBox);
                }
            }
        });
        
        // Remarks column with editable text field
        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));
        remarksColumn.setCellFactory(column -> new TableCell<AttendanceRow, String>() {
            private final TextField textField = new TextField();
            
            {
                textField.setPromptText("Add remarks...");
                textField.textProperty().addListener((obs, old, newVal) -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        AttendanceRow row = getTableView().getItems().get(getIndex());
                        row.setRemarks(newVal);
                    }
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    textField.setText(item);
                    setGraphic(textField);
                }
            }
        });
    }

    private void populateFilters() {
        gradeComboBox.setItems(FXCollections.observableArrayList(classLevels));
        gradeComboBox.setValue("X");
        
        sectionComboBox.setItems(FXCollections.observableArrayList(sections));
        sectionComboBox.setValue("A");
    }

    @FXML
    private void handleLoadStudents() {
        String grade = gradeComboBox.getValue();
        String section = sectionComboBox.getValue();
        LocalDate date = attendanceDatePicker.getValue();

        if (grade == null || section == null || date == null) {
            showAlert("Validation Error", "Please select grade, section, and date", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Get students for the selected class
            List<Student> students = attendanceService.getStudentsForAttendance(grade, section, date);
            
            if (students.isEmpty()) {
                showAlert("No Students", "No active students found for " + grade + "-" + section, 
                         Alert.AlertType.INFORMATION);
                attendanceTable.setItems(FXCollections.observableArrayList());
                return;
            }

            // Get existing attendance for this date
            List<Attendance> existingAttendance = attendanceService.getAttendanceByDate(date);
            
            // Create attendance rows
            List<AttendanceRow> rows = new ArrayList<>();
            for (Student student : students) {
                AttendanceRow row = new AttendanceRow(student);
                
                // Check if attendance already exists
                Optional<Attendance> existing = existingAttendance.stream()
                        .filter(a -> a.getStudentId().equals(student.getId()))
                        .findFirst();
                
                if (existing.isPresent()) {
                    row.setStatus(existing.get().getStatus());
                    row.setRemarks(existing.get().getRemarks());
                    row.setAttendanceId(existing.get().getId());
                } else {
                    row.setStatus(AttendanceStatus.PRESENT); // Default
                }
                
                rows.add(row);
            }
            
            attendanceTable.setItems(FXCollections.observableArrayList(rows));
            
            // Update status label
            boolean hasExisting = existingAttendance.stream()
                    .anyMatch(a -> students.stream()
                            .anyMatch(s -> s.getId().equals(a.getStudentId())));
            
            if (hasExisting) {
                statusLabel.setText("✓ Attendance already marked for this date. You can update it.");
                statusLabel.setStyle("-fx-text-fill: #ff9800;");
            } else {
                statusLabel.setText("No attendance marked yet for this date.");
                statusLabel.setStyle("-fx-text-fill: #666;");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load students: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSaveAttendance() {
        LocalDate date = attendanceDatePicker.getValue();
        
        if (attendanceTable.getItems().isEmpty()) {
            showAlert("No Data", "Please load students first", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Save");
        confirmation.setHeaderText("Save Attendance");
        confirmation.setContentText("Do you want to save attendance for " + date + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String markedBy = adminSession.getCurrentAdmin() != null 
                            ? adminSession.getCurrentAdmin().getUsername() 
                            : "System";
                    
                    List<Attendance> attendanceList = new ArrayList<>();
                    
                    for (AttendanceRow row : attendanceTable.getItems()) {
                        Attendance attendance = new Attendance();
                        attendance.setId(row.getAttendanceId()); // Will be null for new records
                        attendance.setStudentId(row.getStudentId());
                        attendance.setAttendanceDate(date);
                        attendance.setStatus(row.getStatus());
                        attendance.setRemarks(row.getRemarks());
                        attendance.setMarkedBy(markedBy);
                        attendance.setMarkedAt(LocalDate.now());
                        
                        attendanceList.add(attendance);
                    }
                    
                    attendanceService.markBulkAttendance(attendanceList);
                    
                    showAlert("Success", "Attendance saved successfully!", Alert.AlertType.INFORMATION);
                    statusLabel.setText("✓ Attendance saved for " + date);
                    statusLabel.setStyle("-fx-text-fill: #4caf50;");
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to save attendance: " + e.getMessage(), 
                             Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleViewHistory() {
        // TODO: Open attendance history view
        showAlert("Coming Soon", "Attendance history view will be implemented soon", 
                 Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner class to represent a row in the attendance table
    public static class AttendanceRow {
        private Long attendanceId;
        private Long studentId;
        private String rollNumber;
        private String name;
        private String grade;
        private String section;
        private AttendanceStatus status;
        private String remarks;

        public AttendanceRow(Student student) {
            this.studentId = student.getId();
            this.rollNumber = student.getRollNumber();
            this.name = student.getName();
            this.grade = student.getGrade();
            this.section = student.getSection();
            this.status = AttendanceStatus.PRESENT;
            this.remarks = "";
        }

        // Getters and Setters
        public Long getAttendanceId() { return attendanceId; }
        public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }
        
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        
        public String getRollNumber() { return rollNumber; }
        public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        
        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }
        
        public AttendanceStatus getStatus() { return status; }
        public void setStatus(AttendanceStatus status) { this.status = status; }
        
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }
}