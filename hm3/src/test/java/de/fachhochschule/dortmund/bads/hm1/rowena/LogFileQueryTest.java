package de.fachhochschule.dortmund.bads.hm1.rowena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.systems.logic.LogFileManager;
import de.fachhochschule.dortmund.bads.systems.logic.LogFileQuery;
import de.fachhochschule.dortmund.bads.systems.logic.LogFileManager.LogFileMetadata;

/**
 * Unit tests specifically for LogFileQuery functionality
 * isolated from Module2 demonstrations.
 */
class LogFileQueryTest {

    private LogFileManager logFileManager;
    private LogFileQuery logFileQuery;

    @BeforeEach
    void setUp() {
        logFileManager = new LogFileManager();
        logFileQuery = new LogFileQuery(logFileManager);
        
        // Create comprehensive test data matching Module2 demonstrations
        setupTestData();
    }

    @AfterEach
    void tearDown() {
        if (logFileManager != null) {
            logFileManager.closeAll();
            cleanupTestFiles();
        }
    }

    private void setupTestData() {
        // Create the same test data as used in demonstrateQueryLogs()
        logFileManager.writeLogEntry("system", "main", "System started successfully");
        logFileManager.writeLogEntry("AGV", "001", "AGV initialized at position (0, 0)");
        logFileManager.writeLogEntry("AGV", "002", "AGV initialized at position (10, 5)");
        logFileManager.writeLogEntry("AGV", "101", "AGV initialized at position (20, 15)");
        logFileManager.writeLogEntry("charging-station", "001", "Charging station ready");
        logFileManager.writeLogEntry("charging-station", "002", "Charging station ready");
        logFileManager.writeLogEntry("charging-station", "101", "Charging station ready");
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
    @DisplayName("Test Query 1: Find all AGV logs using regex 'AGV.*'")
    void testFindAGVLogsWithRegex() {
        // This replicates Query 1 from demonstrateQueryLogs()
        List<LogFileMetadata> agvLogs = logFileQuery.findByEquipmentName("AGV.*");
        
        assertEquals(3, agvLogs.size(), "Should find all 3 AGV logs");
        
        // Verify all results are AGV equipment
        boolean allAreAGV = agvLogs.stream()
            .allMatch(log -> log.equipmentType().startsWith("AGV"));
        assertTrue(allAreAGV, "All results should be AGV equipment");
        
        // Verify we have the expected IDs
        List<String> agvIds = agvLogs.stream()
            .map(LogFileMetadata::equipmentId)
            .sorted()
            .toList();
        assertEquals(List.of("001", "002", "101"), agvIds, "Should find AGV IDs: 001, 002, 101");
    }

    @Test
    @DisplayName("Test Query 2: Find logs ending with '001' using regex '.*001'")
    void testFindLogsEndingWith001() {
        // This replicates Query 2 from demonstrateQueryLogs()
        List<LogFileMetadata> logs001 = logFileQuery.findByEquipmentName(".*001");
        
        assertEquals(2, logs001.size(), "Should find 2 logs ending with '001'");
        
        // Verify all results have ID ending with '001'
        boolean allEndWith001 = logs001.stream()
            .allMatch(log -> log.equipmentId().endsWith("001"));
        assertTrue(allEndWith001, "All results should have ID ending with '001'");
        
        // Verify we have both AGV-001 and charging-station-001
        boolean hasAGV001 = logs001.stream()
            .anyMatch(log -> "AGV".equals(log.equipmentType()) && "001".equals(log.equipmentId()));
        boolean hasCharger001 = logs001.stream()
            .anyMatch(log -> "charging-station".equals(log.equipmentType()) && "001".equals(log.equipmentId()));
            
        assertTrue(hasAGV001, "Should find AGV-001");
        assertTrue(hasCharger001, "Should find charging-station-001");
    }

    @Test
    @DisplayName("Test Query 3: Find logs for today using date pattern")
    void testFindLogsByDatePattern() {
        // This replicates Query 3 from demonstrateQueryLogs()
        String todayPattern = LocalDate.now().toString();
        List<LogFileMetadata> todayLogs = logFileQuery.findByDatePattern(todayPattern);
        
        assertEquals(7, todayLogs.size(), "Should find all 7 logs created today");
        
        // Verify all logs are from today
        boolean allFromToday = todayLogs.stream()
            .allMatch(log -> log.date().equals(LocalDate.now()));
        assertTrue(allFromToday, "All results should be from today");
    }

    @Test
    @DisplayName("Test Query 4: Find all charging-station logs")
    void testFindChargingStationLogs() {
        // This replicates Query 4 from demonstrateQueryLogs()
        List<LogFileMetadata> chargerLogs = logFileQuery.findByEquipmentType("charging-station");
        
        assertEquals(3, chargerLogs.size(), "Should find all 3 charging-station logs");
        
        // Verify all results are charging-station equipment
        boolean allAreChargers = chargerLogs.stream()
            .allMatch(log -> "charging-station".equals(log.equipmentType()));
        assertTrue(allAreChargers, "All results should be charging-station equipment");
        
        // Verify we have the expected IDs
        List<String> chargerIds = chargerLogs.stream()
            .map(LogFileMetadata::equipmentId)
            .sorted()
            .toList();
        assertEquals(List.of("001", "002", "101"), chargerIds, "Should find charger IDs: 001, 002, 101");
    }

    @Test
    @DisplayName("Test Query 5: Advanced search with multiple patterns")
    void testAdvancedSearchMultiplePatterns() {
        // This replicates Query 5 from demonstrateQueryLogs()
        List<LogFileMetadata> advancedResults = logFileQuery.advancedSearch(".*", "00[12]", ".*");
        
        assertEquals(4, advancedResults.size(), "Should find 4 logs with ID pattern 00[12]");
        
        // Verify all results have IDs matching pattern 00[12] (001 or 002)
        boolean allMatchPattern = advancedResults.stream()
            .allMatch(log -> log.equipmentId().equals("001") || log.equipmentId().equals("002"));
        assertTrue(allMatchPattern, "All results should have ID '001' or '002'");
        
        // Verify we don't have any logs with ID '101'
        boolean hasID101 = advancedResults.stream()
            .anyMatch(log -> "101".equals(log.equipmentId()));
        assertFalse(hasID101, "Should not include logs with ID '101'");
    }

    @Test
    @DisplayName("Test regex pattern matching edge cases")
    void testRegexPatternEdgeCases() {
        // Test exact match - "AGV" pattern will match "AGV" equipment type
        List<LogFileMetadata> exactMatch = logFileQuery.findByEquipmentName("AGV");
        assertEquals(3, exactMatch.size(), "Pattern 'AGV' should match 'AGV' equipment type");
        
        // Test case sensitivity
        List<LogFileMetadata> lowerCase = logFileQuery.findByEquipmentName("agv.*");
        assertEquals(0, lowerCase.size(), "Lowercase 'agv.*' should not match uppercase 'AGV'");
        
        // Test wildcard patterns
        List<LogFileMetadata> allLogs = logFileQuery.findByEquipmentName(".*");
        assertEquals(7, allLogs.size(), "Pattern '.*' should match all equipment names");
        
        // Test specific ID pattern
        List<LogFileMetadata> mainSystem = logFileQuery.findByEquipmentName(".*main");
        assertEquals(1, mainSystem.size(), "Should find only system-main");
        assertEquals("system", mainSystem.get(0).equipmentType());
        assertEquals("main", mainSystem.get(0).equipmentId());
    }

    @Test
    @DisplayName("Test date pattern variations")
    void testDatePatternVariations() {
        String today = LocalDate.now().toString();
        
        // Test full date pattern
        List<LogFileMetadata> fullDate = logFileQuery.findByDatePattern(today);
        assertEquals(7, fullDate.size(), "Should find all logs for exact date");
        
        // Test year pattern
        String year = today.substring(0, 4);
        List<LogFileMetadata> yearLogs = logFileQuery.findByDatePattern(year);
        assertEquals(7, yearLogs.size(), "Should find all logs for current year");
        
        // Test month pattern
        String yearMonth = today.substring(0, 7); // YYYY-MM
        List<LogFileMetadata> monthLogs = logFileQuery.findByDatePattern(yearMonth);
        assertEquals(7, monthLogs.size(), "Should find all logs for current month");
    }

    @Test
    @DisplayName("Test equipment type filtering")
    void testEquipmentTypeFiltering() {
        // Test system logs
        List<LogFileMetadata> systemLogs = logFileQuery.findByEquipmentType("system");
        assertEquals(1, systemLogs.size(), "Should find 1 system log");
        assertEquals("main", systemLogs.get(0).equipmentId());
        
        // Test AGV logs
        List<LogFileMetadata> agvLogs = logFileQuery.findByEquipmentType("AGV");
        assertEquals(3, agvLogs.size(), "Should find 3 AGV logs");
        
        // Test non-existent type
        List<LogFileMetadata> nonExistent = logFileQuery.findByEquipmentType("non-existent");
        assertEquals(0, nonExistent.size(), "Should find no logs for non-existent type");
    }

    @Test
    @DisplayName("Test advanced search parameter combinations")
    void testAdvancedSearchCombinations() {
        // Test specific equipment type with ID pattern
        List<LogFileMetadata> agvWith001 = logFileQuery.advancedSearch("AGV.*", "001", ".*");
        assertEquals(1, agvWith001.size(), "Should find only AGV-001");
        assertEquals("AGV", agvWith001.get(0).equipmentType());
        assertEquals("001", agvWith001.get(0).equipmentId());
        
        // Test charging stations with specific ID pattern
        List<LogFileMetadata> chargersWithPattern = logFileQuery.advancedSearch("charging-station.*", "10[12]", ".*");
        assertEquals(1, chargersWithPattern.size(), "Should find only charging-station-101");
        assertEquals("charging-station", chargersWithPattern.get(0).equipmentType());
        assertEquals("101", chargersWithPattern.get(0).equipmentId());
        
        // Test impossible combination
        List<LogFileMetadata> impossible = logFileQuery.advancedSearch("AGV.*", "main", ".*");
        assertEquals(0, impossible.size(), "Should find no AGV equipment with ID 'main'");
    }

    @Test
    @DisplayName("Test query performance with no results")
    void testQueriesWithNoResults() {
        // Test patterns that should return empty results
        List<LogFileMetadata> noResults1 = logFileQuery.findByEquipmentName("ROBOT.*");
        assertTrue(noResults1.isEmpty(), "Should return empty list for non-matching pattern");
        
        List<LogFileMetadata> noResults2 = logFileQuery.findByEquipmentType("robot");
        assertTrue(noResults2.isEmpty(), "Should return empty list for non-existent equipment type");
        
        List<LogFileMetadata> noResults3 = logFileQuery.findByDatePattern("2020-01-01");
        assertTrue(noResults3.isEmpty(), "Should return empty list for past date");
        
        List<LogFileMetadata> noResults4 = logFileQuery.advancedSearch("ROBOT.*", ".*", ".*");
        assertTrue(noResults4.isEmpty(), "Should return empty list for non-matching advanced search");
    }
}
