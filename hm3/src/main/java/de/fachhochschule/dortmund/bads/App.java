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

// Make it Runnable just so it could be run in a thread if needed
public class App implements Runnable {
	private static final Logger LOGGER = LogManager.getLogger();

	// City and transportation infrastructure
	private Area cityArea;
	private List<Truck> trucks;
	
	// Warehouses
	private Storage warehouse1;
	private Storage warehouse2;
	
	// AGV fleet
	private List<AGV> agvFleet;

	public static void main(String[] args) {
		App app = new App();
		app.run();
	}

	@Override
	public void run() {
		LOGGER.info("=== Initializing Warehouse Management System ===");

		// Initialize the core system
		CoreConfiguration.INSTANCE.autowire();
		
		// Setup city infrastructure
		setupCity();
		
		// Setup warehouses
		setupWarehouses();
		
		// Setup trucks
		setupTrucks();
		
		// Setup AGV fleet
		setupAGVFleet();
		
		// Populate warehouses with initial inventory
		populateWarehouses();
		
		LOGGER.info("=== System Ready ===");
		LOGGER.info("City area: {} nodes", cityArea.getAdjacencyMap().size());
		LOGGER.info("Warehouse 1: {} cells, {} AGVs", warehouse1.AREA.getAdjacencyMap().size(), 3);
		LOGGER.info("Warehouse 2: {} cells, {} AGVs", warehouse2.AREA.getAdjacencyMap().size(), 2);
		LOGGER.info("Trucks: {}", trucks.size());
		LOGGER.info("Total AGVs: {}", agvFleet.size());

		// Start GUI
		startGUI();

		// Keep main thread alive to prevent shutdown
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			LOGGER.info("Application shutting down...");
			CoreConfiguration.INSTANCE.shutdown();
		}
	}
	
	/**
	 * Setup city area with grid layout for truck navigation.
	 * Creates a 10x10 grid representing city blocks.
	 */
	private void setupCity() {
		LOGGER.info("Setting up city area...");
		cityArea = CoreConfiguration.INSTANCE.newArea();
		
		// Create 10x10 city grid
		Map<Point, Set<Point>> cityGraph = new HashMap<>();
		
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				Point current = new Point(x, y);
				Set<Point> neighbors = new java.util.HashSet<>();
				
				// Add connections to adjacent cells (4-directional)
				if (x > 0) neighbors.add(new Point(x - 1, y)); // Left
				if (x < 9) neighbors.add(new Point(x + 1, y)); // Right
				if (y > 0) neighbors.add(new Point(x, y - 1)); // Up
				if (y < 9) neighbors.add(new Point(x, y + 1)); // Down
				
				cityGraph.put(current, neighbors);
			}
		}
		
		cityArea.setGraph(cityGraph);
		cityArea.setStart(0, 0); // City depot at (0,0)
		
		LOGGER.info("City area created: 10x10 grid with {} nodes", cityGraph.size());
	}
	
	/**
	 * Setup two warehouses with different layouts.
	 * Warehouse 1: 20 cells (15 storage + 3 charging stations + 2 loading docks)
	 * Warehouse 2: 15 cells (10 storage + 2 charging stations + 3 loading docks)
	 */
	private void setupWarehouses() {
		LOGGER.info("Setting up warehouses...");
		
		// === WAREHOUSE 1 ===
		Area warehouse1Area = CoreConfiguration.INSTANCE.newArea();
		Map<Point, Set<Point>> wh1Graph = new HashMap<>();
		
		// Create warehouse 1 layout: 5x4 grid
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 4; y++) {
				Point current = new Point(x, y);
				Set<Point> neighbors = new java.util.HashSet<>();
				
				if (x > 0) neighbors.add(new Point(x - 1, y));
				if (x < 4) neighbors.add(new Point(x + 1, y));
				if (y > 0) neighbors.add(new Point(x, y - 1));
				if (y < 3) neighbors.add(new Point(x, y + 1));
				
				wh1Graph.put(current, neighbors);
			}
		}
		warehouse1Area.setGraph(wh1Graph);
		warehouse1Area.setStart(0, 0);
		
		// Create cells for warehouse 1
		StorageCell[] wh1Cells = new StorageCell[20];
		// 15 regular storage cells
		for (int i = 0; i < 15; i++) {
			wh1Cells[i] = CoreConfiguration.INSTANCE.newStorageCell(
				StorageCell.Type.ANY, 120, 120, 150);
		}
		// 3 charging stations
		for (int i = 15; i < 18; i++) {
			wh1Cells[i] = CoreConfiguration.INSTANCE.newChargingStation();
		}
		// 2 loading dock cells
		for (int i = 18; i < 20; i++) {
			wh1Cells[i] = CoreConfiguration.INSTANCE.newStorageCell(
				StorageCell.Type.ANY, 200, 200, 200);
		}
		
		warehouse1 = CoreConfiguration.INSTANCE.newStorage(warehouse1Area, wh1Cells);
		CoreConfiguration.INSTANCE.initializeAGVChargingSystem(warehouse1);
		
		LOGGER.info("Warehouse 1 created: 20 cells (15 storage + 3 charging + 2 loading)");
		
		// === WAREHOUSE 2 ===
		Area warehouse2Area = CoreConfiguration.INSTANCE.newArea();
		Map<Point, Set<Point>> wh2Graph = new HashMap<>();
		
		// Create warehouse 2 layout: 5x3 grid
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 3; y++) {
				Point current = new Point(x, y);
				Set<Point> neighbors = new java.util.HashSet<>();
				
				if (x > 0) neighbors.add(new Point(x - 1, y));
				if (x < 4) neighbors.add(new Point(x + 1, y));
				if (y > 0) neighbors.add(new Point(x, y - 1));
				if (y < 2) neighbors.add(new Point(x, y + 1));
				
				wh2Graph.put(current, neighbors);
			}
		}
		warehouse2Area.setGraph(wh2Graph);
		warehouse2Area.setStart(0, 0);
		
		// Create cells for warehouse 2
		StorageCell[] wh2Cells = new StorageCell[15];
		// 10 regular storage cells
		for (int i = 0; i < 10; i++) {
			wh2Cells[i] = CoreConfiguration.INSTANCE.newStorageCell(
				StorageCell.Type.ANY, 100, 100, 120);
		}
		// 2 charging stations
		for (int i = 10; i < 12; i++) {
			wh2Cells[i] = CoreConfiguration.INSTANCE.newChargingStation();
		}
		// 3 loading dock cells
		for (int i = 12; i < 15; i++) {
			wh2Cells[i] = CoreConfiguration.INSTANCE.newStorageCell(
				StorageCell.Type.ANY, 180, 180, 180);
		}
		
		warehouse2 = CoreConfiguration.INSTANCE.newStorage(warehouse2Area, wh2Cells);
		
		LOGGER.info("Warehouse 2 created: 15 cells (10 storage + 2 charging + 3 loading)");
	}
	
	/**
	 * Setup 3 trucks for inter-warehouse and city transportation.
	 */
	private void setupTrucks() {
		LOGGER.info("Setting up truck fleet...");
		trucks = new ArrayList<>();
		
		for (int i = 1; i <= 3; i++) {
			Truck truck = CoreConfiguration.INSTANCE.newTruck(cityArea);
			trucks.add(truck);
			LOGGER.info("Truck {} created and assigned to city routes", i);
		}
		
		LOGGER.info("Truck fleet ready: {} trucks", trucks.size());
	}
	
	/**
	 * Setup 5 AGVs distributed across warehouses.
	 * 3 AGVs for Warehouse 1, 2 AGVs for Warehouse 2.
	 */
	private void setupAGVFleet() {
		LOGGER.info("Setting up AGV fleet...");
		agvFleet = new ArrayList<>();
		
		// Create 5 AGVs
		for (int i = 1; i <= 5; i++) {
			AGV agv = CoreConfiguration.INSTANCE.newAGV();
			agv.setTicksPerMovement(1); // Fast movement
			agv.setBatteryLowThreshold(25.0); // Request charging at 25%
			agvFleet.add(agv);
		}
		
		// Assign 3 AGVs to Warehouse 1
		for (int i = 0; i < 3; i++) {
			AGV agv = agvFleet.get(i);
			AGV.Statement<?>[] setupProgram = {
				new AGV.Statement<>(AGV.Operand.SETUP, warehouse1, new Point(0, 0))
			};
			agv.executeProgram(setupProgram);
			LOGGER.info("{} assigned to Warehouse 1", agv.getAgvId());
		}
		
		// Assign 2 AGVs to Warehouse 2
		for (int i = 3; i < 5; i++) {
			AGV agv = agvFleet.get(i);
			AGV.Statement<?>[] setupProgram = {
				new AGV.Statement<>(AGV.Operand.SETUP, warehouse2, new Point(0, 0))
			};
			agv.executeProgram(setupProgram);
			LOGGER.info("{} assigned to Warehouse 2", agv.getAgvId());
		}
		
		// Register AGVs with clocking system for tick updates
		for (AGV agv : agvFleet) {
			CoreConfiguration.INSTANCE.registerTickable(agv);
		}
		
		LOGGER.info("AGV fleet ready: {} AGVs (3 in WH1, 2 in WH2)", agvFleet.size());
	}
	
	/**
	 * Populate warehouses with initial beverage inventory.
	 */
	private void populateWarehouses() {
		LOGGER.info("Populating warehouses with initial inventory...");
		
		// Sample beverages for Warehouse 1
		BeveragesBox[] wh1Inventory = {
			CoreConfiguration.INSTANCE.newBeveragesBox(
				BeveragesBox.Type.AMBIENT, "Water", 40, 30, 25, 24),
			CoreConfiguration.INSTANCE.newBeveragesBox(
				BeveragesBox.Type.AMBIENT, "Coca Cola", 40, 30, 25, 24),
			CoreConfiguration.INSTANCE.newBeveragesBox(
				BeveragesBox.Type.REFRIGERATED, "Milk", 35, 30, 28, 12),
			CoreConfiguration.INSTANCE.newBeveragesBox(
				BeveragesBox.Type.REFRIGERATED, "Orange Juice", 35, 30, 28, 12),
			CoreConfiguration.INSTANCE.newBeveragesBox(
				BeveragesBox.Type.BULK, "Beer Keg", 60, 60, 90, 1)
		};
		
		// Sample beverages for Warehouse 2
		BeveragesBox[] wh2Inventory = {
			CoreConfiguration.INSTANCE.newBeveragesBox(
				BeveragesBox.Type.AMBIENT, "Sprite", 40, 30, 25, 24),
			CoreConfiguration.INSTANCE.newBeveragesBox(
				BeveragesBox.Type.REFRIGERATED, "Yogurt Drink", 30, 25, 20, 16),
			CoreConfiguration.INSTANCE.newBeveragesBox(
				BeveragesBox.Type.AMBIENT, "Energy Drink", 35, 28, 22, 20)
		};
		
		LOGGER.info("Warehouses populated with {} + {} beverage boxes", 
			wh1Inventory.length, wh2Inventory.length);
	}
	
	/**
	 * Start the Swing GUI for system visualization and control.
	 */
	private void startGUI() {
		LOGGER.info("Starting GUI...");
		// TODO: Initialize and display Swing GUI
		// GUI should show:
		// - City map with truck positions
		// - Warehouse 1 layout with AGV positions
		// - Warehouse 2 layout with AGV positions
		// - Control panel for tasks
		// - Status panel for system metrics
		
		LOGGER.warn("GUI implementation pending - running in headless mode");
	}
	
	// === Public Getters for GUI Access ===
	
	public Area getCityArea() {
		return cityArea;
	}
	
	public Storage getWarehouse1() {
		return warehouse1;
	}
	
	public Storage getWarehouse2() {
		return warehouse2;
	}
	
	public List<Truck> getTrucks() {
		return trucks;
	}
	
	public List<AGV> getAGVFleet() {
		return agvFleet;
	}
	
	public List<AGV> getWarehouse1AGVs() {
		return agvFleet.subList(0, 3);
	}
	
	public List<AGV> getWarehouse2AGVs() {
		return agvFleet.subList(3, 5);
	}
}