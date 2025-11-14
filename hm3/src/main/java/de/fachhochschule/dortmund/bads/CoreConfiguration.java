package de.fachhochschule.dortmund.bads;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.model.Task;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;
import de.fachhochschule.dortmund.bads.resources.Resource;
import de.fachhochschule.dortmund.bads.resources.Truck;
import de.fachhochschule.dortmund.bads.systems.Operation;
import de.fachhochschule.dortmund.bads.systems.Process;
import de.fachhochschule.dortmund.bads.systems.Systems;
import de.fachhochschule.dortmund.bads.systems.Systems.SystemBuilder;
import de.fachhochschule.dortmund.bads.systems.logic.ClockingSimulation;
import de.fachhochschule.dortmund.bads.systems.logic.Observation;
import de.fachhochschule.dortmund.bads.systems.logic.StorageManagement;
import de.fachhochschule.dortmund.bads.systems.logic.TaskManagement;
import de.fachhochschule.dortmund.bads.systems.logic.utils.ITickable;

/**
 * Core Configuration - Central coordinator for all systems.
 * Manages system lifecycle and inter-system communication.
 */
public enum CoreConfiguration implements IConfiguration {
	INSTANCE;
	
	public static final Logger LOGGER = LogManager.getLogger();
	private boolean isAutowired;
	private ClockingSimulation clockingSystem;
	private TaskManagement taskManagementSystem;
	private StorageManagement storageManagementSystem;
	private Observation observationSystem;
	
	@Override
	public IConfiguration autowire() {
		if (isAutowired) {
			LOGGER.warn("CoreConfiguration is already autowired!");
			return this;
		}
		
		LOGGER.info("=== Starting Core System Autowiring ===");
		
		// Autowire all configuration subsystems
		AGVManagementConfiguration.INSTANCE.autowire();
		TaskManagementConfiguration.INSTANCE.autowire();
		StorageManagementConfiguration.INSTANCE.autowire();
		TruckManagementConfiguration.INSTANCE.autowire();
		ObservabilityConfiguration.INSTANCE.autowire();
		
		LOGGER.info("All configurations autowired. Initializing systems...");
		
		// Create system instances
		clockingSystem = new ClockingSimulation();
		taskManagementSystem = new TaskManagement();
		storageManagementSystem = new StorageManagement();
		observationSystem = new Observation();
		
		// Register systems with ClockingSimulation for tick-based coordination
		clockingSystem.registerTickable(taskManagementSystem);
		clockingSystem.registerTickable(storageManagementSystem);
		clockingSystem.registerTickable(observationSystem);
		
		// Build and start all systems
		SystemBuilder.INSTANCE
			.system(Systems.CLOCKING).logic(clockingSystem).buildAndStart()
			.system(Systems.TASK_MANAGEMENT).logic(taskManagementSystem).buildAndStart()
			.system(Systems.STORAGE_MANAGEMENT).logic(storageManagementSystem).buildAndStart()
			.system(Systems.OBSERVATION).logic(observationSystem).buildAndStart();
		
		isAutowired = true;
		LOGGER.info("=== Core System Autowiring Complete ===");
		LOGGER.info("Systems running: CLOCKING, AGV_FLEET, TASK_MANAGEMENT, STORAGE_MANAGEMENT, OBSERVATION");
		return this;
	}
	
	public boolean getAutowiredStatus() {
		return isAutowired;
	}
	
	/**
	 * Register a custom ITickable component with the clocking system.
	 */
	public void registerTickable(ITickable tickable) {
		if (clockingSystem != null) {
			clockingSystem.registerTickable(tickable);
		}
	}
	
	/**
	 * Get the Task Management System for direct interaction.
	 */
	public TaskManagement getTaskManagementSystem() {
		return taskManagementSystem;
	}
	
	/**
	 * Get the Storage Management System for direct interaction.
	 */
	public StorageManagement getStorageManagementSystem() {
		return storageManagementSystem;
	}
	
