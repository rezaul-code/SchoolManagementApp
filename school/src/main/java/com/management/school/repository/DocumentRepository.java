package com.management.school.repository;

import com.management.school.model.Document;
import com.management.school.model.Document.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByStudentIdAndActiveTrue(Long studentId);
    
    Optional<Document> findByStudentIdAndDocumentTypeAndActiveTrue(Long studentId, DocumentType documentType);
    
    List<Document> findByDocumentTypeAndActiveTrue(DocumentType documentType);
    
    boolean existsByStudentIdAndDocumentTypeAndActiveTrue(Long studentId, DocumentType documentType);
}