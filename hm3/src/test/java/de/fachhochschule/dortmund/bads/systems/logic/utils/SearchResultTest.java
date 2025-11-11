package de.fachhochschule.dortmund.bads.systems.logic.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class SearchResultTest {

	@Test
	void testConstructor() {
		Path testPath = Paths.get("test.log");
		SearchResult result = new SearchResult(testPath);
		
		assertNotNull(result);
		assertEquals(testPath, result.getLogFile());
		assertEquals(0, result.getMatchCount());
		assertNotNull(result.getMatches());
		assertTrue(result.getMatches().isEmpty());
	}

	@Test
	void testAddMatch() {
		Path testPath = Paths.get("test.log");
		SearchResult result = new SearchResult(testPath);
		
		result.addMatch(1, "Error occurred");
		assertEquals(1, result.getMatchCount());
		assertEquals(1, result.getMatches().size());
	}

	@Test
	void testMultipleMatches() {
		Path testPath = Paths.get("test.log");
		SearchResult result = new SearchResult(testPath);
		
		result.addMatch(1, "First match");
		result.addMatch(5, "Second match");
		result.addMatch(10, "Third match");
		
		assertEquals(3, result.getMatchCount());
		assertEquals(3, result.getMatches().size());
	}

	@Test
	void testGetMatches() {
		Path testPath = Paths.get("test.log");
		SearchResult result = new SearchResult(testPath);
		
		result.addMatch(1, "Line one");
		result.addMatch(2, "Line two");
		
		assertEquals(2, result.getMatches().size());
		assertEquals(1, result.getMatches().get(0).getLineNumber());
		assertEquals("Line one", result.getMatches().get(0).getContent());
		assertEquals(2, result.getMatches().get(1).getLineNumber());
		assertEquals("Line two", result.getMatches().get(1).getContent());
	}

	@Test
	void testToString() {
		Path testPath = Paths.get("test.log");
		SearchResult result = new SearchResult(testPath);
		result.addMatch(1, "Test line");
		
		String str = result.toString();
		assertNotNull(str);
		assertTrue(str.contains("test.log"));
		assertTrue(str.contains("matches=1") || str.contains("matches="));
	}

	@Test
	void testMatchedLineConstructor() {
		SearchResult.MatchedLine line = new SearchResult.MatchedLine(42, "Error message");
		
		assertEquals(42, line.getLineNumber());
		assertEquals("Error message", line.getContent());
	}

	@Test
	void testMatchedLineToString() {
		SearchResult.MatchedLine line = new SearchResult.MatchedLine(10, "Test content");
		
		String str = line.toString();
		assertNotNull(str);
		assertTrue(str.contains("10"));
		assertTrue(str.contains("Test content"));
	}

	@Test
	void testEmptySearchResult() {
		Path testPath = Paths.get("empty.log");
		SearchResult result = new SearchResult(testPath);
		
		assertEquals(0, result.getMatchCount());
		assertTrue(result.getMatches().isEmpty());
	}

	@Test
	void testGetLogFile() {
		Path testPath = Paths.get("logs", "app.log");
		SearchResult result = new SearchResult(testPath);
		
		assertEquals(testPath, result.getLogFile());
	}
}
