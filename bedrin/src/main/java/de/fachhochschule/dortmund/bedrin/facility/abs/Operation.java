package de.fachhochschule.dortmund.bedrin.facility.abs;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import de.fachhochschule.dortmund.bedrin.inheritance.Resource;

public abstract class Operation {
	private String id;
	private String description;

	protected Time creationTime;
	protected List<Resource> resources;

	public Operation(String newId, String newDescription) {
		this.id = newId;
		this.description = newDescription;
		this.creationTime = new Time(new java.util.Date().getTime());
		this.resources = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
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
