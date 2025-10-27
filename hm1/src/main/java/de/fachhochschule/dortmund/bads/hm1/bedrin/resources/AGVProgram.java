package de.fachhochschule.dortmund.bads.hm1.bedrin.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import de.fachhochschule.dortmund.bads.hm1.bedrin.interfaces.ICPU;

public class AGVProgram extends Resource {
	private ICPU<InputStream, OutputStream> compatibleCPU;
	private ByteArrayInputStream program;

	public AGVProgram(ICPU<InputStream, OutputStream> hardwareResource, ByteArrayInputStream program) {
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
