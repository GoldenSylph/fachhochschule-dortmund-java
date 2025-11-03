package de.fachhochschule.dortmund.bads.hm2.exceptions;

/**
 * Exception thrown when there's a mismatch between storage cells and area places.
 */
public class StorageCellMismatchException extends StorageException {
    
    private static final long serialVersionUID = -7772462093361113662L;
	private final int expectedCells;
    private final int actualCells;
    
    public StorageCellMismatchException(int expectedCells, int actualCells) {
        super(String.format("Mismatch between storage cells (%d) and area places (%d)", actualCells, expectedCells));
        this.expectedCells = expectedCells;
        this.actualCells = actualCells;
    }
    
    public int getExpectedCells() {
        return expectedCells;
    }
    
    public int getActualCells() {
        return actualCells;
    }
}