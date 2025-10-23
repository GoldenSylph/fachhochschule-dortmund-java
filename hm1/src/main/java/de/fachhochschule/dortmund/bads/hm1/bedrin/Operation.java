package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public abstract class Operation {
	protected Time creationTime;
	protected List<Resource> resources;

	public Operation() {
		this.creationTime = new Time(new java.util.Date().getTime());
		this.resources = new ArrayList<>();
	}

	public Time getCreationTime() {
		return creationTime;
	}

	public Resource getResource(int index) {
		return this.resources.get(index);
	}
	
	public int getResourcesCount() {
		return this.resources.size();
	}
}
