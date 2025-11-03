package com.management.school.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.nio.file.StandardCopyOption;

/**
 * Professional Database Backup & Restore Controller
 * 
 * Features:
 * - Encrypted, password-protected backups using WinRAR
 * - Automatic backup retention (keeps 4 most recent)
 * - Google Drive synchronization
 * - Secure credential handling via temp config files
 * - Comprehensive error handling and logging
 * 
 * @author School Management System
 * @version 2.0.0
 */
@Component
public class BackupController {

    private static final Logger LOGGER = Logger.getLogger(BackupController.class.getName());
    private static final int MAX_BACKUP_RETENTION = 4;
    private static final String BACKUP_FILE_EXTENSION = ".rar";
    private static final String SQL_FILE_EXTENSION = ".sql";
    
    @FXML private Button btnCreateBackup;
    @FXML private Button btnRestore;
    @FXML private Button btnOpenFolder;
    @FXML private Label backupPathLabel;
    @FXML private ListView<File> backupListView;
    @FXML private TextArea logArea;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private CheckBox cbAutoCleanup;
    @FXML private Spinner<Integer> retentionSpinner;

    @Autowired
    private Environment env;

    private Path backupsDir;
    private final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    private String mysqlBinPath;
    private String winrarExePath;

    @FXML
    public void initialize() {
        initializeUI();
        loadConfiguration();
        validateConfiguration();
        setupEventHandlers();
        refreshBackupList();
    }

    /**
     * Initialize UI components
     */
    private void initializeUI() {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, MAX_BACKUP_RETENTION);
        retentionSpinner.setValueFactory(factory);
        
        // Hide manual cleanup controls as automatic cleanup is enabled
        cbAutoCleanup.setVisible(false);
        retentionSpinner.setVisible(false);
        
