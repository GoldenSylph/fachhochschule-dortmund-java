package de.fachhochschule.dortmund.bads.hm1.rowena;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.systems.logic.LogFileManager;
import de.fachhochschule.dortmund.bads.systems.logic.LogFileQuery;
import de.fachhochschule.dortmund.bads.systems.logic.LogFileManager.LogFileMetadata;

/**
 * Integration tests for Module2 functionality.
 * Tests complete workflows and scenarios from the demonstrations.
 */
class CustomLoggerIntegrationTest {

    private LogFileManager logFileManager;
    private LogFileQuery logFileQuery;

    @BeforeEach
    void setUp() {
        logFileManager = new LogFileManager();
        logFileQuery = new LogFileQuery(logFileManager);
    }

    @AfterEach
    void tearDown() {
        if (logFileManager != null) {
            logFileManager.closeAll();
            cleanupTestFiles();
        }
    }

    private void cleanupTestFiles() {
        try {
            Path logDir = Paths.get("logs");
            if (Files.exists(logDir)) {
                Files.walk(logDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            Files.deleteIfExists(file);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
            }
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }

    @Test
    @DisplayName("Test complete workflow: Create -> Query -> Delete")
    void testCompleteWorkflow() {
        // Step 1: Create logs (from demonstrateCreateLogs)
        logFileManager.writeLogEntry("system", "main", "System started successfully");
        logFileManager.writeLogEntry("AGV", "001", "AGV initialized at position (0, 0)");
        logFileManager.writeLogEntry("AGV", "002", "AGV initialized at position (10, 5)");
        logFileManager.writeLogEntry("charging-station", "001", "Charging station ready");
        logFileManager.writeLogEntry("charging-station", "002", "Charging station ready");

        // Verify creation
        List<LogFileMetadata> initialLogs = logFileManager.getAllMetadata();
        assertEquals(5, initialLogs.size(), "Should have created 5 log files");

        // Step 2: Query logs (from demonstrateQueryLogs)
        List<LogFileMetadata> agvLogs = logFileQuery.findByEquipmentName("AGV.*");
        assertEquals(2, agvLogs.size(), "Should find 2 AGV logs");

        List<LogFileMetadata> logs001 = logFileQuery.findByEquipmentName(".*001");
        assertEquals(2, logs001.size(), "Should find 2 logs ending with '001'");

        // Step 3: Delete logs (from demonstrateDeleteLogs)
        LogFileMetadata toDelete = initialLogs.get(0);
        boolean deleted = logFileManager.deleteLogFile(toDelete.filePath());
        assertTrue(deleted, "Should successfully delete log file");

        List<LogFileMetadata> remainingLogs = logFileManager.getAllMetadata();
        assertEquals(4, remainingLogs.size(), "Should have 4 logs remaining after deletion");
    }

    @Test
    @DisplayName("Test log activity simulation workflow")
    void testLogActivityWorkflow() {
        // Simulate the complete demonstrateLogActivity scenario
        
        // AGV activity sequence
        logFileManager.writeLogEntry("AGV", "001", "Started movement sequence");
        logFileManager.writeLogEntry("AGV", "001", "Moving to position (5, 10)");
        logFileManager.writeLogEntry("AGV", "001", "Reached destination");
        logFileManager.writeLogEntry("AGV", "001", "Picking up beverage box #42");

        // System events
        logFileManager.writeLogEntry("system", "main", "Task queue: 3 tasks pending");
        logFileManager.writeLogEntry("system", "main", "Storage occupancy: 67%");

        // Charging station events
        logFileManager.writeLogEntry("charging-station", "001", "AGV docked for charging");
        logFileManager.writeLogEntry("charging-station", "001", "Battery level: 23% -> 87%");
        logFileManager.writeLogEntry("charging-station", "001", "AGV undocked");

        // Verify the activity resulted in appropriate log files
        List<LogFileMetadata> allLogs = logFileManager.getAllMetadata();
        assertEquals(3, allLogs.size(), "Should have 3 distinct log files (AGV-001, system-main, charging-station-001)");

        // Verify each equipment type exists
        assertTrue(allLogs.stream().anyMatch(log -> "AGV".equals(log.equipmentType())));
        assertTrue(allLogs.stream().anyMatch(log -> "system".equals(log.equipmentType())));
        assertTrue(allLogs.stream().anyMatch(log -> "charging-station".equals(log.equipmentType())));
    }

    @Test
    @DisplayName("Test scenario: Create logs, run queries, then add more activity")
    void testCreateQueryActivityScenario() {
        // Phase 1: Initial log creation
        logFileManager.writeLogEntry("system", "main", "System started successfully");
        logFileManager.writeLogEntry("AGV", "001", "AGV initialized at position (0, 0)");
        logFileManager.writeLogEntry("charging-station", "001", "Charging station ready");

        assertEquals(3, logFileManager.getAllMetadata().size(), "Should have 3 initial logs");

        // Phase 2: Run queries on initial data
        List<LogFileMetadata> initialAGVs = logFileQuery.findByEquipmentName("AGV.*");
        assertEquals(1, initialAGVs.size(), "Should find 1 AGV initially");

        // Phase 3: Add more activity (like in demonstrateLogActivity)
        logFileManager.writeLogEntry("AGV", "001", "Started movement sequence");
        logFileManager.writeLogEntry("AGV", "002", "AGV initialized at position (10, 5)");
        logFileManager.writeLogEntry("charging-station", "002", "Charging station ready");

        // Phase 4: Verify queries reflect new activity
        List<LogFileMetadata> finalAGVs = logFileQuery.findByEquipmentName("AGV.*");
        assertEquals(2, finalAGVs.size(), "Should find 2 AGVs after activity");

        List<LogFileMetadata> allChargers = logFileQuery.findByEquipmentType("charging-station");
        assertEquals(2, allChargers.size(), "Should find 2 charging stations");

        assertEquals(5, logFileManager.getAllMetadata().size(), "Should have 5 total logs");
    }

    @Test
    @DisplayName("Test deletion scenarios from displayLogsAndPromptDelete")
    void testDeletionScenarios() {
        // Create test logs similar to what would be displayed
        logFileManager.writeLogEntry("AGV", "001", "AGV test entry");
        logFileManager.writeLogEntry("system", "main", "System test entry");
        logFileManager.writeLogEntry("charging-station", "001", "Charger test entry");

        List<LogFileMetadata> initialLogs = logFileManager.getAllMetadata();
        assertEquals(3, initialLogs.size(), "Should have 3 logs for deletion testing");

        // Test deleting first log (index 0, which would be "1" in user input)
        LogFileMetadata firstLog = initialLogs.get(0);
        boolean deleted1 = logFileManager.deleteLogFile(firstLog.filePath());
        assertTrue(deleted1, "Should successfully delete first log");

        // Test deleting another log (simulate selecting index 2 from original list)
        if (initialLogs.size() > 2) {
            LogFileMetadata thirdLog = initialLogs.get(2);
            // Check if this log still exists (it should, since we only deleted the first one)
            List<LogFileMetadata> currentLogs = logFileManager.getAllMetadata();
            boolean thirdLogExists = currentLogs.stream()
                .anyMatch(log -> log.filePath().equals(thirdLog.filePath()));
            
            if (thirdLogExists) {
                boolean deleted2 = logFileManager.deleteLogFile(thirdLog.filePath());
                assertTrue(deleted2, "Should successfully delete third log");
            }
        }

        // Verify final state
        List<LogFileMetadata> remainingLogs = logFileManager.getAllMetadata();
        assertTrue(remainingLogs.size() <= 2, "Should have at most 2 logs remaining");
        assertTrue(remainingLogs.size() >= 1, "Should have at least 1 log remaining");
    }

    @Test
    @DisplayName("Test main method workflow simulation")
    void testMainMethodWorkflow() {
        // Simulate the complete main method workflow without the interactive parts
        
        // 1. Create initial logs
        logFileManager.writeLogEntry("system", "main", "System started successfully");
        logFileManager.writeLogEntry("AGV", "001", "AGV initialized at position (0, 0)");
        logFileManager.writeLogEntry("AGV", "002", "AGV initialized at position (10, 5)");
        logFileManager.writeLogEntry("charging-station", "001", "Charging station ready");
        logFileManager.writeLogEntry("charging-station", "002", "Charging station ready");

        assertTrue(logFileManager.getAllMetadata().size() >= 4, "Should have created initial logs");

        // 2. Simulate some deletion (skip interactive part)
        List<LogFileMetadata> allLogs = logFileManager.getAllMetadata();
        if (!allLogs.isEmpty()) {
            logFileManager.deleteLogFile(allLogs.get(0).filePath());
        }

        // 3. Continue with log activity if logs remain
        if (!logFileManager.getAllMetadata().isEmpty()) {
            // Add log activity
            logFileManager.writeLogEntry("AGV", "001", "Started movement sequence");
            logFileManager.writeLogEntry("system", "main", "Task queue: 3 tasks pending");
            logFileManager.writeLogEntry("charging-station", "001", "AGV docked for charging");

            // Run queries
            List<LogFileMetadata> agvLogs = logFileQuery.findByEquipmentName("AGV.*");
            assertFalse(agvLogs.isEmpty(), "Should find AGV logs after activity");

            List<LogFileMetadata> todayLogs = logFileQuery.findByDatePattern(java.time.LocalDate.now().toString());
            assertFalse(todayLogs.isEmpty(), "Should find today's logs");
        }

        // 4. Final shutdown
        assertDoesNotThrow(() -> logFileManager.closeAll(), "Shutdown should complete successfully");
    }

    @Test
    @DisplayName("Test error handling and edge cases in workflows")
    void testWorkflowErrorHandling() {
        // Test deletion of non-existent files
        boolean deleteResult = logFileManager.deleteLogFile("non-existent-file.log");
        assertFalse(deleteResult, "Should return false for non-existent file deletion");

        // Test queries on empty system
        List<LogFileMetadata> emptyResults = logFileQuery.findByEquipmentName(".*");
        assertTrue(emptyResults.isEmpty(), "Should return empty list when no logs exist");

        // Create log, then delete it, then query
        logFileManager.writeLogEntry("test", "001", "Test entry");
        List<LogFileMetadata> beforeDelete = logFileManager.getAllMetadata();
        assertEquals(1, beforeDelete.size(), "Should have one log");

        logFileManager.deleteLogFile(beforeDelete.get(0).filePath());
        List<LogFileMetadata> afterDelete = logFileQuery.findByEquipmentType("test");
        assertTrue(afterDelete.isEmpty(), "Should find no logs after deletion");
    }

    @Test
    @DisplayName("Test file path consistency across operations")
    void testFilePathConsistency() {
        // Create logs and verify file paths are consistent across operations
        logFileManager.writeLogEntry("AGV", "001", "Initial entry");
        
        List<LogFileMetadata> initialMetadata = logFileManager.getAllMetadata();
        assertEquals(1, initialMetadata.size(), "Should have one log");
        
        String initialPath = initialMetadata.get(0).filePath();
        assertNotNull(initialPath, "File path should not be null");
        assertTrue(initialPath.contains("AGV"), "File path should contain equipment type");

        // Add more entries to same equipment
        logFileManager.writeLogEntry("AGV", "001", "Second entry");
        logFileManager.writeLogEntry("AGV", "001", "Third entry");

        // Verify metadata still shows same file path
        List<LogFileMetadata> updatedMetadata = logFileManager.getAllMetadata();
        assertEquals(1, updatedMetadata.size(), "Should still have one log file");
        assertEquals(initialPath, updatedMetadata.get(0).filePath(), "File path should remain consistent");

        // Test querying by equipment name returns same path
        List<LogFileMetadata> queryResults = logFileQuery.findByEquipmentName("AGV.*");
        assertEquals(1, queryResults.size(), "Query should find the log");
        assertEquals(initialPath, queryResults.get(0).filePath(), "Query result should have same file path");
    }
}