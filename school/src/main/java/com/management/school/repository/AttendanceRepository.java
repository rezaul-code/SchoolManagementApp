package com.management.school.repository;

import com.management.school.model.Attendance;
import com.management.school.model.Attendance.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    /**
     * Find attendance record for a specific student on a specific date
     */
    Optional<Attendance> findByStudentIdAndAttendanceDate(Long studentId, LocalDate attendanceDate);
    
    /**
     * Find all attendance records for a specific date
     */
    List<Attendance> findByAttendanceDate(LocalDate attendanceDate);
    
    /**
     * Find all attendance records for a specific student
     */
    List<Attendance> findByStudentId(Long studentId);
    
    /**
     * Find attendance records for a student within a date range
     */
    @Query("SELECT a FROM Attendance a WHERE a.studentId = :studentId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.attendanceDate DESC")
    List<Attendance> findByStudentIdAndDateRange(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find attendance by date and status
     */
    List<Attendance> findByAttendanceDateAndStatus(LocalDate date, AttendanceStatus status);
    
    /**
     * Find attendance records within a date range
     */
    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.attendanceDate DESC, a.studentId")
    List<Attendance> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * Get attendance statistics for a student
     */
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.studentId = :studentId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate GROUP BY a.status")
    List<Object[]> getAttendanceStatsByStudentId(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * Delete attendance records for a specific date
     */
    void deleteByAttendanceDate(LocalDate date);
}