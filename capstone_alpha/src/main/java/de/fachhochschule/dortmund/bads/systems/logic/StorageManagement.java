package de.fachhochschule.dortmund.bads.systems.logic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.StorageManagementConfiguration;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.systems.logic.utils.ITickable;

/**
 * Storage Management System - Manages storage operations and optimization.
 */
public class StorageManagement extends Thread implements ITickable {
	private static final Logger LOGGER = LogManager.getLogger(StorageManagement.class.getName());
	
	private final Map<String, Storage> storages = new ConcurrentHashMap<>();
	private volatile boolean running = true;
	
	public StorageManagement() {
		super("StorageManagement-Thread");
	}
	
	@Override
	public void run() {
		LOGGER.info("Storage Management System started");
		
		StorageManagementConfiguration config = StorageManagementConfiguration.INSTANCE;
		long startTime = System.currentTimeMillis();
		int cycles = 0;
		
		try {
			while (running && !Thread.currentThread().isInterrupted()) {
				if (config.isAutoCompactionEnabled()) {
					performMaintenance(config);
					cycles++;
				}
				Thread.sleep(config.getCompactionIntervalMillis());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.info("Storage Management System interrupted after {} maintenance cycles", cycles);
		}
		
		long runtime = System.currentTimeMillis() - startTime;
		double avgTime = cycles > 0 ? (double)runtime / cycles : 0.0;
		LOGGER.info("Storage Management System stopped after {} maintenance cycles in {}ms (avg: {}ms per cycle)", 
				cycles, runtime, avgTime);
	}
	
	@Override
	public void onTick(int currentTick) {
		if (currentTick % 20 == 0) {
			LOGGER.info("Tick {} - Storage Status - Total Storages: {}", currentTick, storages.size());
		}
	}
	
	private void performMaintenance(StorageManagementConfiguration config) {
		int overThreshold = 0;
		
		for (Map.Entry<String, Storage> entry : storages.entrySet()) {
			double utilization = calculateUtilization(entry.getValue());
			
			if (utilization > config.getStorageUtilizationThreshold()) {
				overThreshold++;
				LOGGER.warn("Storage {} utilization ({:.2%}) exceeds threshold ({})", 
						entry.getKey(), utilization, config.getStorageUtilizationThreshold());
			}
		}
		
		LOGGER.debug("Maintenance completed - {} storage(s) over threshold", overThreshold);
	}
	
	private double calculateUtilization(Storage storage) {
		if (storage == null || storage.getAllStorages() == null) {
			return 0.0;
		}
		
		// Get all cells from the storage
		Map<de.fachhochschule.dortmund.bads.model.Area.Point, StorageCell> cells = 
			storage.getAllStorages();
		
		if (cells.isEmpty()) {
			return 0.0;
		}
		
		// Calculate utilization based on occupied volume vs total available volume
		long totalMaxVolume = 0;
		long totalUsedVolume = 0;
		int storageCellCount = 0;
		
		// Iterate through all storage cells (excluding corridors and charging stations)
		for (StorageCell cell : cells.values()) {
			// Only count actual storage cells (not corridors or charging stations)
			if (cell.TYPE == StorageCell.Type.AMBIENT ||
				cell.TYPE == StorageCell.Type.REFRIGERATED ||
				cell.TYPE == StorageCell.Type.BULK) {
				
				storageCellCount++;
				
				// Calculate max volume for this cell
				long maxVolume = (long) cell.MAX_LENGTH * cell.MAX_WIDTH * cell.MAX_HEIGHT;
				totalMaxVolume += maxVolume;
				
				// Calculate actual used volume (sum of all box volumes)
				long usedVolume = cell.getActualUsedVolume();
				totalUsedVolume += usedVolume;
			}
		}
		
		// Avoid division by zero
		if (totalMaxVolume == 0) {
			LOGGER.debug("No storage cells with capacity found in storage");
			return 0.0;
		}
		
		// Calculate utilization as a percentage (0.0 to 1.0)
		double utilization = (double) totalUsedVolume / totalMaxVolume;
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Storage utilization: {:.2%} ({} / {} volume units across {} storage cells)", 
					utilization, totalUsedVolume, totalMaxVolume, storageCellCount);
		}
		
		return utilization;
	}
	
	public void registerStorage(String id, Storage storage) {
		if (id == null || storage == null) {
			LOGGER.warn("Cannot register storage with null id or storage");
			return;
		}
		
		Storage previous = storages.put(id, storage);
		LOGGER.info("{} storage: {}. Total storages: {}", 
				previous != null ? "Replaced" : "Registered", id, storages.size());
	}
	
	public Storage getStorage(String id) {
		return storages.get(id);
	}
	
	public void unregisterStorage(String id) {
		if (id == null) {
			LOGGER.warn("Cannot unregister storage with null id");
			return;
		}
		
		Storage removed = storages.remove(id);
		if (removed != null) {
			LOGGER.info("Unregistered storage: {}. Remaining storages: {}", id, storages.size());
		}
	}
	
	public Map<String, Storage> getAllStorages() {
		return new ConcurrentHashMap<>(storages);
	}
	
	public void stopSystem() {
		LOGGER.info("Stopping Storage Management System (total storages: {})", storages.size());
		running = false;
		interrupt();
	}
}