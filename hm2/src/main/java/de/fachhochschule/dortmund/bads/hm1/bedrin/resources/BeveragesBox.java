package de.fachhochschule.dortmund.bads.hm1.bedrin.resources;

public class BeveragesBox extends Resource {
	public enum Type {
		AMBIENT, REFRIGERATED, BULK
	}

	private Type type;
	private String beverageName;
	private int width;
	private int height;
	private int length;
	private int quantityOfBottles;

	public BeveragesBox(Type type, String beverageName, int width, int height, int length, int quantityOfBottles) {
		this.type = type;
		this.beverageName = beverageName;
		this.width = width;
		this.height = height;
		this.length = length;
		this.quantityOfBottles = quantityOfBottles;
	}

	@Override
	public Resource call() throws Exception {
		return this;
	}

	@Override
	public double getQuantity() {
		return quantityOfBottles;
	}

	public Type getType() {
		return type;
	}

	public String getBeverageName() {
		return beverageName;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getLength() {
		return length;
	}
}
