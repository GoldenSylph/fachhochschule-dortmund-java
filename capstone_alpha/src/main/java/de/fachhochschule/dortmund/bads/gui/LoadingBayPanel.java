package de.fachhochschule.dortmund.bads.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import de.fachhochschule.dortmund.bads.resources.AGV;
import de.fachhochschule.dortmund.bads.resources.Truck;
import de.fachhochschule.dortmund.bads.systems.logic.TaskManagement;
import de.fachhochschule.dortmund.bads.systems.logic.Observation;
import de.fachhochschule.dortmund.bads.systems.logic.utils.ITickable;

/**
 * Panel containing tabbed loading bays and AGV fleet status
 */
public class LoadingBayPanel extends JPanel implements ITickable {
	private static final long serialVersionUID = -8627091584477696665L;

	private JTabbedPane tabbedPane;
    private LoadingBayView[] bayViews;
    private JPanel fleetStatusPanel;
    private JLabel[] fleetStatusLabels;

    // Backend dependencies
    private List<AGV> agvFleet;
    private List<Truck> trucks;
    private TaskManagement taskManagement;
    private Observation observationSystem;

    public LoadingBayPanel() {
        initializeComponents();
    }

    /**
     * Set the task management system (dependency injection)
     */
    public void setTaskManagement(TaskManagement taskManagement) {
        this.taskManagement = taskManagement;
        // Inject into all bay views
        for (LoadingBayView bayView : bayViews) {
            if (bayView != null) {
                bayView.setTaskManagement(taskManagement);
            }
        }
    }

    /**
     * Set the observation system (dependency injection)
     */
    public void setObservationSystem(Observation observationSystem) {
        this.observationSystem = observationSystem;
        // Inject into all bay views
        for (LoadingBayView bayView : bayViews) {
            if (bayView != null) {
                bayView.setObservationSystem(observationSystem);
            }
        }
    }

    /**
     * Set the AGV fleet (dependency injection)
     */
    public void setAGVFleet(List<AGV> agvFleet) {
        this.agvFleet = agvFleet;
        updateBayViewsWithBackendData(); // Update views when fleet is set
    }

    /**
     * Set the truck list (dependency injection)
     */
    public void setTrucks(List<Truck> trucks) {
        this.trucks = trucks;
        updateBayViewsWithBackendData(); // Update views when trucks are set
    }

    @Override
    public void onTick(int currentTick) {
        // Update UI every 3 ticks to reduce overhead
        if (currentTick % 3 == 0) {
            SwingUtilities.invokeLater(this::refresh);
        }
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Automated Loading Bays - AGV System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(titleLabel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        bayViews = new LoadingBayView[3];

        // Create bay views with placeholder data initially
        // These will be updated when backend data is injected
        bayViews[0] = new LoadingBayView(1, "T-001", "Active");
        tabbedPane.addTab("[*] Bay 1", null, bayViews[0], "Loading Bay 1 - Active");
        tabbedPane.setForegroundAt(0, new Color(0, 180, 0));

        bayViews[1] = new LoadingBayView(2, "T-002", "Preparing");
        tabbedPane.addTab("[*] Bay 2", null, bayViews[1], "Loading Bay 2 - Preparing");
        tabbedPane.setForegroundAt(1, new Color(200, 180, 0));

        bayViews[2] = new LoadingBayView(3, null, "Idle");
        tabbedPane.addTab("[*] Bay 3", null, bayViews[2], "Loading Bay 3 - Idle");
        tabbedPane.setForegroundAt(2, new Color(200, 0, 0));

        add(tabbedPane, BorderLayout.CENTER);

        createFleetStatusPanel();
        add(fleetStatusPanel, BorderLayout.SOUTH);
    }

    private void createFleetStatusPanel() {
        fleetStatusPanel = new JPanel();
        fleetStatusPanel.setLayout(new BoxLayout(fleetStatusPanel, BoxLayout.Y_AXIS));
        fleetStatusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 193, 7)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        fleetStatusPanel.setBackground(new Color(255, 243, 205));

        JLabel fleetTitle = new JLabel("AGV Fleet Status:");
        fleetTitle.setFont(new Font("Arial", Font.BOLD, 11));
        fleetStatusPanel.add(fleetTitle);

        fleetStatusLabels = new JLabel[3]; // Support 3 AGVs to match loading bays
        for (int i = 0; i < fleetStatusLabels.length; i++) {
            fleetStatusLabels[i] = new JLabel("- AGV-" + (i + 1) + ": Initializing... | Battery: --% [BAT]");
            fleetStatusLabels[i].setFont(new Font("Arial", Font.PLAIN, 10));
            fleetStatusPanel.add(fleetStatusLabels[i]);
        }
    }

