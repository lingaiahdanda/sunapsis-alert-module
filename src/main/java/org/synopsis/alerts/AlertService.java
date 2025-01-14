package org.synopsis.alerts;


import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class AlertService {
    private  static String rootDir = "*/code_problem"; //update the path to the root directory
    private static final String SYSTEM_LIST_PATH = rootDir + "/systemList.txt";
    private static final String ALERTS_DIRECTORY = rootDir + "/ioffice/alerts";
    private static final String INSTITUTION_ALERTS_DIRECTORY = rootDir + "/ioffice/institution/alerts";

    public static void main(String[] args) {
        try {
            moveAlertFilesToDestinationFolder();
        } catch (IOException e) {
            System.err.println("Error moving alert files: " + e.getMessage());
        } 
    }

    public static void moveAlertFilesToDestinationFolder() throws IOException {
        Set<String> systemAlerts = loadSystemAlerts();
        Path institutionAlertsPath = Paths.get(INSTITUTION_ALERTS_DIRECTORY);
        createDirectoryIfNotExists(institutionAlertsPath);

        Set<Path> alertFiles = getAlertFiles();
        if (alertFiles.isEmpty()) {
            System.out.println("No files found in the alerts directory.");
            return;
        }

        Set<String> movedFiles = new HashSet<>();
        for (Path file : alertFiles) {
            try {
                processAlertFile(file, systemAlerts, institutionAlertsPath, movedFiles);
            } catch (IOException e) {
                System.err.println("Error processing file " + file.toAbsolutePath() + ": " + e.getMessage());
            }
        }

        // Log results for debugging
        System.out.println("Number of files in alert directory: " + alertFiles.size());
        System.out.println("Number of system alert files: " + systemAlerts.size());
        System.out.println("Number of files moved: " + movedFiles.size());
    }

    private static Set<Path> getAlertFiles() throws IOException {
        Path alertsPath = Paths.get(ALERTS_DIRECTORY);
        // Check if the directory exists
        if (!Files.exists(alertsPath)) {
            throw new IOException("Alert directory does not exist: " + ALERTS_DIRECTORY);
        }

        Set<Path> alertFiles = new HashSet<>();
        try (Stream<Path> files = Files.list(alertsPath)) {
            files.forEach(file -> alertFiles.add(file));
        }
        return alertFiles;
    }

    private static Set<String> loadSystemAlerts() throws IOException {
        List<String> systemAlerts = Files.readAllLines(Paths.get(SYSTEM_LIST_PATH));
        return new HashSet<>(systemAlerts);
    }

    private static void createDirectoryIfNotExists(Path directoryPath) throws IOException {
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
            System.out.println("Created directory: " + directoryPath);
        }
    }

    private static void processAlertFile(Path file, Set<String> systemAlerts, Path institutionAlertsPath, Set<String> movedFiles) throws IOException {
        String fileName = file.getFileName().toString();

        if (!systemAlerts.contains(fileName)) {
            Path targetPath = institutionAlertsPath.resolve(fileName);
            Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
            movedFiles.add(fileName);
            System.out.println("Moved custom alert: " + fileName);
        }
    }
}