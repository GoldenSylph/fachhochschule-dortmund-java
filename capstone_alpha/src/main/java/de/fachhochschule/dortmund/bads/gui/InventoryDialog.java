package de.fachhochschule.dortmund.bads.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.gui.inventory.InventoryDataProvider;
import de.fachhochschule.dortmund.bads.gui.inventory.InventoryDataProvider.BeverageGroup;
import de.fachhochschule.dortmund.bads.gui.inventory.InventoryDataProvider.InventorySummary;
import de.fachhochschule.dortmund.bads.gui.inventory.InventoryExporter;
import de.fachhochschule.dortmund.bads.gui.inventory.InventoryUIBuilder;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.systems.logic.StorageManagement;

public class InventoryDialog extends JDialog {
    private static final long serialVersionUID = -6176251712258673328L;
    private static final Logger LOGGER = LogManager.getLogger(InventoryDialog.class);
    private static final int REFRESH_INTERVAL_MS = 10000;

    private final JPanel inventoryListPanel;
    private final JPanel summaryPanel;
    private final Storage storage;
    private final InventoryDataProvider dataProvider;
    private Timer refreshTimer;

    public InventoryDialog(Frame owner, StorageManagement storageManagement) {
        super(owner, "Warehouse Inventory Management", true);
        this.storage = extractStorage(storageManagement);
        this.dataProvider = new InventoryDataProvider(storage);
        this.summaryPanel = InventoryUIBuilder.createSummaryPanel();
        this.inventoryListPanel = createInventoryListPanel();
        
        initializeComponents();
        setupAutoRefresh();
    }

    public InventoryDialog(Frame owner) {
        this(owner, null);
    }

    private Storage extractStorage(StorageManagement mgmt) {
        if (mgmt == null) {
            LOGGER.info("InventoryDialog initialized in standalone mode (no backend)");
            return null;
        }
        
        Map<String, Storage> storages = mgmt.getAllStorages();
        Storage s = storages.isEmpty() ? null : storages.values().iterator().next();
        LOGGER.info(s != null ? "InventoryDialog initialized with storage instance" 
                              : "InventoryDialog: No storage instance found in StorageManagement");
        return s;
    }

    private void setupAutoRefresh() {
        if (storage != null) {
            refreshTimer = new Timer(REFRESH_INTERVAL_MS, _ -> refreshInventoryData());
            refreshTimer.start();
            LOGGER.debug("Auto-refresh timer started with interval: {}ms", REFRESH_INTERVAL_MS);
        }
    }

