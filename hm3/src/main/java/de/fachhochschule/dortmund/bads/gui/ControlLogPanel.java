package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Panel for simulation controls and system log display
 *
 * INTEGRATION POINTS:
 * - Connect Start/Pause/Stop buttons to ClockingSimulation methods
 * - Speed slider should update ClockingSimulation.setDelay()
 * - Log area should receive events from Observation system
 */
public class ControlLogPanel extends JPanel {

    private JButton startButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JRadioButton autoModeRadio;
    private JRadioButton manualModeRadio;
    private JSlider speedSlider;
    private JTextArea logArea;
    private JScrollPane logScrollPane;

    private boolean isRunning = false;

    // TODO [CONCURRENCY]: Uncomment to receive system references
    // private ClockingSimulation clockingSystem;
    // private Observation observationSystem;

    public ControlLogPanel() {
        // TODO [CONCURRENCY]: Uncomment to receive system references
        /* public ControlLogPanel(ClockingSimulation clockingSystem, Observation observationSystem) {
             this.clockingSystem = clockingSystem;
             this.observationSystem = observationSystem;
             initializeComponents();
             setupObservationListener();
         }*/

        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Title
        JLabel titleLabel = new JLabel("System Control & Log");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(titleLabel, BorderLayout.NORTH);

        // Control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.WEST);

        // Log panel
        JPanel logPanel = createLogPanel();
        add(logPanel, BorderLayout.CENTER);
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Simulation control group
        JPanel simControlGroup = new JPanel();
        simControlGroup.setBorder(BorderFactory.createTitledBorder("Simulation Control"));
        simControlGroup.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        startButton = new JButton("▶ Start");
        startButton.setPreferredSize(new Dimension(90, 25));
        // TODO [CONTROL-BINDING]: Uncomment to start simulation
        // startButton.addActionListener(e -> startSimulation());
        simControlGroup.add(startButton);

        pauseButton = new JButton("⏸ Pause");
        pauseButton.setPreferredSize(new Dimension(90, 25));
        pauseButton.setEnabled(false);
        // TODO [CONTROL-BINDING]: Uncomment to pause simulation
        // pauseButton.addActionListener(e -> pauseSimulation());
        simControlGroup.add(pauseButton);

        stopButton = new JButton("■ Stop");
        stopButton.setPreferredSize(new Dimension(90, 25));
        stopButton.setEnabled(false);
        // TODO [CONTROL-BINDING]: Uncomment to stop simulation
        // stopButton.addActionListener(e -> stopSimulation());
        simControlGroup.add(stopButton);

        controlPanel.add(simControlGroup);

        // Mode selection group
        JPanel modeGroup = new JPanel();
        modeGroup.setBorder(BorderFactory.createTitledBorder("Mode"));
        modeGroup.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        ButtonGroup modeButtonGroup = new ButtonGroup();
        autoModeRadio = new JRadioButton("Auto", true);
        manualModeRadio = new JRadioButton("Manual");
        modeButtonGroup.add(autoModeRadio);
        modeButtonGroup.add(manualModeRadio);

        // TODO [CONTROL-BINDING]: Uncomment to handle mode changes
        // autoModeRadio.addActionListener(e -> setSimulationMode(true));
        // manualModeRadio.addActionListener(e -> setSimulationMode(false));

        modeGroup.add(autoModeRadio);
        modeGroup.add(manualModeRadio);

        controlPanel.add(modeGroup);

        // Speed control group
        JPanel speedGroup = new JPanel();
        speedGroup.setBorder(BorderFactory.createTitledBorder("Speed"));
        speedGroup.setLayout(new BorderLayout(5, 5));

        speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        speedSlider.setPreferredSize(new Dimension(120, 40));
        speedSlider.setMajorTickSpacing(25);
        speedSlider.setMinorTickSpacing(5);
        speedSlider.setPaintTicks(true);

        // TODO [CONTROL-BINDING]: Uncomment to update simulation speed
        /* speedSlider.addChangeListener(e -> {
             if (!speedSlider.getValueIsAdjusting()) {
                 updateSimulationSpeed(speedSlider.getValue());
             }
         });*/

        speedGroup.add(speedSlider, BorderLayout.CENTER);

        controlPanel.add(speedGroup);

        return controlPanel;
    }

    private JPanel createLogPanel() {
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JLabel logLabel = new JLabel("System Log:");
        logPanel.add(logLabel, BorderLayout.NORTH);

        logArea = new JTextArea(4, 80);
        logArea.setEditable(false);
        logArea.setFont(new Font("Courier New", Font.PLAIN, 10));
        logArea.setBackground(Color.WHITE);

        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        logPanel.add(logScrollPane, BorderLayout.CENTER);

        // Add sample log entries
        addSampleLogEntries();

        return logPanel;
    }

