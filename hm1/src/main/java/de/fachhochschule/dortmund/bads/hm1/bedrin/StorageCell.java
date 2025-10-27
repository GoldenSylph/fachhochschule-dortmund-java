package de.fachhochschule.dortmund.bads.hm1.bedrin;

public class StorageCell {
	private final Storage parentStorage;

	// 3 dimensions (e.g., meters)
	private final double length;
	private final double width;
	private final double height;

	// currently occupied dimensions (bounded by length/width/height)
	private double occupiedLength;
	private double occupiedWidth;
	private double occupiedHeight;

	public enum Type {
		REFRIGERATED, BULK_STORAGE, AMBIENT, CHARGING_STATION
	}

	private final Type type;

	public StorageCell(Storage parentStorage, double length, double width, double height, Type type) {
		if (parentStorage == null) {
			throw new IllegalArgumentException("parentStorage must not be null");
		}
		if (length <= 0 || width <= 0 || height <= 0) {
			throw new IllegalArgumentException("dimensions must be positive");
		}
		if (type == null) {
			throw new IllegalArgumentException("type must not be null");
		}
		this.parentStorage = parentStorage;
		this.length = length;
		this.width = width;
		this.height = height;
		this.type = type;
		this.occupiedLength = 0.0;
		this.occupiedWidth = 0.0;
		this.occupiedHeight = 0.0;
	}

	public Storage getParentStorage() {
		return parentStorage;
	}

	public double getLength() {
		return length;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public double getVolume() {
		return length * width * height;
	}

	public double getOccupiedLength() {
		return occupiedLength;
	}

	public double getOccupiedWidth() {
		return occupiedWidth;
	}

	public double getOccupiedHeight() {
		return occupiedHeight;
	}

	public double getOccupiedVolume() {
		return occupiedLength * occupiedWidth * occupiedHeight;
	}

	public double getAvailableVolume() {
		double available = getVolume() - getOccupiedVolume();
		return available < 0 ? 0 : available;
	}

	public Type getType() {
		return type;
	}

	public boolean isOccupied() {
		return this.occupiedHeight > 0 && this.occupiedLength > 0 && this.occupiedWidth > 0;
	}

	public boolean isEmpty() {
		return !isOccupied();
	}

	public synchronized void setOccupiedDimensions(double occupiedLength, double occupiedWidth, double occupiedHeight) {
		if (occupiedLength < 0 || occupiedWidth < 0 || occupiedHeight < 0) {
			throw new IllegalArgumentException("occupied dimensions must be non-negative");
		}
		if (occupiedLength > length || occupiedWidth > width || occupiedHeight > height) {
			throw new IllegalArgumentException("occupied dimensions exceed cell capacity");
		}
		this.occupiedLength = occupiedLength;
		this.occupiedWidth = occupiedWidth;
		this.occupiedHeight = occupiedHeight;
	}

	public synchronized boolean fill() {
		boolean alreadyFull = this.occupiedLength == length && this.occupiedWidth == width
				&& this.occupiedHeight == height;
		if (alreadyFull) {
			return false;
		}
		this.occupiedLength = length;
		this.occupiedWidth = width;
		this.occupiedHeight = height;
		return true;
	}

	public synchronized boolean clear() {
		boolean alreadyEmpty = this.occupiedLength == 0.0 && this.occupiedWidth == 0.0 && this.occupiedHeight == 0.0;
		if (alreadyEmpty) {
			return false;
		}
		this.occupiedLength = 0.0;
		this.occupiedWidth = 0.0;
		this.occupiedHeight = 0.0;
		return true;
	}

	@Override
	public String toString() {
		return "StorageCell{" + "type=" + type + ", dims=" + length + "x" + width + "x" + height + ", occupiedDims="
				+ occupiedLength + "x" + occupiedWidth + "x" + occupiedHeight + '}';
	}
}