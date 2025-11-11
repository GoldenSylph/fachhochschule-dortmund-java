package de.fachhochschule.dortmund.bads.systems.logic.utils;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Represents a command for the ArchiveManager thread to process.
 */
public class ArchiveCommand {
    
    public enum CommandType {
        ARCHIVE,
        DEARCHIVE,
        SEARCH,
        SHUTDOWN
    }
    
    private final CommandType type;
    private final LocalDate date;
    private final Pattern searchPattern;
    private final String zipFileName;
    
    private ArchiveCommand(CommandType type, LocalDate date, Pattern searchPattern, String zipFileName) {
        this.type = type;
        this.date = date;
        this.searchPattern = searchPattern;
        this.zipFileName = zipFileName;
    }
    
    public static ArchiveCommand archive(LocalDate date) {
        return new ArchiveCommand(CommandType.ARCHIVE, date, null, null);
    }
    
    public static ArchiveCommand dearchive(String zipFileName) {
        return new ArchiveCommand(CommandType.DEARCHIVE, null, null, zipFileName);
    }
    
    public static ArchiveCommand search(Pattern pattern) {
        return new ArchiveCommand(CommandType.SEARCH, null, pattern, null);
    }
    
    public static ArchiveCommand shutdown() {
        return new ArchiveCommand(CommandType.SHUTDOWN, null, null, null);
    }
    
    public CommandType getType() {
        return type;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public Pattern getSearchPattern() {
        return searchPattern;
    }
    
    public String getZipFileName() {
        return zipFileName;
    }
    
    @Override
    public String toString() {
        return "ArchiveCommand{type=" + type + 
               ", date=" + date + 
               ", pattern=" + (searchPattern != null ? searchPattern.pattern() : null) +
               ", zipFile=" + zipFileName + "}";
    }
}
