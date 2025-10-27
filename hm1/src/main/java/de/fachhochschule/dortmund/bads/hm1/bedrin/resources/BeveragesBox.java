package de.fachhochschule.dortmund.bads.hm1.bedrin.resources;

public class BeveragesBox extends Resource {
	public enum Type {
		AMBIENT,
		REFRIGERATED,
		BULK
	}
	
	private Type type;
	private String beverageName;
	private int storageCellX;
	private int storageCellY;
	private int width;
	private int height;
	private int length;
	private int quantityOfBottles;
	
	@Override
	public Resource call() throws Exception {
		return this;
	}

	@Override
	public double getQuantity() {
		return quantityOfBottles;
	}

}
