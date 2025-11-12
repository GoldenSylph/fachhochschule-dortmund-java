package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Panel displaying warehouse storage grid and AGV charging station
 *
 * INTEGRATION POINTS:
 * - Needs StorageManagement reference to read cell states
 * - Should implement ITickable to update on simulation ticks
 * - Grid buttons should reflect StorageCell occupancy
 */
public class WarehousePanel extends JPanel /* implements ITickable */ {

    private static final int GRID_ROWS = 5;
    private static final int GRID_COLS = 10;

    private JButton[][] gridButtons;
    private JPanel gridPanel;
    private JPanel chargingStationPanel;
    private JLabel[] chargingSlotLabels;

    // TODO [CONCURRENCY]: Uncomment when integrating with backend
    // private StorageManagement storageManagement;
    // private Storage storage;

    public WarehousePanel() {
        // TODO [CONCURRENCY]: Uncomment to receive StorageManagement reference
        // public WarehousePanel(StorageManagement storageManagement) {
        //     this.storageManagement = storageManagement;
        //     initializeComponents();
        // }

        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(350, 700));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Warehouse Storage Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // Warehouse grid
        createWarehouseGrid();
        add(gridPanel);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // Legend
        createLegend();
        add(Box.createRigidArea(new Dimension(0, 15)));