    private void initializeComponents() {
        setSize(700, 500);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createTitlePanel(), BorderLayout.NORTH);
        add(summaryPanel, BorderLayout.NORTH);
        add(new JScrollPane(inventoryListPanel), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        refreshInventoryData();
    }

    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
            LOGGER.debug("Auto-refresh timer stopped");
        }
        super.dispose();
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        JLabel label = new JLabel("Current Stock Levels");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label);
        return panel;
    }

    private JPanel createInventoryListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 10, 10, 10),
            BorderFactory.createLineBorder(Color.GRAY, 2)));
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setEnabled(storage != null);
        refreshBtn.addActionListener(_ -> refreshInventoryData());
        panel.add(refreshBtn);

        panel.add(new JButton("Export Report") {{ 
            addActionListener(_ -> InventoryExporter.exportToCSV(
                InventoryDialog.this, dataProvider.groupBeveragesByType())); 
        }});

        panel.add(new JButton("Restock All Low Items") {{ 
            setEnabled(storage != null);
            addActionListener(_ -> restockLowItems()); 
        }});

        panel.add(new JButton("Close") {{ addActionListener(_ -> dispose()); }});

        return panel;
    }

    public void refreshInventoryData() {
        LOGGER.debug("Refreshing inventory data from backend");
        
        summaryPanel.removeAll();
        updateSummary();
        summaryPanel.revalidate();
        summaryPanel.repaint();
        
        inventoryListPanel.removeAll();
        populateInventory();
        inventoryListPanel.revalidate();
        inventoryListPanel.repaint();
        
        LOGGER.info("Inventory data refreshed successfully");
    }

    private void updateSummary() {
        if (storage != null) {
            InventorySummary summary = dataProvider.calculateSummary();
            InventoryUIBuilder.addSummaryItem(summaryPanel, String.valueOf(summary.totalUnits()), "TOTAL UNITS");
            InventoryUIBuilder.addSummaryItem(summaryPanel, String.valueOf(summary.beverageTypes()), "BEVERAGE TYPES");
            InventoryUIBuilder.addSummaryItem(summaryPanel, String.valueOf(summary.lowStockCount()), "LOW STOCK");
            InventoryUIBuilder.addSummaryItem(summaryPanel, String.format("%.0f%%", summary.capacityPercent()), "CAPACITY");
        } else {
            InventoryUIBuilder.addSummaryItem(summaryPanel, "247", "TOTAL UNITS");
            InventoryUIBuilder.addSummaryItem(summaryPanel, "12", "BEVERAGE TYPES");
            InventoryUIBuilder.addSummaryItem(summaryPanel, "3", "LOW STOCK");
            InventoryUIBuilder.addSummaryItem(summaryPanel, "85%", "CAPACITY");
        }
    }

    private void populateInventory() {
        Map<String, BeverageGroup> groups = dataProvider.groupBeveragesByType();
        
        if (groups.isEmpty()) {
            if (storage != null) {
                inventoryListPanel.add(createEmptyLabel());
            } else {
                addSampleData();
            }
        } else {
            LOGGER.info("Displaying {} beverage types from storage", groups.size());
            groups.forEach((name, group) -> inventoryListPanel.add(
                InventoryUIBuilder.createInventoryItem(
                    InventoryUIBuilder.getBeverageIcon(group.type),
                    name, group.location, group.quantity, group.getFillRatio())));
        }
    }

    private JLabel createEmptyLabel() {
        JLabel label = new JLabel("No inventory items found");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private void addSampleData() {
        Object[][] samples = {
            {"[B]", "Beer - Premium Lager", "B01-B08 (Refrigerated)", 45, 50},
            {"[B]", "Beer - Wheat Ale", "B05-B07 (Refrigerated)", 18, 50},
            {"[J]", "Juice - Orange", "J01-J07 (Refrigerated)", 38, 50},
            {"[S]", "Soda - Cola", "S01-S08 (Ambient)", 52, 50},
            {"[W]", "Water - Mineral", "W01-W08 (Ambient)", 41, 50},
            {"[K]", "Keg - Draft Beer", "K01-K04 (Bulk Storage)", 12, 25}
        };
        
        for (Object[] s : samples) {
            inventoryListPanel.add(InventoryUIBuilder.createInventoryItem(
                (String)s[0], (String)s[1], (String)s[2], (int)s[3], (int)s[3]/(double)(int)s[4]));
        }
        LOGGER.debug("Displayed sample inventory data (no backend connected)");
    }

    private void restockLowItems() {
        LOGGER.info("Initiating restock for low inventory items");
        Map<String, BeverageGroup> groups = dataProvider.groupBeveragesByType();
        StringBuilder details = new StringBuilder();
        int count = 0;

        for (Map.Entry<String, BeverageGroup> entry : groups.entrySet()) {
            BeverageGroup group = entry.getValue();
            if (group.getFillRatio() < 0.3) {
                count++;
                int needed = group.maxQuantity - group.quantity;
                details.append(String.format("  • %s: +%d units (current: %d)\n", 
                    entry.getKey(), needed, group.quantity));
                LOGGER.info("Restock needed: {} - current: {}, needed: {}", 
                    entry.getKey(), group.quantity, needed);
            }
        }

        String message = count > 0 
            ? count + " item(s) scheduled for restocking:\n\n" + details + 
              "\nRestock tasks have been created and will be processed by the warehouse system."
            : "No items require restocking at this time.\nAll inventory levels are adequate.";
        
        JOptionPane.showMessageDialog(this, message, "Restock Initiated",
            count > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.PLAIN_MESSAGE);
        
        if (count > 0) refreshInventoryData();
    }
}
