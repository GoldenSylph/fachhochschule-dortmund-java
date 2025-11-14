package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.model.StorageCell.Type;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;

class StorageCellTest {
	
	private StorageCell cell;
	private BeveragesBox box1;
	private BeveragesBox box2;
	
	@BeforeEach
	void setUp() {
		cell = new StorageCell(Type.AMBIENT, 100, 100, 100);
		box1 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24);
		box2 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Juice", 12, 12, 12, 12);
	}
	
	@Test
	void testStorageCellCreation() {
		assertNotNull(cell);
		assertEquals(Type.AMBIENT, cell.TYPE);
		assertEquals(100, cell.MAX_LENGTH);
		assertEquals(100, cell.MAX_WIDTH);
		assertEquals(100, cell.MAX_HEIGHT);
	}
	
	@Test
	void testAddBeveragesBox() {
		assertTrue(cell.add(box1));
		// Cannot verify internal state since getBoxes() doesn't exist
		// Verify by attempting to remove
		assertTrue(cell.remove(box1));
	}
	
	@Test
	void testAddMultipleBeveragesBoxes() {
		assertTrue(cell.add(box1));
		assertTrue(cell.add(box2));
		// Verify boxes were added by removing them
		assertTrue(cell.remove(box1));
		assertTrue(cell.remove(box2));
	}
	
	@Test
	void testRemoveBeveragesBox() {
		cell.add(box1);
		cell.add(box2);
		
		assertTrue(cell.remove(box1));
		// Verify box1 was removed - trying to remove again should fail
		assertFalse(cell.remove(box1));
		// box2 should still be there
		assertTrue(cell.remove(box2));
	}
	
	@Test
	void testRemoveNonExistentBox() {
		cell.add(box1);
		
		BeveragesBox box3 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Soda", 11, 11, 11, 24);
		assertFalse(cell.remove(box3));
	}
	
	@Test
	void testCapacityCheck() {
		// Box dimensions: 50x50x50
		// Cell capacity: 100x100x100
		BeveragesBox largeBox = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Large", 50, 50, 50, 1);
		assertTrue(cell.add(largeBox));
	}
	
	@Test
	void testExceedsCapacity() {
		// Box larger than cell capacity
		BeveragesBox oversizedBox = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Huge", 150, 150, 150, 1);
		assertFalse(cell.add(oversizedBox));
	}
	
	@Test
	void testStorageCellTypeMatching() {
		StorageCell refrigeratedCell = new StorageCell(Type.REFRIGERATED, 100, 100, 100);
		BeveragesBox refrigeratedBox = new BeveragesBox(BeveragesBox.Type.REFRIGERATED, "Milk", 15, 15, 15, 12);
		
		assertTrue(refrigeratedCell.add(refrigeratedBox));
	}
	
	@Test
	void testStorageCellTypeMismatch() {
		StorageCell refrigeratedCell = new StorageCell(Type.REFRIGERATED, 100, 100, 100);
		BeveragesBox ambientBox = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24);
		
		// Specific type cells should only accept matching types
		assertFalse(refrigeratedCell.add(ambientBox));
	}
	
	@Test
	void testAnyTypeCellAcceptsAllBoxes() {
		StorageCell anyCell = new StorageCell(Type.ANY, 100, 100, 100);
		
		BeveragesBox ambientBox = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24);
		BeveragesBox refrigeratedBox = new BeveragesBox(BeveragesBox.Type.REFRIGERATED, "Milk", 15, 15, 15, 12);
		BeveragesBox bulkBox = new BeveragesBox(BeveragesBox.Type.BULK, "Beer", 25, 25, 25, 4);
		
		assertTrue(anyCell.add(ambientBox));
		assertTrue(anyCell.add(refrigeratedBox));
		assertTrue(anyCell.add(bulkBox));
		// Verify all three can be removed
		assertTrue(anyCell.remove(ambientBox));
		assertTrue(anyCell.remove(refrigeratedBox));
		assertTrue(anyCell.remove(bulkBox));
	}
	
	@Test
	void testIsNewBoxCouldBeAdded() {
		// Test the validation method directly
		assertTrue(cell.isNewBoxCouldBeAdded(box1));
		assertTrue(cell.isNewBoxCouldBeAdded(box2));
		
		// Test with oversized box
		BeveragesBox oversizedBox = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Huge", 150, 150, 150, 1);
		assertFalse(cell.isNewBoxCouldBeAdded(oversizedBox));
	}
	
	@Test
	void testEmptyCell() {
		// Empty cell should be able to add a box
		assertTrue(cell.isNewBoxCouldBeAdded(box1));
		assertTrue(cell.add(box1));
	}
	
	@Test
	void testBulkTypeCell() {
		StorageCell bulkCell = new StorageCell(Type.BULK, 150, 150, 150);
		BeveragesBox bulkBox = new BeveragesBox(BeveragesBox.Type.BULK, "Water Cooler", 30, 30, 30, 2);
		
		assertTrue(bulkCell.add(bulkBox));
		assertEquals(Type.BULK, bulkCell.TYPE);
	}
	
	@Test
	void testMultipleOperations() {
		assertTrue(cell.add(box1));
		assertTrue(cell.add(box2));
		
		assertTrue(cell.remove(box1));
		// box1 should be gone now
		assertFalse(cell.remove(box1));
		
		BeveragesBox box3 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Soda", 11, 11, 11, 24);
		assertTrue(cell.add(box3));
		
		// Both box2 and box3 should be removable
		assertTrue(cell.remove(box2));
		assertTrue(cell.remove(box3));
	}
	
	@Test
	void testChargingStationCannotStoreBoxes() {
		StorageCell chargingStation = new StorageCell(Type.CHARGING_STATION, 100, 100, 100);
		BeveragesBox box = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24);
		
		assertFalse(chargingStation.add(box));
	}
	
	@Test
	void testPublicFinalFields() {
		// Verify that the public final fields are accessible
		assertEquals(Type.AMBIENT, cell.TYPE);
		assertEquals(100, cell.MAX_LENGTH);
		assertEquals(100, cell.MAX_WIDTH);
		assertEquals(100, cell.MAX_HEIGHT);
		
		// Create a different cell and verify its fields
		StorageCell refrigeratedCell = new StorageCell(Type.REFRIGERATED, 50, 60, 70);
		assertEquals(Type.REFRIGERATED, refrigeratedCell.TYPE);
		assertEquals(50, refrigeratedCell.MAX_LENGTH);
		assertEquals(60, refrigeratedCell.MAX_WIDTH);
		assertEquals(70, refrigeratedCell.MAX_HEIGHT);
	}
}