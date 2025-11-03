package de.fachhochschule.dortmund.bads.hm2.exceptions;

/**
 * Exception thrown when there are process execution errors.
 */
public class ProcessExecutionException extends SystemException {
    
    private static final long serialVersionUID = 811850330513307621L;

	public ProcessExecutionException(String message) {
        super(message);
    }
    
    public ProcessExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}