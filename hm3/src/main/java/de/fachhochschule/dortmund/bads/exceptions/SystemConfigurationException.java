package de.fachhochschule.dortmund.bads.exceptions;

/**
 * Exception thrown when a system configuration error occurs.
 */
public class SystemConfigurationException extends SystemException {
    
    private static final long serialVersionUID = 7074016889090399807L;

	public SystemConfigurationException(String message) {
        super(message);
    }
    
    public SystemConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}