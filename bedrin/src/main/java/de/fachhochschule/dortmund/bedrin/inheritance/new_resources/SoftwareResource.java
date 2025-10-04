package de.fachhochschule.dortmund.bedrin.inheritance.new_resources;

import de.fachhochschule.dortmund.bedrin.inheritance.Resource;
import de.fachhochschule.dortmund.bedrin.inheritance.new_resources.base.NonHumanResource;

public class SoftwareResource extends NonHumanResource {

	private String licenseKey;

	public SoftwareResource(String licenseKey) {
		super(1d, false);
		this.licenseKey = licenseKey;
	}

	public String getLicenseKey() {
		return licenseKey;
	}

	@Override
	public <T extends Resource> Resource interchange(T intoWhat) {
		throw new UnsupportedOperationException("A software license cannot be interchanged.");
	}

	@Override
	public Resource call() throws Exception {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

}
