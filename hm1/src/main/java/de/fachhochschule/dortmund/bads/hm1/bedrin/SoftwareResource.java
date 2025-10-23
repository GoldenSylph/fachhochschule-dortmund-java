package de.fachhochschule.dortmund.bads.hm1.bedrin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import de.fachhochschule.dortmund.bads.hm1.bedrin.interfaces.ICPU;

public class SoftwareResource extends Resource {

	private ICPU<InputStream, OutputStream> compatibleCPU;
	private ByteArrayInputStream program;

	public SoftwareResource(ICPU<InputStream, OutputStream> hardwareResource, ByteArrayInputStream program) {
		this.compatibleCPU = hardwareResource;
		this.program = program;
	}

	@Override
	public Resource call() {
		this.compatibleCPU.cacheProgram(this.program);
		return this;
	}
	
	public ICPU<InputStream, OutputStream> getCompatibleCPU() {
		return compatibleCPU;
	}

	@Override
	public double getQuantity() {
		return 1.0;
	}

}
