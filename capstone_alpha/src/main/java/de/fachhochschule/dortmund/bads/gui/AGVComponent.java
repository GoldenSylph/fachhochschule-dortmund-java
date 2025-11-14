package de.fachhochschule.dortmund.bads.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.gui.agv.AGVRenderer;
import de.fachhochschule.dortmund.bads.gui.agv.AGVStateUpdater;
import de.fachhochschule.dortmund.bads.resources.AGV;
import de.fachhochschule.dortmund.bads.systems.logic.utils.ITickable;

public class AGVComponent extends JPanel implements ITickable {
	private static final long serialVersionUID = 5666910554083721161L;
	private static final Logger LOGGER = LogManager.getLogger(AGVComponent.class);
	
	private String agvId;
	private int batteryLevel;
	private boolean hasCargo;
	private boolean isMoving;
	private AGV.AGVState agvState = AGV.AGVState.IDLE;
	private int animationOffset = 0;
	
	private final AGVStateUpdater stateUpdater;
	private final Timer animationTimer;

	/**
	 * Constructor with real AGV instance (PREFERRED - FULLY INTEGRATED)
	 */
	public AGVComponent(AGV agv) {
		this.agvId = agv != null ? agv.getAgvId() : "AGV-?";
		this.stateUpdater = new AGVStateUpdater(agv, this::handleStateUpdate);
		this.animationTimer = new Timer(100, e -> {
			if (isMoving) {
				animationOffset = (animationOffset + 5) % 50;
				repaint();
			}
		});
		stateUpdater.updateFromBackend();
		initializeComponent();
		LOGGER.debug("AGVComponent created with backend AGV: {}", agvId);
	}

	/**
	 * Constructor with placeholder data (for testing/demo only)
	 */
	public AGVComponent(String agvId, int batteryLevel, boolean hasCargo) {
		this.agvId = agvId;
		this.batteryLevel = batteryLevel;
		this.hasCargo = hasCargo;
		this.isMoving = false;
		this.stateUpdater = new AGVStateUpdater(null, null);
		this.animationTimer = new Timer(100, e -> {
			if (isMoving) {
				animationOffset = (animationOffset + 5) % 50;
				repaint();
			}
		});
		initializeComponent();
		LOGGER.debug("AGVComponent created in standalone mode: {}", agvId);
	}

	private void initializeComponent() {
		setPreferredSize(new Dimension(80, 80));
		setOpaque(false);
		animationTimer.start();
	}
	
	private void handleStateUpdate(int battery, AGV.AGVState state, boolean cargo, boolean moving) {
		this.batteryLevel = battery;
		this.agvState = state;
		this.hasCargo = cargo;
		this.isMoving = moving;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;

		AGVRenderer.drawBody(g2d, centerX, centerY, agvState);
		AGVRenderer.drawWheels(g2d, centerX, centerY);
		
		if (hasCargo) {
			AGVRenderer.drawCargo(g2d, centerX, centerY);
		}
		
		AGVRenderer.drawBattery(g2d, centerX, centerY, batteryLevel);
		AGVRenderer.drawState(g2d, centerX, centerY, agvState);
		AGVRenderer.drawId(g2d, centerX, centerY, agvId);
	}

	@Override
	public void onTick(int currentTick) {
		if (stateUpdater.hasBackend() && currentTick % 5 == 0) {
			stateUpdater.updateFromBackend();
		}
	}

	// Setters for manual updates (when AGV backend not available)
	public void setBatteryLevel(int level) {
		this.batteryLevel = Math.max(0, Math.min(100, level));
		repaint();
	}

	public void setHasCargo(boolean hasCargo) {
		this.hasCargo = hasCargo;
		repaint();
	}

	public void setMoving(boolean moving) {
		this.isMoving = moving;
		repaint();
	}

	public void setAgvId(String agvId) {
		this.agvId = agvId;
		repaint();
	}

	/**
	 * Connect this component to a real backend AGV instance
	 * This allows late-binding of AGV after component creation
	 */
	public void setBackendAGV(AGV agv) {
		if (agv == null) {
			LOGGER.warn("Attempted to set null AGV backend");
			return;
		}
		
		// Update the AGV ID
		this.agvId = agv.getAgvId();
		
		// Create a new state updater with the backend AGV
		AGVStateUpdater newUpdater = new AGVStateUpdater(agv, this::handleStateUpdate);
		
		// Replace the old state updater field via reflection
		try {
			java.lang.reflect.Field field = this.getClass().getDeclaredField("stateUpdater");
			field.setAccessible(true);
			field.set(this, newUpdater);
			
			// Immediately update from the backend
			newUpdater.updateFromBackend();
			
			LOGGER.info("AGVComponent successfully connected to backend AGV: {}", agv.getAgvId());
		} catch (Exception e) {
			LOGGER.error("Failed to update stateUpdater field: {}", e.getMessage(), e);
		}
	}

	// Getters
	public String getAgvId() { return agvId; }
	public int getBatteryLevel() { return batteryLevel; }
	public boolean hasCargo() { return hasCargo; }
	public boolean isMoving() { return isMoving; }
	public AGV getBackendAGV() { return stateUpdater.getAGV(); }
	public AGV.AGVState getAgvState() { return agvState; }
}