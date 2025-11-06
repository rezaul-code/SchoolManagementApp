package com.management.school.controller;

import com.management.school.model.Document;
import com.management.school.model.Document.DocumentType;
import com.management.school.model.Student;
import com.management.school.service.DocumentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DocumentUploadController {
    
    @FXML private GridPane documentGrid;
    @FXML private Button studentAadharUploadBtn;
    @FXML private Button studentBirthCertUploadBtn;
    @FXML private Button studentPhotoUploadBtn;
    @FXML private Button parentAadharUploadBtn;
    @FXML private Button parentVoterIdUploadBtn;
    
    @FXML private Label studentAadharLabel;
    @FXML private Label studentBirthCertLabel;
    @FXML private Label studentPhotoLabel;
    @FXML private Label parentAadharLabel;
    @FXML private Label parentVoterIdLabel;
    
    @FXML private Button studentAadharViewBtn;
    @FXML private Button studentBirthCertViewBtn;
    @FXML private Button studentPhotoViewBtn;
    @FXML private Button parentAadharViewBtn;
    @FXML private Button parentVoterIdViewBtn;
    
    @FXML private Button studentAadharDeleteBtn;
    @FXML private Button studentBirthCertDeleteBtn;
    @FXML private Button studentPhotoDeleteBtn;
    @FXML private Button parentAadharDeleteBtn;
    @FXML private Button parentVoterIdDeleteBtn;
    
    @Autowired
    private DocumentService documentService;
    
    private Student currentStudent;
    private Map<DocumentType, Document> documentMap = new HashMap<>();
    
    @FXML
    public void initialize() {
        // Initialize will be called after FXML is loaded
    }
    
    public void setStudent(Student student) {
        this.currentStudent = student;
        if (student != null && student.getId() != null) {
            loadDocuments();
        }
    }
    
    private void loadDocuments() {
        List<Document> documents = documentService.getStudentDocuments(currentStudent.getId());
        documentMap.clear();
        
        for (Document doc : documents) {
            documentMap.put(doc.getDocumentType(), doc);
        }
        
        updateUI();
    }
    
    private void updateUI() {
        updateDocumentUI(DocumentType.STUDENT_AADHAR, studentAadharLabel, 
                        studentAadharViewBtn, studentAadharDeleteBtn);
        updateDocumentUI(DocumentType.STUDENT_BIRTH_CERTIFICATE, studentBirthCertLabel, 
                        studentBirthCertViewBtn, studentBirthCertDeleteBtn);
        updateDocumentUI(DocumentType.STUDENT_PHOTO, studentPhotoLabel, 
                        studentPhotoViewBtn, studentPhotoDeleteBtn);
        updateDocumentUI(DocumentType.PARENT_AADHAR, parentAadharLabel, 
                        parentAadharViewBtn, parentAadharDeleteBtn);
        updateDocumentUI(DocumentType.PARENT_VOTER_ID, parentVoterIdLabel, 
                        parentVoterIdViewBtn, parentVoterIdDeleteBtn);
    }
    
    private void updateDocumentUI(DocumentType type, Label label, Button viewBtn, Button deleteBtn) {
        Document doc = documentMap.get(type);
        if (doc != null) {
            label.setText("✓ " + doc.getFileName());
            label.setStyle("-fx-text-fill: green;");
            viewBtn.setDisable(false);
            deleteBtn.setDisable(false);
        } else {
            label.setText("Not uploaded");
            label.setStyle("-fx-text-fill: gray;");
            viewBtn.setDisable(true);
            deleteBtn.setDisable(true);
        }
    }
    
    @FXML
    private void handleStudentAadharUpload() {
        uploadDocument(DocumentType.STUDENT_AADHAR);
    }
    
    @FXML
    private void handleStudentBirthCertUpload() {
        uploadDocument(DocumentType.STUDENT_BIRTH_CERTIFICATE);
    }
    
    @FXML
    private void handleStudentPhotoUpload() {
        uploadDocument(DocumentType.STUDENT_PHOTO);
    }
    
    @FXML
    private void handleParentAadharUpload() {
        uploadDocument(DocumentType.PARENT_AADHAR);
    }
    
    @FXML
    private void handleParentVoterIdUpload() {
        uploadDocument(DocumentType.PARENT_VOTER_ID);
    }
    
    private void uploadDocument(DocumentType documentType) {
        if (currentStudent == null || currentStudent.getId() == null) {
            showAlert("Error", "Please save the student information first before uploading documents.", 
                     Alert.AlertType.ERROR);
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select " + documentType.getDisplayName());
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.jpg", "*.jpeg", "*.png"),
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
        );
        
        Stage stage = (Stage) documentGrid.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                Document document = documentService.uploadDocument(
                    currentStudent.getId(), 
                    documentType, 
                    selectedFile, 
                    selectedFile.getName()
                );
                
                documentMap.put(documentType, document);
                updateUI();
                
                showAlert("Success", documentType.getDisplayName() + " uploaded successfully!", 
                         Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Upload Failed", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void handleStudentAadharView() {
        viewDocument(DocumentType.STUDENT_AADHAR);
    }
    
    @FXML
    private void handleStudentBirthCertView() {
        viewDocument(DocumentType.STUDENT_BIRTH_CERTIFICATE);
    }
    
    @FXML
    private void handleStudentPhotoView() {
        viewDocument(DocumentType.STUDENT_PHOTO);
    }
    
    @FXML
    private void handleParentAadharView() {
        viewDocument(DocumentType.PARENT_AADHAR);
    }
    
    @FXML
    private void handleParentVoterIdView() {
        viewDocument(DocumentType.PARENT_VOTER_ID);
    }
    
    private void viewDocument(DocumentType documentType) {
        Document doc = documentMap.get(documentType);
        if (doc != null) {
            try {
                File file = documentService.getDocumentFile(doc.getId());
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(file);
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to open document: " + e.getMessage(), 
                         Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void handleStudentAadharDelete() {
        deleteDocument(DocumentType.STUDENT_AADHAR);
    }
    
    @FXML
    private void handleStudentBirthCertDelete() {
        deleteDocument(DocumentType.STUDENT_BIRTH_CERTIFICATE);
    }
    
    @FXML
    private void handleStudentPhotoDelete() {
        deleteDocument(DocumentType.STUDENT_PHOTO);
    }
    
    @FXML
    private void handleParentAadharDelete() {
        deleteDocument(DocumentType.PARENT_AADHAR);
    }
    
    @FXML
    private void handleParentVoterIdDelete() {
        deleteDocument(DocumentType.PARENT_VOTER_ID);
    }
    
    private void deleteDocument(DocumentType documentType) {
        Document doc = documentMap.get(documentType);
        if (doc != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Document");
            confirmation.setHeaderText("Delete " + documentType.getDisplayName());
            confirmation.setContentText("Are you sure you want to delete this document?");
            
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        documentService.deleteDocument(doc.getId());
                        documentMap.remove(documentType);
                        updateUI();
                        showAlert("Success", "Document deleted successfully!", 
                                 Alert.AlertType.INFORMATION);
                    } catch (Exception e) {
                        showAlert("Error", "Failed to delete document: " + e.getMessage(), 
                                 Alert.AlertType.ERROR);
                    }
                }
            });
        }
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}