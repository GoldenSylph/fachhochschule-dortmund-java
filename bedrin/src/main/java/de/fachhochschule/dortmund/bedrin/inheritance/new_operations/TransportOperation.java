package de.fachhochschule.dortmund.bedrin.inheritance.new_operations;

import de.fachhochschule.dortmund.bedrin.facility.abs.Operation;
import de.fachhochschule.dortmund.bedrin.inheritance.new_resources.base.NonHumanResource;

public class TransportOperation extends Operation {
	
	public TransportOperation(String newId, String newDescription) {
		super(newId, newDescription);
	}
	
	public <T extends NonHumanResource> void addResource(T newResource) {
		this.resources.add(newResource);
	}
	

}
