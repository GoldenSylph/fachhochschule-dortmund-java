package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.systems.logic.ClockingSimulation;
import de.fachhochschule.dortmund.bads.systems.logic.Observation;
import de.fachhochschule.dortmund.bads.systems.logic.StorageManagement;
import de.fachhochschule.dortmund.bads.systems.logic.TaskManagement;

/**
 * Main application window for BADS (Beverage Automated Distribution System)
 */
public class MainFrame extends JFrame {
	private static final long serialVersionUID = 3376583837337391665L;
	private static final Logger LOGGER = LogManager.getLogger(MainFrame.class);

	private WarehousePanel warehousePanel;
    private LoadingBayPanel loadingBayPanel;
    private OrderManagementPanel orderManagementPanel;
    private ControlLogPanel controlLogPanel;

    private ClockingSimulation clockingSystem;
    private TaskManagement taskManagement;
    private StorageManagement storageManagement;
    private Observation observationSystem;
    
    // Domain objects for logistics control
    private de.fachhochschule.dortmund.bads.model.Area cityArea;
    private de.fachhochschule.dortmund.bads.model.Storage warehouse;
    private java.util.List<de.fachhochschule.dortmund.bads.resources.Truck> trucks;

    /**
     * Constructor with system dependencies (used by GUIConfiguration)
     */
    public MainFrame(ClockingSimulation clocking, TaskManagement taskMgmt,
                     StorageManagement storageMgmt, Observation observation) {
        this.clockingSystem = clocking;
        this.taskManagement = taskMgmt;
        this.storageManagement = storageMgmt;
        this.observationSystem = observation;
        
        LOGGER.info("MainFrame initializing with backend systems - Clocking: {}, TaskMgmt: {}, StorageMgmt: {}, Observation: {}",
            clocking != null, taskMgmt != null, storageMgmt != null, observation != null);
        
        initializeComponents();
    }

    /**
     * Default constructor for standalone testing
     */
    public MainFrame() {
        LOGGER.info("MainFrame initializing in standalone mode (no backend systems)");
        initializeComponents();
    }

    private void initializeComponents() {
        setTitle("BADS - Beverage Automated Distribution System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Set Look and Feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.error("Failed to set system look and feel", e);
        }

        // Create menu bar
        createMenuBar();

        // Main layout
        setLayout(new BorderLayout());

        // Create panels with system references
        warehousePanel = new WarehousePanel();
        loadingBayPanel = new LoadingBayPanel();
        orderManagementPanel = new OrderManagementPanel();
        controlLogPanel = new ControlLogPanel(clockingSystem, observationSystem);

        // Wire backend dependencies to panels
        wireBackendDependencies();
        
        // Register panels with clocking system for tick updates
        registerTickableComponents();

        // Build UI layout
        buildLayout();
        
        LOGGER.info("MainFrame initialization complete");
    }

    /**
     * Wire all backend system dependencies to GUI panels
     */
    private void wireBackendDependencies() {
        // TaskManagement integration
        if (taskManagement != null) {
            orderManagementPanel.setTaskManagement(taskManagement);
            loadingBayPanel.setTaskManagement(taskManagement);
            LOGGER.debug("TaskManagement wired to OrderManagementPanel and LoadingBayPanel");
        } else {
            LOGGER.warn("TaskManagement not available - panels will use sample data");
        }
        
        // StorageManagement integration
        // Note: Storage and AGV fleet will be set by GUIConfiguration.setWarehouseData()
        // This allows for more flexible configuration from App.java
        if (storageManagement != null) {
            LOGGER.debug("StorageManagement available - waiting for GUIConfiguration to set Storage");
        } else {
            LOGGER.warn("StorageManagement not available - WarehousePanel will use sample data");
        }
        
        // Observation system integration
        if (observationSystem != null) {
            loadingBayPanel.setObservationSystem(observationSystem);
            LOGGER.debug("Observation system wired to LoadingBayPanel");
        } else {
            LOGGER.warn("Observation system not available");
        }
    }
    
