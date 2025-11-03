package de.fachhochschule.dortmund.bads.hm1.rowena;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.rowena.LogFileManager.LogFileMetadata;

/**
 * Query utility for searching log files using regular expressions.
 * Demonstrates Pattern and Matcher usage from java.util.regex.
 */
public class LogFileQuery {
	private static final Logger LOGGER = LogManager.getLogger();
	private final LogFileManager logFileManager;

	public LogFileQuery(LogFileManager logFileManager) {
		this.logFileManager = logFileManager;
	}

	/**
	 * Finds log files by equipment name using regex pattern matching.
	 *
	 * @param namePattern Regex pattern to match equipment names (e.g., "agv.*", ".*001", "charging-station-.*")
	 * @return List of matching LogFileMetadata
	 */
	public List<LogFileMetadata> findByEquipmentName(String namePattern) {
		List<LogFileMetadata> results = new ArrayList<>();

		try {
			// Compile regex pattern
			Pattern pattern = Pattern.compile(namePattern);

			// Search through all metadata
			for (LogFileMetadata metadata : logFileManager.getAllMetadata()) {
				String fullName = metadata.equipmentType() + "-" + metadata.equipmentId();

				// Use Matcher to test if the name matches the pattern
				Matcher matcher = pattern.matcher(fullName);
				if (matcher.matches() || matcher.find()) {
					results.add(metadata);
					LOGGER.debug("Match found: {} matches pattern '{}'", fullName, namePattern);
				}
			}

			LOGGER.info("Found {} log files matching pattern: '{}'", results.size(), namePattern);
		} catch (Exception e) {
			LOGGER.error("Invalid regex pattern: {}", namePattern, e);
		}

		return results;
	}

	/**
	 * Finds log files by date pattern using regex.
	 *
	 * @param datePattern Regex pattern for dates (e.g., "2025-10-.*", ".*-10-27", "2025-.*")
	 * @return List of matching LogFileMetadata
	 */
	public List<LogFileMetadata> findByDatePattern(String datePattern) {
		List<LogFileMetadata> results = new ArrayList<>();

		try {
			// Compile regex pattern
			Pattern pattern = Pattern.compile(datePattern);

			for (LogFileMetadata metadata : logFileManager.getAllMetadata()) {
				String dateStr = metadata.date().toString();

				// Use Matcher to test date string
				Matcher matcher = pattern.matcher(dateStr);
				if (matcher.matches() || matcher.find()) {
					results.add(metadata);
					LOGGER.debug("Match found: date {} matches pattern '{}'", dateStr, datePattern);
				}
			}

			LOGGER.info("Found {} log files matching date pattern: '{}'", results.size(), datePattern);
		} catch (Exception e) {
			LOGGER.error("Invalid regex pattern: {}", datePattern, e);
		}

		return results;
	}

	/**
	 * Finds log files by exact date.
	 *
	 * @param date LocalDate to search for
	 * @return List of matching LogFileMetadata
	 */
	public List<LogFileMetadata> findByDate(LocalDate date) {
		List<LogFileMetadata> results = new ArrayList<>();

		for (LogFileMetadata metadata : logFileManager.getAllMetadata()) {
			if (metadata.date().equals(date)) {
				results.add(metadata);
			}
		}

		LOGGER.info("Found {} log files for date: {}", results.size(), date);
		return results;
	}

	/**
	 * Finds log files by exact date string.
	 *
	 * @param dateStr Date string in ISO format (e.g., "2025-10-27")
	 * @return List of matching LogFileMetadata
	 */
	public List<LogFileMetadata> findByDate(String dateStr) {
		try {
			LocalDate date = LocalDate.parse(dateStr);
			return findByDate(date);
		} catch (DateTimeParseException e) {
			LOGGER.error("Invalid date format: {}. Expected ISO format (yyyy-MM-dd)", dateStr, e);
			return new ArrayList<>();
		}
	}

	/**
	 * Finds log files by equipment type.
	 *
	 * @param equipmentType Type to filter by (e.g., "AGV", "charging-station", "system")
	 * @return List of matching LogFileMetadata
	 */
	public List<LogFileMetadata> findByEquipmentType(String equipmentType) {
		List<LogFileMetadata> results = new ArrayList<>();

		for (LogFileMetadata metadata : logFileManager.getAllMetadata()) {
			if (metadata.equipmentType().equalsIgnoreCase(equipmentType)) {
				results.add(metadata);
			}
		}

		LOGGER.info("Found {} log files of type: {}", results.size(), equipmentType);
		return results;
	}

	/**
	 * Advanced search using multiple regex patterns.
	 * All patterns must match for a result to be included.
	 *
	 * @param typePattern Regex for equipment type
	 * @param idPattern Regex for equipment ID
	 * @param datePattern Regex for date
	 * @return List of matching LogFileMetadata
	 */
	public List<LogFileMetadata> advancedSearch(String typePattern, String idPattern, String datePattern) {
		List<LogFileMetadata> results = new ArrayList<>();

		try {
			Pattern typePat = Pattern.compile(typePattern);
			Pattern idPat = Pattern.compile(idPattern);
			Pattern datePat = Pattern.compile(datePattern);

			for (LogFileMetadata metadata : logFileManager.getAllMetadata()) {
				Matcher typeMatcher = typePat.matcher(metadata.equipmentType());
				Matcher idMatcher = idPat.matcher(metadata.equipmentId());
				Matcher dateMatcher = datePat.matcher(metadata.date().toString());

				// All patterns must match
				if ((typeMatcher.matches() || typeMatcher.find()) &&
				    (idMatcher.matches() || idMatcher.find()) &&
				    (dateMatcher.matches() || dateMatcher.find())) {
					results.add(metadata);
				}
			}

			LOGGER.info("Advanced search found {} results", results.size());
		} catch (Exception e) {
			LOGGER.error("Invalid regex pattern in advanced search", e);
		}

		return results;
	}
}
