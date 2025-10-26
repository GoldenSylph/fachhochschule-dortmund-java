package de.fachhochschule.dortmund.bads.hm1.bedrin.systems;

import java.util.ArrayList;
import java.util.List;

import de.fachhochschule.dortmund.bads.hm1.bedrin.resources.Resource;
import de.fachhochschule.dortmund.bads.hm1.bedrin.systems.logic.ClockingSimulation;

public class Operation {
	protected int creationTime;
	protected List<Resource> resources;

	public Operation() {
		this.creationTime = ((ClockingSimulation) Systems.CLOCKING.getLogic()).getCurrentTime();
		this.resources = new ArrayList<>();
	}

	public int getCreationTime() {
		return creationTime;
	}

	public Resource getResource(int index) {
		return this.resources.get(index);
	}
	
	public int getResourcesCount() {
		return this.resources.size();
	}
	
	public void addResource(Resource resource) {
		this.resources.add(resource);
	}
}