    private void addSampleLogEntries() {
        appendLog("[10:23:15] System initialized - BADS v2.0");
        appendLog("[10:23:16] Connected to warehouse database");
        appendLog("[10:23:18] AGV Fleet Status: 4 vehicles online");
        appendLog("[10:23:18] Charging Station: 3 slots available");
        appendLog("[10:23:18] Loading bay 1, 2, 3 activated");
        appendLog("[10:23:20] Order ORD-001 assigned to Truck T-001 at Bay 1");
        appendLog("[10:23:22] AGV-1 (Battery: 85%) dispatched to grid B01");
        appendLog("[10:23:28] AGV-1 loaded - transporting to Bay 1");
        appendLog("[10:23:35] AGV-1 unloading into Truck T-001");
        appendLog("[10:23:40] Loading progress Bay 1: 65% (3250kg/5000kg)");
        appendLog("[10:23:42] AGV-4 battery critical (35%) - moving to charging station");
        appendLog("[10:23:45] AGV-2 (Battery: 55%) standby at Bay 2");
        appendLog("[10:23:50] AGV-3 (Battery: 92%) idle at Bay 3");
        appendLog("[10:23:55] AGV-4 charging initiated at Slot 2");

        // TODO [OBSERVABILITY]: Remove sample entries and connect to real Observation system
        // setupObservationListener();
    }

    // TODO [OBSERVABILITY]: Uncomment to set up observation listener
    /* private void setupObservationListener() {
         if (observationSystem != null) {
             // Register a listener for observation events
             // The specific mechanism depends on your Observation implementation
             // Example: observationSystem.registerListener(this::handleObservationEvent);
         }
     }*/

    // TODO [OBSERVABILITY]: Uncomment to handle observation events
    /* private void handleObservationEvent(Observation.ObservationData data) {
         String logMessage = String.format("[Tick %d] %s: %s",
             data.clockingTime(),
             data.eventType(),
             data.details()
         );
         appendLog(logMessage);
     }*/

    // TODO [CONTROL-BINDING]: Uncomment to start simulation
    /* private void startSimulation() {
         if (clockingSystem != null && !isRunning) {
             clockingSystem.toggleClocking(); // Resume if paused
              isRunning = true;
     
              startButton.setEnabled(false);
              pauseButton.setEnabled(true);
              stopButton.setEnabled(true);
     
              appendLog(getCurrentTimestamp() + " Simulation started");
          }
     }*/

    // TODO [CONTROL-BINDING]: Uncomment to pause simulation
    /* private void pauseSimulation() {
         if (clockingSystem != null && isRunning) {
             clockingSystem.toggleClocking(); // Pause
             isRunning = false;
    
             startButton.setEnabled(true);
             pauseButton.setEnabled(false);
    
             appendLog(getCurrentTimestamp() + " Simulation paused");
         }
     }*/

    // TODO [CONTROL-BINDING]: Uncomment to stop simulation
    /* private void stopSimulation() {
         if (clockingSystem != null) {
             clockingSystem.stopSimulation();
             isRunning = false;
    
             startButton.setEnabled(true);
             pauseButton.setEnabled(false);
             stopButton.setEnabled(false);
    
             appendLog(getCurrentTimestamp() + " Simulation stopped");
         }
     }*/

    // TODO [CONTROL-BINDING]: Uncomment to set simulation mode
    /* private void setSimulationMode(boolean autoMode) {
         // Implement mode switching logic
         // Auto mode: AGVs work automatically
         // Manual mode: Require user intervention for each step
         appendLog(getCurrentTimestamp() + " Simulation mode: " + (autoMode ? "Auto" : "Manual"));
     }*/

    // TODO [CONTROL-BINDING]: Uncomment to update simulation speed
    /* private void updateSimulationSpeed(int sliderValue) {
         if (clockingSystem != null) {
             // Convert slider value (0-100) to delay in milliseconds
             // Higher slider value = faster simulation = lower delay
             // Slider 0 = 2000ms delay (slow), Slider 100 = 100ms delay (fast)
             int delay = 2000 - (sliderValue * 19);
             clockingSystem.setDelay(Math.max(100, delay));
    
             appendLog(getCurrentTimestamp() + " Simulation speed adjusted: " + sliderValue + "%");
         }
     }*/

    /**
     * Appends a log message to the log area with automatic scrolling
     */
    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Clears all log entries
     */
    public void clearLog() {
        SwingUtilities.invokeLater(() -> logArea.setText(""));
    }

    private String getCurrentTimestamp() {
        return "[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "]";
    }

    // Getters
    public JTextArea getLogArea() {
        return logArea;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
