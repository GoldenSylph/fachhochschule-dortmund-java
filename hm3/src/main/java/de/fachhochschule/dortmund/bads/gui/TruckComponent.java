package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Custom component for visualizing a delivery truck with cargo
 *
 * INTEGRATION POINTS:
 * - Receive Truck instance to read inventory and loading status
 * - Update cargo visualization based on truck capacity
 * - Show real-time loading progress
 */
public class TruckComponent extends JPanel {

    private String truckId;
    private int maxCapacity;       // Maximum number of cargo items
    private int currentLoad;       // Current number of cargo items
    private double loadProgress;   // 0.0 to 1.0

    // TODO [CONCURRENCY]: Uncomment to receive Truck reference
    // private Truck truck;

    public TruckComponent(String truckId, int maxCapacity, int currentLoad) {
        // TODO [CONCURRENCY]: Uncomment to use real Truck instance
        /* public TruckComponent(Truck truck) {
             this.truck = truck;
             this.truckId = /* get from truck */;
        //     updateFromTruck();
        //     initializeComponent();
        // } 

        this.truckId = truckId;
        this.maxCapacity = maxCapacity;
        this.currentLoad = currentLoad;
        this.loadProgress = maxCapacity > 0 ? (double) currentLoad / maxCapacity : 0.0;

        initializeComponent();
    }

    private void initializeComponent() {
        setPreferredSize(new Dimension(140, 70));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int startX = 10;
        int startY = 10;

        // Draw truck cab
        drawTruckCab(g2d, startX, startY);

        // Draw truck body/container
        drawTruckBody(g2d, startX + 30, startY);

        // Draw cargo inside
        drawCargo(g2d, startX + 30, startY);

        // Draw wheels
        drawWheels(g2d, startX, startY);

        // Draw truck ID label
        drawTruckLabel(g2d, startX);
    }

    private void drawTruckCab(Graphics2D g2d, int x, int y) {
        int cabWidth = 28;
        int cabHeight = 40;

        // Cab body
        g2d.setColor(new Color(102, 102, 102));
        g2d.fillRect(x, y + 10, cabWidth, cabHeight);

        // Cab border
        g2d.setColor(new Color(51, 51, 51));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y + 10, cabWidth, cabHeight);

        // Windshield
        g2d.setColor(new Color(135, 206, 250)); // Light blue
        g2d.fillRect(x + 5, y + 15, cabWidth - 10, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x + 5, y + 15, cabWidth - 10, 12);

