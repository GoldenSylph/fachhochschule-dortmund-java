package de.fachhochschule.dortmund.bads;

import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.gui.MainFrame;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.resources.AGV;
import de.fachhochschule.dortmund.bads.resources.Truck;
import de.fachhochschule.dortmund.bads.systems.logic.ClockingSimulation;
import de.fachhochschule.dortmund.bads.systems.logic.Observation;
import de.fachhochschule.dortmund.bads.systems.logic.StorageManagement;
import de.fachhochschule.dortmund.bads.systems.logic.TaskManagement;

/**
 * GUI Configuration - Wires GUI components with backend systems.
 * Responsible for dependency injection and registering GUI panels as tickables.
 */
public enum GUIConfiguration implements IConfiguration {
	INSTANCE;
	
	private static final Logger LOGGER = LogManager.getLogger();
	private MainFrame mainFrame;
	private boolean isAutowired = false;
	
	// Domain objects to be set before autowiring
	private de.fachhochschule.dortmund.bads.model.Area cityArea;
	private Storage primaryWarehouse;
	private Storage secondaryWarehouse;
	private List<AGV> agvFleet;
	private List<Truck> trucks;
	
	/**
	 * Set the domain objects that the GUI will display.
	 * Call this before autowire().
	 */
	public GUIConfiguration setWarehouseData(de.fachhochschule.dortmund.bads.model.Area city,
			Storage primary, Storage secondary, 
			List<AGV> agvs, List<Truck> truckList) {
		this.cityArea = city;
		this.primaryWarehouse = primary;
		this.secondaryWarehouse = secondary;
		this.agvFleet = agvs;
		this.trucks = truckList;
		
		LOGGER.info("GUI data set - City: {}, Primary warehouse: {}, Secondary warehouse: {}, {} AGVs, {} trucks", 
				city != null, primary != null, secondary != null,
				agvs != null ? agvs.size() : 0, truckList != null ? truckList.size() : 0);
		return this;
	}
	
	@Override
	public IConfiguration autowire() {
		if (isAutowired) {
			LOGGER.warn("GUIConfiguration already autowired");
			return this;
		}
		
		LOGGER.info("=== Starting GUI Configuration Autowiring ===");
		
		// Get system references from CoreConfiguration
		if (!CoreConfiguration.INSTANCE.getAutowiredStatus()) {
			LOGGER.error("CoreConfiguration must be autowired before GUI");
			throw new IllegalStateException("CoreConfiguration not initialized");
		}
		
		ClockingSimulation clockingSystem = CoreConfiguration.INSTANCE.getClockingSystem();
		TaskManagement taskManagement = CoreConfiguration.INSTANCE.getTaskManagementSystem();
		StorageManagement storageManagement = CoreConfiguration.INSTANCE.getStorageManagementSystem();
		Observation observationSystem = CoreConfiguration.INSTANCE.getObservationSystem();
		
		LOGGER.info("Retrieved system references from CoreConfiguration");
		
		// Create MainFrame with system dependencies on EDT
		SwingUtilities.invokeLater(() -> {
			LOGGER.info("Creating MainFrame on EDT");
			
			// Create MainFrame - it will wire TaskManagement and Observation internally
			mainFrame = new MainFrame(clockingSystem, taskManagement, 
					storageManagement, observationSystem);
			
			// Now wire the domain-specific data (Storage, AGVs, Trucks)
			// This happens AFTER MainFrame construction to avoid conflicts
			
			// Configure WarehousePanel with Storage and AGV fleet
			if (primaryWarehouse != null) {
				mainFrame.getWarehousePanel().setStorage(primaryWarehouse);
				LOGGER.info("WarehousePanel configured with primary warehouse ({} cells)", 
						primaryWarehouse.getAllStorages().size());
			}
			
			if (agvFleet != null) {
				mainFrame.getWarehousePanel().setAGVFleet(agvFleet);
				LOGGER.info("WarehousePanel configured with {} AGVs", agvFleet.size());
			}
			
			// Configure LoadingBayPanel with AGVs and Trucks
			if (agvFleet != null) {
				mainFrame.getLoadingBayPanel().setAGVFleet(agvFleet);
				LOGGER.info("LoadingBayPanel configured with {} AGVs", agvFleet.size());
				for (int i = 0; i < agvFleet.size(); i++) {
					LOGGER.debug("  - AGV {}: {} (Battery: {}%)", i, agvFleet.get(i).getAgvId(), agvFleet.get(i).getBatteryLevel());
				}
			}
			
			if (trucks != null) {
				mainFrame.getLoadingBayPanel().setTrucks(trucks);
				LOGGER.info("LoadingBayPanel configured with {} trucks", trucks.size());
				for (int i = 0; i < trucks.size(); i++) {
					LOGGER.debug("  - Truck {}: {} cells", i, trucks.get(i).getInventoryCell() != null ? "has inventory" : "no inventory");
				}
			}
			
			// Set MainFrame domain objects for Logistics Control Panel
			if (cityArea != null) {
				mainFrame.setCityArea(cityArea);
				LOGGER.info("MainFrame city area set for logistics control");
			}
			
			if (primaryWarehouse != null) {
				mainFrame.setWarehouse(primaryWarehouse);
				LOGGER.info("MainFrame warehouse set for logistics control");
			}
			
			if (trucks != null) {
				mainFrame.setTrucks(trucks);
				LOGGER.info("MainFrame trucks set for logistics control");
			}
			
			// Note: TaskManagement is already wired by MainFrame constructor
			// Note: Tickables are already registered by MainFrame constructor
			
			// Trigger initial refresh to populate data
			mainFrame.getWarehousePanel().refresh();
			mainFrame.getLoadingBayPanel().refresh();
			mainFrame.getOrderManagementPanel().refresh();
			LOGGER.info("Initial panel refresh completed");
			
			// Make frame visible
			mainFrame.setVisible(true);
			LOGGER.info("MainFrame displayed");
		});
		
		isAutowired = true;
		LOGGER.info("=== GUI Configuration Autowiring Complete ===");
		return this;
	}
	
	/**
	 * Get the main frame instance.
	 * Will be null until autowire() completes on EDT.
	 */
	public MainFrame getMainFrame() {
		return mainFrame;
	}
	
	/**
	 * Check if GUI has been autowired.
	 */
	public boolean isAutowired() {
		return isAutowired;
	}
	
	// Getters for domain objects
	public de.fachhochschule.dortmund.bads.model.Area getCityArea() { return cityArea; }
	public Storage getPrimaryWarehouse() { return primaryWarehouse; }
	public Storage getSecondaryWarehouse() { return secondaryWarehouse; }
	public List<AGV> getAGVFleet() { return agvFleet; }
	public List<Truck> getTrucks() { return trucks; }
}