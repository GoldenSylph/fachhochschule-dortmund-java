package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.util.List;

// The de.fachhochschule.dortmund.bedrin.inheritance.Process is from the third Assignment.
public class IndustrialProcess extends Process {
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
