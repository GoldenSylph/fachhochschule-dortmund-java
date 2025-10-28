package de.fachhochschule.dortmund.bads.hm1.rowena;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.Systems;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.Observation;

/**
 * Module2 - Log File Management System
 * Demonstrates file I/O operations: create, delete, and query log files.
 * Uses character streams (BufferedWriter/FileWriter) and regular expressions.
 */
public enum Module2 {
	INSTANCE;

	private static final Logger LOGGER = LogManager.getLogger();
	private LogFileManager logFileManager;
	private LogFileQuery logFileQuery;
	
	/**
	 * Demonstrates creating log files for different equipment types.
	 */
	public void demonstrateCreateLogs() {
		LOGGER.info("=== Demonstrating Log File Creation ===");

		// Write sample entries - files created automatically on first write
		logFileManager.writeLogEntry("system", "main", "System started successfully");
		logFileManager.writeLogEntry("AGV", "001", "AGV initialized at position (0, 0)");
		logFileManager.writeLogEntry("AGV", "002", "AGV initialized at position (10, 5)");
		logFileManager.writeLogEntry("charging-station", "001", "Charging station ready");
		logFileManager.writeLogEntry("charging-station", "002", "Charging station ready");

		LOGGER.info("Created {} log files with sample entries\n", logFileManager.getAllMetadata().size());
	}

	/**
	 * Demonstrates querying log files using regular expressions.
	 */
	public void demonstrateQueryLogs() {
		LOGGER.info("=== Demonstrating Log File Queries with Regex ===");

		// Query by equipment name pattern (regex)
		LOGGER.info("\n--- Query 1: Find all AGV logs using regex 'AGV.*' ---");
		List<LogFileMetadata> agvLogs = logFileQuery.findByEquipmentName("AGV.*");
		printQueryResults(agvLogs);

		// Query by specific ID pattern
		LOGGER.info("\n--- Query 2: Find logs ending with '001' using regex '.*001' ---");
		List<LogFileMetadata> logs001 = logFileQuery.findByEquipmentName(".*001");
		printQueryResults(logs001);

		// Query by date pattern
		LOGGER.info("\n--- Query 3: Find logs for today using date pattern ---");
		String todayPattern = LocalDate.now().toString();
		List<LogFileMetadata> todayLogs = logFileQuery.findByDatePattern(todayPattern);
		printQueryResults(todayLogs);

		// Query by equipment type
		LOGGER.info("\n--- Query 4: Find all charging-station logs ---");
		List<LogFileMetadata> chargerLogs = logFileQuery.findByEquipmentType("charging-station");
		printQueryResults(chargerLogs);

		// Advanced regex search
		LOGGER.info("\n--- Query 5: Advanced search with multiple patterns ---");
		List<LogFileMetadata> advancedResults = logFileQuery.advancedSearch(".*", "00[12]", ".*");
		printQueryResults(advancedResults);
	}

	/**
	 * Demonstrates deleting log files.
	 */
	public void demonstrateDeleteLogs() {
		LOGGER.info("=== Demonstrating Log File Deletion ===");

		// Get all metadata before deletion
		List<LogFileMetadata> allLogs = logFileManager.getAllMetadata();
		LOGGER.info("Total log files before deletion: {}", allLogs.size());

		if (!allLogs.isEmpty()) {
			// Delete the first log file as example
			LogFileMetadata toDelete = allLogs.get(0);
			LOGGER.info("Deleting log file: {}", toDelete.getFilePath());

			boolean deleted = logFileManager.deleteLogFile(toDelete.getFilePath());
			if (deleted) {
				LOGGER.info("Successfully deleted log file");

				// Show updated count
				LOGGER.info("Total log files after deletion: {}", logFileManager.getAllMetadata().size());
			} else {
				LOGGER.error("Failed to delete log file");
			}
		} else {
			LOGGER.info("No log files to delete");
		}
	}

