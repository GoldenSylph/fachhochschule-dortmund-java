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
	private Storage warehouse;
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
		setupWarehouse();
		setupTrucks();
		setupAGVFleet();
		populateWarehouse();
		
		LOGGER.info("=== System Ready ===");
		LOGGER.info("City: {} nodes | Warehouse: {} cells, {} AGVs | Trucks: {}", 
			cityArea.getAdjacencyMap().size(), warehouse.AREA.getAdjacencyMap().size(),
			agvFleet.size(), trucks.size());
		
		startGUI();
		
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			LOGGER.info("Application shutting down...");
			CoreConfiguration.INSTANCE.shutdown();
		}
	}
	
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
	
	private void setupCity() {
		LOGGER.info("Setting up city area...");
		cityArea = CoreConfiguration.INSTANCE.newArea();
		cityArea.setGraph(createGrid(10, 10));
		cityArea.setStart(0, 0);
		LOGGER.info("City area created: 10x10 grid");
	}
	
	private void setupWarehouse() {
		LOGGER.info("Setting up warehouse...");
		
		/*
		 * WAREHOUSE LAYOUT (7x5 grid = 35 cells):
		 * 
		 * Point(x,y) notation: (x+1)(y as letter) where y: 0=A, 1=B, 2=C, 3=D, 4=E
		 * Example: Point(0,0)=1A, Point(6,0)=7A, Point(0,4)=1E, Point(6,4)=7E
		 * 
		 *     Col:0    1    2    3    4    5    6
		 *   +----+----+----+----+----+----+----+
		 * 0 | A  | A  | =  | R  | R  | =  | B  |  A = Ambient Storage (6 cells)
		 *   |1A  |2A  |3A  |4A  |5A  |6A  |7A  |  R = Refrigerated Storage (4 cells)
		 *   +----+----+----+----+----+----+----+  B = Bulk Storage (2 cells)
		 * 1 | A  | A  | =  | R  | R  | =  | B  |  C = Charging Station (3 cells)
		 *   |1B  |2B  |3B  |4B  |5B  |6B  |7B  |  L = Loading Dock (2 cells)
		 *   +----+----+----+----+----+----+----+  = = Corridor (18 cells)
		 * 2 | A  | A  | =  | =  | =  | =  | =  |
		 *   |1C  |2C  |3C  |4C  |5C  |6C  |7C  |  Cell sizes:
		 *   +----+----+----+----+----+----+----+  - Ambient: 120x120x150
		 * 3 | C  | C  | =  | C  | =  | L  | L  |  - Refrigerated: 120x120x150
		 *   |1D  |2D  |3D  |4D  |5D  |6D  |7D  |  - Bulk: 160x160x180
		 *   +----+----+----+----+----+----+----+  - Loading: 200x200x200
		 * 4 | =  | =  | =  | =  | =  | =  | =  |  - Corridor: 50x50x50
		 *   |1E  |2E  |3E  |4E  |5E  |6E  |7E  |
		 *   +----+----+----+----+----+----+----+
		 * 
		 * Storage fills cells by iterating Point(x,y) in order:
		 * (0,0), (1,0), (2,0), ..., (6,0), (0,1), (1,1), ..., (6,4)
		 * Maps to notations: 1A, 2A, 3A, ..., 7A, 1B, 2B, ..., 7E
		 * 
		 * WAREHOUSE LOCATION IN CITY:
		 * The warehouse is located at city position (2, 2)
		 * Trucks navigate to this position to load/unload
		 */
		
		Area area = CoreConfiguration.INSTANCE.newArea();
		area.setGraph(createGrid(7, 5));
		area.setStart(0, 0);
		
		String[][] layout = {
			{"A", "A", "=", "R", "R", "=", "B"},  // Row 0
			{"A", "A", "=", "R", "R", "=", "B"},  // Row 1
			{"A", "A", "=", "=", "=", "=", "="},  // Row 2
			{"C", "C", "=", "C", "=", "L", "L"},  // Row 3
			{"=", "=", "=", "=", "=", "=", "="}   // Row 4
		};
		
		StorageCell[] cells = new StorageCell[35];
		int index = 0;
		for (int y = 0; y < 5; y++) {
			for (int x = 0; x < 7; x++) {
				cells[index++] = createCellByType(layout[y][x]);
			}
		}
		
		warehouse = CoreConfiguration.INSTANCE.newStorage(area, cells);
		
		// Set warehouse position in the city (center of the 10x10 grid)
		warehouse.setCityPosition(2, 2);
		
		CoreConfiguration.INSTANCE.initializeAGVChargingSystem(warehouse);
		LOGGER.info("Warehouse: 35 cells (6 ambient + 4 refrigerated + 2 bulk + 3 charging + 2 loading + 18 corridors)");
		LOGGER.info("Warehouse located at city position: {}", warehouse.getCityPosition());
	}
	
	private StorageCell createCellByType(String type) {
		return switch (type) {
			case "A" -> CoreConfiguration.INSTANCE.newStorageCell(StorageCell.Type.AMBIENT, 120, 120, 150);
			case "R" -> CoreConfiguration.INSTANCE.newStorageCell(StorageCell.Type.REFRIGERATED, 120, 120, 150);
			case "B" -> CoreConfiguration.INSTANCE.newStorageCell(StorageCell.Type.BULK, 160, 160, 180);
			case "C" -> CoreConfiguration.INSTANCE.newChargingStation();
			case "L" -> CoreConfiguration.INSTANCE.newStorageCell(StorageCell.Type.ANY, 200, 200, 200);
			case "=" -> CoreConfiguration.INSTANCE.newStorageCell(StorageCell.Type.CORRIDOR, 50, 50, 50);
			default -> throw new IllegalArgumentException("Unknown cell type: " + type);
		};
	}
	
	private void setupTrucks() {
		LOGGER.info("Setting up truck fleet...");
		trucks = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			// Create trucks with default beverages pre-loaded
			Truck truck = CoreConfiguration.INSTANCE.newTruckWithDefaults(cityArea);
			trucks.add(truck);
		}
		LOGGER.info("Truck fleet ready: {} trucks with default beverage inventory", trucks.size());
	}
	
	private void setupAGVFleet() {
		LOGGER.info("Setting up AGV fleet...");
		agvFleet = new ArrayList<>();
		
		for (int i = 0; i < 3; i++) {
			AGV agv = CoreConfiguration.INSTANCE.newAGV();
			agv.setTicksPerMovement(1);
			agv.setBatteryLowThreshold(25.0);
			agv.executeProgram(new AGV.Statement<?>[] {
				new AGV.Statement<>(AGV.Operand.SETUP, warehouse, new Point(0, 0))
			});
			agvFleet.add(agv);
			LOGGER.info("{} assigned to Main Warehouse", agv.getAgvId());
		}
		
		agvFleet.forEach(CoreConfiguration.INSTANCE::registerTickable);
		LOGGER.info("AGV fleet ready: {} AGVs assigned to warehouse", agvFleet.size());
	}
	
	private void populateWarehouse() {
		LOGGER.info("Populating warehouse with initial inventory...");
		
		// Ambient beverages
		createBeverageBox(BeveragesBox.Type.AMBIENT, "Water", 40, 30, 25, 24);
		createBeverageBox(BeveragesBox.Type.AMBIENT, "Coca Cola", 40, 30, 25, 24);
		createBeverageBox(BeveragesBox.Type.AMBIENT, "Sprite", 40, 30, 25, 24);
		createBeverageBox(BeveragesBox.Type.AMBIENT, "Energy Drink", 35, 28, 22, 20);
		
		// Refrigerated beverages
		createBeverageBox(BeveragesBox.Type.REFRIGERATED, "Milk", 35, 30, 28, 12);
		createBeverageBox(BeveragesBox.Type.REFRIGERATED, "Orange Juice", 35, 30, 28, 12);
		createBeverageBox(BeveragesBox.Type.REFRIGERATED, "Yogurt Drink", 30, 25, 20, 16);
		createBeverageBox(BeveragesBox.Type.REFRIGERATED, "Apple Juice", 35, 30, 28, 12);
		
		// Bulk items
		createBeverageBox(BeveragesBox.Type.BULK, "Beer Keg", 60, 60, 90, 1);
		createBeverageBox(BeveragesBox.Type.BULK, "Soda Syrup", 55, 55, 85, 1);
		
		LOGGER.info("Warehouse populated: 10 beverage types");
	}
	
	private void createBeverageBox(BeveragesBox.Type type, String name, int length, int width, int height, int qty) {
		CoreConfiguration.INSTANCE.newBeveragesBox(type, name, length, width, height, qty);
	}
	
	private void startGUI() {
		LOGGER.info("Starting GUI...");
		GUIConfiguration.INSTANCE.setWarehouseData(cityArea, warehouse, null, agvFleet, trucks).autowire();
		LOGGER.info("GUI initialized and displayed");
	}
	
	public Area getCityArea() { return cityArea; }
	public Storage getWarehouse() { return warehouse; }
	public List<Truck> getTrucks() { return trucks; }
	public List<AGV> getAGVFleet() { return agvFleet; }
}