package de.fachhochschule.dortmund.bedrin.inheritance.new_operations;

import de.fachhochschule.dortmund.bedrin.facility.abs.Operation;
import de.fachhochschule.dortmund.bedrin.inheritance.new_resources.base.HardwareResource;

public class TransportOperation extends Operation {
	
	public TransportOperation(String newId, String newDescription) {
		super(newId, newDescription);
	}
	
	public void addHardwareResource(HardwareResource newResource) {
		this.resources.add(newResource);
	}

}
