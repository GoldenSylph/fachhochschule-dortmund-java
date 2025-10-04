package de.fachhochschule.dortmund.bedrin.facility;

import java.util.List;

import de.fachhochschule.dortmund.bedrin.inheritance.new_operations.TransportOperation;

// The de.fachhochschule.dortmund.bedrin.inheritance.Process is from the third Assignment.
public class IndustrialProcess extends de.fachhochschule.dortmund.bedrin.inheritance.Process {
	private String id;

	protected List<TransportOperation> operations;

	public IndustrialProcess(String newId, List<TransportOperation> newOperations) {
		this.id = newId;
		this.operations = newOperations;
	}

	public String getId() {
		return id;
	}

	@Override
	public List<TransportOperation> getOperations() {
		return operations;
	}
}
