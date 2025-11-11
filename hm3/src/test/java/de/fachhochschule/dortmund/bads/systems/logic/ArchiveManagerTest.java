package de.fachhochschule.dortmund.bads.systems.logic;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.fachhochschule.dortmund.bads.systems.logic.utils.ArchiveCommand;
import de.fachhochschule.dortmund.bads.systems.logic.utils.SearchResult;

class ArchiveManagerTest {

	@TempDir
	Path tempDir;
	
	private Path logsDir;
	private Path archiveDir;
	private ArchiveManager archiveManager;

	@BeforeEach
	void setUp() throws IOException {
		logsDir = tempDir.resolve("logs");
		archiveDir = tempDir.resolve("archive");
		Files.createDirectories(logsDir);
		Files.createDirectories(archiveDir);
		
		archiveManager = new ArchiveManager(logsDir, archiveDir);
	}

	@AfterEach
	void tearDown() throws InterruptedException {
		if (archiveManager != null && archiveManager.isAlive()) {
			archiveManager.shutdown();
			archiveManager.join(1000);
		}
	}

	@Test
	void testConstructor() {
		assertNotNull(archiveManager);
		assertEquals("ArchiveManager-Thread", archiveManager.getName());
	}

	@Test
	void testThreadStartAndStop() throws InterruptedException {
		archiveManager.start();
		assertTrue(archiveManager.isAlive());
		
		archiveManager.shutdown();
		archiveManager.join(1000);
		assertFalse(archiveManager.isAlive());
	}

	@Test
	void testArchiveLogsByDate() throws IOException {
		// Create test log files with today's date
		LocalDate today = LocalDate.now();
		String dateStr = today.toString();
		Path logFile1 = logsDir.resolve("app_" + dateStr + ".log");
		Path logFile2 = logsDir.resolve("error_" + dateStr + ".log");
		
		Files.writeString(logFile1, "Log content 1");
		Files.writeString(logFile2, "Log content 2");
		
		// Archive the logs
		Path zipPath = archiveManager.archiveLogsByDate(today);
		
		assertNotNull(zipPath);
		assertTrue(Files.exists(zipPath));
		assertTrue(zipPath.toString().contains(dateStr));
		
		// Original files should be moved to originals directory
		assertFalse(Files.exists(logFile1));
		assertFalse(Files.exists(logFile2));
	}

	@Test
	void testArchiveLogsNoFiles() throws IOException {
		LocalDate pastDate = LocalDate.of(2000, 1, 1);
		Path zipPath = archiveManager.archiveLogsByDate(pastDate);
		
		assertNull(zipPath);
	}

	@Test
	void testArchiveLogsInvalidDirectory() {
		Path invalidDir = tempDir.resolve("nonexistent");
		ArchiveManager invalidManager = new ArchiveManager(invalidDir, archiveDir);
		
		assertThrows(IllegalArgumentException.class, () -> {
			invalidManager.archiveLogsByDate(LocalDate.now());
		});
	}

	@Test
	void testDearchiveZipFile() throws IOException {
		// Create and archive some logs first
		LocalDate today = LocalDate.now();
		String dateStr = today.toString();
		Path logFile = logsDir.resolve("test_" + dateStr + ".log");
		Files.writeString(logFile, "Test content");
		
		Path zipPath = archiveManager.archiveLogsByDate(today);
		assertNotNull(zipPath);
		
		// Clear logs directory
		Files.list(logsDir).forEach(p -> {
			try {
				if (Files.isRegularFile(p)) {
					Files.delete(p);
				}
			} catch (IOException e) {
				// Ignore
			}
		});
		
		// De-archive
		String zipFileName = zipPath.getFileName().toString();
		int count = archiveManager.dearchiveZipFile(zipFileName);
		
		assertTrue(count > 0);
		assertTrue(Files.exists(logsDir.resolve("test_" + dateStr + ".log")));
	}

	@Test
	void testDearchiveNonExistentZip() {
		assertThrows(IllegalArgumentException.class, () -> {
			archiveManager.dearchiveZipFile("nonexistent.zip");
		});
	}

	@Test
	void testSearchInLogs() throws IOException {
		// Create test log files with searchable content
		Path logFile1 = logsDir.resolve("app.log");
		Files.writeString(logFile1, "INFO: Application started\nERROR: Something failed\nINFO: Done");
		
		Pattern pattern = Pattern.compile("ERROR");
		List<SearchResult> results = archiveManager.searchInLogs(pattern);
		
		assertNotNull(results);
		// Should find at least one match
		int totalMatches = results.stream().mapToInt(SearchResult::getMatchCount).sum();
		assertTrue(totalMatches >= 1);
	}

	@Test
	void testSearchInLogsNoMatches() throws IOException {
		Path logFile = logsDir.resolve("app.log");
		Files.writeString(logFile, "INFO: Everything is fine");
		
		Pattern pattern = Pattern.compile("ERROR");
		List<SearchResult> results = archiveManager.searchInLogs(pattern);
		
		assertNotNull(results);
		int totalMatches = results.stream().mapToInt(SearchResult::getMatchCount).sum();
		assertEquals(0, totalMatches);
	}

	@Test
	void testSubmitCommand() throws InterruptedException {
		archiveManager.start();
		
		ArchiveCommand command = ArchiveCommand.shutdown();
		archiveManager.submitCommand(command);
		
		// Give it time to process
		archiveManager.join(1000);
		assertFalse(archiveManager.isAlive());
	}

	@Test
	void testShutdown() throws InterruptedException {
		archiveManager.start();
		assertTrue(archiveManager.isAlive());
		
		archiveManager.shutdown();
		archiveManager.join(1000);
		
		assertFalse(archiveManager.isAlive());
	}

	@Test
	void testMultipleCommands() throws InterruptedException, IOException {
		archiveManager.start();
		
		// Create test log
		LocalDate today = LocalDate.now();
		String dateStr = today.toString();
		Path logFile = logsDir.resolve("test_" + dateStr + ".log");
		Files.writeString(logFile, "Test content");
		
		// Submit archive command
		archiveManager.submitCommand(ArchiveCommand.archive(today));
		
		// Wait a bit for processing
		Thread.sleep(200);
		
		// Submit shutdown
		archiveManager.shutdown();
		archiveManager.join(1000);
		
		assertFalse(archiveManager.isAlive());
	}
}
