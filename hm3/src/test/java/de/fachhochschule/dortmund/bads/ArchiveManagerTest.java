package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.fachhochschule.dortmund.bads.systems.logic.ArchiveManager;

class ArchiveManagerTest {

	@TempDir
	Path tempDir;

	@Test
	void testArchiveLogsByDate_createsZipAndMovesOriginals() throws Exception {
		Path logsDir = tempDir.resolve("logs");
		Path archiveDir = tempDir.resolve("archive");
		Files.createDirectories(logsDir);

		// prepare three log files for the date
		LocalDate date = LocalDate.of(2025, 10, 27);
		String dateStr = date.toString(); // "2025-10-27"

		Path log1 = logsDir.resolve("storage_vehicle_1_" + dateStr + ".log");
		Path log2 = logsDir.resolve("charging_station_1_" + dateStr + ".log");
		Path log3 = logsDir.resolve("system_" + dateStr + ".log");

		Files.writeString(log1, "line1\nline2\n");
		Files.writeString(log2, "charging station log\n");
		Files.writeString(log3, "system log\n");

		ArchiveManager manager = new ArchiveManager(logsDir, archiveDir);
		Path zipPath = manager.archiveLogsByDate(date);

		assertNotNull(zipPath, "zip path should not be null when files exist");
		assertTrue(Files.exists(zipPath), "zip must exist");

		// zip should contain 3 entries
		try (ZipFile zf = new ZipFile(zipPath.toFile())) {
			assertEquals(3, zf.size()); // expects exactly 3 entries
			assertNotNull(zf.getEntry(log1.getFileName().toString()));
			assertNotNull(zf.getEntry(log2.getFileName().toString()));
			assertNotNull(zf.getEntry(log3.getFileName().toString()));
		}

		// originals moved to archive/originals/date/
		Path originalsDir = archiveDir.resolve("originals").resolve(dateStr);
		assertTrue(Files.exists(originalsDir.resolve(log1.getFileName())));
		assertTrue(Files.exists(originalsDir.resolve(log2.getFileName())));
		assertTrue(Files.exists(originalsDir.resolve(log3.getFileName())));

		// logs dir should no longer have those files
		List<Path> remaining = Files.list(logsDir).toList();
		assertEquals(0, remaining.size());
	}

	@Test
	void testCopyAsBytesAndChars() throws Exception {
		Path src = tempDir.resolve("srcfile.txt");
		Path dstBytes = tempDir.resolve("dst_bytes.txt");
		Path dstChars = tempDir.resolve("dst_chars.txt");

		Files.writeString(src, "some text with ünicode\nline2\n");

		ArchiveManager.copyAsBytes(src, dstBytes);
		ArchiveManager.copyAsChars(src, dstChars);

		String s1 = Files.readString(dstBytes);
		String s2 = Files.readString(dstChars);

		assertEquals(Files.readString(src), s1);
		assertEquals(Files.readString(src), s2);
	}
}
