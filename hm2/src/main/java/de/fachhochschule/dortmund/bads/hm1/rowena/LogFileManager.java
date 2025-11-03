package de.fachhochschule.dortmund.bads.hm1.rowena;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Core log file management system. Handles file creation, deletion, and
 * metadata tracking.
 */
public class LogFileManager {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String LOG_DIR = "logs";
	private static final String METADATA_FILE = "logs/metadata.properties";

	private final Map<String, LogFileMetadata> metadataCache;
	private final Map<String, LogWriter> activeWriters;
	private final Path logDirectory;

	public record LogFileMetadata(String filePath, String equipmentType, String equipmentId, LocalDate date,
			long createdAt, long fileSize) {
		public LogFileMetadata(String filePath, String equipmentType, String equipmentId, LocalDate date,
				long createdAt, long fileSize) {
			this.filePath = filePath;
			this.equipmentType = equipmentType;
			this.equipmentId = equipmentId;
			this.date = date;
			this.createdAt = createdAt;
			this.fileSize = fileSize;
		}

		public LogFileMetadata(String filePath, String equipmentType, String equipmentId, LocalDate date) {
			this(filePath, equipmentType, equipmentId, date, System.currentTimeMillis(), 0L);
		}
	}

	public LogFileManager() {
		this.metadataCache = new HashMap<>();
		this.activeWriters = new HashMap<>();
		this.logDirectory = Paths.get(LOG_DIR);
		initializeLogDirectory();
		loadMetadata();
	}

	/**
	 * Initialize log directory structure.
	 */
	private void initializeLogDirectory() {
		try {
			if (!Files.exists(logDirectory)) {
				Files.createDirectories(logDirectory);
				LOGGER.info("Created log directory: {}", logDirectory.toAbsolutePath());
			}
		} catch (IOException e) {
			LOGGER.error("Failed to create log directory", e);
		}
	}

	/**
	 * Creates a log file for specific equipment. Uses java.nio.file.Files for file
	 * creation.
	 */
	public synchronized String createLogFile(String equipmentType, String equipmentId) {
		LocalDate today = LocalDate.now();
		long timestamp = System.currentTimeMillis();
		String fileName = equipmentType + "-" + equipmentId + "-" + today + "-" + timestamp + ".log";
		Path filePath = logDirectory.resolve(fileName);

		try {
			if (!Files.exists(filePath)) {
				Files.createFile(filePath);
				LOGGER.debug("Created log file: {}", filePath.toAbsolutePath());
			}

			// Create metadata
			LogFileMetadata metadata = new LogFileMetadata(filePath.toString(), equipmentType, equipmentId, today);
			metadataCache.put(filePath.toString(), metadata);
			saveMetadata();

			return filePath.toString();
		} catch (IOException e) {
			LOGGER.error("Failed to create log file: {}", fileName, e);
			return null;
		}
	}

	/**
	 * Deletes a log file and its metadata. Uses java.nio.file.Files.delete() for
	 * file deletion.
	 */
	public synchronized boolean deleteLogFile(String filePath) {
		try {
			Path path = Paths.get(filePath);

			// Close writer if active
			LogWriter writer = activeWriters.remove(filePath);
			if (writer != null) {
				writer.close();
			}

			// Delete the file
			if (Files.exists(path)) {
				Files.delete(path);
				LOGGER.debug("Deleted log file: {}", filePath);
			}

			// Remove metadata
			metadataCache.remove(filePath);
			saveMetadata();

			return true;
		} catch (IOException e) {
			LOGGER.error("Failed to delete log file: {}", filePath, e);
			return false;
		}
	}

	/**
	 * Writes a log entry to the appropriate equipment log file. Creates file if it
	 * doesn't exist.
	 */
	public synchronized void writeLogEntry(String equipmentType, String equipmentId, String message) {
		// Get or create log file
		String key = equipmentType + "-" + equipmentId;
		LogWriter writer = activeWriters.get(key);

		if (writer == null) {
			String filePath = createLogFile(equipmentType, equipmentId);
			if (filePath == null) {
				LOGGER.error("Cannot write log entry - file creation failed");
				return;
			}

			try {
				writer = new LogWriter(filePath);
				activeWriters.put(key, writer);
			} catch (IOException e) {
				LOGGER.error("Failed to create log writer for: {}", filePath, e);
				return;
			}
		}

		// Write entry using character stream
		try {
			writer.appendEntry(message);

			// Update file size in metadata
			String filePath = writer.getFilePath();
			LogFileMetadata metadata = metadataCache.get(filePath);
			if (metadata != null) {
				metadata = new LogFileMetadata(
						metadata.filePath(), 
						metadata.equipmentType(), 
						metadata.equipmentId(),
						metadata.date(), 
						metadata.createdAt(), 
						Files.size(Paths.get(filePath)));
			}
		} catch (IOException e) {
			LOGGER.error("Failed to write log entry", e);
		}
	}

	/**
	 * Returns all tracked log file metadata.
	 */
	public synchronized List<LogFileMetadata> getAllMetadata() {
		return new ArrayList<>(metadataCache.values());
	}

	/**
	 * Loads metadata from properties file.
	 */
	private void loadMetadata() {
		Path metadataPath = Paths.get(METADATA_FILE);
		if (!Files.exists(metadataPath)) {
			return;
		}

		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(METADATA_FILE)) {
			props.load(fis);

			for (String key : props.stringPropertyNames()) {
				String[] parts = props.getProperty(key).split("\\|");
				if (parts.length == 3) {
					LogFileMetadata metadata = new LogFileMetadata(key, parts[0], // equipmentType
							parts[1], // equipmentId
							LocalDate.parse(parts[2]) // date
					);
					metadataCache.put(key, metadata);
				}
			}
			LOGGER.debug("Loaded {} metadata entries", metadataCache.size());
		} catch (IOException e) {
			LOGGER.error("Failed to load metadata", e);
		}
	}

	/**
	 * Saves metadata to properties file.
	 */
	private void saveMetadata() {
		Properties props = new Properties();
		for (LogFileMetadata metadata : metadataCache.values()) {
			String value = metadata.equipmentType() + "|" + metadata.equipmentId() + "|" + metadata.date();
			props.setProperty(metadata.filePath(), value);
		}

		try (FileOutputStream fos = new FileOutputStream(METADATA_FILE)) {
			props.store(fos, "Log File Metadata");
			LOGGER.debug("Saved metadata for {} files", metadataCache.size());
		} catch (IOException e) {
			LOGGER.error("Failed to save metadata", e);
		}
	}

	/**
	 * Closes all active log writers.
	 */
	public synchronized void closeAll() {
		for (LogWriter writer : activeWriters.values()) {
			try {
				writer.close();
			} catch (IOException e) {
				LOGGER.error("Failed to close writer", e);
			}
		}
		activeWriters.clear();
		LOGGER.debug("Closed all log writers");
	}
}
