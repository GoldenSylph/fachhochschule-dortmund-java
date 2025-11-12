package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import java.awt.*;

/**
 * View for a single loading bay showing AGV, truck, and loading progress
 *
 * INTEGRATION POINTS:
 * - Receive AGV and Truck instances for this bay
 * - Update progress bar from actual loading status
 * - Control buttons should trigger backend operations
 */
public class LoadingBayView extends JPanel {

    private int bayNumber;
    private String status;  // "Active", "Preparing", "Idle"
    private AGVComponent agvComponent;
    private TruckComponent truckComponent;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel progressLabel;
    private JPanel buttonPanel;

    // TODO [CONCURRENCY]: Uncomment to receive backend instances
    // private AGV agv;
    // private Truck truck;

    public LoadingBayView(int bayNumber, String truckId, String status) {
        // TODO [CONCURRENCY]: Uncomment to use real instances
        /* public LoadingBayView(int bayNumber, AGV agv, Truck truck) {
             this.bayNumber = bayNumber;
             this.agv = agv;
             this.truck = truck;
             this.status = determineStatus();
             initializeComponents();
         } */

        this.bayNumber = bayNumber;
        this.status = status;

        initializeComponents(truckId);
    }

    private void initializeComponents(String truckId) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(153, 153, 153), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(new Color(245, 245, 245));

        // Header with bay name and status
        JPanel headerPanel = createHeader(truckId);
        add(headerPanel, BorderLayout.NORTH);

        // Visualization area (AGV -> arrow -> Truck)
        JPanel visualPanel = createVisualizationPanel();
        add(visualPanel, BorderLayout.CENTER);

