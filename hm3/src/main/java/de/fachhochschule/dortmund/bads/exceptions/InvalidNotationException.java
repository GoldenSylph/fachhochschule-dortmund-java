package de.fachhochschule.dortmund.bads.exceptions;

/**
 * Exception thrown when an invalid notation format is provided to storage operations.
 */
public class InvalidNotationException extends StorageException {
    
    private static final long serialVersionUID = -8514129485407752228L;
	private final String notation;
    
    public InvalidNotationException(String notation, String reason) {
        super(String.format("Invalid notation '%s': %s", notation, reason));
        this.notation = notation;
    }
    
    public String getNotation() {
        return notation;
    }
}