package de.fachhochschule.dortmund.bads.gui.inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.model.Area;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;

/**
 * Handles data aggregation and calculations for inventory display
 */
public class InventoryDataProvider {
    private static final Logger LOGGER = LogManager.getLogger(InventoryDataProvider.class);
    private final Storage storage;

    public InventoryDataProvider(Storage storage) {
        this.storage = storage;
    }

    public Map<String, BeverageGroup> groupBeveragesByType() {
        Map<String, BeverageGroup> groups = new HashMap<>();
        if (storage == null) return groups;

        Map<Area.Point, StorageCell> allCells = storage.getAllStorages();
        LOGGER.debug("Processing {} storage cells for inventory grouping", allCells.size());
        
        for (Map.Entry<Area.Point, StorageCell> entry : allCells.entrySet()) {
            if (isStorageCell(entry.getValue())) {
                processCell(entry, groups);
            }
        }
        
        LOGGER.debug("Grouped beverages into {} types", groups.size());
        return groups;
    }

    public InventorySummary calculateSummary() {
        if (storage == null) return new InventorySummary(0, 0, 0, 0.0);

        Map<Area.Point, StorageCell> allCells = storage.getAllStorages();
        int totalUnits = 0;
        Set<String> beverageTypes = new HashSet<>();
        long totalCapacity = 0;
        long usedCapacity = 0;
        Map<String, Integer> beverageQuantities = new HashMap<>();

        for (StorageCell cell : allCells.values()) {
            if (!isStorageCell(cell)) continue;
            
            totalCapacity += (long) cell.MAX_LENGTH * cell.MAX_WIDTH * cell.MAX_HEIGHT;
            usedCapacity += cell.getActualUsedVolume();
            
            for (BeveragesBox box : cell.getStoredBoxes()) {
                totalUnits += (int) box.getQuantity();
                String bevName = box.getBeverageName();
                beverageTypes.add(bevName);
                beverageQuantities.merge(bevName, (int) box.getQuantity(), Integer::sum);
            }
        }

        int lowStockCount = (int) beverageQuantities.values().stream().filter(qty -> qty < 20).count();
        double capacity = totalCapacity > 0 ? (double) usedCapacity / totalCapacity * 100 : 0;

        return new InventorySummary(totalUnits, beverageTypes.size(), lowStockCount, capacity);
    }

    private boolean isStorageCell(StorageCell cell) {
        return cell.TYPE != StorageCell.Type.CORRIDOR && cell.TYPE != StorageCell.Type.CHARGING_STATION;
    }

    private void processCell(Map.Entry<Area.Point, StorageCell> entry, Map<String, BeverageGroup> groups) {
        Area.Point location = entry.getKey();
        StorageCell cell = entry.getValue();
        
        for (BeveragesBox box : cell.getStoredBoxes()) {
            String key = box.getBeverageName();
            BeverageGroup group = groups.computeIfAbsent(key, 
                _ -> new BeverageGroup(
                    Storage.pointToNotation(location) + " (" + getCellTypeString(cell.TYPE) + ")", 
                    0, 50, box.getType()));
            
            group.quantity += (int) box.getQuantity();
            updateLocationRange(group, Storage.pointToNotation(location));
        }
    }

    private void updateLocationRange(BeverageGroup group, String currentLoc) {
        if (!group.location.contains(currentLoc)) {
            int parenIndex = group.location.indexOf(" (");
            String firstLoc = group.location.substring(0, parenIndex > 0 ? parenIndex : group.location.length());
            String type = parenIndex > 0 ? group.location.substring(parenIndex) : "";
            
            if (!firstLoc.equals(currentLoc)) {
                if (firstLoc.contains("-")) {
                    group.location = firstLoc.substring(0, firstLoc.indexOf("-") + 1) + currentLoc + type;
                } else {
                    group.location = firstLoc + "-" + currentLoc + type;
                }
            }
        }
    }

    private String getCellTypeString(StorageCell.Type type) {
        return switch (type) {
            case REFRIGERATED -> "Refrigerated";
            case AMBIENT -> "Ambient";
            case BULK -> "Bulk Storage";
            default -> "Storage";
        };
    }

    public static class BeverageGroup {
        public String location;
        public int quantity;
        public int maxQuantity;
        public BeveragesBox.Type type;
        
        public BeverageGroup(String location, int quantity, int maxQuantity, BeveragesBox.Type type) {
            this.location = location;
            this.quantity = quantity;
            this.maxQuantity = maxQuantity;
            this.type = type;
        }

        public double getFillRatio() {
            return maxQuantity > 0 ? (double) quantity / maxQuantity : 0.0;
        }
    }

    public record InventorySummary(int totalUnits, int beverageTypes, int lowStockCount, double capacityPercent) {}
}
