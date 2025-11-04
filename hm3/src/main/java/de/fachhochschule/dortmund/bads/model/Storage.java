package de.fachhochschule.dortmund.bads.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.exceptions.InvalidCoordinatesException;
import de.fachhochschule.dortmund.bads.exceptions.InvalidNotationException;
import de.fachhochschule.dortmund.bads.exceptions.StorageCellMismatchException;
import de.fachhochschule.dortmund.bads.model.Area.Point;

public class Storage {
	private static final Logger LOGGER = LogManager.getLogger();

	public final Area AREA;
	private final Map<Point, StorageCell> CELLS;

	public Storage(Area area, StorageCell[] cells) {
		this.AREA = area;
		this.CELLS = new HashMap<>();
		Set<Point> places = this.AREA.getAdjacencyMap().keySet();
		if (places.size() != cells.length) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Mismatch between storage cells ({}) and area places ({})", cells.length, places.size());
			}
			throw new StorageCellMismatchException(places.size(), cells.length);
		}
		int i = 0;
		for (Point place : places) {
			this.CELLS.put(place, cells[i++]);
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Storage initialized with {} cells", this.CELLS.size());
		}
	}

	public StorageCell getCellByNotation(String notation) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Getting cell by notation: {}", notation);
		}
		Point point = notationToPoint(notation);
		StorageCell cell = CELLS.get(point);
		if (cell == null && LOGGER.isWarnEnabled()) {
			LOGGER.warn("No cell found for notation: {} (point: {})", notation, point);
		}
		return cell;
	}

	/**
	 * Converts a Point with (x, y) coordinates to chess-like notation.
	 * x-coordinate becomes the number (1-based), y-coordinate becomes the letter(s).
	 * Examples: (1, 0) -> "1A", (10, 25) -> "10Z", (5, 26) -> "5AA"
	 * 
	 * @param point the Point to convert
	 * @return the chess-like notation string
	 * @throws IllegalArgumentException if point is null or coordinates are negative
	 */
	public static String pointToNotation(Point point) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Converting point to notation: {}", point);
		}
		
		if (point == null) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Point cannot be null");
			}
			throw new InvalidNotationException("null", "Point cannot be null");
		}
		if (point.x() < 0 || point.y() < 0) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Coordinates must be non-negative: x={}, y={}", point.x(), point.y());
			}
			throw new InvalidCoordinatesException(point.x(), point.y(), "Coordinates must be non-negative");
		}
		
		int number = point.x() + 1; // Convert to 1-based
		String letters = convertToLetters(point.y());
		String notation = number + letters;
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Point {} converted to notation: {}", point, notation);
		}
		return notation;
	}

	/**
	 * Converts chess-like notation to a Point with (x, y) coordinates.
	 * Number part becomes x-coordinate (0-based), letter part becomes y-coordinate.
	 * Examples: "1A" -> (0, 0), "10Z" -> (9, 25), "5AA" -> (4, 26)
	 * 
	 * @param notation the chess-like notation string
	 * @return the Point with corresponding coordinates
	 * @throws IllegalArgumentException if notation is null, empty, or invalid format
	 */
	public static Point notationToPoint(String notation) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Converting notation to point: {}", notation);
		}
		
		if (notation == null || notation.isEmpty()) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Notation cannot be null or empty");
			}
			throw new InvalidNotationException(notation, "Notation cannot be null or empty");
		}
		
		// Find where letters start
		int letterStart = 0;
		while (letterStart < notation.length() && Character.isDigit(notation.charAt(letterStart))) {
			letterStart++;
		}
		
		if (letterStart == 0 || letterStart == notation.length()) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Invalid notation format: must contain both numbers and letters: {}", notation);
			}
			throw new InvalidNotationException(notation, "Must contain both numbers and letters");
		}
		
		String numberPart = notation.substring(0, letterStart);
		String letterPart = notation.substring(letterStart);
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Parsed notation '{}' into number part '{}' and letter part '{}'", notation, numberPart, letterPart);
		}
		
		try {
			int number = Integer.parseInt(numberPart);
			if (number <= 0) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Number part must be positive: {}", number);
				}
				throw new InvalidNotationException(notation, "Number part must be positive");
			}
			
			int x = number - 1; // Convert to 0-based
			int y = convertFromLetters(letterPart);
			Point point = new Point(x, y);
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Notation '{}' converted to point: {}", notation, point);
			}
			return point;
		} catch (NumberFormatException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Invalid number format in notation '{}': {}", notation, e.getMessage());
			}
			throw new InvalidNotationException(notation, "Invalid number format: " + e.getMessage());
		}
	}

	private static String convertToLetters(int number) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Converting number to letters: {}", number);
		}
		
		if (number < 0) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Number must be non-negative: {}", number);
			}
			throw new IllegalArgumentException("Number must be non-negative");
		}
		
		StringBuilder result = new StringBuilder();
		int originalNumber = number;
		do {
			result.insert(0, (char) ('A' + (number % 26)));
			number = number / 26;
		} while (number > 0);
		
		String letters = result.toString();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Number {} converted to letters: {}", originalNumber, letters);
		}
		return letters;
	}

	private static int convertFromLetters(String letters) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Converting letters to number: {}", letters);
		}
		
		if (letters == null || letters.isEmpty()) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Letters cannot be null or empty");
			}
			throw new IllegalArgumentException("Letters cannot be null or empty");
		}
		
		int result = 0;
		for (char c : letters.toCharArray()) {
			if (c < 'A' || c > 'Z') {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Invalid character in letter part: {}", c);
				}
				throw new IllegalArgumentException("Invalid character in letter part: " + c);
			}
			result = result * 26 + (c - 'A');
		}
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Letters '{}' converted to number: {}", letters, result);
		}
		return result;
	}
}