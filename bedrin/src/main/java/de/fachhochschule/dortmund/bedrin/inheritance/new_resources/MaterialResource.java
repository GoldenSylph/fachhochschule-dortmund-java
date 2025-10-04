package de.fachhochschule.dortmund.bedrin.inheritance.new_resources;

import de.fachhochschule.dortmund.bedrin.inheritance.Resource;
import de.fachhochschule.dortmund.bedrin.inheritance.new_resources.base.NonHumanResource;

public class MaterialResource extends NonHumanResource {

	private String materialType;
	
	public MaterialResource(double quantity, String materialType) {
		super(quantity, false);
		this.materialType = materialType;
	}

	public String getMaterialType() {
		return materialType;
	}

	@Override
	public Resource call() throws Exception {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

}
