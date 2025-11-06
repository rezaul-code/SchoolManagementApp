package com.management.school.controller;

import com.management.school.core.SchoolSpringFXMLLoader;
import com.management.school.model.Teacher;
import com.management.school.service.TeacherService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private SchoolSpringFXMLLoader fxmlLoader;

    // Filter ComboBoxes
    @FXML
    private ComboBox<String> subjectComboBox;
    @FXML
    private ComboBox<Integer> yearComboBox;

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Teacher> teacherTable;
    @FXML
    private TableColumn<Teacher, Long> idColumn;
    @FXML
    private TableColumn<Teacher, String> employeeIdColumn;
    @FXML
    private TableColumn<Teacher, String> nameColumn;
    @FXML
    private TableColumn<Teacher, String> subjectColumn;
    @FXML
    private TableColumn<Teacher, String> emailColumn;
    @FXML
    private TableColumn<Teacher, String> phoneColumn;
    @FXML
    private TableColumn<Teacher, Boolean> activeColumn;
    @FXML
    private TableColumn<Teacher, Void> actionsColumn;

    // Subject definitions
    private final List<String> subjects = List.of(
        "Mathematics", "Physics", "Chemistry", "Biology", 
        "English", "Hindi", "History", "Geography", 
        "Computer Science", "Physical Education", "Art", "Music"
    );

    @FXML
    public void initialize() {
        setupTableColumns();
        setupActionsColumn();
        populateFilterComboBoxes();

        teacherTable.setPlaceholder(new Label("No teachers found for the selected filters."));

        // Add listeners to filters
        subjectComboBox.setOnAction(e -> loadTeachersByFilter());
        yearComboBox.setOnAction(e -> loadTeachersByFilter());
        
        // Load initial data
        loadTeachersByFilter();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Custom cell for active status with badge styling
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeColumn.setCellFactory(column -> new TableCell<Teacher, Boolean>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(active ? "Active" : "Inactive");
                    statusLabel.getStyleClass().add(active ? "status-active" : "status-inactive");
                    setGraphic(statusLabel);
                }
            }
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(new Callback<TableColumn<Teacher, Void>, TableCell<Teacher, Void>>() {
            @Override
            public TableCell<Teacher, Void> call(TableColumn<Teacher, Void> param) {
                return new TableCell<Teacher, Void>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final HBox actionBox = new HBox(8, editBtn, deleteBtn);

                    {
                        editBtn.getStyleClass().add("edit-button");
                        deleteBtn.getStyleClass().add("delete-button");
                        actionBox.setAlignment(Pos.CENTER);

                        editBtn.setOnAction(event -> {
                            Teacher teacher = getTableView().getItems().get(getIndex());
                            showTeacherDialog(teacher);
                        });

                        deleteBtn.setOnAction(event -> {
                            Teacher teacher = getTableView().getItems().get(getIndex());
                            handleDeleteTeacher(teacher);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : actionBox);
                    }
                };
            }
        });
    }

    private void populateFilterComboBoxes() {
        subjectComboBox.setItems(FXCollections.observableArrayList(subjects));
        subjectComboBox.setValue("Mathematics");

        int currentYear = LocalDate.now().getYear();
        List<Integer> years = IntStream.rangeClosed(currentYear - 10, currentYear).boxed().collect(Collectors.toList());
        yearComboBox.setItems(FXCollections.observableArrayList(years));
        yearComboBox.setValue(currentYear);
    }

    @FXML
    private void loadTeachersByFilter() {
        String subject = subjectComboBox.getValue();
        Integer year = yearComboBox.getValue();

        if (subject == null || year == null) {
            return;
        }

        System.out.println("Filtering for: Subject=" + subject + ", Year=" + year);

        List<Teacher> filteredTeachers = teacherService.getTeachersByFilter(subject, year);
        teacherTable.setItems(FXCollections.observableArrayList(filteredTeachers));
    }

    @FXML
    private void handleAdd() {
        showTeacherDialog(null);
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadTeachersByFilter();
        } else {
            List<Teacher> results = teacherService.searchTeachers(searchTerm.trim());
            teacherTable.setItems(FXCollections.observableArrayList(results));
        }
    }

    private void showTeacherDialog(Teacher teacher) {
        try {
            // 1. Load the FXML view and its controller
            SchoolSpringFXMLLoader.FXMLView<TeacherFormController> fxmlView = 
                fxmlLoader.loadWithController("/fxml/teacher_form.fxml");

            Parent formRoot = fxmlView.getRoot();
            TeacherFormController formController = fxmlView.getController();

            // 2. Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(teacher == null ? "Add New Teacher" : "Edit Teacher");
            dialog.getDialogPane().setContent(formRoot);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // 3. Populate the form if editing
            if (teacher != null) {
                formController.setTeacher(teacher);
            }

            // 4. Add validation to OK button
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                // Validate inputs
                if (!formController.validateInputs()) {
                    event.consume();
                    return;
                }

                // Get data from the form controller
                Teacher teacherToSave = formController.getTeacherData(teacher);

                // Save the data using the service
                try {
                    if (teacher != null) {
                        teacherService.updateTeacher(teacher.getId(), teacherToSave);
                        showAlert("Success", "Teacher updated successfully!", Alert.AlertType.INFORMATION);
                    } else {
                        teacherService.createTeacher(teacherToSave);
                        showAlert("Success", "Teacher added successfully!", Alert.AlertType.INFORMATION);

                        // Update filters to show the new teacher
                        int newTeacherYear = teacherToSave.getJoiningDate().getYear();
                        
                        // Add the new year if it's not in the list
                        if (!yearComboBox.getItems().contains(newTeacherYear)) {
                            yearComboBox.getItems().add(newTeacherYear);
                            FXCollections.sort(yearComboBox.getItems());
                        }

                        // Save current listeners
                        var subjectAction = subjectComboBox.getOnAction();
                        var yearAction = yearComboBox.getOnAction();
                        
                        // Temporarily disable listeners
                        subjectComboBox.setOnAction(null);
                        yearComboBox.setOnAction(null);
                        
                        // Set values
                        subjectComboBox.setValue(teacherToSave.getSubject());
                        yearComboBox.setValue(newTeacherYear);
                        
                        // Restore listeners
                        subjectComboBox.setOnAction(subjectAction);
                        yearComboBox.setOnAction(yearAction);
                    }
                    
                    // Refresh the table
                    loadTeachersByFilter(); 
                    
                } catch (IllegalArgumentException e) {
                    showAlert("Validation Error", e.getMessage(), Alert.AlertType.ERROR);
                    event.consume();
                } catch (Exception e) {
                    showAlert("Error", "Failed to save teacher: " + e.getMessage(), Alert.AlertType.ERROR);
                    event.consume();
                }
            });

            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load teacher form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDeleteTeacher(Teacher teacher) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Deactivate Teacher");
        confirmation.setContentText("Are you sure you want to deactivate " + teacher.getName() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    teacherService.deleteTeacher(teacher.getId());
                    showAlert("Success", "Teacher deactivated successfully!", Alert.AlertType.INFORMATION);
                    loadTeachersByFilter();
                } catch (Exception e) {
                    showAlert("Error", "Failed to deactivate teacher: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}