package de.fachhochschule.dortmund.bads.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.resources.AGV;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;

public class StorageCell {
	private static final Logger LOGGER = LogManager.getLogger();
	
	public enum Type {
		AMBIENT, REFRIGERATED, BULK, CHARGING_STATION, CORRIDOR, ANY
	}

	public final Type TYPE;
	public final int MAX_LENGTH;
	public final int MAX_WIDTH;
	public final int MAX_HEIGHT;

	private List<BeveragesBox> storedBoxes = new ArrayList<>();
	private AGV chargingAGV;
	private volatile boolean isOccupied = false; // For charging stations
	
	// Track current occupied dimensions
	private int currentLength = 0;
	private int currentWidth = 0;
	private int currentHeight = 0;

	public StorageCell(Type type, int maxLength, int maxWidth, int maxHeight) {
		this.TYPE = type;
		this.MAX_LENGTH = maxLength;
		this.MAX_WIDTH = maxWidth;
		this.MAX_HEIGHT = maxHeight;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Created StorageCell - Type: {}, Max dimensions: {}x{}x{}, Max volume: {}", 
					   type, maxLength, maxWidth, maxHeight, maxLength * maxWidth * maxHeight);
		}
	}

	public boolean add(BeveragesBox box) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Attempting to add box: {} ({}x{}x{}) to {} storage cell", 
						box.getBeverageName(), box.getLength(), box.getWidth(), box.getHeight(), this.TYPE);
		}
		
		if (!isNewBoxCouldBeAdded(box)) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Cannot add box {} to storage cell - validation failed", box.getBeverageName());
			}
			return false;
		}
		
		this.storedBoxes.add(box);
		updateDimensionsAfterAdd(box);
		
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Successfully added box {} - Current dimensions: {}x{}x{}, Space efficiency: {:.1f}%, Boxes: {}", 
					   box.getBeverageName(), currentLength, currentWidth, currentHeight, 
					   getSpaceEfficiency(), storedBoxes.size());
		}
		
		return true;
	}
	
	public boolean remove(BeveragesBox box) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Attempting to remove box: {} from storage cell", box.getBeverageName());
		}
		
		boolean removed = this.storedBoxes.remove(box);
		if (removed) {
			recalculateDimensions();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Successfully removed box {} - Current dimensions: {}x{}x{}, Remaining boxes: {}", 
						   box.getBeverageName(), currentLength, currentWidth, currentHeight, storedBoxes.size());
			}
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Failed to remove box {} - box not found in storage cell", box.getBeverageName());
			}
		}
		return removed;
	}
	
	public boolean isNewBoxCouldBeAdded(BeveragesBox box) {
		if (this.TYPE == Type.ANY) {
			return true;
		} 
		if (this.TYPE == Type.CHARGING_STATION || this.TYPE == Type.CORRIDOR) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Cannot add box to {} type storage cell", this.TYPE);
			}
			return false; // Cannot store boxes in a charging station or corridor
		}
		
		if (!((this.TYPE == Type.AMBIENT && box.getType() == BeveragesBox.Type.AMBIENT)
				|| (this.TYPE == Type.BULK && box.getType() == BeveragesBox.Type.BULK)
				|| (this.TYPE == Type.REFRIGERATED && box.getType() == BeveragesBox.Type.REFRIGERATED))) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Box type {} incompatible with storage cell type {}", box.getType(), this.TYPE);
			}
			return false;
		}

		// Check if the box fits in the remaining 3D space
		boolean fits = fitsIn3DSpace(box);
		if (!fits && LOGGER.isDebugEnabled()) {
			LOGGER.debug("Box {} ({}x{}x{}) does not fit in remaining 3D space - Current: {}x{}x{}, Max: {}x{}x{}", 
						box.getBeverageName(), box.getLength(), box.getWidth(), box.getHeight(),
						currentLength, currentWidth, currentHeight, MAX_LENGTH, MAX_WIDTH, MAX_HEIGHT);
		}
		return fits;
	}
	
	/**
	 * Checks if a box fits within the remaining 3D space of the storage cell.
	 * Uses a more sophisticated approach considering actual volume constraints.
	 */
	private boolean fitsIn3DSpace(BeveragesBox box) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Evaluating 3D space fit for box {} ({}x{}x{}) in cell with current dimensions {}x{}x{}", 
						box.getBeverageName(), box.getLength(), box.getWidth(), box.getHeight(),
						currentLength, currentWidth, currentHeight);
		}
		
		// Strategy 1: Check if stacking vertically still fits
		if (canStackVertically(box)) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Box {} can be placed using vertical stacking strategy", box.getBeverageName());
			}
			return true;
		}
		
		// Strategy 2: Check if we can fit side by side (if current height allows)
		if (canFitSideBySide(box)) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Box {} can be placed using side-by-side strategy", box.getBeverageName());
			}
			return true;
		}
		
		// Strategy 3: Check if the box can fit in a new layer
		if (canFitInNewLayer(box)) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Box {} can be placed using new layer strategy", box.getBeverageName());
			}
			return true;
		}
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Box {} cannot fit using any placement strategy", box.getBeverageName());
		}
		return false;
	}
	
	/**
	 * Checks if the box can be stacked on top of existing boxes
	 */
	private boolean canStackVertically(BeveragesBox box) {
		// Check if the box fits within the current footprint
		boolean fitsInCurrentFootprint = box.getLength() <= currentLength && 
										 box.getWidth() <= currentWidth;
		
		// Check if adding the height doesn't exceed the maximum
		boolean heightFits = (currentHeight + box.getHeight()) <= MAX_HEIGHT;
		
		boolean canStack = fitsInCurrentFootprint && heightFits;
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Vertical stacking check for {}: footprint fits={}, height fits={}, result={}", 
						box.getBeverageName(), fitsInCurrentFootprint, heightFits, canStack);
		}
		
		return canStack;
	}
	
	/**
	 * Checks if the box can fit side by side with existing boxes
	 */
	private boolean canFitSideBySide(BeveragesBox box) {
		// Calculate if we can expand the footprint while keeping the same height
		int potentialLength = Math.max(currentLength, box.getLength());
		int potentialWidth = Math.max(currentWidth, box.getWidth());
		
		// Check if the expanded footprint fits and the box height doesn't exceed current stack
		boolean canFit = potentialLength <= MAX_LENGTH && 
						 potentialWidth <= MAX_WIDTH && 
						 box.getHeight() <= currentHeight;
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Side-by-side check for {}: potential dimensions {}x{}x{}, within limits={}, result={}", 
						box.getBeverageName(), potentialLength, potentialWidth, box.getHeight(), 
						(potentialLength <= MAX_LENGTH && potentialWidth <= MAX_WIDTH), canFit);
		}
		
		return canFit;
	}
	
	/**
	 * Checks if the box can start a new layer (if current boxes allow it)
	 */
	private boolean canFitInNewLayer(BeveragesBox box) {
		// Only works if we have some existing boxes to build upon
		if (storedBoxes.isEmpty()) {
			// For empty storage, just check basic dimensions
			boolean fits = box.getLength() <= MAX_LENGTH && 
						   box.getWidth() <= MAX_WIDTH && 
						   box.getHeight() <= MAX_HEIGHT;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("New layer check for {} in empty storage: dimensions fit={}", box.getBeverageName(), fits);
			}
			return fits;
		}
		
		// Check if adding this box as a new layer would fit
		int newLayerHeight = currentHeight + box.getHeight();
		boolean fits = box.getLength() <= MAX_LENGTH && 
					   box.getWidth() <= MAX_WIDTH && 
					   newLayerHeight <= MAX_HEIGHT;
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("New layer check for {}: new total height would be {}, within limit={}, result={}", 
						box.getBeverageName(), newLayerHeight, (newLayerHeight <= MAX_HEIGHT), fits);
		}
		
		return fits;
	}
	
	/**
	 * Updates dimensions after adding a box using optimized placement strategy
	 */
	private void updateDimensionsAfterAdd(BeveragesBox box) {
		int oldLength = currentLength;
		int oldWidth = currentWidth;
		int oldHeight = currentHeight;
		
		if (storedBoxes.size() == 1) {
			// First box sets the initial dimensions
			currentLength = box.getLength();
			currentWidth = box.getWidth();
			currentHeight = box.getHeight();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("First box {} set initial dimensions to {}x{}x{}", 
							box.getBeverageName(), currentLength, currentWidth, currentHeight);
			}
		} else {
			// Determine placement strategy based on pre-addition state
			
			// Check if box fits in current footprint (stacking vertically)
			boolean canStack = box.getLength() <= oldLength && 
							   box.getWidth() <= oldWidth &&
							   (oldHeight + box.getHeight()) <= MAX_HEIGHT;
			
			if (canStack) {
				// Stack vertically - just add height
				currentHeight += box.getHeight();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Box {} placed using vertical stacking - dimensions: {}x{}x{} -> {}x{}x{}", 
								box.getBeverageName(), oldLength, oldWidth, oldHeight, 
								currentLength, currentWidth, currentHeight);
				}
			} else {
				// Expand footprint
				currentLength = Math.max(oldLength, box.getLength());
				currentWidth = Math.max(oldWidth, box.getWidth());
				
				// Check if this is side-by-side (same height level) or new layer
				boolean sideBySide = currentLength <= MAX_LENGTH && 
									 currentWidth <= MAX_WIDTH && 
									 box.getHeight() <= oldHeight;
				
				if (sideBySide) {
					// Side-by-side placement
					currentHeight = Math.max(oldHeight, box.getHeight());
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Box {} placed side-by-side - dimensions: {}x{}x{} -> {}x{}x{}", 
									box.getBeverageName(), oldLength, oldWidth, oldHeight, 
									currentLength, currentWidth, currentHeight);
					}
				} else {
					// New layer
					currentHeight = oldHeight + box.getHeight();
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Box {} placed in new layer - dimensions: {}x{}x{} -> {}x{}x{}", 
									box.getBeverageName(), oldLength, oldWidth, oldHeight, 
									currentLength, currentWidth, currentHeight);
					}
				}
			}
		}
	}
	
	/**
	 * Recalculates dimensions after removing a box
	 */
	private void recalculateDimensions() {
		int oldLength = currentLength;
		int oldWidth = currentWidth;
		int oldHeight = currentHeight;
		
		if (storedBoxes.isEmpty()) {
			currentLength = 0;
			currentWidth = 0;
			currentHeight = 0;
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Storage cell emptied - dimensions reset to 0x0x0");
			}
			return;
		}
		
		// Recalculate from scratch
		currentLength = 0;
		currentWidth = 0;
		currentHeight = 0;
		
		for (BeveragesBox box : storedBoxes) {
			currentLength = Math.max(currentLength, box.getLength());
			currentWidth = Math.max(currentWidth, box.getWidth());
			currentHeight += box.getHeight();
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Dimensions recalculated after removal: {}x{}x{} -> {}x{}x{}, {} boxes remaining", 
						oldLength, oldWidth, oldHeight, currentLength, currentWidth, currentHeight, storedBoxes.size());
		}
	}
	
	/**
	 * Gets the remaining available volume in the storage cell
	 */
	public int getRemainingVolume() {
		int maxVolume = MAX_LENGTH * MAX_WIDTH * MAX_HEIGHT;
		int usedVolume = currentLength * currentWidth * currentHeight;
		return maxVolume - usedVolume;
	}
	
	/**
	 * Gets the actual used volume (sum of all box volumes)
	 */
	public int getActualUsedVolume() {
		return storedBoxes.stream()
			.mapToInt(box -> box.getLength() * box.getWidth() * box.getHeight())
			.sum();
	}
	
	/**
	 * Gets the space efficiency percentage (actual used volume / occupied space)
	 */
	public double getSpaceEfficiency() {
		if (currentLength == 0 || currentWidth == 0 || currentHeight == 0) {
			return 0.0;
		}
		int occupiedSpace = currentLength * currentWidth * currentHeight;
		int actualUsedVolume = getActualUsedVolume();
		return (double) actualUsedVolume / occupiedSpace * 100.0;
	}
	
	/**
	 * Checks if the storage cell is empty
	 */
	public boolean isEmpty() {
		return storedBoxes.isEmpty();
	}
	
	/**
	 * Checks if the storage cell is full (no more boxes can be added)
	 */
	public boolean isFull() {
		// A cell is considered full if we've reached maximum dimensions in all axes
		return currentLength >= MAX_LENGTH || 
			   currentWidth >= MAX_WIDTH || 
			   currentHeight >= MAX_HEIGHT;
	}
	
	/**
	 * Gets the number of boxes stored
	 */
	public int getBoxCount() {
		return storedBoxes.size();
	}
	
	/**
	 * Gets the current occupied dimensions
	 */
	public int getCurrentLength() {
		return currentLength;
	}
	
	public int getCurrentWidth() {
		return currentWidth;
	}
	
	public int getCurrentHeight() {
		return currentHeight;
	}
	
	/**
	 * Occupy this charging station with an AGV.
	 * Only works for CHARGING_STATION type cells.
	 */
	public synchronized boolean occupyWithAGV(AGV agv) {
		if (TYPE != Type.CHARGING_STATION) {
			LOGGER.warn("Cannot occupy non-charging-station cell with AGV");
			return false;
		}
		if (isOccupied || chargingAGV != null) {
			LOGGER.warn("Charging station already occupied by {}", chargingAGV);
			return false;
		}
		this.chargingAGV = agv;
		this.isOccupied = true;
		LOGGER.info("Charging station occupied by {}", agv);
		return true;
	}
	
	/**
	 * Release the AGV from this charging station.
	 */
	public synchronized boolean releaseAGV() {
		if (TYPE != Type.CHARGING_STATION) {
			LOGGER.warn("Cannot release AGV from non-charging-station cell");
			return false;
		}
		if (!isOccupied || chargingAGV == null) {
			LOGGER.warn("Charging station is not occupied");
			return false;
		}
		AGV releasedAGV = this.chargingAGV;
		this.chargingAGV = null;
		this.isOccupied = false;
		LOGGER.info("Charging station released from {}", releasedAGV);
		return true;
	}
	
	/**
	 * Check if this charging station is occupied by an AGV.
	 */
	public synchronized boolean isOccupiedByAGV() {
		return TYPE == Type.CHARGING_STATION && isOccupied && chargingAGV != null;
	}
	
	/**
	 * Get the AGV currently occupying this charging station.
	 */
	public synchronized AGV getChargingAGV() {
		return chargingAGV;
	}
}