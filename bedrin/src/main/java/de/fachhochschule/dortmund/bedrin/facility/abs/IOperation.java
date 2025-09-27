package de.fachhochschule.dortmund.bedrin.facility.abs;

import java.sql.Time;

import de.fachhochschule.dortmund.bedrin.facility.interfaces.IResource;

public abstract class IOperation {
	private String id;
	private String description;
	private Time nominalTime;
	private IResource[] resources;
	
}
