package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.model.StorageCell.Type;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;
import de.fachhochschule.dortmund.bads.systems.logic.StorageManagement;

class StorageManagementTest {
	
	private StorageManagement storageManagement;
	private StorageCell cell1;
	private StorageCell cell2;
	private BeveragesBox box1;
	private BeveragesBox box2;
	
	@BeforeEach
	void setUp() {
		storageManagement = new StorageManagement();
		
		cell1 = new StorageCell(Type.AMBIENT, 100, 100, 100);
		cell2 = new StorageCell(Type.REFRIGERATED, 100, 100, 100);
		
		box1 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24);
		box2 = new BeveragesBox(BeveragesBox.Type.REFRIGERATED, "Milk", 15, 15, 15, 12);
	}
	
	@Test
	void testStorageManagementCreation() {
		assertNotNull(storageManagement);
	}
	
	@Test
	void testRunSystem() {
		// Should not throw exception when running
		assertDoesNotThrow(() -> {
			Thread thread = new Thread(storageManagement);
			thread.start();
			Thread.sleep(100);
			thread.interrupt();
		});
	}
	
	@Test
	void testStorageOperations() {
		// Test basic storage operations
		assertTrue(cell1.add(box1));
		
		// Verify box was added by attempting to remove it
		assertTrue(cell1.remove(box1));
		// Verify box was removed - second removal should fail
		assertFalse(cell1.remove(box1));
	}
	
	@Test
	void testMultipleCellsManagement() {
		assertTrue(cell1.add(box1));
		assertTrue(cell2.add(box2));
		
		// Verify boxes were added by removing them
		assertTrue(cell1.remove(box1));
		assertTrue(cell2.remove(box2));
	}
	
	@Test
	void testConcurrentStorageAccess() throws InterruptedException {
		Thread t1 = new Thread(() -> {
			for (int i = 0; i < 5; i++) {
				BeveragesBox box = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water" + i, 10, 10, 10, 24);
				cell1.add(box);
			}
		});
		
		Thread t2 = new Thread(() -> {
			for (int i = 0; i < 5; i++) {
				BeveragesBox box = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Juice" + i, 10, 10, 10, 24);
				cell1.add(box);
			}
		});
		
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		
		// Since we can't access getBoxes(), verify indirectly
		// Try to add one more box and verify the cell still accepts boxes
		BeveragesBox testBox = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Test", 10, 10, 10, 24);
		assertTrue(cell1.isNewBoxCouldBeAdded(testBox));
	}
	
	@Test
	void testStorageManagementThreadSafety() throws InterruptedException {
		Thread managerThread = new Thread(storageManagement);
		managerThread.start();
		
		// Perform some operations while the manager is running
		assertTrue(cell1.add(box1));
		assertTrue(cell2.add(box2));
		
		Thread.sleep(100);
		
		// Verify operations succeeded
		assertTrue(cell1.remove(box1));
		assertTrue(cell2.remove(box2));
		
		managerThread.interrupt();
		managerThread.join(500);
	}
	
	@Test
	void testDifferentStorageTypes() {
		StorageCell ambientCell = new StorageCell(Type.AMBIENT, 100, 100, 100);
		StorageCell refrigeratedCell = new StorageCell(Type.REFRIGERATED, 100, 100, 100);
		StorageCell bulkCell = new StorageCell(Type.BULK, 150, 150, 150);
		
		BeveragesBox ambientBox = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24);
		BeveragesBox refrigeratedBox = new BeveragesBox(BeveragesBox.Type.REFRIGERATED, "Milk", 15, 15, 15, 12);
		BeveragesBox bulkBox = new BeveragesBox(BeveragesBox.Type.BULK, "Beer Keg", 30, 30, 30, 2);
		
		assertTrue(ambientCell.add(ambientBox));
		assertTrue(refrigeratedCell.add(refrigeratedBox));
		assertTrue(bulkCell.add(bulkBox));
		
		// Verify type restrictions
		assertFalse(ambientCell.add(refrigeratedBox));
		assertFalse(refrigeratedCell.add(bulkBox));
		assertFalse(bulkCell.add(ambientBox));
	}
}