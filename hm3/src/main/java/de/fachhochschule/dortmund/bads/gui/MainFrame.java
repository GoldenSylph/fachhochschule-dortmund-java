package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window for BADS (Beverage Automated Distribution System)
 *
 * INTEGRATION POINTS:
 * - This frame will be instantiated by GUIConfiguration.autowire()
 * - Pass Systems references via constructor or setters when integrating
 */
public class MainFrame extends JFrame {

    private WarehousePanel warehousePanel;
    private LoadingBayPanel loadingBayPanel;
    private OrderManagementPanel orderManagementPanel;
    private ControlLogPanel controlLogPanel;

    // TODO [CONCURRENCY]: Uncomment when integrating with backend systems
    // private ClockingSimulation clockingSystem;
    // private TaskManagement taskManagement;
    // private StorageManagement storageManagement;
    // private Observation observationSystem;

    public MainFrame() {
        // TODO [CONCURRENCY]: Uncomment to receive system references
        /* public MainFrame(ClockingSimulation clocking, TaskManagement taskMgmt,
                          StorageManagement storageMgmt, Observation observation) {
             this.clockingSystem = clocking;
             this.taskManagement = taskMgmt;
             this.storageManagement = storageMgmt;
             this.observationSystem = observation;
             initializeComponents();
         } */

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
            e.printStackTrace();
        }

        // Create menu bar
        createMenuBar();

        // Main layout
        setLayout(new BorderLayout());

        // Create panels
        // TODO [CONCURRENCY]: Uncomment to pass system references to panels
        // warehousePanel = new WarehousePanel(storageManagement);
        // loadingBayPanel = new LoadingBayPanel(/* pass AGV and Truck instances */);
        // orderManagementPanel = new OrderManagementPanel(taskManagement);
        // controlLogPanel = new ControlLogPanel(clockingSystem, observationSystem);

        warehousePanel = new WarehousePanel();
        loadingBayPanel = new LoadingBayPanel();
        orderManagementPanel = new OrderManagementPanel();
        controlLogPanel = new ControlLogPanel();

        // Left panel - Warehouse
        add(warehousePanel, BorderLayout.WEST);

     // Right side - split into top (loading bays) and bottom (orders)
        JPanel rightPanel = new JPanel(new GridBagLayout());

        // Loading Bay Panel - top 65%
        GridBagConstraints gbc_loadingBay = new GridBagConstraints();
        gbc_loadingBay.gridx = 0;
        gbc_loadingBay.gridy = 0;
        gbc_loadingBay.weightx = 1.0;
        gbc_loadingBay.weighty = 0.65;  // Loading bays take 65% of vertical space
        gbc_loadingBay.fill = GridBagConstraints.BOTH;
        rightPanel.add(loadingBayPanel, gbc_loadingBay);

        // Order Management Panel - bottom 35%
        GridBagConstraints gbc_orderMgmt = new GridBagConstraints();
        gbc_orderMgmt.gridx = 0;
        gbc_orderMgmt.gridy = 1;
        gbc_orderMgmt.weightx = 1.0;
        gbc_orderMgmt.weighty = 0.35;  // Orders take 35% of vertical space
        gbc_orderMgmt.fill = GridBagConstraints.BOTH;
        rightPanel.add(orderManagementPanel, gbc_orderMgmt);

        add(rightPanel, BorderLayout.CENTER);

        // Bottom panel - Controls and Log
        add(controlLogPanel, BorderLayout.SOUTH);

        // TODO [TICK-LISTENER]: Uncomment to register panels as tick listeners
        /* if (clockingSystem != null) {
             clockingSystem.registerTickable(warehousePanel);
             clockingSystem.registerTickable(loadingBayPanel);
             clockingSystem.registerTickable(orderManagementPanel);
         } */
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem preferencesItem = new JMenuItem("Preferences");
        editMenu.add(preferencesItem);

        // View Menu
        JMenu viewMenu = new JMenu("View");
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.addActionListener(e -> refreshAllPanels());
        viewMenu.add(refreshItem);

        // Tools Menu
        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem inventoryItem = new JMenuItem("Inventory Management");
        inventoryItem.addActionListener(e -> showInventoryDialog());
        toolsMenu.add(inventoryItem);

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
        // TODO [STATE-ACCESS]: Uncomment to trigger panel refreshes from backend state
        // warehousePanel.refresh();
        // loadingBayPanel.refresh();
        // orderManagementPanel.refresh();
        // controlLogPanel.refresh();

        repaint();
    }

    public void showInventoryDialog() {
        // TODO [OBSERVABILITY]: Uncomment to pass storage data to inventory dialog
        // InventoryDialog dialog = new InventoryDialog(this, storageManagement);

        InventoryDialog dialog = new InventoryDialog(this);
        dialog.setVisible(true);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "BADS - Beverage Automated Distribution System\n" +
            "Version 2.0\n" +
            "Fachhochschule Dortmund\n\n" +
            "A concurrent simulation system for automated warehouse management.",
            "About BADS",
            JOptionPane.INFORMATION_MESSAGE);
    }

    // Getters for panels (used by GUIConfiguration for wiring)
    public WarehousePanel getWarehousePanel() {
        return warehousePanel;
    }

    public LoadingBayPanel getLoadingBayPanel() {
        return loadingBayPanel;
    }

    public OrderManagementPanel getOrderManagementPanel() {
        return orderManagementPanel;
    }

    public ControlLogPanel getControlLogPanel() {
        return controlLogPanel;
    }

    // Main method for standalone testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
