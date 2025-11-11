package de.fachhochschule.dortmund.bads.systems.logic.utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents search results from log file pattern matching.
 */
public class SearchResult {
    
    private final Path logFile;
    private final List<MatchedLine> matches;
    
    public SearchResult(Path logFile) {
        this.logFile = logFile;
        this.matches = new ArrayList<>();
    }
    
    public void addMatch(int lineNumber, String line) {
        matches.add(new MatchedLine(lineNumber, line));
    }
    
    public Path getLogFile() {
        return logFile;
    }
    
    public List<MatchedLine> getMatches() {
        return matches;
    }
    
    public int getMatchCount() {
        return matches.size();
    }
    
    public static class MatchedLine {
        private final int lineNumber;
        private final String content;
        
        public MatchedLine(int lineNumber, String content) {
            this.lineNumber = lineNumber;
            this.content = content;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public String getContent() {
            return content;
        }
        
        @Override
        public String toString() {
            return lineNumber + ": " + content;
        }
    }
    
    @Override
    public String toString() {
        return "SearchResult{file=" + logFile.getFileName() + 
               ", matches=" + matches.size() + "}";
    }
}
