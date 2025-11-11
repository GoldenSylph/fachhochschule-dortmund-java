package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.model.Area;
import de.fachhochschule.dortmund.bads.model.Area.Point;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.model.StorageCell.Type;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;

class StorageTest {
	
	private Storage storage;
	private Area area;
	private StorageCell[] cells;
	
	@BeforeEach
	void setUp() {
		// Create a 3x3 grid
		area = new Area();
		Map<Point, Set<Point>> graph = new HashMap<>();
		
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				Point p = new Point(x, y);
				Set<Point> neighbors = new HashSet<>();
				
				if (x > 0) neighbors.add(new Point(x - 1, y));
				if (x < 2) neighbors.add(new Point(x + 1, y));
				if (y > 0) neighbors.add(new Point(x, y - 1));
				if (y < 2) neighbors.add(new Point(x, y + 1));
				
				graph.put(p, neighbors);
			}
		}
		
		area.setGraph(graph);
		area.setStart(0, 0);
		
		// Create 9 storage cells
		cells = new StorageCell[9];
		for (int i = 0; i < 9; i++) {
			cells[i] = new StorageCell(Type.ANY, 100, 100, 100);
		}
		
		storage = new Storage(area, cells);
	}
	
	@Test
	void testStorageCreation() {
		assertNotNull(storage);
		assertNotNull(storage.AREA);
		assertEquals(area, storage.AREA);
	}
	
	@Test
	void testGetCellByNotation() {
		StorageCell cell = storage.getCellByNotation("1A");
		assertNotNull(cell);
		assertEquals(cells[0], cell);
	}
	
	@Test
	void testGetCellByDifferentNotations() {
		assertNotNull(storage.getCellByNotation("1A"));
		assertNotNull(storage.getCellByNotation("2B"));
		assertNotNull(storage.getCellByNotation("3C"));
	}
	
	@Test
	void testInvalidNotation() {
		StorageCell cell = storage.getCellByNotation("9Z");
		assertNull(cell);
	}
	
	@Test
	void testNotationToPoint() {
		Point point = Storage.notationToPoint("1A");
		assertNotNull(point);
		assertEquals(0, point.x());
		assertEquals(0, point.y());
	}
	
	@Test
	void testPointToNotation() {
		String notation = Storage.pointToNotation(new Point(0, 0));
		assertEquals("1A", notation);
		
		notation = Storage.pointToNotation(new Point(1, 1));
		assertEquals("2B", notation);
	}
	
	@Test
	void testNotationConversion() {
		// Test round-trip conversion
		Point originalPoint = new Point(2, 1);
		String notation = Storage.pointToNotation(originalPoint);
		Point convertedPoint = Storage.notationToPoint(notation);
		
		assertEquals(originalPoint.x(), convertedPoint.x());
		assertEquals(originalPoint.y(), convertedPoint.y());
	}
	
	@Test
	void testAddBeveragesBoxToCell() {
		BeveragesBox box = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24);
		StorageCell cell = storage.getCellByNotation("1A");
		
		assertTrue(cell.add(box));
		// Verify box was added by removing it
		assertTrue(cell.remove(box));
	}
	
	@Test
	void testMultipleCellsOperations() {
		BeveragesBox box1 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24);
		BeveragesBox box2 = new BeveragesBox(BeveragesBox.Type.REFRIGERATED, "Milk", 15, 15, 15, 12);
		
		StorageCell cell1 = storage.getCellByNotation("1A");
		StorageCell cell2 = storage.getCellByNotation("2B");
		
		assertTrue(cell1.add(box1));
		assertTrue(cell2.add(box2));
		
		// Verify boxes were added by removing them
		assertTrue(cell1.remove(box1));
		assertTrue(cell2.remove(box2));
	}
	
	@Test
	void testGetChargingStationCount() {
		// Initially no charging stations
		int count = storage.getChargingStationCount();
		assertTrue(count >= 0);
	}
	
	@Test
	void testStorageWithChargingStations() {
		// Create storage with charging stations
		StorageCell[] cellsWithCharging = new StorageCell[9];
		cellsWithCharging[0] = new StorageCell(Type.ANY, 100, 100, 100);
		cellsWithCharging[1] = new StorageCell(Type.CHARGING_STATION, 0, 0, 0);
		cellsWithCharging[2] = new StorageCell(Type.ANY, 100, 100, 100);
		cellsWithCharging[3] = new StorageCell(Type.CHARGING_STATION, 0, 0, 0);
		
		for (int i = 4; i < 9; i++) {
			cellsWithCharging[i] = new StorageCell(Type.ANY, 100, 100, 100);
		}
		
		Storage storageWithCharging = new Storage(area, cellsWithCharging);
		assertEquals(2, storageWithCharging.getChargingStationCount());
	}
	
	@Test
	void testAllCellsAccessible() {
		// Test that all 9 cells are accessible
		for (int row = 1; row <= 3; row++) {
			for (char col = 'A'; col <= 'C'; col++) {
				String notation = row + String.valueOf(col);
				StorageCell cell = storage.getCellByNotation(notation);
				assertNotNull(cell, "Cell " + notation + " should be accessible");
			}
		}
	}
}