	/**
	 * Get the Observation System for direct interaction.
	 */
	public Observation getObservationSystem() {
		return observationSystem;
	}
	
	/**
	 * Get the Clocking System for direct interaction.
	 */
	public ClockingSimulation getClockingSystem() {
		return clockingSystem;
	}
	
	/**
	 * Link Storage to AGV charging system and initialize the charging queue.
	 * This must be called after creating a Storage instance to enable AGV charging.
	 * 
	 * @param storage the Storage instance containing charging stations
	 */
	public void initializeAGVChargingSystem(Storage storage) {
		if (storage == null) {
			throw new IllegalArgumentException("Storage cannot be null");
		}
		
		int chargingStationCount = storage.getChargingStationCount();
		de.fachhochschule.dortmund.bads.resources.AGV.initializeChargingSystem(chargingStationCount);
		
		LOGGER.info("AGV Charging System initialized with {} charging stations from Storage", chargingStationCount);
	}

	public Operation newOperation(List<Resource> resources) {
		Operation op = new Operation();
		for (Resource res : resources) {
			op.addResource(res);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created new operation with {} resources", resources.size());
		}
		return op;
	}
	
	public Process newProcess(List<Operation> operations) {
		Process proc = new Process();
		for (Operation op : operations) {
			proc.addOperation(op);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created new process with {} operations", operations.size());
		}
		return proc;
	}

