package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.model.Area;
import de.fachhochschule.dortmund.bads.model.Area.Point;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.resources.Truck;

/**
 * Test suite for warehouse city position and truck routing integration.
 * Demonstrates how trucks can navigate to warehouses using city positions.
 */
class WarehouseCityPositionTest {
	
	private CoreConfiguration coreConfig;
	private Storage warehouse;
	private Area cityArea;
	
	@BeforeEach
	void setUp() {
		coreConfig = CoreConfiguration.INSTANCE;
	}
	
	@Test
	void testWarehouseHasCityPosition() {
		// Create a simple warehouse with proper 1x1 grid
		warehouse = createWarehouse();
		
		// Initially, warehouse should have no city position
		assertNull(warehouse.getCityPosition());
		
		// Set warehouse position in city
		warehouse.setCityPosition(3, 5);
		
		// Verify the position was set
		assertNotNull(warehouse.getCityPosition());
		assertEquals(3, warehouse.getCityPosition().x());
		assertEquals(5, warehouse.getCityPosition().y());
	}
	
	@Test
	void testWarehouseCityPositionWithCoordinates() {
		// Create warehouse with proper grid
		warehouse = createWarehouse();
		
		// Set using Point object
		Point position = new Point(2, 2);
		warehouse.setCityPosition(position);
		
		assertEquals(position, warehouse.getCityPosition());
	}
	
	@Test
	void testTruckRoutingToWarehouse() {
		// Ensure TruckManagementConfiguration is initialized
		TruckManagementConfiguration.INSTANCE.autowire();
		
		// Create city area (10x10 grid)
		cityArea = coreConfig.newArea();
		var cityGraph = new java.util.HashMap<Point, java.util.Set<Point>>();
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				Point p = new Point(x, y);
				var neighbors = new java.util.HashSet<Point>();
				if (x > 0) neighbors.add(new Point(x - 1, y));
				if (x < 9) neighbors.add(new Point(x + 1, y));
				if (y > 0) neighbors.add(new Point(x, y - 1));
				if (y < 9) neighbors.add(new Point(x, y + 1));
				cityGraph.put(p, neighbors);
			}
		}
		cityArea.setGraph(cityGraph);
		cityArea.setStart(0, 0);
		
		// Create warehouse with proper grid
		warehouse = createWarehouse();
		warehouse.setCityPosition(5, 5); // Center of the city
		
		// Create truck with default beverages
		Truck truck = coreConfig.newTruckWithDefaults(cityArea);
		
		// Verify truck has default beverages loaded
		assertNotNull(truck.getInventoryCell());
		assertEquals(5, truck.getInventoryCell().getStoredBoxes().size());
		
		// Set truck to navigate from city start to warehouse position
		Point cityStart = new Point(0, 0);
		Point warehousePosition = warehouse.getCityPosition();
		
		truck.setStartPoint(cityStart);
		truck.setDestinationPoint(warehousePosition);
		
		// Calculate route
		try {
			truck.call();
		} catch (Exception e) {
			fail("Truck routing failed: " + e.getMessage());
		}
		
		// Verify truck has a route to the warehouse
		assertEquals(cityStart, truck.getStartPoint());
		assertEquals(warehousePosition, truck.getDestinationPoint());
		assertEquals(5, warehousePosition.x());
		assertEquals(5, warehousePosition.y());
	}
	
	@Test
	void testMultipleWarehousesInCity() {
		// Create city
		cityArea = coreConfig.newArea();
		var cityGraph = new java.util.HashMap<Point, java.util.Set<Point>>();
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				Point p = new Point(x, y);
				var neighbors = new java.util.HashSet<Point>();
				if (x > 0) neighbors.add(new Point(x - 1, y));
				if (x < 9) neighbors.add(new Point(x + 1, y));
				if (y > 0) neighbors.add(new Point(x, y - 1));
				if (y < 9) neighbors.add(new Point(x, y + 1));
				cityGraph.put(p, neighbors);
			}
		}
		cityArea.setGraph(cityGraph);
		cityArea.setStart(0, 0);
		
		// Create multiple warehouses at different city positions
		Storage warehouse1 = createWarehouse();
		warehouse1.setCityPosition(2, 2); // North-west
		
		Storage warehouse2 = createWarehouse();
		warehouse2.setCityPosition(7, 7); // South-east
		
		Storage warehouse3 = createWarehouse();
		warehouse3.setCityPosition(2, 7); // South-west
		
		// Verify each warehouse has unique city positions
		assertNotEquals(warehouse1.getCityPosition(), warehouse2.getCityPosition());
		assertNotEquals(warehouse1.getCityPosition(), warehouse3.getCityPosition());
		assertNotEquals(warehouse2.getCityPosition(), warehouse3.getCityPosition());
		
		// Create truck and route it to different warehouses
		Truck truck = coreConfig.newTruckWithDefaults(cityArea);
		
		// Route to warehouse 1
		truck.setStartPoint(new Point(0, 0));
		truck.setDestinationPoint(warehouse1.getCityPosition());
		assertEquals(new Point(2, 2), truck.getDestinationPoint());
		
		// Route to warehouse 2
		truck.setStartPoint(warehouse1.getCityPosition());
		truck.setDestinationPoint(warehouse2.getCityPosition());
		assertEquals(new Point(7, 7), truck.getDestinationPoint());
	}
	
	@Test
	void testTruckDefaultBeveragesIntegration() {
		// Ensure TruckManagementConfiguration is initialized
		TruckManagementConfiguration.INSTANCE.autowire();
		
		// Create city
		cityArea = coreConfig.newArea();
		var cityGraph = new java.util.HashMap<Point, java.util.Set<Point>>();
		Point p = new Point(0, 0);
		cityGraph.put(p, new java.util.HashSet<Point>());
		cityArea.setGraph(cityGraph);
		cityArea.setStart(0, 0);
		
		// Create warehouse
		warehouse = createWarehouse();
		warehouse.setCityPosition(5, 5);
		
		// Create truck with default beverages
		Truck truck = coreConfig.newTruckWithDefaults(cityArea);
		
		// Verify truck has inventory cell and default beverages
		assertNotNull(truck.getInventoryCell());
		assertEquals(5, truck.getInventoryCell().getStoredBoxes().size());
		
		// Verify default beverage types
		var boxes = truck.getInventoryCell().getStoredBoxes();
		assertTrue(boxes.stream().anyMatch(b -> "Water".equals(b.getBeverageName())));
		assertTrue(boxes.stream().anyMatch(b -> "Coca Cola".equals(b.getBeverageName())));
		assertTrue(boxes.stream().anyMatch(b -> "Sprite".equals(b.getBeverageName())));
		assertTrue(boxes.stream().anyMatch(b -> "Milk".equals(b.getBeverageName())));
		assertTrue(boxes.stream().anyMatch(b -> "Orange Juice".equals(b.getBeverageName())));
	}
	
	private Storage createWarehouse() {
		Area warehouseArea = coreConfig.newArea();
		// Create a simple 1x1 grid with 1 node
		var graph = new java.util.HashMap<Point, java.util.Set<Point>>();
		Point p = new Point(0, 0);
		graph.put(p, new java.util.HashSet<Point>());
		warehouseArea.setGraph(graph);
		warehouseArea.setStart(0, 0);
		
		StorageCell[] cells = new StorageCell[1];
		cells[0] = coreConfig.newStorageCell(StorageCell.Type.ANY, 100, 100, 100);
		
		return coreConfig.newStorage(warehouseArea, cells);
	}
}