        // Charging station
        createChargingStation();
        add(chargingStationPanel);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // Action buttons
        createActionButtons();
    }

    private void createWarehouseGrid() {
        gridPanel = new JPanel(new GridLayout(GRID_ROWS, GRID_COLS, 2, 2));
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        gridPanel.setMaximumSize(new Dimension(330, 220));

        gridButtons = new JButton[GRID_ROWS][GRID_COLS];

        // Define storage layout 
        String[][] cellLabels = {
            {"B01", "B02", "B03", "", "J01", "J02", "", "B04", "J03", "J04"},     // Row 1 - Refrigerated
            {"B05", "", "B06", "J05", "J06", "", "B07", "B08", "", "J07"},         // Row 2 - Refrigerated
            {"S01", "S02", "", "W01", "W02", "W03", "", "S03", "S04", "W04"},     // Row 3 - Ambient
            {"S05", "", "W05", "W06", "S06", "S07", "W07", "", "S08", "W08"},     // Row 4 - Ambient
            {"K01", "K02", "", "C01", "C02", "", "K03", "K04", "C03", ""}         // Row 5 - Bulk
        };

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                String label = cellLabels[row][col];
                JButton button = new JButton(label);
                button.setMargin(new Insets(2, 2, 2, 2));
                button.setFont(new Font("Monospaced", Font.PLAIN, 9));

                // Set colors based on storage type
                if (!label.isEmpty()) {
                    char type = label.charAt(0);
                    switch (type) {
                        case 'B', 'J' -> button.setBackground(new Color(184, 212, 227)); // Refrigerated - Blue
                        case 'S', 'W' -> button.setBackground(new Color(184, 227, 184)); // Ambient - Green
                        case 'K', 'C' -> button.setBackground(new Color(255, 212, 163)); // Bulk - Orange
                    }

                    button.setOpaque(true);
                    button.setBorderPainted(true);
                } else {
                    button.setBackground(new Color(224, 224, 224)); // Empty
                    button.setOpaque(true);
                    button.setBorderPainted(true);
                    button.setEnabled(false);
                }

                button.setFocusPainted(false);

                // TODO [STATE-ACCESS]: Uncomment to add click listener for cell details
                // final String cellLabel = label;
                // button.addActionListener(e -> showCellDetails(cellLabel));

                gridButtons[row][col] = button;
                gridPanel.add(button);
            }
        }
    }

    private void createLegend() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legendPanel.setMaximumSize(new Dimension(330, 30));

        JLabel legendLabel = new JLabel("Legend:");
        legendPanel.add(legendLabel);

        addLegendItem(legendPanel, "Refrigerated", new Color(184, 212, 227));
        addLegendItem(legendPanel, "Ambient", new Color(184, 227, 184));
        addLegendItem(legendPanel, "Bulk", new Color(255, 212, 163));

        add(legendPanel);
    }

    private void addLegendItem(JPanel panel, String text, Color color) {
        JLabel colorBox = new JLabel("   ");
        colorBox.setOpaque(true);
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.add(colorBox);

        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 10));
        panel.add(label);
    }

    private void createChargingStation() {
        chargingStationPanel = new JPanel();
        chargingStationPanel.setLayout(new BorderLayout());
        chargingStationPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(76, 175, 80), 2),
            "⚡ CHARGING STATION",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            new Color(46, 125, 50)
        ));
        chargingStationPanel.setBackground(new Color(232, 245, 233));
        chargingStationPanel.setMaximumSize(new Dimension(330, 100));

        // Create 3 charging slots
        JPanel slotsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        slotsPanel.setOpaque(false);
        slotsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chargingSlotLabels = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            chargingSlotLabels[i] = createChargingSlot(i + 1, i == 1); // Slot 2 occupied
        }

        slotsPanel.add(chargingSlotLabels[0]);
        slotsPanel.add(chargingSlotLabels[1]);
        slotsPanel.add(chargingSlotLabels[2]);

        chargingStationPanel.add(slotsPanel, BorderLayout.CENTER);
    }

    private JLabel createChargingSlot(int slotNumber, boolean occupied) {
        JLabel slotLabel = new JLabel();
        slotLabel.setHorizontalAlignment(SwingConstants.CENTER);
        slotLabel.setVerticalAlignment(SwingConstants.CENTER);
        slotLabel.setOpaque(true);
        slotLabel.setPreferredSize(new Dimension(80, 60));

        if (occupied) {
            slotLabel.setBackground(new Color(200, 230, 201));
            slotLabel.setBorder(BorderFactory.createLineBorder(new Color(76, 175, 80), 2));
            slotLabel.setText("<html><center>⚡<br>AGV-4<br>35%</center></html>");
            slotLabel.setForeground(new Color(198, 40, 40)); // Red for low battery
        } else {
            slotLabel.setBackground(Color.WHITE);
            slotLabel.setBorder(BorderFactory.createDashedBorder(new Color(76, 175, 80), 2, 5, 5, false));
            slotLabel.setText("<html><center>🔌<br>Slot " + slotNumber + "<br>Empty</center></html>");
            slotLabel.setForeground(Color.GRAY);
        }

        slotLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        return slotLabel;
    }

    private void createActionButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanel.setMaximumSize(new Dimension(330, 40));

        JButton refreshButton = new JButton("Refresh Grid");
        // TODO [STATE-ACCESS]: Uncomment to refresh grid from storage state
        // refreshButton.addActionListener(e -> refreshGridFromStorage());
        buttonPanel.add(refreshButton);

        JButton inventoryButton = new JButton("Check Inventory");
        inventoryButton.addActionListener(e -> showInventory());
        buttonPanel.add(inventoryButton);

        JButton restockButton = new JButton("Restock Alert");
        // TODO [OBSERVABILITY]: Uncomment to trigger restock alert through observation system
        // restockButton.addActionListener(e -> triggerRestockAlert());
        buttonPanel.add(restockButton);

        add(buttonPanel);
    }

    // TODO [STATE-ACCESS]: Uncomment to implement storage grid refresh
    /* private void refreshGridFromStorage() {
         if (storage == null) return;
    
         SwingUtilities.invokeLater(() -> {
             for (int row = 0; row < GRID_ROWS; row++) {
                 for (int col = 0; col < GRID_COLS; col++) {
                     JButton button = gridButtons[row][col];
                     String label = button.getText();
                     if (!label.isEmpty()) {
                         StorageCell cell = storage.getCellByNotation(label);
                         if (cell != null) {
                             // Update button appearance based on cell occupancy
                             int boxCount = cell.getBoxCount();
                             if (boxCount > 0) {
                                 button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                                 button.setToolTipText(boxCount + " boxes");
                             } else {
                                 button.setBorder(null);
                                 button.setToolTipText("Empty");
                             }
                         }
                     }
                 }
             }
         });
     } */

    // TODO [STATE-ACCESS]: Uncomment to show cell details dialog
    /* private void showCellDetails(String cellLabel) {
         if (storage == null) return;
    
         StorageCell cell = storage.getCellByNotation(cellLabel);
         if (cell != null) {
             String details = String.format(
                 "Cell: %s\nType: %s\nBoxes: %d\nOccupancy: %.1f%%",
                 cellLabel,
                 cell.TYPE,
                 cell.getBoxCount(),
                 cell.getSpaceEfficiency()
             );
             JOptionPane.showMessageDialog(this, details, "Cell Details", JOptionPane.INFORMATION_MESSAGE);
         }
     } */

    private void showInventory() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof MainFrame mainFrame) {
            // Call MainFrame's method to show inventory dialog
            mainFrame.showInventoryDialog();
        } else {
            // Fallback if not in MainFrame context
            JOptionPane.showMessageDialog(this,
                "Inventory management requires access to storage systems.\n" +
                "Please use Tools > Inventory Management from the main menu.",
                "Inventory Management",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // TODO [TICK-LISTENER]: Uncomment to receive simulation tick updates
    /* @Override
     public void onTick(int currentTick) {
         // Update charging station status
         updateChargingStationDisplay();
    
         // Refresh grid display
         refreshGridFromStorage();
     } */

    // TODO [STATE-ACCESS]: Uncomment to update charging station display
    /* private void updateChargingStationDisplay() {
         // Access charging station cells from storage
         // Update chargingSlotLabels based on actual AGV charging status
         SwingUtilities.invokeLater(() -> {
             // (Example) Get AGVs from charging stations
             // for (int i = 0; i < 3; i++) {
             //     StorageCell slot = getChargingSlot(i);
             //     AGV agv = slot.getChargingAGV();
             //     if (agv != null) {
             //         int battery = agv.getBatteryLevel();
             //         updateSlotDisplay(i, agv, battery);
             //     } else {
             //         updateSlotEmpty(i);
             //     }
             // }
         });
     } */
}
