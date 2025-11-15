package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import java.awt.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.model.Area.Point;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.model.Task;
import de.fachhochschule.dortmund.bads.resources.AGV;
import de.fachhochschule.dortmund.bads.resources.Truck;
import de.fachhochschule.dortmund.bads.systems.logic.TaskManagement;
import de.fachhochschule.dortmund.bads.systems.logic.Observation;

/**
 * View for a single loading bay showing AGV, truck, and loading progress
 */
public class LoadingBayView extends JPanel {
	private static final long serialVersionUID = -5933163740014721296L;
	private static final Logger LOGGER = LogManager.getLogger(LoadingBayView.class);

	private int bayNumber;
    private String status;
    private AGVComponent agvComponent;
    private TruckComponent truckComponent;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel progressLabel;

    private AGV agv;
    private Truck truck;
    private Task currentTask;
    private TaskManagement taskManagement;
    private Observation observationSystem;

    // Animation fields for AGV horizontal movement and progress
    private Timer agvAnimationTimer;
    private int agvStartX = 10;  // AGV starting X position (warehouse entrance)
    private int agvTargetX = 200;  // AGV target X position (near truck)
    private int agvCurrentX = 10;  // Current AGV X position during animation
    private JPanel visualPanel;  // Store reference for repainting

    // Progress bar animation fields
    private int progressStartValue = 0;   // Progress before cargo transfer
    private int progressTargetValue = 0;  // Progress after cargo transfer

    // Component dimensions and positions
    private static final int AGV_WIDTH = 80;
    private static final int AGV_HEIGHT = 80;
    private static final int TRUCK_WIDTH = 100;
    private static final int TRUCK_HEIGHT = 80;

    public LoadingBayView(int bayNumber, AGV agv, Truck truck, Task task) {
        this.bayNumber = bayNumber;
        this.agv = agv;
        this.truck = truck;
        this.currentTask = task;
        this.status = determineStatus();
        initializeComponents("T-00" + bayNumber);
        updateFromBackend();
    }

    public LoadingBayView(int bayNumber, String truckId, String status) {
        this.bayNumber = bayNumber;
        this.status = status;
        initializeComponents(truckId);
    }

    private String determineStatus() {
        // Check if AGV is in any charging state
        if (agv != null) {
            AGV.AGVState agvState = agv.getState();
            if (agvState == AGV.AGVState.CHARGING ||
                agvState == AGV.AGVState.WAITING_FOR_CHARGE ||
                agvState == AGV.AGVState.MOVING_TO_CHARGE) {
                return "Charging";
            }
        }

        // Check if loading is complete (progress bar at 100%)
        if (progressBar != null && progressBar.getValue() >= 100 && truck != null) {
            return "Loaded";
        }

        // Normal operational states
        if (truck != null && agv != null) return "Preparing";
        if (truck != null) return "Active";
        return "Idle";
    }

    private void initializeComponents(String truckId) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(153, 153, 153), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        setBackground(new Color(245, 245, 245));

