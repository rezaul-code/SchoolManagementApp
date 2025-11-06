package com.management.school.controller;

import com.management.school.model.Attendance;
import com.management.school.model.Student;
import com.management.school.model.Teacher;
import com.management.school.service.AttendanceService;
import com.management.school.service.ReportService;
import com.management.school.service.StudentService;
import com.management.school.service.TeacherService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ReportController {

    @Autowired
    private ReportService reportService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private AttendanceService attendanceService;

    // Report Type Selection
    @FXML
    private ComboBox<String> reportTypeComboBox;
    
    // Common Filters
    @FXML
    private VBox studentReportFilters;
    @FXML
    private VBox teacherReportFilters;
    @FXML
    private VBox attendanceReportFilters;
    @FXML
    private VBox classReportFilters;
    
    // Student Report Filters
    @FXML
    private ComboBox<String> studentGradeComboBox;
    @FXML
    private ComboBox<String> studentSectionComboBox;
    @FXML
    private ComboBox<Integer> studentYearComboBox;
    @FXML
    private CheckBox activeOnlyCheckBox;
    
    // Teacher Report Filters
    @FXML
    private ComboBox<String> teacherSubjectComboBox;
    @FXML
    private ComboBox<Integer> teacherYearComboBox;
    
    // Attendance Report Filters
    @FXML
    private ComboBox<String> attendanceGradeComboBox;
    @FXML
    private ComboBox<String> attendanceSectionComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    
    // Class Report Filters
    @FXML
    private ComboBox<String> classGradeComboBox;
    @FXML
    private ComboBox<String> classSectionComboBox;
    
    // Buttons
    @FXML
    private Button generateButton;
    @FXML
    private Button exportPdfButton;
    @FXML
    private Button exportExcelButton;
    
    // Report Preview
    @FXML
    private TextArea reportPreview;
    
    private final List<String> reportTypes = List.of(
        "Student List Report",
        "Teacher List Report",
        "Attendance Summary Report",
        "Class Strength Report",
        "Student Details Report",
        "Absent Students Report"
    );
    
    private final List<String> classLevels = List.of("Nursery", "KG", "I", "II", "III", "IV", "V", 
                                                      "VI", "VII", "VIII", "IX", "X");
    private final List<String> sections = List.of("A", "B");
    private final List<String> subjects = List.of(
        "Mathematics", "Physics", "Chemistry", "Biology", 
        "English", "Hindi", "History", "Geography", 
        "Computer Science", "Physical Education", "Art", "Music"
    );

    @FXML
    public void initialize() {
        setupReportTypes();
        populateFilters();
        hideAllFilters();
        
        reportTypeComboBox.setOnAction(e -> handleReportTypeChange());
        
        // Set default date range (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        
        reportPreview.setEditable(false);
    }

    private void setupReportTypes() {
        reportTypeComboBox.setItems(FXCollections.observableArrayList(reportTypes));
    }

    private void populateFilters() {
        // Student filters
        studentGradeComboBox.setItems(FXCollections.observableArrayList(classLevels));
        studentSectionComboBox.setItems(FXCollections.observableArrayList(sections));
        
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = IntStream.rangeClosed(currentYear - 5, currentYear)
                                .boxed().collect(Collectors.toList());
        studentYearComboBox.setItems(FXCollections.observableArrayList(years));
        
        // Teacher filters
        teacherSubjectComboBox.setItems(FXCollections.observableArrayList(subjects));
        List<Integer> teacherYears = IntStream.rangeClosed(currentYear - 10, currentYear)
                                .boxed().collect(Collectors.toList());
        teacherYearComboBox.setItems(FXCollections.observableArrayList(teacherYears));
        
        // Attendance filters
        attendanceGradeComboBox.setItems(FXCollections.observableArrayList(classLevels));
        attendanceSectionComboBox.setItems(FXCollections.observableArrayList(sections));
        
        // Class filters
        classGradeComboBox.setItems(FXCollections.observableArrayList(classLevels));
        classSectionComboBox.setItems(FXCollections.observableArrayList(sections));
    }

    private void handleReportTypeChange() {
        String selectedReport = reportTypeComboBox.getValue();
        hideAllFilters();
        reportPreview.clear();
        
        if (selectedReport == null) return;
        
        switch (selectedReport) {
            case "Student List Report":
            case "Student Details Report":
                studentReportFilters.setVisible(true);
                studentReportFilters.setManaged(true);
                break;
                
            case "Teacher List Report":
                teacherReportFilters.setVisible(true);
                teacherReportFilters.setManaged(true);
                break;
                
            case "Attendance Summary Report":
            case "Absent Students Report":
                attendanceReportFilters.setVisible(true);
                attendanceReportFilters.setManaged(true);
                break;
                
            case "Class Strength Report":
                classReportFilters.setVisible(true);
                classReportFilters.setManaged(true);
                break;
        }
    }

    private void hideAllFilters() {
        studentReportFilters.setVisible(false);
        studentReportFilters.setManaged(false);
        teacherReportFilters.setVisible(false);
        teacherReportFilters.setManaged(false);
        attendanceReportFilters.setVisible(false);
        attendanceReportFilters.setManaged(false);
        classReportFilters.setVisible(false);
        classReportFilters.setManaged(false);
    }

    @FXML
    private void handleGenerateReport() {
        String selectedReport = reportTypeComboBox.getValue();
        
        if (selectedReport == null) {
            showAlert("No Report Selected", "Please select a report type", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            String reportContent = "";
            
            switch (selectedReport) {
                case "Student List Report":
                    reportContent = generateStudentListReport();
                    break;
                    
                case "Teacher List Report":
                    reportContent = generateTeacherListReport();
                    break;
                    
                case "Attendance Summary Report":
                    reportContent = generateAttendanceSummaryReport();
                    break;
                    
                case "Class Strength Report":
                    reportContent = generateClassStrengthReport();
                    break;
                    
                case "Student Details Report":
                    reportContent = generateStudentDetailsReport();
                    break;
                    
                case "Absent Students Report":
                    reportContent = generateAbsentStudentsReport();
                    break;
            }
            
            reportPreview.setText(reportContent);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to generate report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String generateStudentListReport() {
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════\n");
        report.append("                 STUDENT LIST REPORT\n"); // Centered text
        report.append("═══════════════════════════════════════════════════════\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n");
        report.append("───────────────────────────────────────────────────────\n\n");
        
        String grade = studentGradeComboBox.getValue();
        String section = studentSectionComboBox.getValue();
        Integer year = studentYearComboBox.getValue();
        boolean activeOnly = activeOnlyCheckBox.isSelected();
        
        List<Student> students;
        
        if (grade != null && section != null && year != null) {
            students = studentService.getStudentsByFilter(grade, section, year);
            report.append("Filter: Grade ").append(grade).append("-").append(section)
                  .append(", Year: ").append(year).append("\n\n");
        } else {
            students = studentService.getAllStudents();
            report.append("Filter: All Students\n\n");
        }
        
        if (activeOnly) {
            students = students.stream().filter(Student::isActive).collect(Collectors.toList());
        }
        
        report.append("Total Students: ").append(students.size()).append("\n\n");
        report.append(String.format("%-10s %-15s %-25s %-15s %-25s %-15s%n", 
                                  "Roll No", "Grade", "Name", "Phone", "Email", "Status"));
        report.append("─".repeat(105)).append("\n");
        
        for (Student student : students) {
            report.append(String.format("%-10s %-15s %-25s %-15s %-25s %-15s%n",
                student.getRollNumber(),
                student.getGrade() + "-" + student.getSection(),
                truncate(student.getName(), 24),
                truncate(student.getPhone(), 14),
                truncate(student.getEmail(), 24),
                student.isActive() ? "Active" : "Inactive"
            ));
        }
        
        report.append("\n═══════════════════════════════════════════════════════\n");
        report.append("                       END OF REPORT\n");
        report.append("═══════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    private String generateTeacherListReport() {
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════\n");
        report.append("                 TEACHER LIST REPORT\n");
        report.append("═══════════════════════════════════════════════════════\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n");
        report.append("───────────────────────────────────────────────────────\n\n");
        
        String subject = teacherSubjectComboBox.getValue();
        Integer year = teacherYearComboBox.getValue();
        
        List<Teacher> teachers;
        
        if (subject != null && year != null) {
            teachers = teacherService.getTeachersByFilter(subject, year);
            report.append("Filter: Subject - ").append(subject).append(", Year: ").append(year).append("\n\n");
        } else {
            teachers = teacherService.getAllTeachers();
            report.append("Filter: All Teachers\n\n");
        }
        
        report.append("Total Teachers: ").append(teachers.size()).append("\n\n");
        report.append(String.format("%-12s %-25s %-20s %-25s %-15s%n", 
                                  "Emp ID", "Name", "Subject", "Email", "Status"));
        report.append("─".repeat(97)).append("\n");
        
        for (Teacher teacher : teachers) {
            report.append(String.format("%-12s %-25s %-20s %-25s %-15s%n",
                teacher.getEmployeeId(),
                truncate(teacher.getName(), 24),
                truncate(teacher.getSubject(), 19),
                truncate(teacher.getEmail(), 24),
                teacher.isActive() ? "Active" : "Inactive"
            ));
        }
        
        report.append("\n═══════════════════════════════════════════════════════\n");
        report.append("                       END OF REPORT\n");
        report.append("═══════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    private String generateAttendanceSummaryReport() {
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════\n");
        report.append("             ATTENDANCE SUMMARY REPORT\n");
        report.append("═══════════════════════════════════════════════════════\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n");
        report.append("───────────────────────────────────────────────────────\n\n");
        
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String grade = attendanceGradeComboBox.getValue();
        String section = attendanceSectionComboBox.getValue();
        
        if (startDate == null || endDate == null) {
            return "Please select both start and end dates.";
        }
        
        report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
        if (grade != null && section != null) {
            report.append("Class: ").append(grade).append("-").append(section).append("\n");
        }
        report.append("\n");
        
        List<Attendance> attendanceRecords = attendanceService.getAttendanceByDateRange(startDate, endDate);
        
        if (grade != null && section != null) {
            attendanceRecords = attendanceRecords.stream()
                .filter(a -> a.getStudent() != null && 
                             grade.equals(a.getStudent().getGrade()) && 
                             section.equals(a.getStudent().getSection()))
                .collect(Collectors.toList());
        }
        
        long totalRecords = attendanceRecords.size();
        long present = attendanceRecords.stream()
            .filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT).count();
        long absent = attendanceRecords.stream()
            .filter(a -> a.getStatus() == Attendance.AttendanceStatus.ABSENT).count();
        long late = attendanceRecords.stream()
            .filter(a -> a.getStatus() == Attendance.AttendanceStatus.LATE).count();
        long excused = attendanceRecords.stream()
            .filter(a -> a.getStatus() == Attendance.AttendanceStatus.EXCUSED).count();
        
        report.append("Total Records: ").append(totalRecords).append("\n");
        report.append("Present: ").append(present).append(" (")
              .append(totalRecords > 0 ? String.format("%.1f", present * 100.0 / totalRecords) : "0")
              .append("%)\n");
        report.append("Absent: ").append(absent).append(" (")
              .append(totalRecords > 0 ? String.format("%.1f", absent * 100.0 / totalRecords) : "0")
              .append("%)\n");
        report.append("Late: ").append(late).append(" (")
              .append(totalRecords > 0 ? String.format("%.1f", late * 100.0 / totalRecords) : "0")
              .append("%)\n");
        report.append("Excused: ").append(excused).append(" (")
              .append(totalRecords > 0 ? String.format("%.1f", excused * 100.0 / totalRecords) : "0")
              .append("%)\n\n");
        
        report.append("═══════════════════════════════════════════════════════\n");
        report.append("                       END OF REPORT\n");
        report.append("═══════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    private String generateClassStrengthReport() {
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════\n");
        report.append("               CLASS STRENGTH REPORT\n");
        report.append("═══════════════════════════════════════════════════════\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n");
        report.append("───────────────────────────────────────────────────────\n\n");
        
        String selectedGrade = classGradeComboBox.getValue();
        String selectedSection = classSectionComboBox.getValue();
        
        report.append(String.format("%-15s %-10s %-10s %-10s%n", 
                                  "Class", "Boys", "Girls", "Total"));
        report.append("─".repeat(45)).append("\n");
        
        int totalBoys = 0;
        int totalGirls = 0;
        int grandTotal = 0;

        // --- OPTIMIZATION: Fetch all students ONCE before the loop ---
        List<Student> allStudents = studentService.getAllStudents();
        
        for (String grade : classLevels) {
            for (String section : sections) {
                if ((selectedGrade == null || selectedGrade.equals(grade)) &&
                    (selectedSection == null || selectedSection.equals(section))) {
                    
                    // Filter the pre-fetched list in memory
                    List<Student> students = allStudents.stream()
                        .filter(s -> s.isActive() && grade.equals(s.getGrade()) && 
                                       section.equals(s.getSection()))
                        .collect(Collectors.toList());
                    
                    // --- FIX: Compare with enum Student.Gender.MALE/FEMALE ---
                    long boys = students.stream()
                        .filter(s -> s.getGender() == Student.Gender.MALE).count();
                    long girls = students.stream()
                        .filter(s -> s.getGender() == Student.Gender.FEMALE).count();
                    int total = students.size();
                    
                    if (total > 0) {
                        report.append(String.format("%-15s %-10d %-10d %-10d%n",
                            grade + "-" + section, boys, girls, total));
                        
                        totalBoys += boys;
                        totalGirls += girls;
                        grandTotal += total;
                    }
                }
            }
        }
        
        report.append("─".repeat(45)).append("\n");
        report.append(String.format("%-15s %-10d %-10d %-10d%n",
            "TOTAL", totalBoys, totalGirls, grandTotal));
        
        report.append("\n═══════════════════════════════════════════════════════\n");
        report.append("                       END OF REPORT\n");
        report.append("═══════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    private String generateStudentDetailsReport() {
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════\n");
        report.append("               DETAILED STUDENT REPORT\n");
        report.append("═══════════════════════════════════════════════════════\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n");
        report.append("───────────────────────────────────────────────────────\n\n");
        
        String grade = studentGradeComboBox.getValue();
        String section = studentSectionComboBox.getValue();
        Integer year = studentYearComboBox.getValue();
        
        List<Student> students;
        
        if (grade != null && section != null && year != null) {
            students = studentService.getStudentsByFilter(grade, section, year);
        } else {
            students = studentService.getAllStudents();
        }
        
        if (activeOnlyCheckBox.isSelected()) {
            students = students.stream().filter(Student::isActive).collect(Collectors.toList());
        }
        
        for (Student student : students) {
            report.append("Roll Number: ").append(student.getRollNumber()).append("\n");
            report.append("Name: ").append(student.getName()).append("\n");
            report.append("Grade: ").append(student.getGrade()).append("-").append(student.getSection()).append("\n");
            report.append("Gender: ").append(student.getGender()).append("\n"); // This correctly calls enum.toString()
            report.append("Date of Birth: ").append(student.getDateOfBirth()).append("\n");
            report.append("Email: ").append(student.getEmail()).append("\n");
            report.append("Phone: ").append(student.getPhone()).append("\n");
            report.append("Admission Date: ").append(student.getAdmissionDate()).append("\n");
            report.append("Guardian: ").append(student.getGuardianName()).append("\n");
            report.append("Guardian Phone: ").append(student.getGuardianPhone()).append("\n");
            report.append("Status: ").append(student.isActive() ? "Active" : "Inactive").append("\n");
            report.append("─".repeat(55)).append("\n");
        }
        
        report.append("\nTotal Students: ").append(students.size()).append("\n");
        report.append("\n═══════════════════════════════════════════════════════\n");
        report.append("                       END OF REPORT\n");
        report.append("═══════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    private String generateAbsentStudentsReport() {
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════\n");
        report.append("               ABSENT STUDENTS REPORT\n");
        report.append("═══════════════════════════════════════════════════════\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n");
        report.append("───────────────────────────────────────────────────────\n\n");
        
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate == null || endDate == null) {
            return "Please select both start and end dates.";
        }
        
        report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");
        
        List<Attendance> absentRecords = attendanceService.getAttendanceByDateRange(startDate, endDate)
            .stream()
            .filter(a -> a.getStatus() == Attendance.AttendanceStatus.ABSENT)
            .collect(Collectors.toList());
        
        report.append(String.format("%-12s %-25s %-15s %-15s%n", 
                                  "Date", "Name", "Grade", "Remarks"));
        report.append("─".repeat(67)).append("\n");
        
        for (Attendance record : absentRecords) {
            if (record.getStudent() != null) {
                report.append(String.format("%-12s %-25s %-15s %-15s%n",
                    record.getAttendanceDate(),
                    truncate(record.getStudent().getName(), 24),
                    record.getStudent().getGrade() + "-" + record.getStudent().getSection(),
                    truncate(record.getRemarks() != null ? record.getRemarks() : "", 14)
                ));
            }
        }
        
        report.append("\nTotal Absent Records: ").append(absentRecords.size()).append("\n");
        report.append("\n═══════════════════════════════════════════════════════\n");
        report.append("                       END OF REPORT\n");
        report.append("═══════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    @FXML
    private void handleExportPdf() {
        if (reportPreview.getText().isEmpty()) {
            showAlert("No Report", "Please generate a report first", Alert.AlertType.WARNING);
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report as PDF");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName("report_" + LocalDate.now() + ".pdf");
        
        File file = fileChooser.showSaveDialog(generateButton.getScene().getWindow());
        
        if (file != null) {
            try {
                reportService.exportToPdf(reportPreview.getText(), file.getAbsolutePath());
                showAlert("Success", "Report exported to PDF successfully!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to export PDF: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleExportExcel() {
        if (reportPreview.getText().isEmpty()) {
            showAlert("No Report", "Please generate a report first", Alert.AlertType.WARNING);
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report as Excel");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        fileChooser.setInitialFileName("report_" + LocalDate.now() + ".xlsx");
        
        File file = fileChooser.showSaveDialog(generateButton.getScene().getWindow());
        
        if (file != null) {
            try {
                reportService.exportToExcel(reportPreview.getText(), file.getAbsolutePath());
                showAlert("Success", "Report exported to Excel successfully!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to export Excel: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}