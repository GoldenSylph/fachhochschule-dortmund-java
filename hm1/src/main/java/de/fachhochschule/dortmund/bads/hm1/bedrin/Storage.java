package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.bedrin.Area.Point;

public class Storage {
	private static final Logger LOGGER = LogManager.getLogger();

	public final Area AREA;
	private final Map<Point, StorageCell> CELLS;

	public Storage(Area area, StorageCell[] cells) {
		this.AREA = area;
		this.CELLS = new HashMap<>();
		Set<Point> places = this.AREA.getAdjacencyMap().keySet();
		if (places.size() != cells.length) {
			throw new IllegalArgumentException("Mismatch between storage cells and area places.");
		}
		int i = 0;
		for (Point place : places) {
			this.CELLS.put(place, cells[i++]);
		}
	}

	public StorageCell getCellByNotation(String notation) {
		Point point = notationToPoint(notation);
		return CELLS.get(point);
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
		LOGGER.debug("Converting point to notation: {}", point);
		
		if (point == null) {
			LOGGER.error("Point cannot be null");
			throw new IllegalArgumentException("Point cannot be null");
		}
		if (point.x() < 0 || point.y() < 0) {
			LOGGER.error("Coordinates must be non-negative: x={}, y={}", point.x(), point.y());
			throw new IllegalArgumentException("Coordinates must be non-negative");
		}
		
		int number = point.x() + 1; // Convert to 1-based
		String letters = convertToLetters(point.y());
		String notation = number + letters;
		
		LOGGER.debug("Point {} converted to notation: {}", point, notation);
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
		LOGGER.debug("Converting notation to point: {}", notation);
		
		if (notation == null || notation.isEmpty()) {
			LOGGER.error("Notation cannot be null or empty");
			throw new IllegalArgumentException("Notation cannot be null or empty");
		}
		
		// Find where letters start
		int letterStart = 0;
		while (letterStart < notation.length() && Character.isDigit(notation.charAt(letterStart))) {
			letterStart++;
		}
		
		if (letterStart == 0 || letterStart == notation.length()) {
			LOGGER.error("Invalid notation format: must contain both numbers and letters: {}", notation);
			throw new IllegalArgumentException("Invalid notation format: must contain both numbers and letters");
		}
		
		String numberPart = notation.substring(0, letterStart);
		String letterPart = notation.substring(letterStart);
		
		LOGGER.debug("Parsed notation '{}' into number part '{}' and letter part '{}'", notation, numberPart, letterPart);
		
		try {
			int number = Integer.parseInt(numberPart);
			if (number <= 0) {
				LOGGER.error("Number part must be positive: {}", number);
				throw new IllegalArgumentException("Number part must be positive");
			}
			
			int x = number - 1; // Convert to 0-based
			int y = convertFromLetters(letterPart);
			Point point = new Point(x, y);
			
			LOGGER.debug("Notation '{}' converted to point: {}", notation, point);
			return point;
		} catch (NumberFormatException e) {
			LOGGER.error("Invalid number format in notation '{}': {}", notation, e.getMessage());
			throw new IllegalArgumentException("Invalid number format in notation", e);
		}
	}

	private static String convertToLetters(int number) {
		LOGGER.trace("Converting number to letters: {}", number);
		
		if (number < 0) {
			LOGGER.error("Number must be non-negative: {}", number);
			throw new IllegalArgumentException("Number must be non-negative");
		}
		
		StringBuilder result = new StringBuilder();
		int originalNumber = number;
		do {
			result.insert(0, (char) ('A' + (number % 26)));
			number = number / 26;
		} while (number > 0);
		
		String letters = result.toString();
		LOGGER.trace("Number {} converted to letters: {}", originalNumber, letters);
		return letters;
	}

	private static int convertFromLetters(String letters) {
		LOGGER.trace("Converting letters to number: {}", letters);
		
		if (letters == null || letters.isEmpty()) {
			LOGGER.error("Letters cannot be null or empty");
			throw new IllegalArgumentException("Letters cannot be null or empty");
		}
		
		int result = 0;
		for (char c : letters.toCharArray()) {
			if (c < 'A' || c > 'Z') {
				LOGGER.error("Invalid character in letter part: {}", c);
				throw new IllegalArgumentException("Invalid character in letter part: " + c);
			}
			result = result * 26 + (c - 'A');
		}
		
		LOGGER.trace("Letters '{}' converted to number: {}", letters, result);
		return result;
	}
}