        add(createHeader(truckId), BorderLayout.NORTH);
        add(createVisualizationPanel(), BorderLayout.CENTER);
        add(createProgressPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeader(String truckId) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLabel = new JLabel("Loading Bay " + bayNumber + (truckId != null ? " - Truck " + truckId : ""));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(new Color(0, 0, 128));
        header.add(titleLabel, BorderLayout.WEST);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        statusPanel.setOpaque(false);
        
        JLabel statusDot = new JLabel("[*]");
        statusDot.setFont(new Font("Arial", Font.PLAIN, 16));
        statusDot.setForeground(status.equals("Active") ? new Color(0, 255, 0) : 
                                status.equals("Preparing") ? new Color(255, 255, 0) : new Color(255, 0, 0));
        statusPanel.add(statusDot);

        statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusPanel.add(statusLabel);
        header.add(statusPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createVisualizationPanel() {
        // Use null layout for absolute positioning control
        visualPanel = new JPanel();
        visualPanel.setLayout(null);  // Absolute positioning
        visualPanel.setOpaque(false);
        visualPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        visualPanel.setBackground(Color.WHITE);
        visualPanel.setPreferredSize(new Dimension(400, 120));

        // Calculate vertical center for components
        int centerY = 20;

        // AGV Component - FULLY INTEGRATED with backend
        if (agv != null) {
            agvComponent = new AGVComponent(agv);  // Use backend AGV directly
            LOGGER.debug("LoadingBay {}: Created AGVComponent with backend AGV {}", bayNumber, agv.getAgvId());
        } else {
            int battery = bayNumber == 1 ? 85 : bayNumber == 2 ? 55 : 92;
            agvComponent = new AGVComponent("AGV-" + bayNumber, battery, bayNumber == 1);
            LOGGER.debug("LoadingBay {}: Created AGVComponent in standalone mode", bayNumber);
        }
        // Position AGV at warehouse entrance (left side) and hide initially
        agvComponent.setBounds(agvStartX, centerY, AGV_WIDTH, AGV_HEIGHT);
        agvComponent.setVisible(false);  // Hidden until AGV arrives at loading dock
        visualPanel.add(agvComponent);

        // Arrow label in the middle
        JLabel arrowLabel = new JLabel("  ->  ");
        arrowLabel.setFont(new Font("Arial", Font.BOLD, 24));
        arrowLabel.setForeground(new Color(102, 102, 102));
        arrowLabel.setBounds(140, centerY + 20, 60, 40);
        visualPanel.add(arrowLabel);

        // Truck Component on the right side
        if (truck != null && truck.getInventoryCell() != null) {
            StorageCell inv = truck.getInventoryCell();
            truckComponent = new TruckComponent("T-00" + bayNumber,
                inv.MAX_LENGTH * inv.MAX_WIDTH * inv.MAX_HEIGHT / 1000,
                inv.getActualUsedVolume() / 1000);
            LOGGER.debug("LoadingBay {}: Created TruckComponent with backend Truck data", bayNumber);
        } else {
            // Create empty truck component
            truckComponent = new TruckComponent("T-00" + bayNumber, 10, 0);
            LOGGER.debug("LoadingBay {}: Created TruckComponent in standalone mode", bayNumber);
        }
        truckComponent.setBounds(250, centerY, TRUCK_WIDTH, TRUCK_HEIGHT);
        visualPanel.add(truckComponent);

        return visualPanel;
    }

    private JPanel createProgressPanel() {
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setOpaque(false);

        JPanel barPanel = new JPanel(new BorderLayout(5, 0));
        barPanel.setOpaque(false);
        barPanel.add(new JLabel("Progress:"), BorderLayout.WEST);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        int progress = calculateProgress();
        progressBar.setValue(progress);
        barPanel.add(progressBar, BorderLayout.CENTER);

        progressLabel = new JLabel(getProgressText(progress));
        barPanel.add(progressLabel, BorderLayout.EAST);

        progressPanel.add(barPanel);
        progressPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        return progressPanel;
    }

    private int calculateProgress() {
        if (truck != null && truck.getInventoryCell() != null) {
            StorageCell inv = truck.getInventoryCell();
            int max = inv.MAX_LENGTH * inv.MAX_WIDTH * inv.MAX_HEIGHT;
            return max > 0 ? (int) ((double) inv.getActualUsedVolume() / max * 100) : 0;
        }
        // Return 0% when no truck - don't use fake sample data
        return 0;
    }

    private String getProgressText(int progress) {
        if (truck != null && truck.getInventoryCell() != null) {
            StorageCell inv = truck.getInventoryCell();
            int max = inv.MAX_LENGTH * inv.MAX_WIDTH * inv.MAX_HEIGHT;
            return String.format("%d%% | %d/%d vol | %d boxes", progress, inv.getActualUsedVolume(), max, inv.getBoxCount());
        }
        // Show real data even when no truck assigned
        return progress + "% | Awaiting Truck";
    }



    public void updateFromBackend() {
        SwingUtilities.invokeLater(() -> {
            String newStatus = determineStatus();
            if (!newStatus.equals(status)) {
                status = newStatus;
                if (statusLabel != null) statusLabel.setText(status);
            }

            // Update AGV component visual state
            if (agv != null && agvComponent != null) {
                boolean agvHasCargo = agv.getInventoryCell() != null &&
                                     agv.getInventoryCell().getBoxCount() > 0;
                agvComponent.setHasCargo(agvHasCargo);
                agvComponent.setBatteryLevel(agv.getBatteryLevel());

                // Check AGV state to determine visibility
                AGV.AGVState agvState = agv.getState();

                // Get AGV's current position in the warehouse
                Point agvPosition = agv.getCurrentPosition();
                String agvPositionNotation = agvPosition != null ?
                    Storage.pointToNotation(agvPosition) : null;

                // AGV should only be visible in loading bay when:
                // 1. It's at the loading dock position (6D or 7D)
                // 2. It's NOT in any charging state
                boolean isAtLoadingDock = "6D".equals(agvPositionNotation) || "7D".equals(agvPositionNotation);
                boolean isNotCharging = agvState != AGV.AGVState.CHARGING &&
                                       agvState != AGV.AGVState.WAITING_FOR_CHARGE &&
                                       agvState != AGV.AGVState.MOVING_TO_CHARGE;

                if (isAtLoadingDock && isNotCharging && (agvState == AGV.AGVState.IDLE || agvState == AGV.AGVState.BUSY)) {
                    // AGV is at loading dock and ready to be shown in loading bay
                    if (!status.equals("Charging") && !status.equals("Loaded")) {
                        if (statusLabel != null) {
                            String stateText = agvState == AGV.AGVState.IDLE ? "Idle" : (agvHasCargo ? "Loading..." : "Preparing");
                            statusLabel.setText(stateText);
                            statusLabel.setForeground(agvState == AGV.AGVState.IDLE ?
                                new Color(100, 100, 100) : new Color(0, 150, 0));
                        }
                    }

                    // Show AGV and optionally animate if BUSY with cargo
                    if (agvState == AGV.AGVState.BUSY && agvHasCargo) {
                        LOGGER.debug("Bay {}: AGV {} is at loading dock {} (cargo: {}) - animating",
                            bayNumber, agv.getAgvId(), agvPositionNotation, agvHasCargo);
                        animateAGVToTruck();
                    } else {
                        // IDLE or BUSY without cargo - just show at starting position
                        if (agvComponent != null && !agvComponent.isVisible()) {
                            agvComponent.setVisible(true);
                            agvComponent.setBounds(agvStartX, 20, AGV_WIDTH, AGV_HEIGHT);
                            LOGGER.debug("Bay {}: AGV {} shown at loading dock (IDLE or no cargo)",
                                bayNumber, agv.getAgvId());
                        }
                    }
                } else {
                    // AGV is not at loading dock or is charging - hide from this bay
                    LOGGER.debug("Bay {}: AGV {} hidden (position: {}, state: {})",
                        bayNumber, agv.getAgvId(), agvPositionNotation, agvState);
                    resetAGVPosition();
                }
            }

            int progress = calculateProgress();
            if (progressBar != null) progressBar.setValue(progress);
            if (progressLabel != null) progressLabel.setText(getProgressText(progress));

            if (truck != null && truckComponent != null && truck.getInventoryCell() != null) {
                StorageCell inv = truck.getInventoryCell();
                int max = inv.MAX_LENGTH * inv.MAX_WIDTH * inv.MAX_HEIGHT;
                int used = inv.getActualUsedVolume();

                // Update truck visualization with current cargo
                truckComponent.setMaxCapacity(max / 1000);
                truckComponent.setCurrentLoad(used / 1000);
                truckComponent.setLoadProgress(max > 0 ? (double) used / max : 0.0);

                // Update progress display
                if (progressLabel != null) {
                    int boxes = inv.getBoxCount();
                    progressLabel.setText(String.format("%d%% | %d/%d m³ | %d boxes",
                        progress, used / 1000, max / 1000, boxes));
                }
            }
        });
    }



    /**
     * Animate AGV component moving horizontally from warehouse entrance (left) to truck (right)
     */
    private void animateAGVToTruck() {
        // Don't restart animation if it's already running
        if (agvAnimationTimer != null && agvAnimationTimer.isRunning()) {
            return;  // Animation already in progress
        }

        // Make AGV visible and reset to starting position
        agvCurrentX = agvStartX;
        if (agvComponent != null) {
            agvComponent.setVisible(true);
            agvComponent.setBounds(agvCurrentX, 20, AGV_WIDTH, AGV_HEIGHT);
        }

        // Setup progress bar animation
        progressStartValue = progressBar != null ? progressBar.getValue() : 0;
        // Estimate progress increase (each delivery adds ~10-15%)
        progressTargetValue = Math.min(100, progressStartValue + 12);

        LOGGER.debug("Bay {}: Starting AGV animation ({} -> {} px) and progress ({} -> {}%)",
            bayNumber, agvStartX, agvTargetX, progressStartValue, progressTargetValue);

        // Create animation timer: 30ms intervals, move 3 pixels per frame
        agvAnimationTimer = new Timer(30, e -> {
            // Check if progress bar reached 100% - stop animation and reset position
            if (progressBar != null && progressBar.getValue() >= 100) {
                agvAnimationTimer.stop();
                // Reset AGV to start position (left side) but keep visible
                agvCurrentX = agvStartX;
                if (agvComponent != null) {
                    agvComponent.setBounds(agvStartX, 20, AGV_WIDTH, AGV_HEIGHT);
                }
                if (visualPanel != null) {
                    visualPanel.repaint();
                }
                LOGGER.debug("Bay {}: Animation stopped at 100% - reset to start position", bayNumber);
                return;
            }

            if (agvCurrentX < agvTargetX) {
                // Move AGV 3 pixels to the right
                agvCurrentX += 3;

                // Update AGV position
                if (agvComponent != null) {
                    agvComponent.setBounds(agvCurrentX, 20, AGV_WIDTH, AGV_HEIGHT);
                }

                // Calculate proportional progress
                double travelRatio = (double)(agvCurrentX - agvStartX) / (agvTargetX - agvStartX);
                int currentProgress = (int)(progressStartValue + (progressTargetValue - progressStartValue) * travelRatio);

                // Update progress bar
                if (progressBar != null) {
                    progressBar.setValue(currentProgress);
                    if (progressLabel != null) {
                        progressLabel.setText(currentProgress + "%");
                    }
                }

                // Repaint
                if (visualPanel != null) {
                    visualPanel.repaint();
                }
            } else {
                // Animation complete - reached target position
                agvAnimationTimer.stop();
                LOGGER.debug("Bay {}: AGV animation complete", bayNumber);
            }
        });
        agvAnimationTimer.start();
    }

    /**
     * Reset AGV position and hide (called when AGV is charging or unavailable)
     */
    private void resetAGVPosition() {
        // Stop any running animation
        if (agvAnimationTimer != null && agvAnimationTimer.isRunning()) {
            agvAnimationTimer.stop();
        }

        // Reset position to starting point
        agvCurrentX = agvStartX;

        // Hide the AGV component
        if (agvComponent != null) {
            agvComponent.setVisible(false);
        }

        if (visualPanel != null) {
            visualPanel.repaint();
        }

        LOGGER.debug("Bay {}: AGV hidden (charging or unavailable)", bayNumber);
    }

    private void requestTruck() {
        if (JOptionPane.showConfirmDialog(this, "Request a new truck for Loading Bay " + bayNumber + "?",
                "Request Truck", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            recordEvent("Bay %d: Truck requested", bayNumber);
            showInfo("Truck request submitted for Bay %d\n\nTruck will arrive shortly.", bayNumber);
        }
    }

    private void showAGVDiagnostics() {
        if (agv == null) {
            showError("No AGV assigned to this bay");
            return;
        }
        String diag = String.format("AGV Diagnostics - %s\n\nBattery Level: %d%%\nState: %s\nMovement Speed: %d ticks/move\nCharging: %s",
            agv.getAgvId(), agv.getBatteryLevel(), agv.getState(), agv.getTicksPerMovement(),
            agv.getState() == AGV.AGVState.CHARGING ? "Yes" : "No");
        JOptionPane.showMessageDialog(this, diag, "AGV Diagnostics - " + agv.getAgvId(), JOptionPane.INFORMATION_MESSAGE);
        recordEvent("Bay %d: Diagnostics viewed for AGV %s", bayNumber, agv.getAgvId());
    }

    private void recordEvent(String format, Object... args) {
        if (observationSystem != null) {
            observationSystem.recordEvent("LOADING_BAY", String.format(format, args));
        }
    }

    private void showInfo(String format, Object... args) {
        JOptionPane.showMessageDialog(this, String.format(format, args), "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Public setters
    public void refresh() { updateFromBackend(); }
    public void setTask(Task task) { this.currentTask = task; updateFromBackend(); }
    public void setTruck(Truck truck) { 
        this.truck = truck; 
        // Update truck component if it exists
        if (truckComponent != null && truck != null && truck.getInventoryCell() != null) {
            StorageCell inv = truck.getInventoryCell();
            int maxVol = inv.MAX_LENGTH * inv.MAX_WIDTH * inv.MAX_HEIGHT;
            truckComponent.setMaxCapacity(maxVol / 1000);
            truckComponent.setCurrentLoad(inv.getActualUsedVolume() / 1000);
            truckComponent.setLoadProgress(maxVol > 0 ? (double) inv.getActualUsedVolume() / maxVol : 0.0);
        }
        updateFromBackend(); 
    }
    public void setAGV(AGV agv) { 
        this.agv = agv; 
        // Update AGV component with real backend AGV
        if (agvComponent != null && agv != null) {
            agvComponent.setBackendAGV(agv);
            LOGGER.info("LoadingBay {}: AGV {} connected to AGVComponent", bayNumber, agv.getAgvId());
        }
        updateFromBackend(); 
    }
    public void setTaskManagement(TaskManagement tm) { this.taskManagement = tm; }
    public void setObservationSystem(Observation obs) { this.observationSystem = obs; }
    public void setProgress(int progress, String label) { progressBar.setValue(progress); progressLabel.setText(label); }
    public void setStatus(String status) { this.status = status; statusLabel.setText(status); }
    
    // Getters
    public int getBayNumber() { return bayNumber; }
    public AGVComponent getAgvComponent() { return agvComponent; }
    public TruckComponent getTruckComponent() { return truckComponent; }
}