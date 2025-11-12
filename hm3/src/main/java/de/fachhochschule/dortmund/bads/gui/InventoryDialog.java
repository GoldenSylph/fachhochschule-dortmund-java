package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog for displaying warehouse inventory details
 *
 * INTEGRATION POINTS:
 * - Receive StorageManagement to access all storage cells
 * - Display real beverage inventory from StorageCell data
 * - Update statistics from actual storage state
 */
public class InventoryDialog extends JDialog {

    private JPanel summaryPanel;
    private JPanel inventoryListPanel;

    // TODO [CONCURRENCY]: Uncomment to receive StorageManagement reference
    // private StorageManagement storageManagement;
    // private Storage storage;

    public InventoryDialog(Frame owner) {
        // TODO [CONCURRENCY]: Uncomment to receive StorageManagement reference
        /* public InventoryDialog(Frame owner, StorageManagement storageManagement) {
             super(owner, "Warehouse Inventory Management", true);
             this.storageManagement = storageManagement;
             this.storage = /* get from storageManagement */; 
        //     initializeComponents();
        // }

        super(owner, "Warehouse Inventory Management", true);
        initializeComponents();
    }

    private void initializeComponents() {
        setSize(700, 500);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);

        // Summary statistics
        summaryPanel = createSummaryPanel();
        add(summaryPanel, BorderLayout.NORTH);

