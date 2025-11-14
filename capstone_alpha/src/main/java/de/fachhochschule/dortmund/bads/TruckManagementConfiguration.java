package de.fachhochschule.dortmund.bads;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.resources.BeveragesBox;

/**
 * Configuration for Truck Management. Controls default truck inventory,
 * capacity, and delivery settings.
 */
public enum TruckManagementConfiguration implements IConfiguration {
	INSTANCE;

	private static final Logger LOGGER = LogManager.getLogger();

	private int defaultInventoryCellLength = 200;
	private int defaultInventoryCellWidth = 200;
	private int defaultInventoryCellHeight = 200;
	private boolean enableDefaultBeverages = true;
	private List<BeverageConfig> defaultBeverages = new ArrayList<>();
	private boolean isAutowired = false;

	/**
	 * Configuration for a default beverage to be loaded into trucks.
	 */
	public static class BeverageConfig {
		public final BeveragesBox.Type type;
		public final String name;
		public final int length;
		public final int width;
		public final int height;
		public final int quantity;

		public BeverageConfig(BeveragesBox.Type type, String name, int length, int width, int height, int quantity) {
			this.type = type;
			this.name = name;
			this.length = length;
			this.width = width;
			this.height = height;
			this.quantity = quantity;
		}
	}

	@Override
	public IConfiguration autowire() {
		if (isAutowired) {
			LOGGER.warn("TruckManagementConfiguration already autowired");
			return this;
		}

		// Set up default beverages if none configured
		if (defaultBeverages.isEmpty()) {
			setupDefaultBeverages();
		}

		LOGGER.info("TruckManagementConfiguration autowired");
		LOGGER.info("  Inventory Cell Dimensions: {}x{}x{}", 
			defaultInventoryCellLength, defaultInventoryCellWidth, defaultInventoryCellHeight);
		LOGGER.info("  Default Beverages Enabled: {}, Count: {}", 
			enableDefaultBeverages, defaultBeverages.size());

		isAutowired = true;
		return this;
	}

	private void setupDefaultBeverages() {
		// Ambient beverages
		defaultBeverages.add(new BeverageConfig(BeveragesBox.Type.AMBIENT, "Water", 40, 30, 25, 24));
		defaultBeverages.add(new BeverageConfig(BeveragesBox.Type.AMBIENT, "Coca Cola", 40, 30, 25, 24));
		defaultBeverages.add(new BeverageConfig(BeveragesBox.Type.AMBIENT, "Sprite", 40, 30, 25, 12));
		
		// Refrigerated beverages
		defaultBeverages.add(new BeverageConfig(BeveragesBox.Type.REFRIGERATED, "Milk", 35, 30, 28, 12));
		defaultBeverages.add(new BeverageConfig(BeveragesBox.Type.REFRIGERATED, "Orange Juice", 35, 30, 28, 12));
		
		LOGGER.debug("Default beverages configured: {} types", defaultBeverages.size());
	}

	public int getDefaultInventoryCellLength() {
		return defaultInventoryCellLength;
	}

	public TruckManagementConfiguration setDefaultInventoryCellLength(int length) {
		this.defaultInventoryCellLength = length;
		return this;
	}

	public int getDefaultInventoryCellWidth() {
		return defaultInventoryCellWidth;
	}

	public TruckManagementConfiguration setDefaultInventoryCellWidth(int width) {
		this.defaultInventoryCellWidth = width;
		return this;
	}

	public int getDefaultInventoryCellHeight() {
		return defaultInventoryCellHeight;
	}

	public TruckManagementConfiguration setDefaultInventoryCellHeight(int height) {
		this.defaultInventoryCellHeight = height;
		return this;
	}

	public TruckManagementConfiguration setDefaultInventoryCellDimensions(int length, int width, int height) {
		this.defaultInventoryCellLength = length;
		this.defaultInventoryCellWidth = width;
		this.defaultInventoryCellHeight = height;
		return this;
	}

	public boolean isDefaultBeveragesEnabled() {
		return enableDefaultBeverages;
	}

	public TruckManagementConfiguration setDefaultBeveragesEnabled(boolean enabled) {
		this.enableDefaultBeverages = enabled;
		return this;
	}

	public List<BeverageConfig> getDefaultBeverages() {
		return new ArrayList<>(defaultBeverages);
	}

	public TruckManagementConfiguration addDefaultBeverage(BeveragesBox.Type type, String name, 
			int length, int width, int height, int quantity) {
		defaultBeverages.add(new BeverageConfig(type, name, length, width, height, quantity));
		return this;
	}

	public TruckManagementConfiguration clearDefaultBeverages() {
		defaultBeverages.clear();
		return this;
	}
}
