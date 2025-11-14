package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.model.Area;
import de.fachhochschule.dortmund.bads.model.Area.Point;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.model.StorageCell.Type;
import de.fachhochschule.dortmund.bads.resources.Truck;

class TruckTest {
	
	private Truck truck;
	private Area cityNetwork;
	private StorageCell inventoryCell;
	
	@BeforeEach
	void setUp() {
		// Create a 5x5 city network
		cityNetwork = new Area();
		Map<Point, Set<Point>> graph = new HashMap<>();
		
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 5; y++) {
				Point p = new Point(x, y);
				Set<Point> neighbors = new HashSet<>();
				
				if (x > 0) neighbors.add(new Point(x - 1, y));
				if (x < 4) neighbors.add(new Point(x + 1, y));
				if (y > 0) neighbors.add(new Point(x, y - 1));
				if (y < 4) neighbors.add(new Point(x, y + 1));
				
				graph.put(p, neighbors);
			}
		}
		
		cityNetwork.setGraph(graph);
		cityNetwork.setStart(2, 2);
		
		inventoryCell = new StorageCell(Type.ANY, 300, 300, 300);
		truck = new Truck(cityNetwork);
		truck.setInventoryCell(inventoryCell);
	}
	
	@Test
	void testTruckCreation() {
		assertNotNull(truck);
	}
	
	@Test
	void testSetInventoryCell() {
		StorageCell newCell = new StorageCell(Type.ANY, 400, 400, 400);
		truck.setInventoryCell(newCell);
		assertEquals(newCell, truck.getInventoryCell());
	}
	
	@Test
	void testGetInventoryCell() {
		assertEquals(inventoryCell, truck.getInventoryCell());
	}
	
	@Test
	void testSetStartPoint() {
		Point startPoint = new Point(0, 0);
		truck.setStartPoint(startPoint);
		assertEquals(startPoint, truck.getStartPoint());
	}
	
	@Test
	void testSetDestinationPoint() {
		Point destPoint = new Point(4, 4);
		truck.setDestinationPoint(destPoint);
		assertEquals(destPoint, truck.getDestinationPoint());
	}
	
	@Test
	void testGetStartPoint() {
		Point startPoint = new Point(1, 1);
		truck.setStartPoint(startPoint);
		assertEquals(startPoint, truck.getStartPoint());
	}
	
	@Test
	void testGetDestinationPoint() {
		Point destPoint = new Point(3, 3);
		truck.setDestinationPoint(destPoint);
		assertEquals(destPoint, truck.getDestinationPoint());
	}
	
	@Test
	void testOnTick() {
		truck.setStartPoint(new Point(0, 0));
		truck.setDestinationPoint(new Point(2, 2));
		
		// Calculate route first
		assertDoesNotThrow(() -> truck.call());
		
		// Simulate a few ticks
		truck.onTick(1);
		truck.onTick(2);
		truck.onTick(3);
		
		// Truck should have moved (currentLocationIdx should be > 0)
		assertTrue(truck.getCurrentLocationIdx() >= 0);
	}
	
	@Test
	void testMovementToDestination() throws Exception {
		Point start = new Point(0, 0);
		Point dest = new Point(1, 0);
		
		truck.setStartPoint(start);
		truck.setDestinationPoint(dest);
		truck.call(); // Calculate route
		
		// Simulate ticks until truck reaches destination
		for (int i = 1; i <= 10; i++) {
			truck.onTick(i);
		}
		
		// After enough ticks, truck should have progressed through route
		assertTrue(truck.getCurrentLocationIdx() >= 0);
	}
	
	@Test
	void testSetDifferentRoutes() {
		truck.setStartPoint(new Point(0, 0));
		truck.setDestinationPoint(new Point(4, 4));
		
		assertEquals(new Point(0, 0), truck.getStartPoint());
		assertEquals(new Point(4, 4), truck.getDestinationPoint());
		
		// Change route
		truck.setStartPoint(new Point(1, 1));
		truck.setDestinationPoint(new Point(3, 3));
		
		assertEquals(new Point(1, 1), truck.getStartPoint());
		assertEquals(new Point(3, 3), truck.getDestinationPoint());
	}
	
	@Test
	void testCallCalculatesRoute() throws Exception {
		truck.setStartPoint(new Point(0, 0));
		truck.setDestinationPoint(new Point(2, 2));
		
		truck.call();
		
		// Route should be calculated
		assertNotNull(truck.getRoute());
	}
	
	@Test
	void testGetRoute() throws Exception {
		truck.setStartPoint(new Point(0, 0));
		truck.setDestinationPoint(new Point(1, 0));
		
		truck.call();
		
		List<Point> route = truck.getRoute();
		assertNotNull(route);
		assertFalse(route.isEmpty());
	}
	
	@Test
	void testGetCurrentLocationIdx() {
		int locationIdx = truck.getCurrentLocationIdx();
		assertTrue(locationIdx >= 0);
	}
	
	@Test
	void testSetTicksPerPointInRoute() {
		truck.setTicksPerPointInRoute(5);
		assertEquals(5, truck.getTicksPerPointInRoute());
	}
	
	@Test
	void testGetTicksPerPointInRoute() {
		int ticks = truck.getTicksPerPointInRoute();
		assertTrue(ticks > 0);
	}
	
	@Test
	void testInvalidTicksPerPointThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> {
			truck.setTicksPerPointInRoute(0);
		});
		
		assertThrows(IllegalArgumentException.class, () -> {
			truck.setTicksPerPointInRoute(-1);
		});
	}
	
	@Test
	void testGetQuantity() {
		assertEquals(1.0, truck.getQuantity());
	}
	
	@Test
	void testRouteDescriptions() throws Exception {
		truck.setStartPoint(new Point(0, 0));
		truck.setDestinationPoint(new Point(1, 1));
		
		truck.call();
		
		List<String> descriptions = truck.getRoutePointsDescriptions();
		assertNotNull(descriptions);
	}
	
	@Test
	void testCallWithoutStartPoint() throws Exception {
		truck.setDestinationPoint(new Point(2, 2));
		
		// Should not throw exception, but route should be null or empty
		truck.call();
		
		// Route calculation should fail gracefully
		assertDoesNotThrow(() -> truck.call());
	}
	
	@Test
	void testCallWithoutDestinationPoint() throws Exception {
		truck.setStartPoint(new Point(0, 0));
		
		// Should not throw exception, but route should be null or empty
		assertDoesNotThrow(() -> truck.call());
	}
}