package de.fachhochschule.dortmund.bads.systems.logic;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.util.stream.*;
import java.util.zip.*;
import org.apache.logging.log4j.*;

import de.fachhochschule.dortmund.bads.systems.logic.utils.*;

/**
 * Thread-based archive manager for log files with archive, de-archive, and
 * search capabilities.
 */
public class ArchiveManager extends Thread {
	private static final Logger LOGGER = LogManager.getLogger(ArchiveManager.class);

	private final Path logsDirectory;
	private final Path archiveDirectory;
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final BlockingQueue<ArchiveCommand> commandQueue = new LinkedBlockingQueue<>();
	private volatile boolean running = true;

	public ArchiveManager(Path logsDirectory, Path archiveDirectory) {
		super("ArchiveManager-Thread");
		this.logsDirectory = logsDirectory;
		this.archiveDirectory = archiveDirectory;
		LOGGER.debug("Initialized - logs: {}, archive: {}", logsDirectory, archiveDirectory);
	}

	@Override
	public void run() {
		LOGGER.info("Thread started");
		while (running) {
			try {
				ArchiveCommand command = commandQueue.take();
				LOGGER.debug("Processing: {}", command);
				processCommand(command);
			} catch (InterruptedException e) {
				LOGGER.warn("Interrupted", e);
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) {
				LOGGER.error("Error processing command", e);
			}
		}
		LOGGER.info("Thread stopped");
	}

	public void submitCommand(ArchiveCommand command) {
		try {
			commandQueue.put(command);
			LOGGER.debug("Submitted: {}", command);
		} catch (InterruptedException e) {
			LOGGER.error("Failed to submit command", e);
			Thread.currentThread().interrupt();
		}
	}

	public void shutdown() {
		LOGGER.info("Shutdown requested");
		running = false;
		submitCommand(ArchiveCommand.shutdown());
	}

	private void processCommand(ArchiveCommand command) throws IOException {
		switch (command.getType()) {
		case ARCHIVE:
			archiveLogsByDate(command.getDate());
			break;
		case DEARCHIVE:
			dearchiveZipFile(command.getZipFileName());
			break;
		case SEARCH:
			searchInLogs(command.getSearchPattern());
			break;
		case SHUTDOWN:
			running = false;
			LOGGER.info("Shutdown command received");
			break;
		default:
			LOGGER.warn("Unknown command: {}", command.getType());
		}
	}

	public Path archiveLogsByDate(LocalDate date) throws IOException {
		LOGGER.info("Archiving logs for: {}", date);

		if (!Files.exists(logsDirectory) || !Files.isDirectory(logsDirectory)) {
			throw new IllegalArgumentException("Logs directory doesn't exist: " + logsDirectory);
		}

		long start = System.currentTimeMillis();
		Files.createDirectories(archiveDirectory);

		String dateStr = date.format(dateFormatter);
		try (Stream<Path> stream = Files.list(logsDirectory)) {
			List<Path> toArchive = stream.filter(Files::isRegularFile)
					.filter(p -> p.getFileName().toString().contains(dateStr)).collect(Collectors.toList());

			if (toArchive.isEmpty()) {
				LOGGER.info("No files for date: {}", dateStr);
				return null;
			}

			Path zipPath = archiveDirectory.resolve("logs_" + dateStr + ".zip");
			Path originalsDir = archiveDirectory.resolve("originals").resolve(dateStr);
			Files.createDirectories(originalsDir);

			zipFiles(toArchive, zipPath);

			for (Path p : toArchive) {
				Files.move(p, originalsDir.resolve(p.getFileName()), StandardCopyOption.REPLACE_EXISTING);
			}

			LOGGER.info("Archived {} file(s) in {}ms", toArchive.size(), System.currentTimeMillis() - start);
			return zipPath;
		}
	}

	public int dearchiveZipFile(String zipFileName) throws IOException {
		LOGGER.info("De-archiving: {}", zipFileName);

		Path zipPath = archiveDirectory.resolve(zipFileName);
		if (!Files.exists(zipPath)) {
			throw new IllegalArgumentException("ZIP not found: " + zipPath);
		}

		long start = System.currentTimeMillis();
		int count = 0;
		Files.createDirectories(logsDirectory);

		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipPath)))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					Path target = logsDirectory.resolve(entry.getName());
					try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(target))) {
						byte[] buffer = new byte[8192];
						int len;
						while ((len = zis.read(buffer)) != -1) {
							os.write(buffer, 0, len);
						}
					}
					count++;
				}
				zis.closeEntry();
			}
		}

		LOGGER.info("Extracted {} file(s) in {}ms", count, System.currentTimeMillis() - start);
		return count;
	}

	public List<SearchResult> searchInLogs(Pattern pattern) throws IOException {
		LOGGER.info("Searching with pattern: {}", pattern.pattern());

		long start = System.currentTimeMillis();
		List<SearchResult> results = new ArrayList<>();

		// Search active logs
		if (Files.exists(logsDirectory)) {
			try (Stream<Path> stream = Files.list(logsDirectory)) {
				stream.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".log"))
						.forEach(f -> addSearchResult(f, pattern, results));
			}
		}

		// Search archived logs
		Path originalsDir = archiveDirectory.resolve("originals");
		if (Files.exists(originalsDir)) {
			try (Stream<Path> stream = Files.walk(originalsDir)) {
				stream.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".log"))
						.forEach(f -> addSearchResult(f, pattern, results));
			}
		}

		int matches = results.stream().mapToInt(SearchResult::getMatchCount).sum();
		LOGGER.info("Found {} match(es) in {} file(s) in {}ms", matches, results.size(),
				System.currentTimeMillis() - start);

		return results;
	}

	private void addSearchResult(Path file, Pattern pattern, List<SearchResult> results) {
		try {
			SearchResult result = searchInFile(file, pattern);
			if (result.getMatchCount() > 0) {
				results.add(result);
			}
		} catch (IOException e) {
			LOGGER.error("Search error in {}", file, e);
		}
	}

	private SearchResult searchInFile(Path file, Pattern pattern) throws IOException {
		SearchResult result = new SearchResult(file);
		try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			String line;
			int lineNum = 1;
			while ((line = reader.readLine()) != null) {
				if (pattern.matcher(line).find()) {
					result.addMatch(lineNum, line);
				}
				lineNum++;
			}
		}
		return result;
	}

	private void zipFiles(List<Path> files, Path zipPath) throws IOException {
		Files.createDirectories(zipPath.getParent());

		try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(zipPath)))) {
			for (Path file : files) {
				zos.putNextEntry(new ZipEntry(file.getFileName().toString()));
				try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
					byte[] buffer = new byte[8192];
					int len;
					while ((len = is.read(buffer)) != -1) {
						zos.write(buffer, 0, len);
					}
				}
				zos.closeEntry();
			}
		}
	}

	public static void copyAsBytes(Path source, Path target) throws IOException {
		Files.createDirectories(target.getParent());
		try (InputStream is = Files.newInputStream(source); OutputStream os = Files.newOutputStream(target)) {
			byte[] buffer = new byte[8192];
			int len;
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
		}
	}

	public static void copyAsChars(Path source, Path target) throws IOException {
		Files.createDirectories(target.getParent());
		try (BufferedReader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8);
				BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
			char[] buffer = new char[4096];
			int len;
			while ((len = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, len);
			}
		}
	}
}