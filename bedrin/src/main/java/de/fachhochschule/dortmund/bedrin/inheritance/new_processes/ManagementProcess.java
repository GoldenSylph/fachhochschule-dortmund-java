package de.fachhochschule.dortmund.bedrin.inheritance.new_processes;

import java.util.List;

import de.fachhochschule.dortmund.bedrin.inheritance.new_operations.HumanOperation;

public class ManagementProcess extends de.fachhochschule.dortmund.bedrin.inheritance.Process {

	private String id;
	private List<HumanOperation> operations;

	public ManagementProcess(String newId, List<HumanOperation> newOperations) {
		this.id = newId;
		this.operations = newOperations;
	}

	@Override
	public List<HumanOperation> getOperations() {
		return operations;
	}

	public String getId() {
		return id;
	}

}
