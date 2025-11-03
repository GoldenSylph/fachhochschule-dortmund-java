package de.fachhochschule.dortmund.bads.hm2.exceptions;

/**
 * Base exception for storage-related errors in the storage management system.
 */
public class StorageException extends RuntimeException {
    
    private static final long serialVersionUID = -1785255367786790656L;

	public StorageException(String message) {
        super(message);
    }
    
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}