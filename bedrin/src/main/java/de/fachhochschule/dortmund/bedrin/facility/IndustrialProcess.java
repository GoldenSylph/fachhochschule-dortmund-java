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
	
	public double processDuration(int index) {
		return 0.0;
	}
	
	public IResource<InputStream, OutputStream>[] processResources(int index) {
		return null;
	}

	protected String getId() {
		return id;
	}
}
