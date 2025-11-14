package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fachhochschule.dortmund.bads.gui.agv.AGVRenderer;
import de.fachhochschule.dortmund.bads.model.Area.Point;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.resources.AGV;
import de.fachhochschule.dortmund.bads.systems.logic.utils.ITickable;

/**
 * Panel displaying warehouse storage grid and AGV charging station
 */
public class WarehousePanel extends JPanel implements ITickable {
	private static final long serialVersionUID = 9058921882723603330L;
	
	private static final int GRID_ROWS = 5;
    private static final int GRID_COLS = 7;  // Match actual warehouse1 dimensions (7x5)

    private JButton[][] gridButtons;
    private JPanel gridPanel;
    private JPanel chargingStationPanel;
    private JLabel[] chargingSlotLabels;
    private JPanel agvStatusPanel;
    private JLabel[] agvStatusLabels;

    // Backend dependencies
    private Storage storage;
    private List<AGV> agvFleet;

    // Store original cell colors for restoring after AGV highlights
    private Map<String, Color> cellOriginalColors = new HashMap<>();

    public WarehousePanel() {
        initializeComponents();
    }

    /**
     * Set the storage instance to display (dependency injection)
     */
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    /**
     * Set the AGV fleet for charging station display (dependency injection)
     */
    public void setAGVFleet(List<AGV> agvFleet) {
        this.agvFleet = agvFleet;
    }

