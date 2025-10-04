package de.fachhochschule.dortmund.bedrin.inheritance.new_operations;

import de.fachhochschule.dortmund.bedrin.facility.abs.Operation;
import de.fachhochschule.dortmund.bedrin.inheritance.new_resources.base.HumanResource;

public class HumanOperation extends Operation {

	public HumanOperation(String newId, String newDescription) {
		super(newId, newDescription);
	}

	public void addPeople(HumanResource newResource) {
		this.resources.add(newResource);
	}

}
