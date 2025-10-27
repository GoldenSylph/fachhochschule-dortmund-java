package de.fachhochschule.dortmund.bads.hm1.bedrin;

import static org.junit.Assert.*;

import org.junit.Test;

public class StorageCellTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullParent() {
        new StorageCell(null, 1.0, 1.0, 1.0, StorageCell.Type.AMBIENT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNonPositiveDimensions() {
        Storage storage = new Storage();
        new StorageCell(storage, 0.0, 1.0, 1.0, StorageCell.Type.AMBIENT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullType() {
        Storage storage = new Storage();
        new StorageCell(storage, 1.0, 1.0, 1.0, null);
    }

    @Test
    public void initialStateAndGetters() {
        Storage storage = new Storage();
        StorageCell cell = new StorageCell(storage, 2.0, 3.0, 4.0, StorageCell.Type.REFRIGERATED);

        assertSame(storage, cell.getParentStorage());
        assertEquals(2.0, cell.getLength(), 1e-9);
        assertEquals(3.0, cell.getWidth(), 1e-9);
        assertEquals(4.0, cell.getHeight(), 1e-9);
        assertEquals(24.0, cell.getVolume(), 1e-9);
        assertEquals(StorageCell.Type.REFRIGERATED, cell.getType());
        assertFalse(cell.isOccupied());
        assertTrue(cell.isEmpty());

        // occupied metrics start at zero
        assertEquals(0.0, cell.getOccupiedLength(), 1e-9);
        assertEquals(0.0, cell.getOccupiedWidth(), 1e-9);
        assertEquals(0.0, cell.getOccupiedHeight(), 1e-9);
        assertEquals(0.0, cell.getOccupiedVolume(), 1e-9);
        assertEquals(24.0, cell.getAvailableVolume(), 1e-9);
    }

    @Test
    public void occupancyTransitions() {
        Storage storage = new Storage();
        StorageCell cell = new StorageCell(storage, 1.0, 1.0, 1.0, StorageCell.Type.AMBIENT);

        assertTrue(cell.fill());
        assertTrue(cell.isOccupied());
        assertFalse(cell.isEmpty());
        assertEquals(1.0, cell.getOccupiedLength(), 1e-9);
        assertEquals(1.0, cell.getOccupiedWidth(), 1e-9);
        assertEquals(1.0, cell.getOccupiedHeight(), 1e-9);
        assertEquals(0.0, cell.getAvailableVolume(), 1e-9);

        // idempotent fill
        assertFalse(cell.fill());

        assertTrue(cell.clear());
        assertFalse(cell.isOccupied());
        assertTrue(cell.isEmpty());
        assertEquals(0.0, cell.getOccupiedLength(), 1e-9);
        assertEquals(0.0, cell.getOccupiedWidth(), 1e-9);
        assertEquals(0.0, cell.getOccupiedHeight(), 1e-9);
        assertEquals(1.0, cell.getAvailableVolume(), 1e-9);

        // idempotent clear
        assertFalse(cell.clear());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOccupiedDimensionsRejectsNegative() {
        Storage storage = new Storage();
        StorageCell cell = new StorageCell(storage, 2.0, 2.0, 2.0, StorageCell.Type.AMBIENT);
        cell.setOccupiedDimensions(-1.0, 0.5, 0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOccupiedDimensionsRejectsExceeding() {
        Storage storage = new Storage();
        StorageCell cell = new StorageCell(storage, 2.0, 2.0, 2.0, StorageCell.Type.AMBIENT);
        cell.setOccupiedDimensions(2.1, 1.0, 1.0);
    }

    @Test
    public void setOccupiedDimensionsUpdatesStateAndVolumes() {
        Storage storage = new Storage();
        StorageCell cell = new StorageCell(storage, 2.0, 3.0, 4.0, StorageCell.Type.AMBIENT);

        // partial occupancy
        cell.setOccupiedDimensions(1.0, 0.0, 0.5);
        assertFalse(cell.isOccupied()); // requires all three > 0 to be considered occupied
        assertEquals(1.0, cell.getOccupiedLength(), 1e-9);
        assertEquals(0.0, cell.getOccupiedWidth(), 1e-9);
        assertEquals(0.5, cell.getOccupiedHeight(), 1e-9);
        assertEquals(0.0, cell.getOccupiedVolume(), 1e-9); // zero because width is zero
        assertEquals(24.0, cell.getAvailableVolume(), 1e-9);

        // more occupancy
        cell.setOccupiedDimensions(2.0, 3.0, 4.0);
        assertTrue(cell.isOccupied());
        assertEquals(24.0, cell.getOccupiedVolume(), 1e-9);
        assertEquals(0.0, cell.getAvailableVolume(), 1e-9);

        // clear via clear()
        assertTrue(cell.clear());
        assertFalse(cell.isOccupied());
        assertEquals(0.0, cell.getOccupiedVolume(), 1e-9);
        assertEquals(24.0, cell.getAvailableVolume(), 1e-9);
    }
}