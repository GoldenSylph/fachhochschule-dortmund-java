package de.fachhochschule.dortmund.bads.exceptions;

/**
 * Base exception for system configuration and operation errors.
 */
public class SystemException extends RuntimeException {
    
    private static final long serialVersionUID = 301110757530711445L;

	public SystemException(String message) {
        super(message);
    }
    
    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}