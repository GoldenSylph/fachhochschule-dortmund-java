package de.fachhochschule.dortmund.bads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.model.Area;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.model.Area.Point;
import de.fachhochschule.dortmund.bads.resources.AGV;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;
import de.fachhochschule.dortmund.bads.resources.Truck;

public class App implements Runnable {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private Area cityArea;
	private Storage warehouse1, warehouse2;
	private List<Truck> trucks;
	private List<AGV> agvFleet;

	public static void main(String[] args) {
		new App().run();
	}

	@Override
	public void run() {
		LOGGER.info("=== Initializing Warehouse Management System ===");
		CoreConfiguration.INSTANCE.autowire();
		
		setupCity();
		setupWarehouses();
		setupTrucks();
		setupAGVFleet();
		populateWarehouses();
		
		LOGGER.info("=== System Ready ===");
		LOGGER.info("City: {} nodes | WH1: {} cells, 3 AGVs | WH2: {} cells, 2 AGVs | Trucks: {}", 
			cityArea.getAdjacencyMap().size(), warehouse1.AREA.getAdjacencyMap().size(),
			warehouse2.AREA.getAdjacencyMap().size(), trucks.size());
		
		startGUI();
		
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			LOGGER.info("Application shutting down...");
			CoreConfiguration.INSTANCE.shutdown();
		}
	}
	
	// Helper: Create grid graph
	private Map<Point, Set<Point>> createGrid(int width, int height) {
		Map<Point, Set<Point>> graph = new HashMap<>();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Point current = new Point(x, y);
				Set<Point> neighbors = new java.util.HashSet<>();
				if (x > 0) neighbors.add(new Point(x - 1, y));
				if (x < width - 1) neighbors.add(new Point(x + 1, y));
				if (y > 0) neighbors.add(new Point(x, y - 1));
				if (y < height - 1) neighbors.add(new Point(x, y + 1));
				graph.put(current, neighbors);
			}
		}
		return graph;
	}
	
	// Helper: Create warehouse storage with specialized cells and corridors
	private Storage createWarehouseWithCorridors(int width, int height, 
			int ambientCells, int refrigeratedCells, int bulkCells,
			int chargingStations, int loadingDocks, int corridors, int corridorLength, int cellSize) {
		Area area = CoreConfiguration.INSTANCE.newArea();
		
		// Create grid graph (all points are navigable)
		Map<Point, Set<Point>> graph = createGrid(width, height);
		area.setGraph(graph);
		area.setStart(0, 0);
		
		int totalCells = ambientCells + refrigeratedCells + bulkCells + chargingStations + loadingDocks + corridors;
		int totalGridPoints = width * height;
		
		if (totalCells != totalGridPoints) {
			LOGGER.warn("Cell count mismatch: total cells={}, grid points={}", totalCells, totalGridPoints);
		}
		
		StorageCell[] cells = new StorageCell[totalGridPoints];
		int index = 0;
		
		// Ambient storage cells (room temperature beverages)
		for (int i = 0; i < ambientCells; i++) {
			cells[index++] = CoreConfiguration.INSTANCE.newStorageCell(
				StorageCell.Type.AMBIENT, cellSize, cellSize, cellSize + 30);
		}
		
		// Refrigerated storage cells (temperature-controlled)
		for (int i = 0; i < refrigeratedCells; i++) {
			cells[index++] = CoreConfiguration.INSTANCE.newStorageCell(
				StorageCell.Type.REFRIGERATED, cellSize, cellSize, cellSize + 30);
		}
		
		// Bulk storage cells (large items)
		for (int i = 0; i < bulkCells; i++) {
			cells[index++] = CoreConfiguration.INSTANCE.newStorageCell(
				StorageCell.Type.BULK, cellSize + 40, cellSize + 40, cellSize + 60);
		}
		
		// Charging stations
		for (int i = 0; i < chargingStations; i++) {
			cells[index++] = CoreConfiguration.INSTANCE.newChargingStation();
		}
		
		// Loading docks (any type allowed, larger cells)
		for (int i = 0; i < loadingDocks; i++) {
			cells[index++] = CoreConfiguration.INSTANCE.newStorageCell(
				StorageCell.Type.ANY, cellSize + 80, cellSize + 80, cellSize + 80);
		}
		
		// Corridor cells (navigation only, no storage)
		for (int i = 0; i < corridors; i++) {
			cells[index++] = CoreConfiguration.INSTANCE.newStorageCell(
				StorageCell.Type.CORRIDOR, corridorLength, corridorLength, corridorLength);
		}
		
		LOGGER.info("Created warehouse: {}x{} grid with {} storage cells ({} ambient, {} refrigerated, {} bulk), {} charging, {} loading, {} corridors", 
			width, height, ambientCells + refrigeratedCells + bulkCells, 
			ambientCells, refrigeratedCells, bulkCells, chargingStations, loadingDocks, corridors);
		
		return CoreConfiguration.INSTANCE.newStorage(area, cells);
	}
	
	private void setupCity() {
		LOGGER.info("Setting up city area...");
		cityArea = CoreConfiguration.INSTANCE.newArea();
		cityArea.setGraph(createGrid(10, 10));
		cityArea.setStart(0, 0);
		LOGGER.info("City area created: 10x10 grid");
	}
	
	private void setupWarehouses() {
		LOGGER.info("Setting up warehouses...");
		
		/*
		 * WAREHOUSE 1 LAYOUT (7x5 grid = 35 cells):
		 * 
		 *     0    1    2    3    4    5    6
		 *   ┌────┬────┬────┬────┬────┬────┬────┐
		 * 0 │ A  │ A  │ ═  │ R  │ R  │ ═  │ B  │  A = Ambient Storage (6 cells)
		 *   │1A  │2A  │3A  │4A  │5A  │6A  │7A  │  R = Refrigerated Storage (4 cells)
		 *   ├────┼────┼────┼────┼────┼────┼────┤  B = Bulk Storage (2 cells)
		 * 1 │ A  │ A  │ ═  │ R  │ R  │ ═  │ B  │  C = Charging Station (3 cells)
		 *   │1B  │2B  │3B  │4B  │5B  │6B  │7B  │  L = Loading Dock (2 cells)
		 *   ├────┼────┼────┼────┼────┼────┼────┤  ═ = Corridor (18 cells)
		 * 2 │ A  │ A  │ ═  │ ═  │ ═  │ ═  │ ═  │
		 *   │1C  │2C  │3C  │4C  │5C  │6C  │7C  │  Cell sizes:
		 *   ├────┼────┼────┼────┼────┼────┼────┤  - Ambient: 120x120x150
		 * 3 │ C  │ C  │ ═  │ C  │ ═  │ L  │ L  │  - Refrigerated: 120x120x150
		 *   │1D  │2D  │3D  │4D  │5D  │6D  │7D  │  - Bulk: 160x160x180
		 *   ├────┼────┼────┼────┼────┼────┼────┤  - Loading: 200x200x200
		 * 4 │ ═  │ ═  │ ═  │ ═  │ ═  │ ═  │ ═  │  - Corridor: 50x50x50
		 *   │1E  │2E  │3E  │4E  │5E  │6E  │7E  │
		 *   └────┴────┴────┴────┴────┴────┴────┘
		 * 
		 * Cell Array Mapping (index → notation):
		 * Ambient (0-5):        0-1=1A-2A, 2-3=1B-2B, 4-5=1C-2C
		 * Refrigerated (6-9):   6-7=4A-5A, 8-9=4B-5B
		 * Bulk (10-11):         10-11=7A-7B
		 * Charging (12-14):     12-13=1D-2D, 14=4D
		 * Loading (15-16):      15-16=6D-7D
		 * Corridors (17-34):    17=3A, 18=6A, 19=3B, 20=6B, 21-25=3C-7C,
		 *                       26=3D, 27=5D, 28-34=1E-7E
		 * 
		 * Purpose:
		 * - Central and perimeter corridors for AGV movement
		 * - Ambient zone (left side): water, cola, sprite, energy drinks
		 * - Refrigerated zone (center): milk, juice, yogurt drinks
		 * - Bulk zone (right side): kegs and large containers
		 * - Loading docks at bottom-right for truck access
		 * 
		 * AGVs: 3 assigned
		 * Total: 17 storage cells + 18 corridor cells
		 */
		warehouse1 = createWarehouseWithCorridors(7, 5, 6, 4, 2, 3, 2, 18, 50, 120);
		CoreConfiguration.INSTANCE.initializeAGVChargingSystem(warehouse1);
		LOGGER.info("Warehouse 1: 35 cells (6 ambient + 4 refrigerated + 2 bulk + 3 charging + 2 loading + 18 corridors)");
		
		/*
		 * WAREHOUSE 2 LAYOUT (6x4 grid = 24 cells):
		 * 
		 *     0    1    2    3    4    5
		 *   ┌────┬────┬────┬────┬────┬────┐
		 * 0 │ A  │ A  │ ═  │ R  │ R  │ B  │  A = Ambient Storage (4 cells)
		 *   │1A  │2A  │3A  │4A  │5A  │6A  │  R = Refrigerated Storage (3 cells)
		 *   ├────┼────┼────┼────┼────┼────┤  B = Bulk Storage (1 cell)
		 * 1 │ A  │ A  │ ═  │ R  │ ═  │ ═  │  C = Charging Station (2 cells)
		 *   │1B  │2B  │3B  │4B  │5B  │6B  │  L = Loading Dock (3 cells)
		 *   ├────┼────┼────┼────┼────┼────┤  ═ = Corridor (11 cells)
		 * 2 │ C  │ C  │ ═  │ ═  │ ═  │ L  │
		 *   │1C  │2C  │3C  │4C  │5C  │6C  │  Cell sizes:
		 *   ├────┼────┼────┼────┼────┼────┤  - Ambient: 100x100x130
		 * 3 │ ═  │ ═  │ ═  │ L  │ L  │ ═  │  - Refrigerated: 100x100x130
		 *   │1D  │2D  │3D  │4D  │5D  │6D  │  - Bulk: 140x140x160
		 *   └────┴────┴────┴────┴────┴────┘  - Loading: 180x180x180
		 *                                      - Corridor: 50x50x50
		 * Cell Array Mapping (index → notation):
		 * Ambient (0-3):       0-1=1A-2A, 2-3=1B-2B
		 * Refrigerated (4-6):  4-5=4A-5A, 6=4B
		 * Bulk (7):            7=6A
		 * Charging (8-9):      8-9=1C-2C
		 * Loading (10-12):     10=6C, 11-12=4D-5D
		 * Corridors (13-23):   13=3A, 14-15=3B,5B-6B, 16-18=3C-5C,
		 *                      19-21=1D-3D, 22=6D
		 * 
		 * Purpose:
		 * - Central corridor for AGV movement throughout warehouse
		 * - Ambient zone (left): water, cola, sprite, energy drinks
		 * - Refrigerated zone (center-right): milk, juice, yogurt
		 * - Bulk storage (top-right): kegs
		 * - Loading docks at bottom for truck access
		 * 
		 * AGVs: 2 assigned
		 * Total: 13 storage cells + 11 corridor cells
		 */
		warehouse2 = createWarehouseWithCorridors(6, 4, 4, 3, 1, 2, 3, 11, 50, 100);
		LOGGER.info("Warehouse 2: 24 cells (4 ambient + 3 refrigerated + 1 bulk + 2 charging + 3 loading + 11 corridors)");
	}
	
	private void setupTrucks() {
		LOGGER.info("Setting up truck fleet...");
		trucks = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			trucks.add(CoreConfiguration.INSTANCE.newTruck(cityArea));
		}
		LOGGER.info("Truck fleet ready: {} trucks", trucks.size());
	}
	
	private void setupAGVFleet() {
		LOGGER.info("Setting up AGV fleet...");
		agvFleet = new ArrayList<>();
		
		// Create 5 AGVs
		for (int i = 0; i < 5; i++) {
			AGV agv = CoreConfiguration.INSTANCE.newAGV();
			agv.setTicksPerMovement(1);
			agv.setBatteryLowThreshold(25.0);
			agvFleet.add(agv);
		}
		
		// Assign 3 to WH1, 2 to WH2
		for (int i = 0; i < 3; i++) {
			assignAGVToWarehouse(agvFleet.get(i), warehouse1, "Warehouse 1");
		}
		for (int i = 3; i < 5; i++) {
			assignAGVToWarehouse(agvFleet.get(i), warehouse2, "Warehouse 2");
		}
		
		agvFleet.forEach(CoreConfiguration.INSTANCE::registerTickable);
		LOGGER.info("AGV fleet ready: {} AGVs (3 in WH1, 2 in WH2)", agvFleet.size());
	}
	
	private void assignAGVToWarehouse(AGV agv, Storage warehouse, String name) {
		agv.executeProgram(new AGV.Statement<?>[] {
			new AGV.Statement<>(AGV.Operand.SETUP, warehouse, new Point(0, 0))
		});
		LOGGER.info("{} assigned to {}", agv.getAgvId(), name);
	}
	
	private void populateWarehouses() {
		LOGGER.info("Populating warehouses with initial inventory...");
		
		BeveragesBox[] wh1Inventory = {
			CoreConfiguration.INSTANCE.newBeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 40, 30, 25, 24),
			CoreConfiguration.INSTANCE.newBeveragesBox(BeveragesBox.Type.AMBIENT, "Coca Cola", 40, 30, 25, 24),
			CoreConfiguration.INSTANCE.newBeveragesBox(BeveragesBox.Type.REFRIGERATED, "Milk", 35, 30, 28, 12),
			CoreConfiguration.INSTANCE.newBeveragesBox(BeveragesBox.Type.REFRIGERATED, "Orange Juice", 35, 30, 28, 12),
			CoreConfiguration.INSTANCE.newBeveragesBox(BeveragesBox.Type.BULK, "Beer Keg", 60, 60, 90, 1)
		};
		
		BeveragesBox[] wh2Inventory = {
			CoreConfiguration.INSTANCE.newBeveragesBox(BeveragesBox.Type.AMBIENT, "Sprite", 40, 30, 25, 24),
			CoreConfiguration.INSTANCE.newBeveragesBox(BeveragesBox.Type.REFRIGERATED, "Yogurt Drink", 30, 25, 20, 16),
			CoreConfiguration.INSTANCE.newBeveragesBox(BeveragesBox.Type.AMBIENT, "Energy Drink", 35, 28, 22, 20)
		};
		
		LOGGER.info("Warehouses populated: {} + {} beverage boxes", wh1Inventory.length, wh2Inventory.length);
	}
	
	private void startGUI() {
		LOGGER.info("Starting GUI...");
		LOGGER.warn("GUI implementation pending - running in headless mode");
	}
	
	// Getters
	public Area getCityArea() { return cityArea; }
	public Storage getWarehouse1() { return warehouse1; }
	public Storage getWarehouse2() { return warehouse2; }
	public List<Truck> getTrucks() { return trucks; }
	public List<AGV> getAGVFleet() { return agvFleet; }
	public List<AGV> getWarehouse1AGVs() { return agvFleet.subList(0, 3); }
	public List<AGV> getWarehouse2AGVs() { return agvFleet.subList(3, 5); }
}