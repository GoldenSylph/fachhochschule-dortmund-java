package de.fachhochschule.dortmund.bads.systems.logic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.StorageManagementConfiguration;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.systems.logic.utils.ITickable;

/**
 * Storage Management System - Manages storage operations and optimization.
 * Integrated with ClockingSimulation for timing.
 */
public class StorageManagement extends Thread implements ITickable {
	private static final Logger LOGGER = LogManager.getLogger(StorageManagement.class.getName());
	
	private final Map<String, Storage> storages;
	private volatile boolean running = true;
	
	public StorageManagement() {
		super("StorageManagement-Thread");
		this.storages = new ConcurrentHashMap<>();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("StorageManagement initialized with empty storage registry");
		}
	}
	
	@Override
	public void run() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Storage Management System started");
		}
		
		StorageManagementConfiguration config = StorageManagementConfiguration.INSTANCE;
		long systemStartTime = System.currentTimeMillis();
		int maintenanceCycles = 0;
		
		try {
			while (running && !Thread.currentThread().isInterrupted()) {
				// Perform periodic maintenance if enabled
				if (config.isAutoCompactionEnabled()) {
					long cycleStartTime = System.currentTimeMillis();
					performMaintenance();
					long cycleDuration = System.currentTimeMillis() - cycleStartTime;
					maintenanceCycles++;
					
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Maintenance cycle {} completed in {}ms", maintenanceCycles, cycleDuration);
					}
					
					if (cycleDuration > config.getCompactionIntervalMillis() / 2 && LOGGER.isWarnEnabled()) {
						LOGGER.warn("Maintenance cycle {} took {}ms ({}% of interval) - performance degradation detected", 
								maintenanceCycles, cycleDuration, 
								(cycleDuration * 100) / config.getCompactionIntervalMillis());
					}
				}
				
				Thread.sleep(config.getCompactionIntervalMillis());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Storage Management System interrupted after {} maintenance cycles", maintenanceCycles);
			}
		}
		
		long totalRuntime = System.currentTimeMillis() - systemStartTime;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Storage Management System stopped after {} maintenance cycles in {}ms (avg: {:.2f}ms per cycle)", 
					maintenanceCycles, totalRuntime, 
					maintenanceCycles > 0 ? (double)totalRuntime / maintenanceCycles : 0.0);
		}
	}
	
	@Override
	public void onTick(int currentTick) {
		// Monitor and log storage status periodically
		if (currentTick % 20 == 0) {
			if (LOGGER.isInfoEnabled()) {
				logStorageStatus(currentTick);
			}
		}
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("StorageManagement tick {} processed", currentTick);
		}
	}
	
	private void performMaintenance() {
		StorageManagementConfiguration config = StorageManagementConfiguration.INSTANCE;
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Starting maintenance on {} storages", storages.size());
		}
		
		int storagesOverThreshold = 0;
		
		storages.forEach((id, storage) -> {
			double utilization = calculateUtilization(storage);
			
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Storage {} utilization: {:.2%}", id, utilization);
			}
			
			if (utilization > config.getStorageUtilizationThreshold()) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Storage {} utilization ({:.2%}) exceeds threshold ({:.2%}) - maintenance recommended", 
							id, utilization, config.getStorageUtilizationThreshold());
				}
			}
		});
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Maintenance completed - {} storage(s) over threshold", storagesOverThreshold);
		}
	}
	
	private double calculateUtilization(Storage storage) {
		// TODO: Implement actual utilization calculation based on Storage implementation
		return 0.0;
	}
	
	private void logStorageStatus(int currentTick) {
		LOGGER.info("Tick {} - Storage Status - Total Storages: {}", currentTick, storages.size());
	}
	
	public void registerStorage(String id, Storage storage) {
		if (id == null || storage == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Attempted to register storage with null id or storage - ignoring");
			}
			return;
		}
		
		Storage previous = storages.put(id, storage);
		
		if (LOGGER.isInfoEnabled()) {
			if (previous != null) {
				LOGGER.info("Replaced existing storage: {}. Total storages: {}", id, storages.size());
			} else {
				LOGGER.info("Registered new storage: {}. Total storages: {}", id, storages.size());
			}
		}
	}
	
	public Storage getStorage(String id) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Storage {} requested", id);
		}
		return storages.get(id);
	}
	
	public void unregisterStorage(String id) {
		if (id == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Attempted to unregister storage with null id - ignoring");
			}
			return;
		}
		
		Storage removed = storages.remove(id);
		
		if (LOGGER.isInfoEnabled()) {
			if (removed != null) {
				LOGGER.info("Unregistered storage: {}. Remaining storages: {}", id, storages.size());
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Storage {} was not registered, nothing to remove", id);
			}
		}
	}
	
	public Map<String, Storage> getAllStorages() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("All storages requested - returning {} storages", storages.size());
		}
		return new ConcurrentHashMap<>(storages);
	}
	
	public void stopSystem() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Stopping Storage Management System gracefully (total storages: {})", storages.size());
		}
		running = false;
		interrupt();
	}
}