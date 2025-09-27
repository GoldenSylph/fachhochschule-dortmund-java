package de.fachhochschule.dortmund.bedrin.facility;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;

import de.fachhochschule.dortmund.bedrin.facility.interfaces.IResource;

public class AGV implements IResource<InputStream, OutputStream> {
	private String id;
	private double batteryLoad;
	private double batteryConsuptionPerMinute;
	private Time lastCharged;
	private float maxSpeedPerMinute;
	private float actSpeedPerMinute;
	private OutputStream outputBuffer;
	
	private final double[] position = new double[2];

	public AGV(String newId) {
		this.id = newId;
	}

	@Override
	public OutputStream getData() {
		return outputBuffer;
	}

	@Override
	public void setData(InputStream newData) {
		// just want to make a simple stack machine
		
	}

	protected String getId() {
		return id;
	}

	protected double getBatteryLoad() {
		return batteryLoad;
	}

	protected double getBatteryConsuptionPerMinute() {
		return batteryConsuptionPerMinute;
	}

	protected Time getLastCharged() {
		return lastCharged;
	}

	protected float getMaxSpeedPerMinute() {
		return maxSpeedPerMinute;
	}

	protected float getActSpeedPerMinute() {
		return actSpeedPerMinute;
	}

	protected double[] getPosition() {
		return position;
	}

}
