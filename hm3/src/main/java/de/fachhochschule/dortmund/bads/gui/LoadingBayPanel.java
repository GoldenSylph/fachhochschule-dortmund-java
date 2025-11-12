package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Panel containing tabbed loading bays and AGV fleet status
 *
 * INTEGRATION POINTS:
 * - Should receive list of AGV and Truck instances
 * - Implement ITickable to update bay states
 * - Update fleet status from actual AGV states
 */
public class LoadingBayPanel extends JPanel /* implements ITickable */ {

    private JTabbedPane tabbedPane;
    private LoadingBayView[] bayViews;
    private JPanel fleetStatusPanel;
    private JLabel[] fleetStatusLabels;

    // TODO [CONCURRENCY]: Uncomment to receive backend instances
    // private List<AGV> agvFleet;
    // private List<Truck> trucks;

    public LoadingBayPanel() {
        // TODO [CONCURRENCY]: Uncomment to receive AGV and Truck lists
        /* public LoadingBayPanel(List<AGV> agvFleet, List<Truck> trucks) {
             this.agvFleet = agvFleet;
             this.trucks = trucks;
             initializeComponents();
         } */

        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Automated Loading Bays - AGV System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(titleLabel, BorderLayout.NORTH);

        // Tabbed pane for 3 bays
        tabbedPane = new JTabbedPane();
        bayViews = new LoadingBayView[3];

        // Bay 1 - Active
        bayViews[0] = new LoadingBayView(1, "T-001", "Active");
        tabbedPane.addTab("● Bay 1", null, bayViews[0], "Loading Bay 1 - Active");
        tabbedPane.setForegroundAt(0, new Color(0, 180, 0));

        // Bay 2 - Preparing
        bayViews[1] = new LoadingBayView(2, "T-002", "Preparing");
        tabbedPane.addTab("● Bay 2", null, bayViews[1], "Loading Bay 2 - Preparing");
        tabbedPane.setForegroundAt(1, new Color(200, 180, 0));

        // Bay 3 - Idle
        bayViews[2] = new LoadingBayView(3, null, "Idle");
        tabbedPane.addTab("● Bay 3", null, bayViews[2], "Loading Bay 3 - Idle");
        tabbedPane.setForegroundAt(2, new Color(200, 0, 0));

        add(tabbedPane, BorderLayout.CENTER);

        // AGV Fleet status at bottom
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

        // Fleet status labels
        fleetStatusLabels = new JLabel[4];
        String[] statuses = {
            "• AGV-1: Active (Bay 1) | Battery: 85% 🔋",
            "• AGV-2: Standby (Bay 2) | Battery: 55% ⚠️",
            "• AGV-3: Idle (Bay 3) | Battery: 92% 🔋",
            "• AGV-4: Charging (Station) | Battery: 35% ⚡"
        };

        for (int i = 0; i < 4; i++) {
            fleetStatusLabels[i] = new JLabel(statuses[i]);
            fleetStatusLabels[i].setFont(new Font("Arial", Font.PLAIN, 10));
            fleetStatusPanel.add(fleetStatusLabels[i]);
        }
    }

    // TODO [STATE-ACCESS]: Uncomment to update fleet status from real AGVs
    /* private void updateFleetStatus() {
         if (agvFleet == null || agvFleet.isEmpty()) return;
    
         SwingUtilities.invokeLater(() -> {
             for (int i = 0; i < Math.min(agvFleet.size(), fleetStatusLabels.length); i++) {
                 AGV agv = agvFleet.get(i);
                 int battery = agv.getBatteryLevel();
                 String status = determineAGVStatus(agv);
                 String location = determineAGVLocation(agv);
                 String batteryIcon = getBatteryIcon(battery);
    
                 String statusText = String.format(
                     "• AGV-%d: %s (%s) | Battery: %d%% %s",
                     i + 1, status, location, battery, batteryIcon
                 );
                 fleetStatusLabels[i].setText(statusText);
             }
         });
     } */

    /* private String determineAGVStatus(AGV agv) {
         if (agv.isCharging()) return "Charging";
         if (agv.isMoving()) return "Active";
         if (!agv.getInventoryCell().isEmpty()) return "Standby";
         return "Idle";
     } */

    /* private String determineAGVLocation(AGV agv) {
         // Determine which bay or station the AGV is at
         // This depends on your Storage/Area implementation
         return "Unknown";
     } */

    /* private String getBatteryIcon(int battery) {
         if (battery > 60) return "🔋";
         if (battery > 30) return "⚠️";
         return "⚡";
     } */

    // TODO [STATE-ACCESS]: Uncomment to update bay status from backend
    /* private void updateBayStatuses() {
         if (trucks == null) return;
    
         SwingUtilities.invokeLater(() -> {
             for (int i = 0; i < Math.min(trucks.size(), bayViews.length); i++) {
                 Truck truck = trucks.get(i);
                 bayViews[i].updateFromBackend();
    
                 // Update tab status indicator
                 String status = determineBayStatus(truck);
                 updateTabIndicator(i, status);
             }
         });
     } */

    /* private String determineBayStatus(Truck truck) {
         if (truck.hasReachedDestination()) return "Idle";
         if (truck.getInventoryCell().getBoxCount() == 0) return "Preparing";
         return "Active";
     } */

    /* private void updateTabIndicator(int bayIndex, String status) {
         Color color = switch (status) {
             case "Active" -> new Color(0, 180, 0);
             case "Preparing" -> new Color(200, 180, 0);
             default -> new Color(200, 0, 0);
         };
         tabbedPane.setForegroundAt(bayIndex, color);
     } */

    // TODO [TICK-LISTENER]: Uncomment to receive simulation tick updates
    /* @Override
     public void onTick(int currentTick) {
         updateFleetStatus();
         updateBayStatuses();
    
         // Update individual bay views
         for (LoadingBayView bay : bayViews) {
             bay.updateFromBackend();
         }
     } */

    // Getters
    public LoadingBayView[] getBayViews() {
        return bayViews;
    }

    public LoadingBayView getBayView(int bayNumber) {
        if (bayNumber >= 1 && bayNumber <= bayViews.length) {
            return bayViews[bayNumber - 1];
        }
        return null;
    }

    public void selectBay(int bayNumber) {
        if (bayNumber >= 1 && bayNumber <= bayViews.length) {
            tabbedPane.setSelectedIndex(bayNumber - 1);
        }
    }
}
