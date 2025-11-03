package com.management.school.controller;

import com.management.school.core.SchoolSpringFXMLLoader;
import com.management.school.model.Student;
import com.management.school.service.StudentService;
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
public class StudentController {

	@Autowired
	private StudentService studentService;

	@Autowired
	private SchoolSpringFXMLLoader fxmlLoader;

	// Filter ComboBoxes
	@FXML
	private ComboBox<String> gradeComboBox;
	@FXML
	private ComboBox<String> sectionComboBox;
	@FXML
	private ComboBox<Integer> yearComboBox;

	@FXML
	private TextField searchField;
	@FXML
	private TableView<Student> studentTable;
	@FXML
	private TableColumn<Student, Long> idColumn;
	@FXML
	private TableColumn<Student, String> rollNumberColumn;
	@FXML
	private TableColumn<Student, String> nameColumn;
	@FXML
	private TableColumn<Student, String> gradeColumn;
	@FXML
	private TableColumn<Student, String> emailColumn;
	@FXML
	private TableColumn<Student, String> phoneColumn;
	@FXML
	private TableColumn<Student, Boolean> activeColumn;
	@FXML
	private TableColumn<Student, Void> actionsColumn;

	// Class and Section definitions
	private final List<String> classLevels = List.of("Nursery", "KG", "I", "II", "III", "IV", "V", "VI", "VII", "VIII",
			"IX", "X");
	private final List<String> sections = List.of("A", "B");

	@FXML
    public void initialize() {
        setupTableColumns();
        setupActionsColumn();
        populateFilterComboBoxes();

        // --- ADD THIS LINE ---
        studentTable.setPlaceholder(new Label("No students found for the selected filters."));
        // --- END OF NEW LINE ---

        // Add listeners to filters
        gradeComboBox.setOnAction(e -> loadStudentsByFilter());
        sectionComboBox.setOnAction(e -> loadStudentsByFilter());
        yearComboBox.setOnAction(e -> loadStudentsByFilter());
        
        // Load initial data
        loadStudentsByFilter();
    }

