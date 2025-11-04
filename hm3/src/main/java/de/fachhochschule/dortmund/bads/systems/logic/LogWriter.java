package de.fachhochschule.dortmund.bads.systems.logic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thread-safe log writer using character streams.
 * Uses BufferedWriter and FileWriter for efficient text writing.
 */
public class LogWriter implements AutoCloseable {
	private final BufferedWriter writer;
	private final String filePath;
	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	public LogWriter(String filePath) throws IOException {
		this.filePath = filePath;
		// Character stream: FileWriter -> BufferedWriter for efficient text I/O
		this.writer = new BufferedWriter(new FileWriter(filePath, true)); // append mode
	}

	/**
	 * Appends a timestamped entry to the log file.
	 * Thread-safe method for concurrent writes.
	 */
	public synchronized void appendEntry(String message) throws IOException {
		String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
		writer.write("[" + timestamp + "] " + message);
		writer.newLine();
		writer.flush(); // Ensure data is written to disk
	}

	/**
	 * Closes the writer and releases resources.
	 */
	@Override
	public synchronized void close() throws IOException {
		if (writer != null) {
			writer.close();
		}
	}

	public String getFilePath() {
		return filePath;
	}
}
