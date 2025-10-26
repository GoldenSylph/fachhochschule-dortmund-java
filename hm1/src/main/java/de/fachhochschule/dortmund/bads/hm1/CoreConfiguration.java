package de.fachhochschule.dortmund.bads.hm1;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.hm1.bedrin.ChargingStation;
import de.fachhochschule.dortmund.bads.hm1.bedrin.Storage;
import de.fachhochschule.dortmund.bads.hm1.bedrin.StorageCell;
import de.fachhochschule.dortmund.bads.hm1.bedrin.resources.Beverage;
import de.fachhochschule.dortmund.bads.hm1.bedrin.resources.Resource;
import de.fachhochschule.dortmund.bads.hm1.bedrin.resources.Restaurant;
import de.fachhochschule.dortmund.bads.hm1.bedrin.resources.Truck;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.Operation;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.Process;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.Systems;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.Systems.SystemBuilder;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.ClockingSimulation;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.Observation;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.StorageManagement;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.TaskManagement;

public enum CoreConfiguration implements IConfiguration {
	INSTANCE;
	
	public static final Logger LOGGER = LogManager.getLogger();
	private boolean isAutowired;
	
	@Override
	public void autowire() {
		if (isAutowired) {
			LOGGER.warn("CoreConfiguration is already autowired!");
			return;
		}
		SystemBuilder.INSTANCE
			.system(Systems.CLOCKING).logic(new ClockingSimulation()).buildAndStart()
			.system(Systems.TASK_MANAGEMENT).logic(new TaskManagement()).buildAndStart()
			.system(Systems.STORAGE_MANAGEMENT).logic(new StorageManagement()).buildAndStart()
			.system(Systems.OBSERVATION).logic(new Observation()).buildAndStart();
		isAutowired = true;
		LOGGER.info("CoreConfiguration autowired successfully.");
	}

	public Operation newOperation(List<Resource> resources) {
		Operation op = new Operation();
		for (Resource res : resources) {
			op.addResource(res);
		}
		return op;
	}
	
	public Process newProcess() {
		return null;
	}

	public StorageCell newStorageCell() {
		return null;
	}

	public ChargingStation newChargingStation() {
		return null;
	}

	public Storage newStorage() {
		return null;
	}
	
	public Truck newTruck() {
		return null;
	}
	
	public Beverage newBeverage() {
		return null;
	}
	
	public Restaurant newRestaurant() {
		return null;
	}
}