	private void setupTableColumns() {
		idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		rollNumberColumn.setCellValueFactory(new PropertyValueFactory<>("rollNumber"));
		gradeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
				cellData.getValue().getGrade() + "-" + cellData.getValue().getSection()));
		emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
		phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

		// Custom cell for active status with badge styling
		activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
		activeColumn.setCellFactory(column -> new TableCell<Student, Boolean>() {
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
		actionsColumn.setCellFactory(new Callback<TableColumn<Student, Void>, TableCell<Student, Void>>() {
			@Override
			public TableCell<Student, Void> call(TableColumn<Student, Void> param) {
				return new TableCell<Student, Void>() {
					private final Button editBtn = new Button("Edit");
					private final Button deleteBtn = new Button("Delete");
					private final HBox actionBox = new HBox(8, editBtn, deleteBtn);

					{
						editBtn.getStyleClass().add("edit-button");
						deleteBtn.getStyleClass().add("delete-button");
						actionBox.setAlignment(Pos.CENTER);

						editBtn.setOnAction(event -> {
							Student student = getTableView().getItems().get(getIndex());
							showStudentDialog(student);
						});

						deleteBtn.setOnAction(event -> {
							Student student = getTableView().getItems().get(getIndex());
							handleDeleteStudent(student);
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
		gradeComboBox.setItems(FXCollections.observableArrayList(classLevels));
		gradeComboBox.setValue("X");

		sectionComboBox.setItems(FXCollections.observableArrayList(sections));
		sectionComboBox.setValue("A");

		int currentYear = LocalDate.now().getYear();
		List<Integer> years = IntStream.rangeClosed(currentYear - 5, currentYear).boxed().collect(Collectors.toList());
		yearComboBox.setItems(FXCollections.observableArrayList(years));
		yearComboBox.setValue(currentYear);
	}

	@FXML
    private void loadStudentsByFilter() {
        String grade = gradeComboBox.getValue();
        String section = sectionComboBox.getValue();
        Integer year = yearComboBox.getValue();

        if (grade == null || section == null || year == null) {
            return;
        }

        // --- ADD THIS LINE FOR DEBUGGING ---
        System.out.println("Filtering for: Grade=" + grade + ", Section=" + section + ", Year=" + year);
        // --- END OF NEW LINE ---

        List<Student> filteredStudents = studentService.getStudentsByFilter(grade, section, year);
        studentTable.setItems(FXCollections.observableArrayList(filteredStudents));
    }

	@FXML
	private void handleAdd() {
		showStudentDialog(null);
	}

	@FXML
	private void handleSearch() {
		String searchTerm = searchField.getText();
		if (searchTerm == null || searchTerm.trim().isEmpty()) {
			loadStudentsByFilter();
		} else {
			List<Student> results = studentService.searchStudents(searchTerm.trim());
			studentTable.setItems(FXCollections.observableArrayList(results));
		}
	}

	private void showStudentDialog(Student student) {
        try {
            // 1. Load the FXML view and its controller
            SchoolSpringFXMLLoader.FXMLView<StudentFormController> fxmlView = 
                fxmlLoader.loadWithController("/fxml/student_form.fxml");

            Parent formRoot = fxmlView.getRoot();
            StudentFormController formController = fxmlView.getController();

            // 2. Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(student == null ? "Add New Student" : "Edit Student");
            dialog.getDialogPane().setContent(formRoot);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // 3. Populate the form if editing
            if (student != null) {
                formController.setStudent(student);
            }

            // 4. Add validation to OK button
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                // Validate inputs
                if (!formController.validateInputs()) {
                    event.consume(); // Prevent dialog from closing
                    return;
                }

                // Get data from the form controller
                Student studentToSave = formController.getStudentData(student);

                // Save the data using the service
                try {
                    if (student != null) {
                        studentService.updateStudent(student.getId(), studentToSave);
                        showAlert("Success", "Student updated successfully!", Alert.AlertType.INFORMATION);
                    } else {
                        studentService.createStudent(studentToSave);
                        showAlert("Success", "Student added successfully!", Alert.AlertType.INFORMATION);

                        // --- THIS IS THE "PERFECT" FIX ---
                        
                        int newStudentYear = studentToSave.getAdmissionDate().getYear();
                        
                        // 1. Add the new year to the ComboBox list if it's not there
                        if (!yearComboBox.getItems().contains(newStudentYear)) {
                            yearComboBox.getItems().add(newStudentYear);
                            FXCollections.sort(yearComboBox.getItems()); // Keep the list sorted
                        }

                        // 2. Get the current listeners
                        var gradeAction = gradeComboBox.getOnAction();
                        var sectionAction = sectionComboBox.getOnAction();
                        var yearAction = yearComboBox.getOnAction();
                        
                        // 3. Temporarily disable listeners to prevent multiple reloads
                        gradeComboBox.setOnAction(null);
                        sectionComboBox.setOnAction(null);
                        yearComboBox.setOnAction(null);
                        
                        // 4. Set all values
                        gradeComboBox.setValue(studentToSave.getGrade());
                        sectionComboBox.setValue(studentToSave.getSection());
                        yearComboBox.setValue(newStudentYear); // Use the variable
                        
                        // 5. Restore the listeners
                        gradeComboBox.setOnAction(gradeAction);
                        sectionComboBox.setOnAction(sectionAction);
                        yearComboBox.setOnAction(yearAction);
                        
                        // --- END OF FIX ---
                    }
                    
                    // 6. Refresh the table just ONCE, after all values are set
                    loadStudentsByFilter(); 
                    
                } catch (IllegalArgumentException e) {
                    showAlert("Validation Error", e.getMessage(), Alert.AlertType.ERROR);
                    event.consume(); // Prevent dialog from closing on validation error
                } catch (Exception e) {
                    showAlert("Error", "Failed to save student: " + e.getMessage(), Alert.AlertType.ERROR);
                    event.consume(); // Prevent dialog from closing on error
                }
            });

            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load student form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

	private void handleDeleteStudent(Student student) {
		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Confirm Deletion");
		confirmation.setHeaderText("Deactivate Student");
		confirmation.setContentText("Are you sure you want to deactivate " + student.getName() + "?");

		confirmation.showAndWait().ifPresent(response -> {
			if (response == ButtonType.OK) {
				try {
					studentService.deleteStudent(student.getId());
					showAlert("Success", "Student deactivated successfully!", Alert.AlertType.INFORMATION);
					loadStudentsByFilter();
				} catch (Exception e) {
					showAlert("Error", "Failed to deactivate student: " + e.getMessage(), Alert.AlertType.ERROR);
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