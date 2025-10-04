package de.fachhochschule.dortmund.bedrin.inheritance.new_resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import de.fachhochschule.dortmund.bedrin.facility.interfaces.ICPU;
import de.fachhochschule.dortmund.bedrin.inheritance.Resource;
import de.fachhochschule.dortmund.bedrin.inheritance.new_resources.base.NonHumanResource;

public class SoftwareResource extends NonHumanResource {

	private ICPU<InputStream, OutputStream> hardwareResource;
	private ByteArrayInputStream program;

	public SoftwareResource(ICPU<InputStream, OutputStream> hardwareResource, ByteArrayInputStream program) {
		super(1d, false);
		this.hardwareResource = hardwareResource;
		this.program = program;
	}

	@Override
	public Resource call() {
		this.hardwareResource.cacheProgram(this.program);
		return this;
	}
	
	public ICPU<InputStream, OutputStream> getHardwareResource() {
		return hardwareResource;
	}

}