        // Grille
        g2d.setColor(new Color(80, 80, 80));
        for (int i = 0; i < 4; i++) {
            g2d.drawLine(x + 8, y + 32 + i * 4, x + cabWidth - 8, y + 32 + i * 4);
        }
    }

    private void drawTruckBody(Graphics2D g2d, int x, int y) {
        int bodyWidth = 90;
        int bodyHeight = 50;

        // Container body
        g2d.setColor(new Color(153, 153, 153)); // Light gray
        Rectangle2D body = new Rectangle2D.Double(x, y, bodyWidth, bodyHeight);
        g2d.fill(body);

        // Container border
        g2d.setColor(new Color(102, 102, 102));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(body);

        // Door lines
        g2d.setColor(new Color(120, 120, 120));
        g2d.drawLine(x + bodyWidth - 20, y, x + bodyWidth - 20, y + bodyHeight);

        // Handle
        g2d.fillRect(x + bodyWidth - 15, y + bodyHeight / 2 - 5, 3, 10);
    }

    private void drawCargo(Graphics2D g2d, int x, int y) {
        if (currentLoad == 0) return;

        int itemWidth = 14;
        int itemHeight = 14;
        int padding = 2;
        int itemsPerRow = 5;
        int startX = x + 5;
        int startY = y + 5;

        g2d.setColor(new Color(139, 69, 19)); // Brown boxes
        g2d.setStroke(new BasicStroke(1));

        for (int i = 0; i < currentLoad && i < 15; i++) {  // Max 15 visible items
            int row = i / itemsPerRow;
            int col = i % itemsPerRow;
            int itemX = startX + col * (itemWidth + padding);
            int itemY = startY + row * (itemHeight + padding);

            // Box
            g2d.fillRect(itemX, itemY, itemWidth, itemHeight);

            // Border
            g2d.setColor(new Color(101, 50, 14));
            g2d.drawRect(itemX, itemY, itemWidth, itemHeight);

            // Tape
            g2d.setColor(new Color(200, 150, 100));
            g2d.drawLine(itemX, itemY + itemHeight / 2, itemX + itemWidth, itemY + itemHeight / 2);

            g2d.setColor(new Color(139, 69, 19));
        }
    }

    private void drawWheels(Graphics2D g2d, int x, int y) {
        int wheelRadius = 6;
        int wheelY = y + 52;

        g2d.setColor(new Color(40, 40, 40));

        // Cab wheels
        g2d.fillOval(x + 5 - wheelRadius, wheelY - wheelRadius, wheelRadius * 2, wheelRadius * 2);
        g2d.fillOval(x + 20 - wheelRadius, wheelY - wheelRadius, wheelRadius * 2, wheelRadius * 2);

        // Body wheels
        g2d.fillOval(x + 80 - wheelRadius, wheelY - wheelRadius, wheelRadius * 2, wheelRadius * 2);
        g2d.fillOval(x + 95 - wheelRadius, wheelY - wheelRadius, wheelRadius * 2, wheelRadius * 2);
        g2d.fillOval(x + 110 - wheelRadius, wheelY - wheelRadius, wheelRadius * 2, wheelRadius * 2);

        // Wheel rims
        g2d.setColor(new Color(180, 180, 180));
        g2d.fillOval(x + 5 - 3, wheelY - 3, 6, 6);
        g2d.fillOval(x + 20 - 3, wheelY - 3, 6, 6);
        g2d.fillOval(x + 80 - 3, wheelY - 3, 6, 6);
        g2d.fillOval(x + 95 - 3, wheelY - 3, 6, 6);
        g2d.fillOval(x + 110 - 3, wheelY - 3, 6, 6);
    }

    private void drawTruckLabel(Graphics2D g2d, int x) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString(truckId, x + 35, 10);
    }

    // Public setters for updating truck state
    public void setCurrentLoad(int load) {
        this.currentLoad = Math.max(0, Math.min(maxCapacity, load));
        this.loadProgress = maxCapacity > 0 ? (double) currentLoad / maxCapacity : 0.0;
        repaint();
    }

    public void setLoadProgress(double progress) {
        this.loadProgress = Math.max(0.0, Math.min(1.0, progress));
        this.currentLoad = (int) (maxCapacity * progress);
        repaint();
    }

    public void setTruckId(String truckId) {
        this.truckId = truckId;
        repaint();
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.loadProgress = maxCapacity > 0 ? (double) currentLoad / maxCapacity : 0.0;
        repaint();
    }

    // TODO [STATE-ACCESS]: Uncomment to update from real Truck instance
    /* public void updateFromTruck() {
         if (truck != null) {
             SwingUtilities.invokeLater(() -> {
                 StorageCell inventory = truck.getInventoryCell();
                 if (inventory != null) {
                     this.currentLoad = inventory.getBoxCount();
                     // Calculate max capacity from cell dimensions
                     int maxVolume = inventory.MAX_LENGTH * inventory.MAX_WIDTH * inventory.MAX_HEIGHT;
                     this.maxCapacity = maxVolume / 100; // Approximate item count
                     this.loadProgress = maxCapacity > 0 ? (double) currentLoad / maxCapacity : 0.0;
                 }
                 repaint();
             });
         }
     } */

    // TODO [TICK-LISTENER]: Uncomment if TruckComponent implements ITickable
    // @Override
    // public void onTick(int currentTick) {
    //     updateFromTruck();
    // }

    // Getters
    public String getTruckId() {
        return truckId;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    public double getLoadProgress() {
        return loadProgress;
    }
}
