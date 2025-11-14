package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.model.Area;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;
import de.fachhochschule.dortmund.bads.systems.Operation;

class CoreConfigurationTest {
	
	@BeforeEach
	void setUp() {
		// Ensure clean state before each test
		if (CoreConfiguration.INSTANCE.getAutowiredStatus()) {
			CoreConfiguration.INSTANCE.shutdown();
		}
	}
	
	@Test
	void testSingletonInstance() {
		assertNotNull(CoreConfiguration.INSTANCE);
		assertSame(CoreConfiguration.INSTANCE, CoreConfiguration.INSTANCE);
	}
	
	@Test
	void testInitialAutowireStatus() {
		assertFalse(CoreConfiguration.INSTANCE.getAutowiredStatus());
	}
	
	@Test
	void testAutowire() {
		CoreConfiguration.INSTANCE.autowire();
		assertTrue(CoreConfiguration.INSTANCE.getAutowiredStatus());
		
		// Cleanup
		CoreConfiguration.INSTANCE.shutdown();
	}
	
	@Test
	void testSystemsAfterAutowire() {
		CoreConfiguration.INSTANCE.autowire();
		
		assertNotNull(CoreConfiguration.INSTANCE.getClockingSystem());
		assertNotNull(CoreConfiguration.INSTANCE.getTaskManagementSystem());
		assertNotNull(CoreConfiguration.INSTANCE.getStorageManagementSystem());
		assertNotNull(CoreConfiguration.INSTANCE.getObservationSystem());
		
		// Cleanup
		CoreConfiguration.INSTANCE.shutdown();
	}
	
	@Test
	void testShutdown() throws InterruptedException {
		CoreConfiguration.INSTANCE.autowire();
		assertTrue(CoreConfiguration.INSTANCE.getAutowiredStatus());
		
		Thread.sleep(100); // Let systems start
		
		CoreConfiguration.INSTANCE.shutdown();
		
		// Give systems time to shut down
		Thread.sleep(600);
		
		assertFalse(CoreConfiguration.INSTANCE.getAutowiredStatus());
	}
	
	@Test
	void testNewArea() {
		var area = CoreConfiguration.INSTANCE.newArea();
		assertNotNull(area);
	}
	
	@Test
	void testNewStorageCell() {
		var cell = CoreConfiguration.INSTANCE.newStorageCell(
			StorageCell.Type.AMBIENT, 
			100, 100, 100
		);
		assertNotNull(cell);
		assertEquals(100, cell.MAX_LENGTH);
	}
	
	@Test
	void testNewChargingStation() {
		var station = CoreConfiguration.INSTANCE.newChargingStation();
		assertNotNull(station);
		assertEquals(StorageCell.Type.CHARGING_STATION, station.TYPE);
	}
	
	@Test
	void testNewStorage() {
		var area = CoreConfiguration.INSTANCE.newArea();
		
		// Initialize the area with 5 points
		Map<Area.Point, Set<Area.Point>> graph = new HashMap<>();
		Area.Point p0 = new Area.Point(0, 0);
		Area.Point p1 = new Area.Point(1, 0);
		Area.Point p2 = new Area.Point(2, 0);
		Area.Point p3 = new Area.Point(3, 0);
		Area.Point p4 = new Area.Point(4, 0);
		
		graph.put(p0, Set.of(p1));
		graph.put(p1, Set.of(p0, p2));
		graph.put(p2, Set.of(p1, p3));
		graph.put(p3, Set.of(p2, p4));
		graph.put(p4, Set.of(p3));
		area.setGraph(graph);
		
		var cells = new StorageCell[5];
		for (int i = 0; i < 5; i++) {
			cells[i] = CoreConfiguration.INSTANCE.newStorageCell(
				StorageCell.Type.ANY, 
				100, 100, 100
			);
		}
		
		var storage = CoreConfiguration.INSTANCE.newStorage(area, cells);
		assertNotNull(storage);
		assertNotNull(storage.AREA);
	}
	
	@Test
	void testNewAGV() {
		var agv = CoreConfiguration.INSTANCE.newAGV();
		assertNotNull(agv);
		assertNotNull(agv.getAgvId());
	}
	
	@Test
	void testNewTruck() {
		var area = CoreConfiguration.INSTANCE.newArea();
		var truck = CoreConfiguration.INSTANCE.newTruck(area);
		assertNotNull(truck);
		// Truck doesn't have an ID method, just verify it was created
	}
	
	@Test
	void testNewBeveragesBox() {
		var box = CoreConfiguration.INSTANCE.newBeveragesBox(
			BeveragesBox.Type.AMBIENT,
			"Water", 10, 10, 10, 24
		);
		assertNotNull(box);
		assertEquals("Water", box.getBeverageName());
	}
	
	@Test
	void testNewTask() {
		var task = CoreConfiguration.INSTANCE.newTask(5);
		assertNotNull(task);
		assertEquals(5, task.getPriority());
	}
	
	@Test
	void testNewProcess() {
		var operations = java.util.List.of(new Operation());
		var process = CoreConfiguration.INSTANCE.newProcess(operations);
		assertNotNull(process);
	}
	
	@Test
	void testMultipleAutowireCalls() {
		CoreConfiguration.INSTANCE.autowire();
		assertTrue(CoreConfiguration.INSTANCE.getAutowiredStatus());
		
		// Second autowire should not cause issues
		CoreConfiguration.INSTANCE.autowire();
		assertTrue(CoreConfiguration.INSTANCE.getAutowiredStatus());
		
		// Cleanup
		CoreConfiguration.INSTANCE.shutdown();
	}
}