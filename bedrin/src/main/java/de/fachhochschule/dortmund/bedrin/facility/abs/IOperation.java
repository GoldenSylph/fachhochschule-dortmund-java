package de.fachhochschule.dortmund.bedrin.facility.abs;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.util.List;

import de.fachhochschule.dortmund.bedrin.facility.interfaces.IResource;

public abstract class IOperation {
	private String id;
	private String description;
	
	protected Time nominalTime;
	protected List<IResource<InputStream, OutputStream>> resources;
	
	public IOperation(String newId, String newDescription) {
		this.id = newId;
		this.description = newDescription;
	}
	
	public void setData(int index, InputStream data) {
		((IResource<InputStream, OutputStream>) resources.get(index)).setData(data);
	}
	
	public OutputStream getData(int index) {
		return ((IResource<InputStream, OutputStream>) resources.get(index)).getData();
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public Time getNominalTime() {
		return nominalTime;
	}

	public void setNominalTime(Time nominalTime) {
		this.nominalTime = nominalTime;
	}
}
