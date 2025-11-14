package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import java.awt.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        JPanel visualPanel = new JPanel();
        visualPanel.setLayout(new BoxLayout(visualPanel, BoxLayout.X_AXIS));
        visualPanel.setOpaque(false);
        visualPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        visualPanel.setBackground(Color.WHITE);

        // AGV Component - FULLY INTEGRATED with backend
        if (agv != null) {
            agvComponent = new AGVComponent(agv);  // Use backend AGV directly
            LOGGER.debug("LoadingBay {}: Created AGVComponent with backend AGV {}", bayNumber, agv.getAgvId());
        } else {
            int battery = bayNumber == 1 ? 85 : bayNumber == 2 ? 55 : 92;
            agvComponent = new AGVComponent("AGV-" + bayNumber, battery, bayNumber == 1);
            LOGGER.debug("LoadingBay {}: Created AGVComponent in standalone mode", bayNumber);
        }
        visualPanel.add(agvComponent);
        visualPanel.add(Box.createHorizontalGlue());

        JLabel arrowLabel = new JLabel("  ->  ");
        arrowLabel.setFont(new Font("Arial", Font.BOLD, 24));
        arrowLabel.setForeground(new Color(102, 102, 102));
        visualPanel.add(arrowLabel);
        visualPanel.add(Box.createHorizontalGlue());

        // Truck Component
        if (truck != null && truck.getInventoryCell() != null) {
            StorageCell inv = truck.getInventoryCell();
            truckComponent = new TruckComponent("T-00" + bayNumber, 
                inv.MAX_LENGTH * inv.MAX_WIDTH * inv.MAX_HEIGHT / 1000, 
                inv.getActualUsedVolume() / 1000);
            LOGGER.debug("LoadingBay {}: Created TruckComponent with backend Truck data", bayNumber);
        } else {
            // Create empty truck component - no fake sample data
            truckComponent = new TruckComponent("T-00" + bayNumber, 10, 0);
            LOGGER.debug("LoadingBay {}: Created TruckComponent in standalone mode", bayNumber);
        }
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
        progressPanel.add(createButtonPanel());

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

    private JPanel createButtonPanel() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttons.setOpaque(false);

        if ("Active".equals(status)) {
            JButton pauseBtn = new JButton("Pause AGV");
            pauseBtn.addActionListener(_ -> pauseAGV());
            buttons.add(pauseBtn);
            JButton completeBtn = new JButton("Complete");
            completeBtn.addActionListener(_ -> completeLoading());
            buttons.add(completeBtn);
        } else if ("Preparing".equals(status)) {
            JButton startBtn = new JButton("Start Loading");
            startBtn.addActionListener(_ -> startLoading());
            buttons.add(startBtn);
            JButton assignBtn = new JButton("Assign Order");
            assignBtn.addActionListener(_ -> assignOrder());
            buttons.add(assignBtn);
        } else {
            JButton requestBtn = new JButton("Request Truck");
            requestBtn.addActionListener(_ -> requestTruck());
            buttons.add(requestBtn);
            JButton diagBtn = new JButton("AGV Diagnostics");
            diagBtn.addActionListener(_ -> showAGVDiagnostics());
            buttons.add(diagBtn);
        }
        return buttons;
    }

    public void updateFromBackend() {
        SwingUtilities.invokeLater(() -> {
            String newStatus = determineStatus();
            if (!newStatus.equals(status)) {
                status = newStatus;
                if (statusLabel != null) statusLabel.setText(status);
            }
            
            int progress = calculateProgress();
            if (progressBar != null) progressBar.setValue(progress);
            if (progressLabel != null) progressLabel.setText(getProgressText(progress));
            
            if (truck != null && truckComponent != null && truck.getInventoryCell() != null) {
                StorageCell inv = truck.getInventoryCell();
                int max = inv.MAX_LENGTH * inv.MAX_WIDTH * inv.MAX_HEIGHT;
                truckComponent.setLoadProgress(max > 0 ? (double) inv.getActualUsedVolume() / max : 0.0);
            }
        });
    }

    private void pauseAGV() {
        if (agv == null) {
            showError("No AGV assigned to this bay");
            return;
        }
        int curr = agv.getTicksPerMovement();
        agv.setTicksPerMovement(curr * 2);
        recordEvent("Bay %d: AGV %s slowed down (ticks/move: %d -> %d)", bayNumber, agv.getAgvId(), curr, curr * 2);
        showInfo("AGV %s operations slowed down", agv.getAgvId());
    }

    private void completeLoading() {
        if (truck == null || currentTask == null) {
            showError("No active loading task or truck at this bay");
            return;
        }
        Thread.State state = currentTask.getState();
        if (state != Thread.State.RUNNABLE && state != Thread.State.BLOCKED) {
            showWarning("Task is not in running state");
            return;
        }
        
        recordEvent("Bay %d: Loading completed for Task T-%d", bayNumber, currentTask.getTaskId());
        if (JOptionPane.showConfirmDialog(this, "Complete loading for Task T-" + currentTask.getTaskId() + "?\n\nTruck will depart.",
                "Complete Loading", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            currentTask = null;
            truck = null;
            status = determineStatus();
            updateFromBackend();
            showInfo("Loading completed successfully!\nTruck has departed.");
        }
    }

    private void startLoading() {
        if (currentTask == null || truck == null) {
            showError("No task or truck assigned to this bay");
            return;
        }
        if (currentTask.getState() != Thread.State.NEW) {
            showWarning("Task has already been started");
            return;
        }
        currentTask.start();
        recordEvent("Bay %d: Loading started for Task T-%d", bayNumber, currentTask.getTaskId());
        status = "Active";
        updateFromBackend();
        showInfo("Loading started for Task T-%d", currentTask.getTaskId());
    }

    private void assignOrder() {
        if (taskManagement == null) {
            showError("Task management system not connected");
            return;
        }
        var tasks = taskManagement.getAllTasks().stream()
            .filter(t -> t.getState() == Thread.State.NEW)
            .toArray(Task[]::new);
        
        if (tasks.length == 0) {
            showInfo("No pending tasks available to assign");
            return;
        }
        
        String[] opts = java.util.Arrays.stream(tasks)
            .map(t -> String.format("T-%d (Priority: %d, Processes: %d)", t.getTaskId(), t.getTaskPriority(), t.getProcessCount()))
            .toArray(String[]::new);
        
        String sel = (String) JOptionPane.showInputDialog(this, "Select a task to assign to Bay " + bayNumber + ":",
            "Assign Order", JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
        
        if (sel != null) {
            int taskId = Integer.parseInt(sel.substring(2, sel.indexOf(" ")));
            currentTask = tasks[java.util.Arrays.asList(tasks).stream()
                .map(Task::getTaskId).toList().indexOf(taskId)];
            status = "Preparing";
            recordEvent("Bay %d: Task T-%d assigned", bayNumber, taskId);
            updateFromBackend();
            showInfo("Task T-%d assigned to Bay %d", taskId, bayNumber);
        }
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