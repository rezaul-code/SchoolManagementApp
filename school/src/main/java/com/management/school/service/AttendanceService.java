package com.management.school.service;

import com.management.school.model.Attendance;
import com.management.school.model.Attendance.AttendanceStatus;
import com.management.school.model.Student;
import com.management.school.repository.AttendanceRepository;
import com.management.school.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class AttendanceService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    /**
     * Mark attendance for a student on a specific date
     */
    @Transactional
    public Attendance markAttendance(Long studentId, LocalDate date, AttendanceStatus status, 
                                     String remarks, String markedBy) {
        // Verify student exists and is active
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found with id: " + studentId));
        
        if (!student.isActive()) {
            throw new IllegalArgumentException("Cannot mark attendance for inactive student");
        }
        
        // Check if attendance already exists for this student on this date
        Attendance attendance = attendanceRepository
                .findByStudentIdAndAttendanceDate(studentId, date)
                .orElse(new Attendance());
        
        attendance.setStudentId(studentId);
        attendance.setAttendanceDate(date);
        attendance.setStatus(status);
        attendance.setRemarks(remarks);
        attendance.setMarkedBy(markedBy);
        attendance.setMarkedAt(LocalDate.now());
        
        return attendanceRepository.save(attendance);
    }
    
    /**
     * Bulk mark attendance for multiple students
     */
    @Transactional
    public void markBulkAttendance(List<Attendance> attendanceList) {
        for (Attendance attendance : attendanceList) {
            attendanceRepository.save(attendance);
        }
    }
    
    /**
     * Get attendance for a specific date
     */
    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        List<Attendance> attendanceList = attendanceRepository.findByAttendanceDate(date);
        loadStudentsForAttendance(attendanceList);
        return attendanceList;
    }
    
    /**
     * Get attendance for a student within date range
     */
    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByStudentAndDateRange(Long studentId, 
                                                                LocalDate startDate, 
                                                                LocalDate endDate) {
        List<Attendance> attendanceList = attendanceRepository
                .findByStudentIdAndDateRange(studentId, startDate, endDate);
        loadStudentsForAttendance(attendanceList);
        return attendanceList;
    }
    
    /**
     * Get all attendance within date range
     */
    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendanceList = attendanceRepository.findByDateRange(startDate, endDate);
        loadStudentsForAttendance(attendanceList);
        return attendanceList;
    }
    
    /**
     * Get attendance statistics for a student
     */
    @Transactional(readOnly = true)
    public Map<AttendanceStatus, Long> getAttendanceStats(Long studentId, 
                                                          LocalDate startDate, 
                                                          LocalDate endDate) {
        List<Object[]> stats = attendanceRepository
                .getAttendanceStatsByStudentId(studentId, startDate, endDate);
        
        Map<AttendanceStatus, Long> statsMap = new HashMap<>();
        for (Object[] row : stats) {
            statsMap.put((AttendanceStatus) row[0], (Long) row[1]);
        }
        
        return statsMap;
    }
    
    /**
     * Update attendance record
     */
    @Transactional
    public Attendance updateAttendance(Long id, AttendanceStatus status, String remarks) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Attendance record not found with id: " + id));
        
        attendance.setStatus(status);
        attendance.setRemarks(remarks);
        attendance.setMarkedAt(LocalDate.now());
        
        return attendanceRepository.save(attendance);
    }
    
    /**
     * Delete attendance record
     */
    @Transactional
    public void deleteAttendance(Long id) {
        attendanceRepository.deleteById(id);
    }
    
    /**
     * Delete all attendance for a specific date
     */
    @Transactional
    public void deleteAttendanceByDate(LocalDate date) {
        attendanceRepository.deleteByAttendanceDate(date);
    }
    
    /**
     * Check if attendance exists for a date
     */
    @Transactional(readOnly = true)
    public boolean hasAttendanceForDate(LocalDate date) {
        return !attendanceRepository.findByAttendanceDate(date).isEmpty();
    }
    
    /**
     * Get students who need attendance for a specific date and class
     */
    @Transactional(readOnly = true)
    public List<Student> getStudentsForAttendance(String grade, String section, LocalDate date) {
        // Get all active students in the grade/section
        List<Student> students = studentRepository.findByGradeAndSection(grade, section);
        students = students.stream().filter(Student::isActive).toList();
        
        // Get existing attendance for this date
        List<Attendance> existingAttendance = attendanceRepository.findByAttendanceDate(date);
        Map<Long, Attendance> attendanceMap = new HashMap<>();
        for (Attendance att : existingAttendance) {
            attendanceMap.put(att.getStudentId(), att);
        }
        
        // Attach attendance to students (if exists)
        for (Student student : students) {
            // This is just for reference, not persisted
            if (attendanceMap.containsKey(student.getId())) {
                // You can store this in a transient field if needed
            }
        }
        
        return students;
    }
    
    // Helper method to load student details
    private void loadStudentsForAttendance(List<Attendance> attendanceList) {
        for (Attendance attendance : attendanceList) {
            studentRepository.findById(attendance.getStudentId())
                    .ifPresent(attendance::setStudent);
        }
    }
}