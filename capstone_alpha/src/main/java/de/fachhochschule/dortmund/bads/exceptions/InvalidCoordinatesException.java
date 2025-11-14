package de.fachhochschule.dortmund.bads.exceptions;

/**
 * Exception thrown when invalid coordinates are provided for storage operations.
 */
public class InvalidCoordinatesException extends StorageException {
    
    private static final long serialVersionUID = 7775807780327075700L;
	private final int x;
    private final int y;
    
    public InvalidCoordinatesException(int x, int y, String reason) {
        super(String.format("Invalid coordinates (%d, %d): %s", x, y, reason));
        this.x = x;
        this.y = y;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
}