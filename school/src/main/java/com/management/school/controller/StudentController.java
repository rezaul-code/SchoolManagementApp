package com.management.school.controller;

import com.management.school.core.SchoolSpringFXMLLoader;
import com.management.school.model.Student;
import com.management.school.service.StudentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class StudentController {

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, Long> idColumn;
    @FXML private TableColumn<Student, String> rollNumberColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> gradeColumn;
    @FXML private TableColumn<Student, String> emailColumn;
    @FXML private TableColumn<Student, String> phoneColumn;
    @FXML private TableColumn<Student, String> activeColumn;
    @FXML private TableColumn<Student, Void> actionsColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> gradeComboBox;
    @FXML private ComboBox<String> sectionComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private Button addButton;

    @Autowired
    private StudentService studentService;
    
    @Autowired
    private SchoolSpringFXMLLoader schoolSpringFXMLLoader;

    private final List<String> classLevels = List.of(
            "Nursery", "KG", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X");
    private final List<String> sections = List.of("A", "B");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilterComboBoxes();
        loadAllStudents();
        setupFilterListeners();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleLongProperty(cellData.getValue().getId()).asObject());
        
        rollNumberColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRollNumber()));
        
        nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
        
        gradeColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue();
            return new SimpleStringProperty(student.getGrade() + "-" + student.getSection());
        });
        
        emailColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEmail()));
        
        phoneColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPhone()));
        
        activeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));
        
        // Style the active column
        activeColumn.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Active")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red;");
                    }
                }
            }
        });
        
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox actionBox = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("action-button");
                deleteButton.getStyleClass().add("delete-button");
                actionBox.setAlignment(Pos.CENTER);

                editButton.setOnAction(event -> {
                    Student student = getTableView().getItems().get(getIndex());
                    handleEdit(student);
                });

                deleteButton.setOnAction(event -> {
                    Student student = getTableView().getItems().get(getIndex());
                    handleDelete(student);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    private void setupFilterComboBoxes() {
        // Populate grade combo box
        gradeComboBox.setItems(FXCollections.observableArrayList(classLevels));
        
        // Populate section combo box
        sectionComboBox.setItems(FXCollections.observableArrayList(sections));
        
        // Populate year combo box (last 10 years)
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = IntStream.rangeClosed(currentYear - 9, currentYear)
                .boxed()
                .sorted((a, b) -> b - a)
                .collect(Collectors.toList());
        yearComboBox.setItems(FXCollections.observableArrayList(years));
    }

    private void setupFilterListeners() {
        gradeComboBox.setOnAction(event -> applyFilters());
        sectionComboBox.setOnAction(event -> applyFilters());
        yearComboBox.setOnAction(event -> applyFilters());
    }

    private void applyFilters() {
        String selectedGrade = gradeComboBox.getValue();
        String selectedSection = sectionComboBox.getValue();
        Integer selectedYear = yearComboBox.getValue();

        if (selectedGrade != null && selectedSection != null && selectedYear != null) {
            List<Student> filteredStudents = studentService.getStudentsByFilter(
                    selectedGrade, selectedSection, selectedYear);
            studentTable.setItems(FXCollections.observableArrayList(filteredStudents));
        } else {
            loadAllStudents();
        }
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            List<Student> searchResults = studentService.searchStudents(searchTerm);
            studentTable.setItems(FXCollections.observableArrayList(searchResults));
        } else {
            loadAllStudents();
        }
    }

    @FXML
    private void handleAdd() {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Add New Student");
            dialog.setHeaderText("Enter Student Information");
            
            // Create TabPane for organizing form
            TabPane tabPane = new TabPane();
            
            // TAB 1: Student Information
            Tab studentInfoTab = new Tab("Student Information");
            SchoolSpringFXMLLoader.FXMLView<StudentFormController> formView = 
                schoolSpringFXMLLoader.loadWithController("/fxml/student_form.fxml");
            studentInfoTab.setContent(formView.getRoot());
            studentInfoTab.setClosable(false);
            
            // TAB 2: Documents
            Tab documentsTab = new Tab("Documents");
            SchoolSpringFXMLLoader.FXMLView<DocumentUploadController> docView = 
                schoolSpringFXMLLoader.loadWithController("/fxml/document_upload.fxml");
            documentsTab.setContent(docView.getRoot());
            documentsTab.setClosable(false);
            
            tabPane.getTabs().addAll(studentInfoTab, documentsTab);
            
            dialog.getDialogPane().setContent(tabPane);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Set minimum size
            dialog.getDialogPane().setMinWidth(700);
            dialog.getDialogPane().setMinHeight(600);
            
            Optional<ButtonType> result = dialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (formView.getController().validateInputs()) {
                    Student newStudent = formView.getController().getStudentData(null);
                    
                    // Save student first
                    Student savedStudent = studentService.createStudent(newStudent);
                    
                    // Now enable document uploads
                    docView.getController().setStudent(savedStudent);
                    
                    showAlert("Success", "Student added successfully!", Alert.AlertType.INFORMATION);
                    refreshTable();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleEdit(Student student) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Student");
            dialog.setHeaderText("Edit Student Information");
            
            // Create TabPane
            TabPane tabPane = new TabPane();
            
            // TAB 1: Student Information
            Tab studentInfoTab = new Tab("Student Information");
            SchoolSpringFXMLLoader.FXMLView<StudentFormController> formView = 
                schoolSpringFXMLLoader.loadWithController("/fxml/student-form.fxml");
            formView.getController().setStudent(student); // Load existing data
            studentInfoTab.setContent(formView.getRoot());
            studentInfoTab.setClosable(false);
            
            // TAB 2: Documents
            Tab documentsTab = new Tab("Documents");
            SchoolSpringFXMLLoader.FXMLView<DocumentUploadController> docView = 
                schoolSpringFXMLLoader.loadWithController("/fxml/document-upload.fxml");
            docView.getController().setStudent(student); // Load existing documents
            documentsTab.setContent(docView.getRoot());
            documentsTab.setClosable(false);
            
            tabPane.getTabs().addAll(studentInfoTab, documentsTab);
            
            dialog.getDialogPane().setContent(tabPane);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            dialog.getDialogPane().setMinWidth(700);
            dialog.getDialogPane().setMinHeight(600);
            
            Optional<ButtonType> result = dialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (formView.getController().validateInputs()) {
                    Student updatedStudent = formView.getController().getStudentData(student);
                    studentService.updateStudent(student.getId(), updatedStudent);
                    
                    showAlert("Success", "Student updated successfully!", Alert.AlertType.INFORMATION);
                    refreshTable();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to edit student: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDelete(Student student) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Student");
        confirmation.setHeaderText("Delete " + student.getName());
        confirmation.setContentText("Are you sure you want to delete this student? This action cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                studentService.deleteStudent(student.getId());
                showAlert("Success", "Student deleted successfully!", Alert.AlertType.INFORMATION);
                refreshTable();
            } catch (Exception e) {
                showAlert("Error", "Failed to delete student: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void loadAllStudents() {
        List<Student> students = studentService.getAllStudents();
        studentTable.setItems(FXCollections.observableArrayList(students));
    }

    private void refreshTable() {
        // Clear filters
        gradeComboBox.setValue(null);
        sectionComboBox.setValue(null);
        yearComboBox.setValue(null);
        searchField.clear();
        
        // Reload all students
        loadAllStudents();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}