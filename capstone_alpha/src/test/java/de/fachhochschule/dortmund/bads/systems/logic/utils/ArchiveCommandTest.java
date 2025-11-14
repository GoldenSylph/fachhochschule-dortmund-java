package de.fachhochschule.dortmund.bads.systems.logic.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.systems.logic.utils.ArchiveCommand.CommandType;

class ArchiveCommandTest {

	@Test
	void testArchiveCommand() {
		LocalDate date = LocalDate.of(2025, 11, 11);
		ArchiveCommand command = ArchiveCommand.archive(date);
		
		assertNotNull(command);
		assertEquals(CommandType.ARCHIVE, command.getType());
		assertEquals(date, command.getDate());
		assertNull(command.getSearchPattern());
		assertNull(command.getZipFileName());
	}

	@Test
	void testDearchiveCommand() {
		String zipFileName = "logs_2025-11-11.zip";
		ArchiveCommand command = ArchiveCommand.dearchive(zipFileName);
		
		assertNotNull(command);
		assertEquals(CommandType.DEARCHIVE, command.getType());
		assertEquals(zipFileName, command.getZipFileName());
		assertNull(command.getDate());
		assertNull(command.getSearchPattern());
	}

	@Test
	void testSearchCommand() {
		Pattern pattern = Pattern.compile("ERROR");
		ArchiveCommand command = ArchiveCommand.search(pattern);
		
		assertNotNull(command);
		assertEquals(CommandType.SEARCH, command.getType());
		assertEquals(pattern, command.getSearchPattern());
		assertNull(command.getDate());
		assertNull(command.getZipFileName());
	}

	@Test
	void testShutdownCommand() {
		ArchiveCommand command = ArchiveCommand.shutdown();
		
		assertNotNull(command);
		assertEquals(CommandType.SHUTDOWN, command.getType());
		assertNull(command.getDate());
		assertNull(command.getSearchPattern());
		assertNull(command.getZipFileName());
	}

	@Test
	void testCommandTypeEnum() {
		assertEquals(4, CommandType.values().length);
		assertNotNull(CommandType.valueOf("ARCHIVE"));
		assertNotNull(CommandType.valueOf("DEARCHIVE"));
		assertNotNull(CommandType.valueOf("SEARCH"));
		assertNotNull(CommandType.valueOf("SHUTDOWN"));
	}

	@Test
	void testToStringArchive() {
		LocalDate date = LocalDate.of(2025, 11, 11);
		ArchiveCommand command = ArchiveCommand.archive(date);
		
		String str = command.toString();
		assertNotNull(str);
		assertTrue(str.contains("ARCHIVE"));
		assertTrue(str.contains("2025-11-11"));
	}

	@Test
	void testToStringDearchive() {
		ArchiveCommand command = ArchiveCommand.dearchive("test.zip");
		
		String str = command.toString();
		assertNotNull(str);
		assertTrue(str.contains("DEARCHIVE"));
		assertTrue(str.contains("test.zip"));
	}

	@Test
	void testToStringSearch() {
		Pattern pattern = Pattern.compile("ERROR.*");
		ArchiveCommand command = ArchiveCommand.search(pattern);
		
		String str = command.toString();
		assertNotNull(str);
		assertTrue(str.contains("SEARCH"));
		assertTrue(str.contains("ERROR.*"));
	}

	@Test
	void testToStringShutdown() {
		ArchiveCommand command = ArchiveCommand.shutdown();
		
		String str = command.toString();
		assertNotNull(str);
		assertTrue(str.contains("SHUTDOWN"));
	}

	@Test
	void testMultipleCommands() {
		ArchiveCommand cmd1 = ArchiveCommand.archive(LocalDate.now());
		ArchiveCommand cmd2 = ArchiveCommand.dearchive("file.zip");
		ArchiveCommand cmd3 = ArchiveCommand.search(Pattern.compile(".*"));
		ArchiveCommand cmd4 = ArchiveCommand.shutdown();
		
		assertNotEquals(cmd1.getType(), cmd2.getType());
		assertNotEquals(cmd2.getType(), cmd3.getType());
		assertNotEquals(cmd3.getType(), cmd4.getType());
	}
}
