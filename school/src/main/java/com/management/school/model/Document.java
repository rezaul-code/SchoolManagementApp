package com.management.school.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long studentId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(nullable = false)
    private String fileExtension;
    
    private Long fileSize; // in bytes
    
    @Column(nullable = false)
    private LocalDateTime uploadedAt;
    
    private String uploadedBy;
    
    @Column(nullable = false)
    private boolean active = true;
    
    public enum DocumentType {
        STUDENT_AADHAR("Student Aadhar Card"),
        STUDENT_BIRTH_CERTIFICATE("Student Birth Certificate"),
        STUDENT_PHOTO("Student Photo"),
        PARENT_AADHAR("Parent Aadhar Card"),
        PARENT_VOTER_ID("Parent Voter ID");
        
        private final String displayName;
        
        DocumentType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public Document() {
        this.uploadedAt = LocalDateTime.now();
    }
    
    public Document(Long studentId, DocumentType documentType, String fileName, 
                    String filePath, String fileExtension, Long fileSize) {
        this.studentId = studentId;
        this.documentType = documentType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.uploadedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    
    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}