package de.fachhochschule.dortmund.bads.hm1.bedrin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.model.StorageCell;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;

public class StorageCellTest {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static void main(String[] args) {
        LOGGER.info("=== Starting StorageCell 3D Space Management Test with Logging ===");
        
        // Create a storage cell with 10x10x10 dimensions
        StorageCell cell = new StorageCell(StorageCell.Type.AMBIENT, 10, 10, 10);
        
        LOGGER.info("Storage Cell created: 10x10x10 (Volume: {})", 10*10*10);
        
        // Test 1: Add boxes that fit perfectly in vertical stacking
        LOGGER.info("\n--- Test 1: Vertical stacking scenarios ---");
        BeveragesBox box1 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Coca Cola", 5, 3, 5, 24);
        BeveragesBox box2 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Pepsi", 5, 4, 5, 24);
        BeveragesBox box3 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Sprite", 5, 3, 5, 24);
        
        cell.add(box1);
        printCellStatus(cell);
        
        cell.add(box2);
        printCellStatus(cell);
        
        cell.add(box3);
        printCellStatus(cell);
        
        // Test 2: Try to add a box that doesn't fit
        LOGGER.info("\n--- Test 2: Trying to add oversized box (should fail) ---");
        BeveragesBox oversizedBox = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Large Box", 8, 8, 8, 12);
        boolean added = cell.add(oversizedBox);
        LOGGER.info("Oversized box addition result: {}", added);
        printCellStatus(cell);
        
        // Test 3: Remove a box and try again
        LOGGER.info("\n--- Test 3: Remove box and try to add different box ---");
        boolean removed = cell.remove(box2);
        LOGGER.info("Box2 removal result: {}", removed);
        printCellStatus(cell);
        
        BeveragesBox smallBox = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Small Box", 3, 2, 3, 6);
        boolean addedSmall = cell.add(smallBox);
        LOGGER.info("Small box addition result: {}", addedSmall);
        printCellStatus(cell);
        
        // Test 4: Side-by-side placement
        LOGGER.info("\n--- Test 4: Creating new cell for side-by-side test ---");
        StorageCell cell2 = new StorageCell(StorageCell.Type.AMBIENT, 10, 10, 10);
        
        BeveragesBox wideBox1 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Wide Box 1", 4, 5, 6, 12);
        BeveragesBox wideBox2 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Wide Box 2", 6, 5, 4, 12);
        
        cell2.add(wideBox1);
        printCellStatus(cell2);
        
        cell2.add(wideBox2);
        printCellStatus(cell2);
        
        // Test 5: Type compatibility test
        LOGGER.info("\n--- Test 5: Type compatibility test (should fail) ---");
        BeveragesBox refrigeratedBox = new BeveragesBox(BeveragesBox.Type.REFRIGERATED, "Cold Drink", 3, 3, 3, 12);
        boolean addedIncompatible = cell.add(refrigeratedBox);
        LOGGER.info("Incompatible type addition result: {}", addedIncompatible);
        
        // Test 6: Charging station test
        LOGGER.info("\n--- Test 6: Charging station test (should fail) ---");
        StorageCell chargingStation = new StorageCell(StorageCell.Type.CHARGING_STATION, 5, 5, 5);
        BeveragesBox anyBox = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Any Box", 2, 2, 2, 6);
        boolean addedToCharging = chargingStation.add(anyBox);
        LOGGER.info("Addition to charging station result: {}", addedToCharging);
        
        LOGGER.info("\n=== Test completed ===");
    }
    
    private static void printCellStatus(StorageCell cell) {
        LOGGER.info("Cell Status - Current dimensions: {}x{}x{}, Boxes: {}, Efficiency: {:.1f}%, Remaining volume: {}", 
                   cell.getCurrentLength(), cell.getCurrentWidth(), cell.getCurrentHeight(),
                   cell.getBoxCount(), cell.getSpaceEfficiency(), cell.getRemainingVolume());
    }
}