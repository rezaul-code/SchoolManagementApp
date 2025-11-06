package com.management.school.service;

import com.management.school.model.Document;
import com.management.school.model.Document.DocumentType;
import com.management.school.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DocumentService {
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Value("${document.upload.path:uploads/documents}")
    private String uploadPath;
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".pdf", ".jpg", ".jpeg", ".png"};
    
    @Transactional
    public Document uploadDocument(Long studentId, DocumentType documentType, 
                                   File sourceFile, String originalFileName) throws IOException {
        
        // Validate file
        validateFile(sourceFile, originalFileName);
        
        // Check if document already exists for this student and type
        documentRepository.findByStudentIdAndDocumentTypeAndActiveTrue(studentId, documentType)
            .ifPresent(existingDoc -> {
                // Mark old document as inactive
                existingDoc.setActive(false);
                documentRepository.save(existingDoc);
                
                // Delete old file
                try {
                    Files.deleteIfExists(Paths.get(existingDoc.getFilePath()));
                } catch (IOException e) {
                    // Log but don't fail
                    System.err.println("Failed to delete old file: " + e.getMessage());
                }
            });
        
        // Generate unique filename
        String fileExtension = getFileExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("%s_%d_%s%s", 
            documentType.name(), studentId, timestamp, fileExtension);
        
        // Create upload directory if it doesn't exist
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // Copy file to upload directory
        Path destinationPath = uploadDir.resolve(fileName);
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Create document record
        Document document = new Document();
        document.setStudentId(studentId);
        document.setDocumentType(documentType);
        document.setFileName(originalFileName);
        document.setFilePath(destinationPath.toString());
        document.setFileExtension(fileExtension);
        document.setFileSize(sourceFile.length());
        document.setActive(true);
        
        return documentRepository.save(document);
    }
    
    @Transactional(readOnly = true)
    public List<Document> getStudentDocuments(Long studentId) {
        return documentRepository.findByStudentIdAndActiveTrue(studentId);
    }
    
    @Transactional(readOnly = true)
    public Document getDocument(Long documentId) {
        return documentRepository.findById(documentId)
            .orElseThrow(() -> new NoSuchElementException("Document not found with id: " + documentId));
    }
    
    @Transactional(readOnly = true)
    public Document getStudentDocument(Long studentId, DocumentType documentType) {
        return documentRepository.findByStudentIdAndDocumentTypeAndActiveTrue(studentId, documentType)
            .orElse(null);
    }
    
    @Transactional
    public void deleteDocument(Long documentId) throws IOException {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new NoSuchElementException("Document not found with id: " + documentId));
        
        // Mark as inactive
        document.setActive(false);
        documentRepository.save(document);
        
        // Delete physical file
        Files.deleteIfExists(Paths.get(document.getFilePath()));
    }
    
    public File getDocumentFile(Long documentId) {
        Document document = getDocument(documentId);
        File file = new File(document.getFilePath());
        if (!file.exists()) {
            throw new NoSuchElementException("Document file not found: " + document.getFilePath());
        }
        return file;
    }
    
    private void validateFile(File file, String fileName) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist");
        }
        
        if (file.length() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum limit of 5MB");
        }
        
        String extension = getFileExtension(fileName).toLowerCase();
        boolean validExtension = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                validExtension = true;
                break;
            }
        }
        
        if (!validExtension) {
            throw new IOException("File type not allowed. Allowed types: PDF, JPG, JPEG, PNG");
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex);
    }
}