	/**
	 * Displays all log files and prompts user for interactive deletion.
	 * Uses Scanner for character stream input.
	 */
	public void displayLogsAndPromptDelete() {
		Scanner scanner = new Scanner(System.in);

		List<LogFileMetadata> allLogs = logFileManager.getAllMetadata();

		if (allLogs.isEmpty()) {
			LOGGER.info("No log files to display.");
			return;
		}

		// Display all logs with numbers
		LOGGER.info("\n=== Created Log Files ===");
		for (int i = 0; i < allLogs.size(); i++) {
			LogFileMetadata meta = allLogs.get(i);
			LOGGER.info("[{}] {} (Type: {}, ID: {}, Date: {})",
				i + 1,
				meta.getFilePath(),
				meta.getEquipmentType(),
				meta.getEquipmentId(),
				meta.getDate());
		}

		// Prompt user
		System.out.print("\nDelete logs? Enter numbers (e.g., 1,3) or 'n' to skip: ");
		String input = scanner.nextLine().trim();

		if (input.equalsIgnoreCase("n")) {
			LOGGER.info("Skipped deletion.");
			return;
		}

		// Parse and delete
		String[] numbers = input.split(",");
		int deletedCount = 0;

		for (String num : numbers) {
			try {
				int index = Integer.parseInt(num.trim()) - 1;
				if (index >= 0 && index < allLogs.size()) {
					LogFileMetadata toDelete = allLogs.get(index);
					String fileName = Paths.get(toDelete.getFilePath()).getFileName().toString();
					boolean deleted = logFileManager.deleteLogFile(toDelete.getFilePath());
					if (deleted) {
						LOGGER.info("✓ Deleted: {}", fileName);
						deletedCount++;
					} else {
						LOGGER.warn("✗ Failed to delete: {}", fileName);
					}
				} else {
					LOGGER.warn("Invalid number: {} (out of range)", num.trim());
				}
			} catch (NumberFormatException e) {
				LOGGER.warn("Invalid input: {}", num.trim());
			}
		}

		LOGGER.info("\nDeleted {} of {} selected log files.", deletedCount, numbers.length);
		LOGGER.info("Remaining logs: {}", logFileManager.getAllMetadata().size());
	}

	/**
	 * Demonstrates writing multiple log entries to simulate real activity.
	 */
	public void demonstrateLogActivity() {
		LOGGER.info("=== Demonstrating Log Activity ===");

		// Simulate AGV activity
		logFileManager.writeLogEntry("AGV", "001", "Started movement sequence");
		logFileManager.writeLogEntry("AGV", "001", "Moving to position (5, 10)");
		logFileManager.writeLogEntry("AGV", "001", "Reached destination");
		logFileManager.writeLogEntry("AGV", "001", "Picking up beverage box #42");

		// Simulate system events
		logFileManager.writeLogEntry("system", "main", "Task queue: 3 tasks pending");
		logFileManager.writeLogEntry("system", "main", "Storage occupancy: 67%");

		// Simulate charging station events
		logFileManager.writeLogEntry("charging-station", "001", "AGV docked for charging");
		logFileManager.writeLogEntry("charging-station", "001", "Battery level: 23% -> 87%");
		logFileManager.writeLogEntry("charging-station", "001", "AGV undocked");
	}

	/**
	 * Helper method to print query results.
	 */
	private void printQueryResults(List<LogFileMetadata> results) {
		LOGGER.info("Found {} matching log file(s)", results.size());
		if (!results.isEmpty() && results.size() <= 3) {
			// Show details only if 3 or fewer results
			for (LogFileMetadata metadata : results) {
				LOGGER.info("  - {} [{}, {}]",
					metadata.getFilePath(),
					metadata.getEquipmentType(),
					metadata.getEquipmentId());
			}
		} else if (results.size() > 3) {
			LOGGER.info("  (showing first 3 of {}):", results.size());
			for (int i = 0; i < 3; i++) {
				LogFileMetadata metadata = results.get(i);
				LOGGER.info("  - {} [{}, {}]",
					metadata.getFilePath(),
					metadata.getEquipmentType(),
					metadata.getEquipmentId());
			}
		}
	}

	/**
	 * Method to close all log writers.
	 */
	public void shutdown() {
		if (logFileManager != null) {
			logFileManager.closeAll();
			LOGGER.info("Log File Management System shut down");
		}
	}

	/**
	 * Main method
	 */
	public static void main(String[] args) {
		LOGGER.info("========================================");
		LOGGER.info(" Log Files Management Demo");
		LOGGER.info("========================================\n");

		Module2 module2 = Module2.INSTANCE;

		// Initialize (without CoreConfiguration)
		module2.logFileManager = new LogFileManager();
		module2.logFileQuery = new LogFileQuery(module2.logFileManager);
		module2.initialized = true;

		try {
			// Create logs
			module2.demonstrateCreateLogs();
			Thread.sleep(1000);

			// Interactive deletion prompt
			module2.displayLogsAndPromptDelete();
			Thread.sleep(1000);

			// Continue demo with remaining logs (if any)
			if (!module2.logFileManager.getAllMetadata().isEmpty()) {
				module2.demonstrateLogActivity();
				Thread.sleep(1000);

				module2.demonstrateQueryLogs();
			} else {
				LOGGER.info("\nAll logs deleted. Skipping remaining demos.");
			}

			LOGGER.info("\n========================================");
			LOGGER.info("  Completed successfully!");
			LOGGER.info("  Check the 'logs/' directory for files");
			LOGGER.info("========================================");

		} catch (InterruptedException e) {
			LOGGER.error("interrupted", e);
		} finally {
			module2.shutdown();
		}
	}
}