        // Inventory list
        JScrollPane inventoryScrollPane = createInventoryList();
        add(inventoryScrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        JLabel titleLabel = new JLabel("Current Stock Levels");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titlePanel.add(titleLabel);
        return titlePanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summary = new JPanel(new GridLayout(1, 4, 10, 10));
        summary.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 10, 10, 10),
            BorderFactory.createLineBorder(new Color(255, 193, 7))
        ));
        summary.setBackground(new Color(255, 243, 205));

        // TODO [STATE-ACCESS]: Replace with actual values from storage
        addSummaryItem(summary, "247", "TOTAL UNITS");
        addSummaryItem(summary, "12", "BEVERAGE TYPES");
        addSummaryItem(summary, "3", "LOW STOCK");
        addSummaryItem(summary, "85%", "CAPACITY");

        // TODO [STATE-ACCESS]: Uncomment to calculate from storage
        // updateSummaryFromStorage();

        return summary;
    }

    private void addSummaryItem(JPanel panel, String value, String label) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setOpaque(false);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        valueLabel.setForeground(new Color(0, 0, 128));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        textLabel.setForeground(Color.GRAY);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        item.add(valueLabel);
        item.add(textLabel);
        panel.add(item);
    }

    private JScrollPane createInventoryList() {
        inventoryListPanel = new JPanel();
        inventoryListPanel.setLayout(new BoxLayout(inventoryListPanel, BoxLayout.Y_AXIS));
        inventoryListPanel.setBackground(Color.WHITE);
        inventoryListPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Add sample inventory items
        addSampleInventoryItems();

        JScrollPane scrollPane = new JScrollPane(inventoryListPanel);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 10, 10, 10),
            BorderFactory.createLineBorder(Color.GRAY, 2)
        ));

        return scrollPane;
    }

    private void addSampleInventoryItems() {
        // Sample data - Beer
        addInventoryItem("🍺", "Beer - Premium Lager", "B01-B08 (Refrigerated)", 45, 50, 0.90);
        addInventoryItem("🍺", "Beer - Wheat Ale", "B05-B07 (Refrigerated)", 18, 50, 0.36);

        // Sample data - Juice
        addInventoryItem("🧃", "Juice - Orange", "J01-J07 (Refrigerated)", 38, 50, 0.76);
        addInventoryItem("🧃", "Juice - Apple", "J02-J04 (Refrigerated)", 8, 50, 0.16);

        // Sample data - Soda
        addInventoryItem("🥤", "Soda - Cola", "S01-S08 (Ambient)", 52, 50, 1.00);
        addInventoryItem("🥤", "Soda - Lemon Lime", "S03-S06 (Ambient)", 22, 50, 0.44);

        // Sample data - Water
        addInventoryItem("💧", "Water - Mineral", "W01-W08 (Ambient)", 41, 50, 0.82);
        addInventoryItem("💧", "Water - Spring", "W05-W07 (Ambient)", 6, 50, 0.12);

        // Sample data - Bulk
        addInventoryItem("🛢️", "Keg - Draft Beer", "K01-K04 (Bulk Storage)", 12, 25, 0.48);
        addInventoryItem("⚗️", "Concentrate - Soda Syrup", "C01-C03 (Bulk Storage)", 15, 20, 0.75);
        addInventoryItem("⚗️", "Concentrate - Juice Base", "C02 (Bulk Storage)", 4, 20, 0.20);
        addInventoryItem("🍹", "Energy Drink - Mixed", "S07-S08 (Ambient)", 26, 50, 0.52);

        // TODO [STATE-ACCESS]: Remove sample data and populate from storage
        // populateInventoryFromStorage();
    }

    private void addInventoryItem(String icon, String name, String location, int quantity, int maxQuantity, double fillRatio) {
        JPanel itemPanel = new JPanel(new BorderLayout(15, 0));
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(212, 208, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Dialog", Font.PLAIN, 24));
        iconLabel.setPreferredSize(new Dimension(40, 40));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        itemPanel.add(iconLabel, BorderLayout.WEST);

        // Details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 11));
        detailsPanel.add(nameLabel);

        JLabel locationLabel = new JLabel("Location: " + location);
        locationLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        locationLabel.setForeground(Color.GRAY);
        detailsPanel.add(locationLabel);

        itemPanel.add(detailsPanel, BorderLayout.CENTER);

        // Quantity and bar
        JPanel quantityPanel = new JPanel();
        quantityPanel.setLayout(new BoxLayout(quantityPanel, BoxLayout.Y_AXIS));
        quantityPanel.setOpaque(false);
        quantityPanel.setPreferredSize(new Dimension(120, 40));

        // Quantity label with color
        JLabel quantityLabel = new JLabel(quantity + " units");
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 11));
        Color quantityColor;
        if (fillRatio >= 0.6) {
            quantityColor = new Color(76, 175, 80); // Green
        } else if (fillRatio >= 0.3) {
            quantityColor = new Color(255, 152, 0); // Orange
        } else {
            quantityColor = new Color(255, 107, 107); // Red
        }
        quantityLabel.setForeground(quantityColor);
        quantityLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        quantityPanel.add(quantityLabel);

        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) (fillRatio * 100));
        progressBar.setPreferredSize(new Dimension(100, 10));
        progressBar.setMaximumSize(new Dimension(100, 10));
        progressBar.setForeground(quantityColor);
        progressBar.setBorderPainted(true);
        quantityPanel.add(Box.createVerticalGlue());
        quantityPanel.add(progressBar);

        itemPanel.add(quantityPanel, BorderLayout.EAST);

        inventoryListPanel.add(itemPanel);
    }

    // TODO [STATE-ACCESS]: Uncomment to populate from storage
    /* private void populateInventoryFromStorage() {
         if (storage == null) return;
    
         inventoryListPanel.removeAll();
    
         // Iterate through all storage cells
         Map<String, List<BeveragesBox>> beverageGroups = groupBeveragesByType();
    
         for (Map.Entry<String, List<BeveragesBox>> entry : beverageGroups.entrySet()) {
             String beverageName = entry.getKey();
             List<BeveragesBox> boxes = entry.getValue();
             int quantity = boxes.size();
    
             // Get location and icon from first box
             BeveragesBox firstBox = boxes.get(0);
             String icon = getBeverageIcon(firstBox.getType());
             String location = getBeverageLocations(beverageName);
             int maxQuantity = getMaxCapacityForBeverage(beverageName);
             double fillRatio = (double) quantity / maxQuantity;
    
             addInventoryItem(icon, beverageName, location, quantity, maxQuantity, fillRatio);
         }
    
         inventoryListPanel.revalidate();
         inventoryListPanel.repaint();
     } */

    // TODO [STATE-ACCESS]: Uncomment to update summary from storage
    /* private void updateSummaryFromStorage() {
         if (storage == null) return;
    
         // Calculate total units
         int totalUnits = 0;
         Set<String> beverageTypes = new HashSet<>();
         int lowStockCount = 0;
         int totalCapacity = 0;
         int usedCapacity = 0;
    
         // Iterate through all cells
         for (StorageCell cell : getAllStorageCells()) {
             totalUnits += cell.getBoxCount();
             totalCapacity += cell.MAX_LENGTH * cell.MAX_WIDTH * cell.MAX_HEIGHT;
             usedCapacity += cell.getCurrentLength() * cell.getCurrentWidth() * cell.getCurrentHeight();
    
             // Count unique beverage types
             // Check if stock is low (< 30%)
         }
    
         // Update summary panel labels
         updateSummaryItem(0, String.valueOf(totalUnits), "TOTAL UNITS");
         updateSummaryItem(1, String.valueOf(beverageTypes.size()), "BEVERAGE TYPES");
         updateSummaryItem(2, String.valueOf(lowStockCount), "LOW STOCK");
         updateSummaryItem(3, String.format("%.0f%%", (double) usedCapacity / totalCapacity * 100), "CAPACITY");
     } */

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 10));

        JButton exportButton = new JButton("Export Report");
        // TODO [OBSERVABILITY]: Uncomment to implement export functionality
        // exportButton.addActionListener(e -> exportInventoryReport());
        buttonPanel.add(exportButton);

        JButton restockButton = new JButton("Restock All Low Items");
        // TODO [CONTROL-BINDING]: Uncomment to trigger restock
        // restockButton.addActionListener(e -> restockLowItems());
        buttonPanel.add(restockButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        return buttonPanel;
    }

    // TODO [OBSERVABILITY]: Uncomment to implement export
    /* private void exportInventoryReport() {
         // Export inventory to CSV or PDF
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.setDialogTitle("Export Inventory Report");
         int result = fileChooser.showSaveDialog(this);
         if (result == JFileChooser.APPROVE_OPTION) {
             File file = fileChooser.getSelectedFile();
             // Write inventory data to file
         }
     } */

    // TODO [CONTROL-BINDING]: Uncomment to implement restock
    /* private void restockLowItems() {
         // Create restocking tasks for low inventory items
         int itemsRestocked = 0;
         // Logic to identify and restock low items
    
         JOptionPane.showMessageDialog(this,
             itemsRestocked + " items scheduled for restocking",
             "Restock Initiated",
             JOptionPane.INFORMATION_MESSAGE);
     } */
}