    @Override
    public void onTick(int currentTick) {
        // Update UI every tick for smooth AGV movement visualization
        // This is critical for seeing AGV positions update in real-time
        SwingUtilities.invokeLater(this::refresh);
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

        // AGV status panel
        createAGVStatusPanel();
        add(agvStatusPanel);
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

        // Define storage layout matching actual warehouse from App.java (7x5 grid)
        // Cell array mapping (index -> notation):
        // Ambient (0-5):        1A, 2A, 1B, 2B, 1C, 2C
        // Refrigerated (6-9):   4A, 5A, 4B, 5B
        // Bulk (10-11):         7A, 7B
        // Charging (12-14):     1D, 2D, 4D
        // Loading (15-16):      6D, 7D
        // Corridors (17-34):    3A, 6A, 3B, 6B, 3C-7C, 3D, 5D, 1E-7E
        
        String[][] cellLabels = {
            {"1A", "2A", "3A", "4A", "5A", "6A", "7A"},     // Row 0 (y=0)
            {"1B", "2B", "3B", "4B", "5B", "6B", "7B"},     // Row 1 (y=1)
            {"1C", "2C", "3C", "4C", "5C", "6C", "7C"},     // Row 2 (y=2)
            {"1D", "2D", "3D", "4D", "5D", "6D", "7D"},     // Row 3 (y=3)
            {"1E", "2E", "3E", "4E", "5E", "6E", "7E"}      // Row 4 (y=4)
        };
        
        // Color mapping based on actual warehouse layout from App.java
        // A = Ambient (green), R = Refrigerated (blue), B = Bulk (orange)
        // C = Charging (light green), L = Loading (yellow), = = Corridor (gray)
        String[][] cellTypes = {
            {"A",  "A",  "=",  "R",  "R",  "=",  "B" },    // Row 0
            {"A",  "A",  "=",  "R",  "R",  "=",  "B" },    // Row 1
            {"A",  "A",  "=",  "=",  "=",  "=",  "=" },    // Row 2
            {"C",  "C",  "=",  "C",  "=",  "L",  "L" },    // Row 3
            {"=",  "=",  "=",  "=",  "=",  "=",  "=" }     // Row 4
        };

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                String label = cellLabels[row][col];
                String type = cellTypes[row][col];
                JButton button = new JButton(label);
                button.setMargin(new Insets(2, 2, 2, 2));
                button.setFont(new Font("Monospaced", Font.PLAIN, 9));

                // Set colors based on actual cell type from warehouse layout
                Color bgColor = switch (type) {
                    case "A" -> new Color(184, 227, 184);  // Ambient - Green
                    case "R" -> new Color(184, 212, 227);  // Refrigerated - Blue
                    case "B" -> new Color(255, 212, 163);  // Bulk - Orange
                    case "C" -> new Color(232, 245, 233);  // Charging - Light Green
                    case "L" -> new Color(255, 248, 220);  // Loading - Light Yellow
                    case "=" -> new Color(240, 240, 240);  // Corridor - Light Gray
                    default -> new Color(224, 224, 224);   // Unknown - Gray
                };
                
                button.setBackground(bgColor);
                button.setOpaque(true);
                button.setBorderPainted(true);

                // Store original color for later restoration
                cellOriginalColors.put(label, bgColor);

                final String cellLabel = label;
                button.addActionListener(e -> showCellDetails(cellLabel));
                button.setFocusPainted(false);
                
                gridButtons[row][col] = button;
                gridPanel.add(button);
            }
        }
    }

    private void createLegend() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        legendPanel.setMaximumSize(new Dimension(330, 60)); // Increased height for two rows
        legendPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel legendLabel = new JLabel("Legend:");
        legendLabel.setFont(new Font("Arial", Font.BOLD, 10));
        legendPanel.add(legendLabel);

        // Storage types
        addLegendItem(legendPanel, "Refrigerated", new Color(184, 212, 227));
        addLegendItem(legendPanel, "Ambient", new Color(184, 227, 184));
        addLegendItem(legendPanel, "Bulk", new Color(255, 212, 163));
        
        // Charging station and loading dock
        addLegendItem(legendPanel, "[CHG] Charging", new Color(232, 245, 233));
        addLegendItem(legendPanel, "[TRK] Loading", new Color(255, 248, 220));

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
            "[CHG] CHARGING STATION",
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
            chargingSlotLabels[i] = createChargingSlot(i + 1, false);
            slotsPanel.add(chargingSlotLabels[i]);
        }

        chargingStationPanel.add(slotsPanel, BorderLayout.CENTER);
    }

    private JLabel createChargingSlot(int slotNumber, boolean occupied) {
        JLabel slotLabel = new JLabel();
        slotLabel.setHorizontalAlignment(SwingConstants.CENTER);
        slotLabel.setVerticalAlignment(SwingConstants.CENTER);
        slotLabel.setOpaque(true);
        slotLabel.setPreferredSize(new Dimension(80, 60));
        slotLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        updateChargingSlotDisplay(slotLabel, slotNumber, occupied, null, 0);

        return slotLabel;
    }

    private void updateChargingSlotDisplay(JLabel slotLabel, int slotNumber,
                                          boolean occupied, String agvId, int batteryLevel) {
        if (occupied && agvId != null) {
            slotLabel.setBackground(new Color(200, 230, 201));
            slotLabel.setBorder(BorderFactory.createLineBorder(new Color(76, 175, 80), 2));
            slotLabel.setText("<html><center>[CHG]<br>" + agvId + "<br>" + batteryLevel + "%</center></html>");
            slotLabel.setForeground(batteryLevel < 50 ? new Color(198, 40, 40) : new Color(46, 125, 50));
        } else {
            slotLabel.setBackground(Color.WHITE);
            slotLabel.setBorder(BorderFactory.createDashedBorder(new Color(76, 175, 80), 2, 5, 5, false));
            slotLabel.setText("<html><center>[PWR]<br>Slot " + slotNumber + "<br>Empty</center></html>");
            slotLabel.setForeground(Color.GRAY);
        }
    }

    private void createAGVStatusPanel() {
        agvStatusPanel = new JPanel();
        agvStatusPanel.setLayout(new BoxLayout(agvStatusPanel, BoxLayout.Y_AXIS));
        agvStatusPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(33, 150, 243), 2),
            "AGV FLEET STATUS",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 11),
            new Color(13, 71, 161)
        ));
        agvStatusPanel.setBackground(new Color(227, 242, 253));
        agvStatusPanel.setMaximumSize(new Dimension(330, 120));

        agvStatusLabels = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            agvStatusLabels[i] = new JLabel();
            agvStatusLabels[i].setFont(new Font("Courier New", Font.PLAIN, 10));
            agvStatusLabels[i].setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
            agvStatusLabels[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            agvStatusPanel.add(agvStatusLabels[i]);
        }
    }

    private void createActionButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanel.setMaximumSize(new Dimension(330, 40));

        JButton refreshButton = new JButton("Refresh Grid");
        refreshButton.addActionListener(e -> refresh());
        buttonPanel.add(refreshButton);

        JButton inventoryButton = new JButton("Check Inventory");
        inventoryButton.addActionListener(e -> showInventory());
        buttonPanel.add(inventoryButton);

        add(buttonPanel);
    }

    /**
     * Refresh the warehouse grid, charging station, and AGV status from backend state
     */
    public void refresh() {
        updateChargingStationFromAGVs();
        updateAGVStatusDisplay();
        updateGridFromStorage();
    }
        
    private void updateAGVStatusDisplay() {
        if (agvFleet == null || agvStatusLabels == null) return;

        for (int i = 0; i < Math.min(agvFleet.size(), agvStatusLabels.length); i++) {
            AGV agv = agvFleet.get(i);
            String agvId = agv.getAgvId();
            AGV.AGVState state = agv.getState();
            int battery = agv.getBatteryLevel();
            Point position = agv.getCurrentPosition();

            String locationStr = position != null
                ? de.fachhochschule.dortmund.bads.model.Storage.pointToNotation(position)
                : "N/A";

            String stateStr = switch (state) {
                case IDLE -> "IDLE";
                case BUSY -> "BUSY";
                case MOVING_TO_CHARGE -> "→CHG";
                case WAITING_FOR_CHARGE -> "WAIT";
                case CHARGING -> "CHRG";
            };

            String statusText = String.format("%s | %-4s | %3d%% | @%s",
                agvId, stateStr, battery, locationStr);

            agvStatusLabels[i].setText(statusText);

            // Color based on state and battery
            if (state == AGV.AGVState.CHARGING) {
                agvStatusLabels[i].setForeground(new Color(46, 125, 50)); // Green
            } else if (state == AGV.AGVState.BUSY) {
                agvStatusLabels[i].setForeground(new Color(13, 71, 161)); // Blue
            } else if (battery < 25) {
                agvStatusLabels[i].setForeground(new Color(198, 40, 40)); // Red
            } else {
                agvStatusLabels[i].setForeground(Color.BLACK);
            }
        }
    }

    private void updateChargingStationFromAGVs() {
        if (agvFleet == null || chargingSlotLabels == null) return;

        // Step 1: Clear all slots first
        for (int i = 0; i < chargingSlotLabels.length; i++) {
            updateChargingSlotDisplay(chargingSlotLabels[i], i + 1, false, null, 0);
        }

        // Find all charging AGVs and fill slots sequentially
        int slotIndex = 0;
        for (AGV agv : agvFleet) {
            if (slotIndex >= 3) break;  // Only 3 slots available

            AGV.AGVState state = agv.getState();
            // Show AGV in charging station for any charging-related state
            if (state == AGV.AGVState.CHARGING ||
                state == AGV.AGVState.WAITING_FOR_CHARGE ||
                state == AGV.AGVState.MOVING_TO_CHARGE) {
                updateChargingSlotDisplay(chargingSlotLabels[slotIndex],
                    slotIndex + 1, true, agv.getAgvId(), agv.getBatteryLevel());
                slotIndex++;
            }
        }
    }

    private void updateGridFromStorage() {
        if (storage == null) return;

        // Build map of AGV positions for visualization
        Map<String, String> agvPositions = new java.util.HashMap<>();
        if (agvFleet != null) {
            for (AGV agv : agvFleet) {
                if (agv.getCurrentPosition() != null) {
                    String cellNotation = de.fachhochschule.dortmund.bads.model.Storage.pointToNotation(agv.getCurrentPosition());
                    String agvInfo = String.format("%s (%d%%)", agv.getAgvId(), agv.getBatteryLevel());
                    agvPositions.put(cellNotation, agvInfo);
                }
            }
        }

        // Update grid button appearance based on storage cell occupancy and AGV positions
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                JButton button = gridButtons[row][col];
                String label = button.getText();

                if (!label.isEmpty()) {
                    // Get cell by its notation label
                    StorageCell cell = storage.getCellByNotation(label);
                    if (cell != null) {
                        int boxCount = cell.getBoxCount();
                        int maxVolume = cell.MAX_LENGTH * cell.MAX_WIDTH * cell.MAX_HEIGHT;

                        // Check if AGV is at this position
                        String agvInfo = agvPositions.get(label);
                        boolean hasAGV = agvInfo != null;

                        if (hasAGV) {
                            // Highlight cell with AGV - bright yellow/orange background
                            button.setBackground(new Color(255, 235, 59)); // Yellow
                            button.setBorder(BorderFactory.createLineBorder(new Color(255, 87, 34), 3)); // Deep orange border
                            button.setFont(new Font("Arial", Font.BOLD, 9));
                            button.setToolTipText(String.format("<html>%s<br><b>AGV: %s</b><br>Boxes: %d (Max: %d)</html>",
                                label, agvInfo, boxCount, maxVolume));
                        } else if (boxCount > 0) {
                            // Cell has inventory but no AGV - restore original color without heavy borders
                            Color originalColor = cellOriginalColors.getOrDefault(label, new Color(224, 224, 224));
                            button.setBackground(originalColor);
                            button.setBorder(null);  // Remove dark borders for cleaner look
                            button.setFont(new Font("Arial", Font.PLAIN, 10));
                            button.setToolTipText(String.format("%s: %d boxes (Max Volume: %d)",
                                label, boxCount, maxVolume));
                        } else {
                            // Empty cell - restore original color
                            Color originalColor = cellOriginalColors.getOrDefault(label, new Color(224, 224, 224));
                            button.setBackground(originalColor);
                            button.setBorder(null);
                            button.setFont(new Font("Arial", Font.PLAIN, 10));
                            button.setToolTipText(label + ": Empty");
                        }
                    }
                }
            }
        }
    }

    private void showCellDetails(String cellLabel) {
        if (storage == null) {
            JOptionPane.showMessageDialog(this, 
                "Storage not connected", 
                "Cell Details", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StorageCell cell = storage.getCellByNotation(cellLabel);
        if (cell == null) {
            JOptionPane.showMessageDialog(this, 
                "Cell not found: " + cellLabel, 
                "Cell Details", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build detailed cell information
        StringBuilder details = new StringBuilder();
        details.append("Cell: ").append(cellLabel).append("\n");
        details.append("Type: ").append(cell.TYPE).append("\n");
        details.append("Max Dimensions: ").append(cell.MAX_LENGTH).append("x")
               .append(cell.MAX_WIDTH).append("x").append(cell.MAX_HEIGHT).append("\n\n");
        
        if (cell.TYPE == StorageCell.Type.CHARGING_STATION) {
            details.append("Status: ");
            if (cell.isOccupiedByAGV()) {
                details.append("Occupied\n");
                if (cell.getChargingAGV() != null) {
                    details.append("AGV: ").append(cell.getChargingAGV().getAgvId()).append("\n");
                }
            } else {
                details.append("Available\n");
            }
        } else if (cell.TYPE != StorageCell.Type.CORRIDOR) {
            // Storage cell details
            int boxCount = cell.getBoxCount();
            int maxVolume = cell.MAX_LENGTH * cell.MAX_WIDTH * cell.MAX_HEIGHT;
            int usedVolume = cell.getActualUsedVolume();
            double cellUtilization = maxVolume > 0 ? (double) usedVolume / maxVolume * 100 : 0.0;
            
            details.append("Boxes Stored: ").append(boxCount).append("\n");
            details.append("Used Volume: ").append(usedVolume).append(" / ").append(maxVolume).append("\n");
            details.append("Utilization: ").append(String.format("%.1f%%", cellUtilization)).append("\n");
            
            if (boxCount > 0) {
                details.append("\nCurrent Dimensions:\n");
                details.append("  ").append(cell.getCurrentLength()).append("x")
                       .append(cell.getCurrentWidth()).append("x")
                       .append(cell.getCurrentHeight()).append("\n");
                details.append("Space Efficiency: ").append(String.format("%.1f%%", cell.getSpaceEfficiency()));
            } else {
                details.append("\nStatus: Empty");
            }
        } else {
            details.append("Status: Corridor (No storage)");
        }

        JOptionPane.showMessageDialog(this,
            details.toString(),
            "Cell Details - " + cellLabel,
            JOptionPane.INFORMATION_MESSAGE);
    }


    private void showInventory() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof MainFrame mainFrame) {
            // Call MainFrame's method to show inventory dialog
            mainFrame.showInventoryDialog();
        }
    }
//    private void showInventory() {
//        if (storage == null) {
//            JOptionPane.showMessageDialog(this, "Storage not connected", "Inventory", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
//        var allCells = storage.getAllStorages();
//        int[] boxCounts = {0, 0, 0}; // refrigerated, ambient, bulk
//        long totalUsed = 0, totalMax = 0;
//        
//        for (StorageCell cell : allCells.values()) {
//            int idx = switch (cell.TYPE) {
//                case REFRIGERATED -> 0;
//                case AMBIENT -> 1;
//                case BULK -> 2;
//                default -> -1;
//            };
//            
//            if (idx >= 0) {
//                boxCounts[idx] += cell.getBoxCount();
//                totalUsed += cell.getActualUsedVolume();
//                totalMax += (long) cell.MAX_LENGTH * cell.MAX_WIDTH * cell.MAX_HEIGHT;
//            }
//        }
//        
//        double util = totalMax > 0 ? (double) totalUsed / totalMax * 100 : 0.0;
//        int chargingOccupied = storage.getChargingStationCount() - storage.getAvailableChargingStationCount();
//        
//        String msg = String.format(
//            "Warehouse Inventory:\n\n" +
//            "Refrigerated: %d boxes\n" +
//            "Ambient: %d boxes\n" +
//            "Bulk: %d boxes\n\n" +
//            "Total: %d boxes\n" +
//            "Utilization: %.1f%%\n" +
//            "Charging: %d/%d occupied",
//            boxCounts[0], boxCounts[1], boxCounts[2],
//            boxCounts[0] + boxCounts[1] + boxCounts[2],
//            util, chargingOccupied, storage.getChargingStationCount()
//        );
//
//        JOptionPane.showMessageDialog(this, msg, "Warehouse Inventory", JOptionPane.INFORMATION_MESSAGE);
//    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // After all components are painted, overlay AGV graphics on the warehouse grid
        renderAGVsOnGrid(g);
    }

    /**
     * Render AGV graphics overlaid on the warehouse grid.
     * This shows AGVs moving through the warehouse with cargo visualization.
     */
    private void renderAGVsOnGrid(Graphics g) {
        if (agvFleet == null || storage == null || gridPanel == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create(); // Create copy to avoid affecting other components
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        try {
            for (AGV agv : agvFleet) {
                Point agvPos = agv.getCurrentPosition();

                if (agvPos == null) {
                    continue; // AGV not yet positioned
                }

                // Calculate pixel position for this grid cell
                int[] pixelPos = cellToPixelPosition(agvPos);
                if (pixelPos == null) {
                    continue; // Invalid position
                }

                int centerX = pixelPos[0];
                int centerY = pixelPos[1];

                // Get AGV state for visualization
                AGV.AGVState state = agv.getState();
                int battery = agv.getBatteryLevel();
                boolean hasCargo = agv.getInventoryCell() != null &&
                                  agv.getInventoryCell().getBoxCount() > 0;

                // Render AGV components (body, wheels, cargo, battery, state, ID)
                AGVRenderer.drawBody(g2d, centerX, centerY, state);
                AGVRenderer.drawWheels(g2d, centerX, centerY);

                if (hasCargo) {
                    AGVRenderer.drawCargo(g2d, centerX, centerY);
                }

                AGVRenderer.drawBattery(g2d, centerX, centerY, battery);
                AGVRenderer.drawState(g2d, centerX, centerY, state);
                AGVRenderer.drawId(g2d, centerX, centerY, agv.getAgvId());
            }
        } finally {
            g2d.dispose(); // Clean up graphics context
        }
    }

    /**
     * Convert grid position (Point) to pixel coordinates for AGV rendering.
     *
     * @param gridPos the grid position (x: 0-6, y: 0-4 for 7x5 grid)
     * @return [centerX, centerY] pixel coordinates, or null if invalid
     */
    private int[] cellToPixelPosition(Point gridPos) {
        if (gridPos == null || gridPanel == null) {
            return null;
        }

        int gridX = gridPos.x();  // Column: 0-6
        int gridY = gridPos.y();  // Row: 0-4

        // Validate bounds
        if (gridX < 0 || gridX >= GRID_COLS || gridY < 0 || gridY >= GRID_ROWS) {
            return null;
        }

        // Get grid panel dimensions and position
        int gridWidth = gridPanel.getWidth();
        int gridHeight = gridPanel.getHeight();
        int gridPanelX = gridPanel.getX();
        int gridPanelY = gridPanel.getY();

        // Calculate cell dimensions (accounting for 2px gaps between cells)
        int cellWidth = (gridWidth - (GRID_COLS - 1) * 2) / GRID_COLS;
        int cellHeight = (gridHeight - (GRID_ROWS - 1) * 2) / GRID_ROWS;

        // Calculate center of the cell (including gap offset)
        int centerX = gridPanelX + (gridX * (cellWidth + 2)) + (cellWidth / 2);
        int centerY = gridPanelY + (gridY * (cellHeight + 2)) + (cellHeight / 2);

        return new int[]{centerX, centerY};
    }
}