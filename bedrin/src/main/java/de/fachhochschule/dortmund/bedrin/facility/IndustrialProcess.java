package de.fachhochschule.dortmund.bedrin.facility;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import de.fachhochschule.dortmund.bedrin.facility.abs.IOperation;
import de.fachhochschule.dortmund.bedrin.facility.interfaces.IResource;

public class IndustrialProcess {
	private String id;
	
	protected List<IOperation> operations;

	public IndustrialProcess(String newId, List<IOperation> newOperations) {
		this.id = newId;
		this.operations = newOperations;
	}
	
	public double processDuration(int operationIndex) {
		return (new java.util.Date().getTime()) - this.operations.get(operationIndex).getNominalTime().getTime();
	}
	
	public IResource<InputStream, OutputStream> processResources(int operationIndex, int resourceIndex) {
		return ((OperationWithAppendableResources) this.operations.get(operationIndex)).getResource(resourceIndex);
	}

	protected String getId() {
		return id;
	}
}
