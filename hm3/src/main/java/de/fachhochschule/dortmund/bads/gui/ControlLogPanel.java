package de.fachhochschule.dortmund.bads.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import de.fachhochschule.dortmund.bads.systems.logic.ClockingSimulation;
import de.fachhochschule.dortmund.bads.systems.logic.Observation;
import de.fachhochschule.dortmund.bads.systems.logic.utils.ITickable;

/**
 * Panel for simulation controls and system log display
 */
public class ControlLogPanel extends JPanel implements ITickable {
	private static final long serialVersionUID = -439577232890959349L;
	
	private JButton startButton;
	private JButton pauseButton;
	private JButton stopButton;
	private JSlider speedSlider;
	private JTextArea logArea;
	private JScrollPane logScrollPane;

	// Backend dependencies
	private ClockingSimulation clockingSystem;
	private Observation observationSystem;
	
	private int lastEventCount = 0;

	/**
	 * Constructor with system dependencies (used by MainFrame)
	 */
	public ControlLogPanel(ClockingSimulation clockingSystem, Observation observationSystem) {
		this.clockingSystem = clockingSystem;
		this.observationSystem = observationSystem;
		initializeComponents();
		
		// Register this panel as a tickable to receive updates
		if (clockingSystem != null) {
			clockingSystem.registerTickable(this);
		}
	}

	/**
	 * Default constructor for standalone testing
	 */
	public ControlLogPanel() {
		initializeComponents();
	}

	@Override
	public void onTick(int currentTick) {
		// Update UI every 10 ticks to reduce overhead
		if (currentTick % 10 == 0) {
			SwingUtilities.invokeLater(() -> {
				refresh();
				updateLogFromObservation();
			});
		}
	}

