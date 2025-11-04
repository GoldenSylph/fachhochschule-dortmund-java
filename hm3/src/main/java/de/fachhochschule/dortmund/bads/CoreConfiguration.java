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

public enum CoreConfiguration implements IConfiguration {
	INSTANCE;
	
	public static final Logger LOGGER = LogManager.getLogger();
	private boolean isAutowired;
	
	@Override
	public IConfiguration autowire() {
		if (isAutowired) {
			LOGGER.warn("CoreConfiguration is already autowired!");
			return this;
		}
		SystemBuilder.INSTANCE
			.system(Systems.CLOCKING).logic(new ClockingSimulation()).buildAndStart()
			.system(Systems.TASK_MANAGEMENT).logic(new TaskManagement()).buildAndStart()
			.system(Systems.STORAGE_MANAGEMENT).logic(new StorageManagement()).buildAndStart()
			.system(Systems.OBSERVATION).logic(new Observation()).buildAndStart();
		isAutowired = true;
		LOGGER.info("CoreConfiguration autowired successfully.");
		return this;
	}
	
	public boolean getAutowiredStatus() {
		return isAutowired;
	}

	public Operation newOperation(List<Resource> resources) {
		Operation op = new Operation();
		for (Resource res : resources) {
			op.addResource(res);
		}
		return op;
	}
	
	public Process newProcess(List<Operation> operations) {
		Process proc = new Process();
		for (Operation op : operations) {
			proc.addOperation(op);
		}
		return proc;
	}

	public StorageCell newStorageCell(Storage storage) {
		return null;
	}

	public Storage newStorage() {
		return null;
	}
	
	public Truck newTruck() {
		return null;
	}
	
	public BeveragesBox newBeverage() {
		return null;
	}
	
	public Task newTask() {
		return null;
	}
}
