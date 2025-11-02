package de.fachhochschule.dortmund.bads.hm1.bilyaminu;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ArchiveManager - archives log files into a ZIP archive and demonstrates byte/char streams.
 */
public class ArchiveManager {

    private final Path logsDirectory;
    private final Path archiveDirectory;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ArchiveManager(Path logsDirectory, Path archiveDirectory) {
        this.logsDirectory = logsDirectory;
        this.archiveDirectory = archiveDirectory;
    }

    /**
     * Archive log files matching a date (format yyyy-MM-dd). The files will be zipped into:
     * archive/logs_yyyy-MM-dd.zip and originals moved into archive/originals/yyyy-MM-dd/
     *
     * @param date LocalDate to archive
     * @return path to created zip
     * @throws IOException on IO errors
     */
    public Path archiveLogsByDate(LocalDate date) throws IOException {
        if (!Files.exists(logsDirectory) || !Files.isDirectory(logsDirectory)) {
            throw new IllegalArgumentException("Logs directory doesn't exist: " + logsDirectory);
        }
        Files.createDirectories(archiveDirectory);

        String dateStr = date.format(dateFormatter);
        // pattern: contains dateStr - adapt if your log file naming is different
        try (Stream<Path> stream = Files.list(logsDirectory)) {
            List<Path> toArchive = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().contains(dateStr))
                    .collect(Collectors.toList());

            if (toArchive.isEmpty()) {
                // nothing to archive, return null or throw - we return null
                return null;
            }

            Path zipPath = archiveDirectory.resolve("logs_" + dateStr + ".zip");
            // Create a folder to store originals (after moving)
            Path originalsDir = archiveDirectory.resolve("originals").resolve(dateStr);
            Files.createDirectories(originalsDir);

            // Zip files into zipPath
            zipFiles(toArchive, zipPath);

            // Move originals to originalsDir
            for (Path p : toArchive) {
                Path target = originalsDir.resolve(p.getFileName());
                Files.move(p, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return zipPath;
        }
    }

    /**
     * Create a zip from a list of files.
     */
    private void zipFiles(List<Path> files, Path zipPath) throws IOException {
        // Ensure parent dir exists
        Files.createDirectories(zipPath.getParent());
        try (OutputStream fos = Files.newOutputStream(zipPath);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            for (Path file : files) {
                ZipEntry entry = new ZipEntry(file.getFileName().toString());
                zos.putNextEntry(entry);
                // Use byte stream copy
                try (InputStream is = Files.newInputStream(file);
                     BufferedInputStream bis = new BufferedInputStream(is)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = bis.read(buffer)) != -1) {
                        zos.write(buffer, 0, len);
                    }
                }
                zos.closeEntry();
            }
        }
    }

    /**
     * Simulate byte stream copy (for demonstration): copies a file using InputStream/OutputStream.
     */
    public static void copyAsBytes(Path source, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        try (InputStream is = Files.newInputStream(source);
             OutputStream os = Files.newOutputStream(target)) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        }
    }

    /**
     * Simulate character stream copy (for demonstration): copies a file using Readers/Writers.
     */
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