        // Progress section
        JPanel progressPanel = createProgressPanel();
        add(progressPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeader(String truckId) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // Title
        JLabel titleLabel = new JLabel("Loading Bay " + bayNumber +
            (truckId != null ? " - Truck " + truckId : ""));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(new Color(0, 0, 128));
        header.add(titleLabel, BorderLayout.WEST);

        // Status indicator
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        statusPanel.setOpaque(false);

        JLabel statusDot = new JLabel("●");
        statusDot.setFont(new Font("Arial", Font.PLAIN, 16));
        Color dotColor = switch (status) {
            case "Active" -> new Color(0, 255, 0);
            case "Preparing" -> new Color(255, 255, 0);
            default -> new Color(255, 0, 0);
        };
        statusDot.setForeground(dotColor);
        statusPanel.add(statusDot);

        statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusPanel.add(statusLabel);

        header.add(statusPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createVisualizationPanel() {
        JPanel visualPanel = new JPanel();
        visualPanel.setLayout(new BoxLayout(visualPanel, BoxLayout.X_AXIS));
        visualPanel.setOpaque(false);
        visualPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        visualPanel.setBackground(Color.WHITE);

        // Initialize components with sample data
        // TODO [CONCURRENCY]: Replace with actual AGV data
        String agvId = "AGV-" + bayNumber;
        int battery = switch (bayNumber) {
            case 1 -> 85;
            case 2 -> 55;
            default -> 92;
        };
        boolean hasCargo = bayNumber == 1;

        agvComponent = new AGVComponent(agvId, battery, hasCargo);
        visualPanel.add(agvComponent);

        visualPanel.add(Box.createHorizontalGlue());

        // Arrow indicator
        JLabel arrowLabel = new JLabel("  →  ");
        arrowLabel.setFont(new Font("Arial", Font.BOLD, 24));
        arrowLabel.setForeground(new Color(102, 102, 102));
        visualPanel.add(arrowLabel);

        visualPanel.add(Box.createHorizontalGlue());

        // Truck component
        int currentLoad = switch (bayNumber) {
            case 1 -> 6;
            case 2 -> 2;
            default -> 0;
        };
        truckComponent = new TruckComponent("T-00" + bayNumber, 10, currentLoad);
        visualPanel.add(truckComponent);

        return visualPanel;
    }

    private JPanel createProgressPanel() {
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setOpaque(false);

        // Progress bar section
        JPanel barPanel = new JPanel(new BorderLayout(5, 0));
        barPanel.setOpaque(false);

        JLabel progressTitleLabel = new JLabel("Progress:");
        barPanel.add(progressTitleLabel, BorderLayout.WEST);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        int progress = switch (bayNumber) {
            case 1 -> 65;
            case 2 -> 20;
            default -> 0;
        };
        progressBar.setValue(progress);
        barPanel.add(progressBar, BorderLayout.CENTER);

        String progressText = switch (bayNumber) {
            case 1 -> progress + "% | 3250/5000 kg";
            case 2 -> progress + "% | 980/4500 kg";
            default -> progress + "% | Awaiting Truck";
        };
        progressLabel = new JLabel(progressText);
        barPanel.add(progressLabel, BorderLayout.EAST);

        progressPanel.add(barPanel);
        progressPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Button panel
        buttonPanel = createButtonPanel();
        progressPanel.add(buttonPanel);

        return progressPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttons.setOpaque(false);

        if ("Active".equals(status)) {
            JButton pauseButton = new JButton("Pause AGV");
            // TODO [CONTROL-BINDING]: Uncomment to pause AGV
            // pauseButton.addActionListener(e -> pauseAGV());
            buttons.add(pauseButton);

            JButton completeButton = new JButton("Complete");
            // TODO [CONTROL-BINDING]: Uncomment to complete loading
            // completeButton.addActionListener(e -> completeLoading());
            buttons.add(completeButton);
        } else if ("Preparing".equals(status)) {
            JButton startButton = new JButton("Start Loading");
            // TODO [CONTROL-BINDING]: Uncomment to start loading
            // startButton.addActionListener(e -> startLoading());
            buttons.add(startButton);

            JButton assignButton = new JButton("Assign Order");
            // TODO [CONTROL-BINDING]: Uncomment to assign order
            // assignButton.addActionListener(e -> assignOrder());
            buttons.add(assignButton);
        } else {
            JButton requestButton = new JButton("Request Truck");
            // TODO [CONTROL-BINDING]: Uncomment to request truck
            // requestButton.addActionListener(e -> requestTruck());
            buttons.add(requestButton);

            JButton diagnosticsButton = new JButton("AGV Diagnostics");
            // TODO [OBSERVABILITY]: Uncomment to show AGV diagnostics
            // diagnosticsButton.addActionListener(e -> showAGVDiagnostics());
            buttons.add(diagnosticsButton);
        }

        return buttons;
    }

    // TODO [CONTROL-BINDING]: Uncomment control methods
    /* private void pauseAGV() {
         if (agv != null) {
             // Pause AGV operations
             // Send command through backend system
         }
     } 
    
    private void completeLoading() {
         if (truck != null) {
             // Mark loading as complete
             // Update task status in TaskManagement
         }
     } 
    
     private void startLoading() {
         // Start loading process
         // Create task and assign to AGV
     }
    
     private void assignOrder() {
         // Show dialog to select order
         // Assign selected order to this bay
     }
    
     private void requestTruck() {
         // Request new truck for this bay
         // Create truck instance and assign
     }
    
     private void showAGVDiagnostics() {
         if (agv != null) {
             String diagnostics = String.format(
                 "AGV Diagnostics\n\nBattery: %d%%\nPosition: %s\nCargo: %s\nStatus: %s",
                 agv.getBatteryLevel(),
                 agv.getCurrentPosition(),
                 agv.getInventoryCell().isEmpty() ? "Empty" : "Loaded",
                 agv.isCharging() ? "Charging" : "Operational"
             );
             JOptionPane.showMessageDialog(this, diagnostics, "AGV Diagnostics", JOptionPane.INFORMATION_MESSAGE);
         }
     } */

    // TODO [STATE-ACCESS]: Uncomment to update from backend state
    /* public void updateFromBackend() {
         SwingUtilities.invokeLater(() -> {
             if (agv != null) {
                 agvComponent.updateFromAGV();
             }
             if (truck != null) {
                 truckComponent.updateFromTruck();
                 updateProgressBar();
             }
         });
     } 
    
     private void updateProgressBar() {
         if (truck != null && truck.getInventoryCell() != null) {
             StorageCell inventory = truck.getInventoryCell();
             int maxVolume = inventory.MAX_LENGTH * inventory.MAX_WIDTH * inventory.MAX_HEIGHT;
             int currentVolume = inventory.getCurrentLength() * inventory.getCurrentWidth() * inventory.getCurrentHeight();
             int progress = maxVolume > 0 ? (currentVolume * 100) / maxVolume : 0;
    
             progressBar.setValue(progress);
             progressLabel.setText(String.format("%d%% | %d/%d units", progress, inventory.getBoxCount(), maxVolume / 100));
         }
     } */

    // Setters for manual updates (used when backend not connected)
    public void setProgress(int progress, String label) {
        progressBar.setValue(progress);
        progressLabel.setText(label);
    }

    public void setStatus(String status) {
        this.status = status;
        statusLabel.setText(status);
    }

    // Getters
    public int getBayNumber() {
        return bayNumber;
    }

    public AGVComponent getAgvComponent() {
        return agvComponent;
    }

    public TruckComponent getTruckComponent() {
        return truckComponent;
    } 
}