    private void updateBayViewsWithBackendData() {
        // Update as soon as we have at least AGVs or trucks (don't require both)
        if (agvFleet == null && trucks == null) return;

        SwingUtilities.invokeLater(() -> {
            // Assign AGVs and Trucks to bays
            for (int i = 0; i < bayViews.length; i++) {
                AGV agv = (agvFleet != null && i < agvFleet.size()) ? agvFleet.get(i) : null;
                Truck truck = (trucks != null && i < trucks.size()) ? trucks.get(i) : null;
                
                // Update even if only one is available
                bayViews[i].setAGV(agv);
                bayViews[i].setTruck(truck);
                
                // Ensure TaskManagement and Observation are injected
                if (taskManagement != null) {
                    bayViews[i].setTaskManagement(taskManagement);
                }
                if (observationSystem != null) {
                    bayViews[i].setObservationSystem(observationSystem);
                }
                
                bayViews[i].refresh();
                
                // Update tab color based on bay status
                updateTabColor(i, agv, truck);
            }
        });
    }

    /**
     * Update tab color based on bay activity
     */
    private void updateTabColor(int bayIndex, AGV agv, Truck truck) {
        Color color;
        String tooltip;
        
        if (truck != null && agv != null && agv.getState() == AGV.AGVState.BUSY) {
            color = new Color(0, 180, 0); // Green - Active
            tooltip = "Loading Bay " + (bayIndex + 1) + " - Active";
        } else if (truck != null || agv != null) {
            color = new Color(200, 180, 0); // Yellow - Preparing
            tooltip = "Loading Bay " + (bayIndex + 1) + " - Preparing";
        } else {
            color = new Color(200, 0, 0); // Red - Idle
            tooltip = "Loading Bay " + (bayIndex + 1) + " - Idle";
        }
        
        tabbedPane.setForegroundAt(bayIndex, color);
        tabbedPane.setToolTipTextAt(bayIndex, tooltip);
    }

    /**
     * Refresh loading bay and fleet status from backend state
     */
    public void refresh() {
        updateFleetStatus();
        updateBayStatuses();
    }

    private void updateFleetStatus() {
        if (agvFleet == null || agvFleet.isEmpty()) return;

        for (int i = 0; i < fleetStatusLabels.length; i++) {
            if (i < agvFleet.size()) {
                AGV agv = agvFleet.get(i);
                int battery = agv.getBatteryLevel();
                String status = getAGVStatusString(agv.getState());
                String batteryIcon = getBatteryIcon(battery);

                String statusText = String.format(
                    "- %s: %s | Battery: %d%% %s",
                    agv.getAgvId(), status, battery, batteryIcon
                );
                
                // Color code based on battery level
                Color textColor = battery < 30 ? new Color(200, 0, 0) : 
                                 battery < 60 ? new Color(200, 150, 0) : 
                                 new Color(0, 100, 0);
                fleetStatusLabels[i].setForeground(textColor);
                fleetStatusLabels[i].setText(statusText);
            } else {
                // Hide unused labels
                fleetStatusLabels[i].setText("");
            }
        }
    }

    private String getAGVStatusString(AGV.AGVState state) {
        return switch (state) {
            case IDLE -> "Idle";
            case BUSY -> "Active (Busy)";
            case WAITING_FOR_CHARGE -> "Waiting for Charge";
            case CHARGING -> "Charging";
            case MOVING_TO_CHARGE -> "Moving to Charge";
        };
    }

    private String getBatteryIcon(int battery) {
        if (battery > 60) return "[BAT]";
        if (battery > 30) return "[LOW]";
        return "[CHG]";
    }

    private void updateBayStatuses() {
        if (agvFleet == null) return;

        // Update each bay view and its tab color
        for (int i = 0; i < bayViews.length; i++) {
            bayViews[i].refresh();
            
            // Update tab color based on current state
            AGV agv = i < agvFleet.size() ? agvFleet.get(i) : null;
            Truck truck = (trucks != null && i < trucks.size()) ? trucks.get(i) : null;
            updateTabColor(i, agv, truck);
        }
    }
}