	private void initializeComponents() {
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));

		JLabel titleLabel = new JLabel("System Control & Log");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
		add(titleLabel, BorderLayout.NORTH);

		JPanel controlPanel = createControlPanel();
		add(controlPanel, BorderLayout.WEST);

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

		startButton = new JButton("> Start");
		startButton.setPreferredSize(new Dimension(90, 25));
		startButton.addActionListener(_ -> startSimulation());
		simControlGroup.add(startButton);

		pauseButton = new JButton("|| Pause");
		pauseButton.setPreferredSize(new Dimension(90, 25));
		pauseButton.setEnabled(false);
		pauseButton.addActionListener(_ -> pauseSimulation());
		simControlGroup.add(pauseButton);

		stopButton = new JButton("[] Stop");
		stopButton.setPreferredSize(new Dimension(90, 25));
		stopButton.setEnabled(false);
		stopButton.addActionListener(_ -> stopSimulation());
		simControlGroup.add(stopButton);

		controlPanel.add(simControlGroup);

		// Speed control group
		JPanel speedGroup = new JPanel();
		speedGroup.setBorder(BorderFactory.createTitledBorder("Speed"));
		speedGroup.setLayout(new BorderLayout(5, 5));

		speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
		speedSlider.setPreferredSize(new Dimension(120, 40));
		speedSlider.setMajorTickSpacing(25);
		speedSlider.setMinorTickSpacing(5);
		speedSlider.setPaintTicks(true);

		speedSlider.addChangeListener(_ -> {
			if (!speedSlider.getValueIsAdjusting()) {
				updateSimulationSpeed(speedSlider.getValue());
			}
		});

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

		appendLog("System initialized - BADS v2.0");
		appendLog("Waiting for simulation to start...");

		return logPanel;
	}

	/**
	 * Start the simulation
	 */
	private void startSimulation() {
		if (clockingSystem == null) {
			appendLog("ERROR: Clocking system not connected");
			return;
		}

		if (!clockingSystem.isRunning()) {
			clockingSystem.toggleClocking();
			
			// Start observation system if available
			if (observationSystem != null && !observationSystem.isAlive()) {
				observationSystem.start();
				appendLog("Observation system started");
			}
			
			startButton.setEnabled(false);
			pauseButton.setEnabled(true);
			stopButton.setEnabled(true);
			appendLog("> Simulation started");
			
			// Record event in observation system
			if (observationSystem != null) {
				observationSystem.recordEvent("SIMULATION", "Simulation started by user");
			}
		}
	}

	/**
	 * Pause/Resume the simulation
	 */
	private void pauseSimulation() {
		if (clockingSystem == null) {
			appendLog("ERROR: Clocking system not connected");
			return;
		}

		clockingSystem.toggleClocking();
		boolean running = clockingSystem.isRunning();
		
		startButton.setEnabled(!running);
		pauseButton.setEnabled(running);
		
		if (running) {
			pauseButton.setText("|| Pause");
			appendLog("> Simulation resumed");
			
			// Record event in observation system
			if (observationSystem != null) {
				observationSystem.recordEvent("SIMULATION", "Simulation resumed by user");
			}
		} else {
			pauseButton.setText("> Resume");
			appendLog("|| Simulation paused");
			
			// Record event in observation system
			if (observationSystem != null) {
				observationSystem.recordEvent("SIMULATION", "Simulation paused by user");
			}
		}
	}

	/**
	 * Stop the simulation
	 */
	private void stopSimulation() {
		if (clockingSystem == null) {
			appendLog("ERROR: Clocking system not connected");
			return;
		}

		clockingSystem.stopSimulation();
		
		// Stop observation system if available
		if (observationSystem != null) {
			observationSystem.stopSystem();
			appendLog("Observation system stopped");
		}
		
		startButton.setEnabled(true);
		pauseButton.setEnabled(false);
		pauseButton.setText("|| Pause");
		stopButton.setEnabled(false);
		appendLog("[] Simulation stopped");
		
		// Record final event
		if (observationSystem != null) {
			observationSystem.recordEvent("SIMULATION", 
				String.format("Simulation stopped - Total events: %d", observationSystem.getTotalEventsCollected()));
		}
	}

	/**
	 * Update simulation speed based on slider value
	 */
	private void updateSimulationSpeed(int sliderValue) {
		if (clockingSystem == null) return;

		// Convert slider value (0-100) to delay (2000ms-100ms)
		// Higher slider value = faster simulation = lower delay
		int delay = 2000 - (sliderValue * 19);
		clockingSystem.setDelay(delay);
		appendLog(String.format("Speed adjusted: %dms per tick (slider: %d)", delay, sliderValue));
		
		// Record event in observation system
		if (observationSystem != null) {
			observationSystem.recordEvent("CONFIG", String.format("Speed changed to %dms delay", delay));
		}
	}

	/**
	 * Append a log message to the log area
	 */
	public void appendLog(String message) {
		SwingUtilities.invokeLater(() -> {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			String timestamp = sdf.format(new Date());
			logArea.append(String.format("[%s] %s\n", timestamp, message));
			logArea.setCaretPosition(logArea.getDocument().getLength());
		});
	}

	/**
	 * Update log area with new events from the Observation system
	 */
	private void updateLogFromObservation() {
		if (observationSystem == null) return;
		
		var eventBuffer = observationSystem.getEventBuffer();
		int currentEventCount = eventBuffer.size();
		
		// Only process new events
		if (currentEventCount > lastEventCount) {
			// Get the new events (simplified approach - display last few)
			int eventsToShow = Math.min(3, currentEventCount - lastEventCount);
			var events = eventBuffer.stream()
					.skip(Math.max(0, currentEventCount - eventsToShow))
					.toList();
			
			for (var event : events) {
				appendLog(String.format("[%s] %s", event.eventType(), event.details()));
			}
			
			lastEventCount = currentEventCount;
		}
	}

	/**
	 * Refresh the control panel state
	 */
	public void refresh() {
		if (clockingSystem != null) {
			boolean running = clockingSystem.isRunning();
			startButton.setEnabled(!running);
			pauseButton.setEnabled(running);
			stopButton.setEnabled(running || clockingSystem.getCurrentTime() > 0);
		}
	}
}