        // Setup custom cell factory for backup list
        backupListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String size = formatFileSize(item.length());
                    String date = formatFileDate(item.lastModified());
                    setText(String.format("%s  (%s) - %s", item.getName(), size, date));
                }
            }
        });
    }

    /**
     * Load configuration from application properties
     */
    private void loadConfiguration() {
        // Configure backup directory
        String configuredBackupDir = env.getProperty("app.backups.dir");
        if (configuredBackupDir != null && !configuredBackupDir.isBlank()) {
            backupsDir = Paths.get(configuredBackupDir);
        } else {
            backupsDir = Paths.get(System.getProperty("user.home"), "SchoolApp", "backups");
        }
        
        // Configure MySQL bin path
        mysqlBinPath = env.getProperty("app.mysql.bin.path", 
            isWindows ? "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin" : "/usr/bin");
        
        // Configure WinRAR path
        winrarExePath = env.getProperty("app.winrar.path");
        
        backupPathLabel.setText(backupsDir.toString());
    }

    /**
     * Validate critical configuration paths
     */
    private void validateConfiguration() {
        // Validate MySQL bin path
        Path mysqlBinDir = Paths.get(mysqlBinPath);
        if (!Files.exists(mysqlBinDir) || !Files.isDirectory(mysqlBinDir)) {
            logError("MySQL bin directory not found: " + mysqlBinPath);
            logError("Please update 'app.mysql.bin.path' in application.properties");
        } else {
            logInfo("MySQL bin path validated: " + mysqlBinPath);
        }

        // Validate WinRAR path
        if (winrarExePath == null || winrarExePath.isBlank()) {
            logError("WinRAR path not configured. Please set 'app.winrar.path' in application.properties");
        } else {
            Path rarExe = Paths.get(winrarExePath, isWindows ? "Rar.exe" : "rar");
            if (!Files.exists(rarExe)) {
                logError("WinRAR executable not found: " + rarExe);
            } else {
                logInfo("WinRAR path validated: " + winrarExePath);
            }
        }
        
        // Validate backup password
        String backupPassword = env.getProperty("app.backups.password");
        if (backupPassword == null || backupPassword.isBlank()) {
            logError("Backup password not configured. Please set 'app.backups.password' in application.properties");
        }
    }

    /**
     * Setup event handlers for UI buttons
     */
    private void setupEventHandlers() {
        btnCreateBackup.setOnAction(e -> createBackup());
        btnRestore.setOnAction(e -> restoreSelectedBackup());
        btnOpenFolder.setOnAction(e -> openBackupsFolder());
    }

    /**
     * Refresh the list of available backups
     */
    private void refreshBackupList() {
        try {
            if (!Files.exists(backupsDir)) {
                Files.createDirectories(backupsDir);
            }

            List<File> files = Files.list(backupsDir)
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(BACKUP_FILE_EXTENSION))
                    .map(Path::toFile)
                    .sorted(Comparator.comparingLong(File::lastModified).reversed())
                    .collect(Collectors.toList());

            Platform.runLater(() -> backupListView.getItems().setAll(files));
            logInfo("Found " + files.size() + " backup file(s)");

        } catch (IOException e) {
            logException("Failed to refresh backup list", e);
        }
    }

    /**
     * Create a new encrypted database backup
     */
    private void createBackup() {
        Task<Void> task = new Task<>() {
            Path tempConf = null;
            Path tempSqlFile = null;

            @Override
            protected Void call() {
                try {
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(true);
                        btnCreateBackup.setDisable(true);
                    });
                    
                    logInfo("========================================");
                    logInfo("Starting backup process...");
                    
                    // Validate prerequisites
                    if (!validateBackupPrerequisites()) {
                        return null;
                    }

                    // Get database configuration
                    DatabaseConfig dbConfig = getDatabaseConfig();
                    
                    // Create temporary config file for MySQL credentials
                    tempConf = createSecureMySQLConfig(dbConfig, true);
                    
                    // Generate backup file paths
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                    String backupFileName = String.format("%s_backup_%s%s", 
                        dbConfig.dbName, timestamp, BACKUP_FILE_EXTENSION);
                    Path backupFile = backupsDir.resolve(backupFileName);
                    tempSqlFile = Files.createTempFile("db_dump_", SQL_FILE_EXTENSION);
                    
                    Files.createDirectories(backupsDir);

                    // Step 1: Export database to SQL file
                    exportDatabaseToSQL(dbConfig, tempConf, tempSqlFile);
                    
                    // Step 2: Encrypt and compress SQL file with WinRAR
                    encryptSQLFileWithWinRAR(tempSqlFile, backupFile);
                    
                    logSuccess("Backup created successfully: " + backupFileName);
                    
                    // Step 3: Copy to Google Drive (if available)
                    copyBackupToGoogleDrive(backupFile);

                    // Step 4: Automatic cleanup of old backups
                    cleanupOldBackups();
                    
                    logInfo("Backup process completed");
                    logInfo("========================================");

                } catch (Exception ex) {
                    logException("Backup failed", ex);
                } finally {
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        btnCreateBackup.setDisable(false);
                    });
                    cleanupTempFiles(tempConf, tempSqlFile);
                }
                return null;
            }
        };

        new Thread(task, "backup-thread").start();
    }

    /**
     * Restore database from selected encrypted backup
     */
    private void restoreSelectedBackup() {
        File selectedBackup = backupListView.getSelectionModel().getSelectedItem();
        if (selectedBackup == null) {
            showAlert("No Selection", "Please select a backup file to restore.", Alert.AlertType.WARNING);
            return;
        }
        
        if (!selectedBackup.getName().endsWith(BACKUP_FILE_EXTENSION)) {
            showAlert("Invalid File", "Please select a valid encrypted backup file (.rar)", Alert.AlertType.ERROR);
            return;
        }

        // Prompt for password
        String password = promptForPassword("Enter Backup Password", 
            "Please enter the password to decrypt this backup:");
        if (password == null || password.isEmpty()) {
            logInfo("Restore cancelled: No password provided");
            return;
        }

        // Confirm restore operation
        if (!confirmRestoreOperation(selectedBackup.getName())) {
            return;
        }

        Task<Void> task = new Task<>() {
            Path tempConf = null;
            Path tempSqlFile = null;
            Path tempDir = null;

            @Override
            protected Void call() {
                try {
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(true);
                        btnRestore.setDisable(true);
                    });
                    
                    logInfo("========================================");
                    logInfo("Starting restore process...");
                    logInfo("Backup file: " + selectedBackup.getName());

                    // Get database configuration
                    DatabaseConfig dbConfig = getDatabaseConfig();
                    tempConf = createSecureMySQLConfig(dbConfig, false);

                    // Step 1: Extract encrypted backup
                    tempDir = Files.createTempDirectory("restore_");
                    tempSqlFile = extractEncryptedBackup(selectedBackup, password, tempDir);
                    
                    // Step 2: Restore database from SQL file
                    restoreDatabaseFromSQL(dbConfig, tempConf, tempSqlFile);
                    
                    logSuccess("Database restored successfully from: " + selectedBackup.getName());
                    logInfo("Restore process completed");
                    logInfo("========================================");
                    
                    Platform.runLater(() -> 
                        showAlert("Restore Complete", 
                            "Database has been successfully restored!", 
                            Alert.AlertType.INFORMATION));

                } catch (Exception ex) {
                    logException("Restore failed", ex);
                    Platform.runLater(() -> 
                        showAlert("Restore Failed", 
                            "Failed to restore database: " + ex.getMessage(), 
                            Alert.AlertType.ERROR));
                } finally {
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        btnRestore.setDisable(false);
                    });
                    cleanupTempFiles(tempConf, tempSqlFile);
                    cleanupTempDirectory(tempDir);
                }
                return null;
            }
        };

        new Thread(task, "restore-thread").start();
    }

    /**
     * Export database to SQL file using mysqldump
     */
    private void exportDatabaseToSQL(DatabaseConfig dbConfig, Path configFile, Path outputFile) 
            throws IOException, InterruptedException {
        logInfo("Exporting database to SQL file...");
        
        String mysqldumpCmd = Paths.get(mysqlBinPath, 
            isWindows ? "mysqldump.exe" : "mysqldump").toString();
        
        List<String> command = Arrays.asList(
            mysqldumpCmd,
            "--defaults-extra-file=" + configFile.toAbsolutePath(),
            "--single-transaction",
            "--routines",
            "--triggers",
            "--events",
            "--hex-blob",
            "--set-gtid-purged=OFF",
            dbConfig.dbName
        );
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Write mysqldump output to file
        try (InputStream in = process.getInputStream();
             OutputStream out = Files.newOutputStream(outputFile)) {
            in.transferTo(out);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("mysqldump failed with exit code: " + exitCode);
        }
        
        logInfo("Database export completed: " + formatFileSize(Files.size(outputFile)));
    }

    /**
     * Encrypt SQL file using WinRAR with maximum security
     */
    private void encryptSQLFileWithWinRAR(Path sqlFile, Path outputFile) 
            throws IOException, InterruptedException {
        logInfo("Encrypting and compressing backup file...");
        
        String password = env.getProperty("app.backups.password");
        String rarCmd = Paths.get(winrarExePath, isWindows ? "Rar.exe" : "rar").toString();
        
        // WinRAR command with maximum security options:
        // a = add to archive
        // -hp[password] = encrypt both file data and headers (hides file names)
        // -p[password] = set password
        // -m5 = maximum compression
        // -ma5 = RAR5 format (more secure)
        // -ep1 = exclude base folder from paths
        // -df = delete source file after archiving
        // -y = assume Yes on all queries
        // -inul = disable all messages
        List<String> command = Arrays.asList(
            rarCmd,
            "a",                                    // add to archive
            "-hp" + password,                       // encrypt headers
            "-p" + password,                        // set password
            "-m5",                                  // maximum compression
            "-ma5",                                 // use RAR5 format
            "-ep1",                                 // exclude base folder
            "-df",                                  // delete source after
            "-y",                                   // assume yes
            "-inul",                                // disable messages
            outputFile.toAbsolutePath().toString(),
            sqlFile.toAbsolutePath().toString()
        );
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Log WinRAR output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    logInfo("WinRAR: " + line);
                }
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("WinRAR encryption failed with exit code: " + exitCode);
        }
        
        logInfo("Encryption completed: " + formatFileSize(Files.size(outputFile)));
    }

    /**
     * Extract encrypted backup file using WinRAR
     */
    private Path extractEncryptedBackup(File backupFile, String password, Path extractDir) 
            throws IOException, InterruptedException {
        logInfo("Decrypting and extracting backup file...");
        
        String rarCmd = Paths.get(winrarExePath, isWindows ? "Rar.exe" : "rar").toString();
        
        // WinRAR extract command:
        // x = extract with full path
        // -p[password] = password
        // -o+ = overwrite existing files
        // -inul = disable messages
        // -y = assume yes
        List<String> command = Arrays.asList(
            rarCmd,
            "x",
            "-p" + password,
            "-o+",
            "-inul",
            "-y",
            backupFile.getAbsolutePath(),
            extractDir.toAbsolutePath().toString() + File.separator
        );
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Log extraction output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.contains("Extracting")) {
                    logInfo("WinRAR: " + line);
                }
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            if (exitCode == 3 || exitCode == 11) {
                throw new RuntimeException("Invalid password or corrupted archive");
            }
            throw new RuntimeException("WinRAR extraction failed with exit code: " + exitCode);
        }

        // Find the extracted SQL file
        try (var stream = Files.list(extractDir)) {
            Optional<Path> sqlFile = stream
                .filter(f -> f.toString().endsWith(SQL_FILE_EXTENSION))
                .findFirst();
            
            if (sqlFile.isPresent()) {
                logInfo("Decryption successful: " + formatFileSize(Files.size(sqlFile.get())));
                return sqlFile.get();
            } else {
                throw new FileNotFoundException("No SQL file found in extracted backup");
            }
        }
    }

    /**
     * Restore database from SQL file using mysql client
     */
    private void restoreDatabaseFromSQL(DatabaseConfig dbConfig, Path configFile, Path sqlFile) 
            throws IOException, InterruptedException {
        logInfo("Restoring database from SQL file...");
        
        String mysqlCmd = Paths.get(mysqlBinPath, 
            isWindows ? "mysql.exe" : "mysql").toString();
        
        List<String> command = Arrays.asList(
            mysqlCmd,
            "--defaults-extra-file=" + configFile.toAbsolutePath(),
            dbConfig.dbName
        );
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Pipe SQL file to mysql process
        try (OutputStream processIn = process.getOutputStream()) {
            Files.copy(sqlFile, processIn);
        }

        // Log mysql output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    logInfo("MySQL: " + line);
                }
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Database restore failed with exit code: " + exitCode);
        }
        
        logInfo("Database restore completed successfully");
    }

    /**
     * Create a secure temporary MySQL configuration file
     */
    private Path createSecureMySQLConfig(DatabaseConfig config, boolean forDump) throws IOException {
        Path tempConfig = Files.createTempFile("mysql_conf_", ".cnf");
        
        String section = forDump ? "[mysqldump]" : "[mysql]";
        String content = String.format("%s%nuser=%s%npassword=%s%nhost=%s%n", 
            section, config.username, config.password, config.host);
        
        Files.writeString(tempConfig, content, StandardCharsets.UTF_8);

        // Set secure file permissions on Unix-like systems
        if (!isWindows) {
            try {
                Set<PosixFilePermission> perms = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE
                );
                Files.setPosixFilePermissions(tempConfig, perms);
            } catch (UnsupportedOperationException e) {
                logWarning("Unable to set POSIX permissions on temp config file");
            }
        }
        
        return tempConfig;
    }

    /**
     * Copy backup to Google Drive if available
     */
    private void copyBackupToGoogleDrive(Path backupFile) {
        logInfo("Attempting to sync backup to Google Drive...");
        
        try {
            Path googleDriveRoot = Paths.get("G:", "My Drive");
            
            if (!Files.exists(googleDriveRoot)) {
                logWarning("Google Drive not accessible. Skipping cloud sync.");
                return;
            }

            Path googleBackupDir = googleDriveRoot.resolve("School Backup");
            Files.createDirectories(googleBackupDir);

            Path destination = googleBackupDir.resolve(backupFile.getFileName());
            Files.copy(backupFile, destination, StandardCopyOption.REPLACE_EXISTING);
            
            logSuccess("Backup synced to Google Drive: " + destination);
            
            // Cleanup old cloud backups
            cleanupOldBackupsFromGoogleDrive(googleBackupDir);

        } catch (IOException e) {
            logError("Failed to sync backup to Google Drive: " + e.getMessage());
        }
    }

    /**
     * Cleanup old backups from local storage
     */
    private void cleanupOldBackups() {
        cleanupOldBackupsFromDirectory(backupsDir, "local");
        refreshBackupList();
    }

    /**
     * Cleanup old backups from Google Drive
     */
    private void cleanupOldBackupsFromGoogleDrive(Path googleBackupDir) {
        cleanupOldBackupsFromDirectory(googleBackupDir, "Google Drive");
    }

    /**
     * Generic method to cleanup old backups from any directory
     */
    private void cleanupOldBackupsFromDirectory(Path directory, String location) {
        try {
            if (!Files.exists(directory)) {
                return;
            }

            logInfo("Cleaning up old backups from " + location + "...");

            List<Path> backupFiles = Files.list(directory)
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(BACKUP_FILE_EXTENSION))
                    .sorted(Comparator.comparingLong((Path p) -> {
                        try {
                            return Files.getLastModifiedTime(p).toMillis();
                        } catch (IOException e) {
                            return 0;
                        }
                    }).reversed())
                    .collect(Collectors.toList());

            if (backupFiles.size() > MAX_BACKUP_RETENTION) {
                List<Path> filesToDelete = backupFiles.subList(MAX_BACKUP_RETENTION, backupFiles.size());
                
                logInfo(String.format("Keeping %d newest backups, deleting %d old backup(s) from %s", 
                    MAX_BACKUP_RETENTION, filesToDelete.size(), location));

                for (Path file : filesToDelete) {
                    try {
                        Files.deleteIfExists(file);
                        logInfo("Deleted old backup: " + file.getFileName());
                    } catch (IOException e) {
                        logError("Failed to delete " + file.getFileName() + ": " + e.getMessage());
                    }
                }
            } else {
                logInfo(String.format("No cleanup needed in %s (%d backup(s) found)", 
                    location, backupFiles.size()));
            }

        } catch (IOException e) {
            logException("Cleanup error in " + location, e);
        }
    }

    /**
     * Open backups folder in system file explorer
     */
    private void openBackupsFolder() {
        try {
            if (!Files.exists(backupsDir)) {
                Files.createDirectories(backupsDir);
            }
            
            ProcessBuilder pb;
            if (isWindows) {
                pb = new ProcessBuilder("explorer", backupsDir.toString());
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                pb = new ProcessBuilder("open", backupsDir.toString());
            } else {
                pb = new ProcessBuilder("xdg-open", backupsDir.toString());
            }
            
            pb.start();
            logInfo("Opened backups folder: " + backupsDir);
            
        } catch (IOException e) {
            logException("Failed to open backups folder", e);
        }
    }

    // ==================== Validation & Configuration Methods ====================

    /**
     * Validate all prerequisites for backup operation
     */
    private boolean validateBackupPrerequisites() {
        String password = env.getProperty("app.backups.password");
        if (password == null || password.isBlank()) {
            logError("Backup password not configured in application.properties");
            return false;
        }
        
        if (winrarExePath == null || winrarExePath.isBlank()) {
            logError("WinRAR path not configured in application.properties");
            return false;
        }
        
        return true;
    }

    /**
     * Get database configuration from properties
     */
    private DatabaseConfig getDatabaseConfig() {
        String url = env.getProperty("spring.datasource.url", "");
        return new DatabaseConfig(
            extractHostFromUrl(url),
            extractDbNameFromUrl(url),
            env.getProperty("spring.datasource.username", "root"),
            env.getProperty("spring.datasource.password", "")
        );
    }

    /**
     * Extract database name from JDBC URL
     */
    private String extractDbNameFromUrl(String url) {
        try {
            if (url.startsWith("jdbc:mysql:")) {
                String after = url.substring("jdbc:mysql://".length());
                int slashIndex = after.indexOf('/');
                if (slashIndex > 0) {
                    String dbAndParams = after.substring(slashIndex + 1);
                    int questionIndex = dbAndParams.indexOf('?');
                    return questionIndex > 0 ? dbAndParams.substring(0, questionIndex) : dbAndParams;
                }
            }
        } catch (Exception e) {
            logError("Failed to parse database name from URL: " + url);
        }
        return env.getProperty("app.db.name", "schooldb");
    }

    /**
     * Extract host from JDBC URL
     */
    private String extractHostFromUrl(String url) {
        try {
            if (url.startsWith("jdbc:mysql:")) {
                String after = url.substring("jdbc:mysql://".length());
                int slashIndex = after.indexOf('/');
                if (slashIndex > 0) {
                    String hostPort = after.substring(0, slashIndex);
                    return hostPort.contains(":") ? hostPort.split(":")[0] : hostPort;
                }
            }
        } catch (Exception e) {
            logError("Failed to parse host from URL: " + url);
        }
        return "127.0.0.1";
    }

    // ==================== UI Helper Methods ====================

    /**
     * Show password input dialog
     */
    private String promptForPassword(String title, String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(message);
        dialog.setContentText("Password:");
        
        // Make it a password field
        TextField textField = dialog.getEditor();
        textField.setPromptText("Enter password");
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Confirm restore operation with user
     */
    private boolean confirmRestoreOperation(String backupName) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Database Restore");
        confirm.setHeaderText("Restore from: " + backupName);
        confirm.setContentText(
            "⚠ WARNING: This will completely replace your current database!\n\n" +
            "• All current data will be overwritten\n" +
            "• This action cannot be undone\n" +
            "• Make sure you have a recent backup if needed\n\n" +
            "Do you want to proceed?"
        );
        
        Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // ==================== Logging Methods ====================

    private void logInfo(String message) {
        log("INFO", message);
    }

    private void logSuccess(String message) {
        log("SUCCESS", "✓ " + message);
    }

    private void logWarning(String message) {
        log("WARNING", "⚠ " + message);
    }

    private void logError(String message) {
        log("ERROR", "✗ " + message);
    }

    private void logException(String message, Exception e) {
        log("ERROR", "✗ " + message + ": " + e.getMessage());
        LOGGER.severe(message + ": " + e.getMessage());
        e.printStackTrace();
    }

    private void log(String level, String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String formattedMessage = String.format("[%s] [%s] %s", timestamp, level, message);
        
        Platform.runLater(() -> {
            logArea.appendText(formattedMessage + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
        
        LOGGER.info(message);
    }

    // ==================== Utility Methods ====================

    /**
     * Cleanup temporary files
     */
    private void cleanupTempFiles(Path... files) {
        for (Path file : files) {
            if (file != null) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    logWarning("Failed to delete temp file: " + file);
                }
            }
        }
    }

    /**
     * Cleanup temporary directory
     */
    private void cleanupTempDirectory(Path directory) {
        if (directory != null && Files.exists(directory)) {
            try {
                Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            logWarning("Failed to delete: " + path);
                        }
                    });
            } catch (IOException e) {
                logWarning("Failed to cleanup temp directory: " + directory);
            }
        }
    }

    /**
     * Format file size in human-readable format
     */
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * Format file date in readable format
     */
    private String formatFileDate(long timestamp) {
        return new SimpleDateFormat("MMM dd, yyyy HH:mm").format(new Date(timestamp));
    }

    // ==================== Inner Classes ====================

    /**
     * Database configuration container
     */
    private static class DatabaseConfig {
        final String host;
        final String dbName;
        final String username;
        final String password;

        DatabaseConfig(String host, String dbName, String username, String password) {
            this.host = host;
            this.dbName = dbName;
            this.username = username;
            this.password = password;
        }
    }
}