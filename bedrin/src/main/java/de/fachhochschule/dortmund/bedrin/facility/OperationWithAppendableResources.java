package de.fachhochschule.dortmund.bedrin.facility;

import java.io.InputStream;
import java.io.OutputStream;

import de.fachhochschule.dortmund.bedrin.facility.abs.IOperation;
import de.fachhochschule.dortmund.bedrin.facility.interfaces.IResource;

public class OperationWithAppendableResources extends IOperation {

	public OperationWithAppendableResources(String newId, String newDescription) {
		super(newId, newDescription);
	}
	
	public void appendNewResourceAndSetData(IResource<InputStream, OutputStream> newResource, OutputStream newData) {
		this.resources.add(newResource);
		this.resources.get(this.resources.size() - 1).setData(newData);
	}

}
