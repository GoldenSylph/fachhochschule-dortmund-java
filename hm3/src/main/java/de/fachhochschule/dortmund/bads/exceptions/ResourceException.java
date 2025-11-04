package de.fachhochschule.dortmund.bads.exceptions;

/**
 * Exception thrown when there are resource-related errors in operations.
 */
public class ResourceException extends SystemException {
    
    private static final long serialVersionUID = -9004041355933835663L;

	public ResourceException(String message) {
        super(message);
    }
    
    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}