	/**
	 * Create a new StorageCell with specified type and dimensions.
	 * 
	 * @param type the type of storage cell
	 * @param maxLength maximum length dimension
	 * @param maxWidth maximum width dimension
	 * @param maxHeight maximum height dimension
	 * @return new StorageCell instance
	 */
	public StorageCell newStorageCell(StorageCell.Type type, int maxLength, int maxWidth, int maxHeight) {
		StorageCell cell = new StorageCell(type, maxLength, maxWidth, maxHeight);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created new StorageCell - Type: {}, Dimensions: {}x{}x{}", 
					type, maxLength, maxWidth, maxHeight);
		}
		return cell;
	}
	
	/**
	 * Create a charging station cell (convenience method).
	 * Charging stations don't need dimensions since they only hold AGVs.
	 * 
	 * @return new charging station StorageCell
	 */
	public StorageCell newChargingStation() {
		StorageCell cell = new StorageCell(StorageCell.Type.CHARGING_STATION, 0, 0, 0);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created new charging station");
		}
		return cell;
	}

	/**
	 * Create a new Storage with specified area and cells.
	 * 
	 * @param area the Area defining the storage layout
	 * @param cells array of StorageCells (must match area size)
	 * @return new Storage instance
	 */
	public Storage newStorage(de.fachhochschule.dortmund.bads.model.Area area, StorageCell[] cells) {
		Storage storage = new Storage(area, cells);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created new Storage with {} cells", cells.length);
		}
		return storage;
	}
	
	/**
	 * Create a new Truck resource.
	 * 
	 * @param city the Area representing the city for truck navigation
	 * @return new Truck instance
	 */
	public Truck newTruck(de.fachhochschule.dortmund.bads.model.Area city) {
		Truck truck = new Truck(city);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created new Truck");
		}
		return truck;
	}
	
	/**
	 * Create a new Truck resource with default inventory cell and beverages.
	 * The truck will be configured according to TruckManagementConfiguration settings.
	 * 
	 * @param city the Area representing the city for truck navigation
	 * @return new Truck instance with inventory cell and default beverages loaded
	 */
	public Truck newTruckWithDefaults(de.fachhochschule.dortmund.bads.model.Area city) {
		Truck truck = new Truck(city);
		
		// Create and set inventory cell based on configuration
		TruckManagementConfiguration config = TruckManagementConfiguration.INSTANCE;
		StorageCell inventoryCell = newStorageCell(
			StorageCell.Type.ANY,
			config.getDefaultInventoryCellLength(),
			config.getDefaultInventoryCellWidth(),
			config.getDefaultInventoryCellHeight()
		);
		truck.setInventoryCell(inventoryCell);
		
		// Load default beverages if enabled
		if (config.isDefaultBeveragesEnabled()) {
			int beveragesLoaded = 0;
			for (TruckManagementConfiguration.BeverageConfig bevConfig : config.getDefaultBeverages()) {
				BeveragesBox box = newBeveragesBox(
					bevConfig.type,
					bevConfig.name,
					bevConfig.length,
					bevConfig.width,
					bevConfig.height,
					bevConfig.quantity
				);
				
				if (inventoryCell.add(box)) {
					beveragesLoaded++;
				} else {
					LOGGER.warn("Could not load default beverage '{}' into truck - insufficient space", bevConfig.name);
				}
			}
			
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Created new Truck with inventory cell ({}x{}x{}) and {} default beverages loaded",
					config.getDefaultInventoryCellLength(),
					config.getDefaultInventoryCellWidth(),
					config.getDefaultInventoryCellHeight(),
					beveragesLoaded);
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Created new Truck with inventory cell - default beverages disabled");
			}
		}
		
		return truck;
	}
	
	/**
	 * Create a new BeveragesBox resource.
	 * 
	 * @param type the type of beverage box (AMBIENT, REFRIGERATED, BULK)
	 * @param beverageName name of the beverage
	 * @param width box width
	 * @param height box height
	 * @param length box length
	 * @param quantityOfBottles number of bottles in the box
	 * @return new BeveragesBox instance
	 */
	public BeveragesBox newBeveragesBox(BeveragesBox.Type type, String beverageName, 
			int width, int height, int length, int quantityOfBottles) {
		BeveragesBox box = new BeveragesBox(type, beverageName, width, height, length, quantityOfBottles);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created new BeveragesBox - Type: {}, Name: {}, Dimensions: {}x{}x{}, Bottles: {}", 
					type, beverageName, width, height, length, quantityOfBottles);
		}
		return box;
	}
	
	/**
	 * Create a new Task with default priority.
	 * 
	 * @return new Task instance
	 */
	public Task newTask() {
		Task task = new Task();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created new Task with ID: {}", task.getTaskId());
		}
		return task;
	}
	
	/**
	 * Create a new Task with specified priority.
	 * 
	 * @param priority task priority (higher = more important)
	 * @return new Task instance
	 */
	public Task newTask(int priority) {
		Task task = new Task(priority);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created new Task with ID: {}, Priority: {}", task.getTaskId(), priority);
		}
		return task;
	}
	
	/**
	 * Create a new Area for storage or city layout.
	 * 
	 * @return new Area instance
	 */
	public de.fachhochschule.dortmund.bads.model.Area newArea() {
		de.fachhochschule.dortmund.bads.model.Area area = new de.fachhochschule.dortmund.bads.model.Area();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created new Area");
		}
		return area;
	}
	
	/**
	 * Create a new AGV resource.
	 * Note: AGVs are typically managed by the AGVFleetSystem, but this factory
	 * can be used for creating standalone AGV instances if needed.
	 * 
	 * @return new AGV instance
	 */
	public de.fachhochschule.dortmund.bads.resources.AGV newAGV() {
		de.fachhochschule.dortmund.bads.resources.AGV agv = new de.fachhochschule.dortmund.bads.resources.AGV();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created new AGV");
		}
		return agv;
	}
	
	/**
	 * Gracefully shutdown all systems in reverse order.
	 */
	public void shutdown() {
		LOGGER.info("=== Initiating System Shutdown ===");
		
		// Stop systems in reverse order
		Systems.OBSERVATION.stop();
		Systems.STORAGE_MANAGEMENT.stop();
		Systems.TASK_MANAGEMENT.stop();
		Systems.CLOCKING.stop();
		
		isAutowired = false;
		LOGGER.info("=== System Shutdown Complete ===");
	}
}