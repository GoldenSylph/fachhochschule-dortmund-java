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
	
	// Helper: Create warehouse storage
	private Storage createWarehouse(int width, int height, int storageCells, int chargingStations, 
									int loadingDocks, int cellSize) {
		Area area = CoreConfiguration.INSTANCE.newArea();
		area.setGraph(createGrid(width, height));
		area.setStart(0, 0);
		
		int totalCells = storageCells + chargingStations + loadingDocks;
		StorageCell[] cells = new StorageCell[totalCells];
		
		// Storage cells
		for (int i = 0; i < storageCells; i++) {
			cells[i] = CoreConfiguration.INSTANCE.newStorageCell(
				StorageCell.Type.ANY, cellSize, cellSize, cellSize + 30);
		}
		// Charging stations
		for (int i = storageCells; i < storageCells + chargingStations; i++) {
			cells[i] = CoreConfiguration.INSTANCE.newChargingStation();
		}
		// Loading docks (larger cells)
		for (int i = storageCells + chargingStations; i < totalCells; i++) {
			cells[i] = CoreConfiguration.INSTANCE.newStorageCell(
				StorageCell.Type.ANY, cellSize + 80, cellSize + 80, cellSize + 80);
		}
		
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
		 * WAREHOUSE 1 LAYOUT (5x4 grid = 20 cells):
		 * 
		 *     0   1   2   3   4
		 *   ┌───┬───┬───┬───┬───┐
		 * 0 │ S │ S │ S │ S │ S │  S = Storage Cell (15 total)
		 *   ├───┼───┼───┼───┼───┤  C = Charging Station (3 total)
		 * 1 │ S │ S │ S │ S │ S │  L = Loading Dock (2 total)
		 *   ├───┼───┼───┼───┼───┤
		 * 2 │ S │ S │ S │ S │ S │  Cell size: 120x120x150 (storage)
		 *   ├───┼───┼───┼───┼───┤               200x200x200 (loading)
		 * 3 │ C │ C │ C │ L │ L │
		 *   └───┴───┴───┴───┴───┘
		 * 
		 * AGVs: 3 assigned
		 * Capacity: 15 storage + 2 loading = 17 beverage storage locations
		 */
		warehouse1 = createWarehouse(5, 4, 15, 3, 2, 120);
		CoreConfiguration.INSTANCE.initializeAGVChargingSystem(warehouse1);
		LOGGER.info("Warehouse 1: 20 cells (15 storage + 3 charging + 2 loading)");
		
		/*
		 * WAREHOUSE 2 LAYOUT (5x3 grid = 15 cells):
		 * 
		 *     0   1   2   3   4
		 *   ┌───┬───┬───┬───┬───┐
		 * 0 │ S │ S │ S │ S │ S │  S = Storage Cell (10 total)
		 *   ├───┼───┼───┼───┼───┤  C = Charging Station (2 total)
		 * 1 │ S │ S │ S │ S │ S │  L = Loading Dock (3 total)
		 *   ├───┼───┼───┼───┼───┤
		 * 2 │ C │ C │ L │ L │ L │  Cell size: 100x100x130 (storage)
		 *   └───┴───┴───┴───┴───┘               180x180x180 (loading)
		 * 
		 * AGVs: 2 assigned
		 * Capacity: 10 storage + 3 loading = 13 beverage storage locations
		 */
		warehouse2 = createWarehouse(5, 3, 10, 2, 3, 100);
		LOGGER.info("Warehouse 2: 15 cells (10 storage + 2 charging + 3 loading)");
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