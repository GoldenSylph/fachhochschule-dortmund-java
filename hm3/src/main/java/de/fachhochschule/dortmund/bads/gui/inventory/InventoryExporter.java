package de.fachhochschule.dortmund.bads.gui.inventory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.gui.inventory.InventoryDataProvider.BeverageGroup;

/**
 * Handles inventory report export functionality
 */
public class InventoryExporter {
    private static final Logger LOGGER = LogManager.getLogger(InventoryExporter.class);

    public static void exportToCSV(java.awt.Component parent, Map<String, BeverageGroup> groups) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Inventory Report");
        fileChooser.setSelectedFile(new File("inventory_report.csv"));
        
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Beverage Name,Location,Quantity,Max Quantity,Fill Ratio\n");
                
                if (groups != null && !groups.isEmpty()) {
                    for (Map.Entry<String, BeverageGroup> entry : groups.entrySet()) {
                        BeverageGroup group = entry.getValue();
                        writer.write(String.format("%s,%s,%d,%d,%.2f\n", 
                            entry.getKey(), group.location, group.quantity, 
                            group.maxQuantity, group.getFillRatio()));
                    }
                    LOGGER.info("Exported {} beverage types to {}", groups.size(), file.getAbsolutePath());
                } else {
                    writeSampleData(writer);
                    LOGGER.info("Exported sample data to {}", file.getAbsolutePath());
                }
                
                showSuccessDialog(parent, file);
            } catch (IOException ex) {
                LOGGER.error("Error exporting inventory report", ex);
                showErrorDialog(parent, ex);
            }
        }
    }

    private static void writeSampleData(FileWriter writer) throws IOException {
        writer.write("Beer - Premium Lager,B01-B08,45,50,0.90\n");
        writer.write("Juice - Orange,J01-J07,38,50,0.76\n");
    }

    private static void showSuccessDialog(java.awt.Component parent, File file) {
        JOptionPane.showMessageDialog(parent,
            "Inventory report exported successfully to:\n" + file.getAbsolutePath(),
            "Export Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showErrorDialog(java.awt.Component parent, IOException ex) {
        JOptionPane.showMessageDialog(parent,
            "Error exporting report: " + ex.getMessage(),
            "Export Error", JOptionPane.ERROR_MESSAGE);
    }
}