    /**
     * Register all ITickable components with the clocking system
     * Note: This should only be called once, either here or by GUIConfiguration
     */
    private void registerTickableComponents() {
        if (clockingSystem != null) {
            // Only register if not already registered by GUIConfiguration
            // ClockingSimulation should handle duplicate registrations gracefully
            clockingSystem.registerTickable(orderManagementPanel);
            clockingSystem.registerTickable(loadingBayPanel);
            clockingSystem.registerTickable(warehousePanel);
            LOGGER.info("Registered {} tickable components with ClockingSimulation", 3);
        } else {
            LOGGER.warn("ClockingSimulation not available - components will not receive tick updates");
        }
    }

    /**
     * Build the main UI layout
     */
    private void buildLayout() {
        // Left panel - Warehouse
        add(warehousePanel, BorderLayout.WEST);

        // Right side - split into top (loading bays) and bottom (orders)
        JPanel rightPanel = new JPanel(new GridBagLayout());

        // Loading Bay Panel - top 65%
        GridBagConstraints gbc_loadingBay = new GridBagConstraints();
        gbc_loadingBay.gridx = 0;
        gbc_loadingBay.gridy = 0;
        gbc_loadingBay.weightx = 1.0;
        gbc_loadingBay.weighty = 0.65;
        gbc_loadingBay.fill = GridBagConstraints.BOTH;
        rightPanel.add(loadingBayPanel, gbc_loadingBay);

        // Order Management Panel - bottom 35%
        GridBagConstraints gbc_orderMgmt = new GridBagConstraints();
        gbc_orderMgmt.gridx = 0;
        gbc_orderMgmt.gridy = 1;
        gbc_orderMgmt.weightx = 1.0;
        gbc_orderMgmt.weighty = 0.35;
        gbc_orderMgmt.fill = GridBagConstraints.BOTH;
        rightPanel.add(orderManagementPanel, gbc_orderMgmt);

        add(rightPanel, BorderLayout.CENTER);

        // Bottom panel - Controls and Log
        add(controlLogPanel, BorderLayout.SOUTH);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(exitItem);

        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem preferencesItem = new JMenuItem("Preferences");
        preferencesItem.addActionListener(e -> showPreferencesDialog());
        editMenu.add(preferencesItem);

        // View Menu
        JMenu viewMenu = new JMenu("View");
        JMenuItem refreshItem = new JMenuItem("Refresh All");
        refreshItem.addActionListener(e -> refreshAllPanels());
        viewMenu.add(refreshItem);

        // Tools Menu
        JMenu toolsMenu = new JMenu("Tools");
        
        JMenuItem logisticsItem = new JMenuItem("Logistics Control Panel");
        logisticsItem.addActionListener(e -> showLogisticsControlPanel());
        toolsMenu.add(logisticsItem);
        
        toolsMenu.addSeparator();
        
        JMenuItem inventoryItem = new JMenuItem("Inventory Management");
        inventoryItem.addActionListener(e -> showInventoryDialog());
        toolsMenu.add(inventoryItem);
        
        toolsMenu.addSeparator();
        
        JMenuItem systemStatusItem = new JMenuItem("System Status");
        systemStatusItem.addActionListener(e -> showSystemStatus());
        toolsMenu.add(systemStatusItem);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void refreshAllPanels() {
        LOGGER.info("Refreshing all panels");
        if (warehousePanel != null) warehousePanel.refresh();
        if (loadingBayPanel != null) loadingBayPanel.refresh();
        if (orderManagementPanel != null) orderManagementPanel.refresh();
        if (controlLogPanel != null) controlLogPanel.refresh();
        repaint();
        LOGGER.debug("All panels refreshed");
    }

    public void showInventoryDialog() {
        LOGGER.info("Opening Inventory Management dialog");
        InventoryDialog dialog = new InventoryDialog(this, storageManagement);
        dialog.setVisible(true);
    }
    
    private void showSystemStatus() {
        LOGGER.info("Displaying system status");
        StringBuilder status = new StringBuilder();
        status.append("BADS System Status\n\n");
        status.append("Backend Systems:\n");
        status.append("- Clocking System: ").append(clockingSystem != null ? "Connected" : "Not Available").append("\n");
        status.append("- Task Management: ").append(taskManagement != null ? "Connected" : "Not Available").append("\n");
        status.append("- Storage Management: ").append(storageManagement != null ? "Connected" : "Not Available").append("\n");
        status.append("- Observation System: ").append(observationSystem != null ? "Connected" : "Not Available").append("\n\n");
        
        if (storageManagement != null) {
            Map<String, Storage> storages = storageManagement.getAllStorages();
            status.append("Storage Details:\n");
            status.append("- Storage Instances: ").append(storages.size()).append("\n");
            if (!storages.isEmpty()) {
                Storage s = storages.values().iterator().next();
                status.append("- Total Cells: ").append(s.getAllStorages().size()).append("\n");
                status.append("- Charging Stations: ").append(s.getChargingStationCount()).append("\n");
            }
        }
        
        if (taskManagement != null) {
            status.append("\nTask Management:\n");
            status.append("- Active Tasks: ").append(taskManagement.getAllTasks().size()).append("\n");
        }
        
        JOptionPane.showMessageDialog(this, status.toString(), "System Status", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showPreferencesDialog() {
        LOGGER.info("Opening Preferences dialog");
        // TODO: Implement preferences dialog
        JOptionPane.showMessageDialog(this, "Preferences dialog not implemented yet.", 
            "Preferences", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showLogisticsControlPanel() {
        LOGGER.info("Opening Logistics Control Panel");
        LogisticsControlPanel panel = new LogisticsControlPanel(
            this, cityArea, warehouse, trucks
        );
        panel.setVisible(true);
    }
    
    private void exitApplication() {
        LOGGER.info("Application exit requested");
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to exit BADS?", 
            "Exit Application", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            LOGGER.info("Application shutting down");
            System.exit(0);
        }
    }

    private void showAboutDialog() {
        LOGGER.info("Displaying about dialog");
        JOptionPane.showMessageDialog(this,
            "BADS - Beverage Automated Distribution System\n" +
            "Version 2.0\n" +
            "Fachhochschule Dortmund\n\n" +
            "A concurrent simulation system for automated warehouse management.",
            "About BADS",
            JOptionPane.INFORMATION_MESSAGE);
    }

    // Getters for panels (used by GUIConfiguration for wiring)
    public WarehousePanel getWarehousePanel() { return warehousePanel; }
    public LoadingBayPanel getLoadingBayPanel() { return loadingBayPanel; }
    public OrderManagementPanel getOrderManagementPanel() { return orderManagementPanel; }
    public ControlLogPanel getControlLogPanel() { return controlLogPanel; }
    
    // Getters for backend systems
    public ClockingSimulation getClockingSystem() { return clockingSystem; }
    public TaskManagement getTaskManagement() { return taskManagement; }
    public StorageManagement getStorageManagement() { return storageManagement; }
    public Observation getObservationSystem() { return observationSystem; }
    
    // Setters for domain objects (used by GUIConfiguration)
    public void setCityArea(de.fachhochschule.dortmund.bads.model.Area cityArea) {
        this.cityArea = cityArea;
        LOGGER.debug("City area set in MainFrame");
    }
    
    public void setWarehouse(de.fachhochschule.dortmund.bads.model.Storage warehouse) {
        this.warehouse = warehouse;
        LOGGER.debug("Warehouse set in MainFrame");
    }
    
    public void setTrucks(java.util.List<de.fachhochschule.dortmund.bads.resources.Truck> trucks) {
        this.trucks = trucks;
        LOGGER.debug("Trucks set in MainFrame - {} truck(s)", trucks != null ? trucks.size() : 0);
    }
}