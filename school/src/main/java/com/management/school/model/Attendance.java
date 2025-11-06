package com.management.school.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "attendance_date"}))
public class Attendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;
    
    private String remarks;
    
    @Column(name = "marked_by")
    private String markedBy;
    
    @Column(name = "marked_at")
    private LocalDate markedAt;
    
    // Transient fields for display
    @Transient
    private Student student;
    
    // Enum for attendance status
    public enum AttendanceStatus {
        PRESENT("Present"),
        ABSENT("Absent"),
        LATE("Late"),
        EXCUSED("Excused");
        
        private final String displayName;
        
        AttendanceStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public Attendance() {
        this.markedAt = LocalDate.now();
    }
    
    public Attendance(Long studentId, LocalDate attendanceDate, AttendanceStatus status) {
        this.studentId = studentId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.markedAt = LocalDate.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    
    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }
    
    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }
    
    public AttendanceStatus getStatus() {
        return status;
    }
    
    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public String getMarkedBy() {
        return markedBy;
    }
    
    public void setMarkedBy(String markedBy) {
        this.markedBy = markedBy;
    }
    
    public LocalDate getMarkedAt() {
        return markedAt;
    }
    
    public void setMarkedAt(LocalDate markedAt) {
        this.markedAt = markedAt;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", attendanceDate=" + attendanceDate +
                ", status=" + status +
                '}';
    }
}