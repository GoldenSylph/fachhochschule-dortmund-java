package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Custom component for visualizing an AGV (Automated Guided Vehicle)
 *
 * INTEGRATION POINTS:
 * - Receive AGV instance to read battery level, position, cargo status
 * - Update visual state when AGV state changes
 * - Implement animation for movement
 */
public class AGVComponent extends JPanel {

    private String agvId;
    private int batteryLevel;      // 0-100
    private boolean hasCargo;
    private boolean isMoving;
    private int animationOffset;   // For movement animation

    // TODO [CONCURRENCY]: Uncomment to receive AGV reference
    // private AGV agv;

    public AGVComponent(String agvId, int batteryLevel, boolean hasCargo) {
        // TODO [CONCURRENCY]: Uncomment to use real AGV instance
        /* public AGVComponent(AGV agv) {
             this.agv = agv;
             this.agvId = /* get from agv */;/*
             updateFromAGV();
             initializeComponent();
         } */

        this.agvId = agvId;
        this.batteryLevel = batteryLevel;
        this.hasCargo = hasCargo;
        this.isMoving = false;
        this.animationOffset = 0;

        initializeComponent();
    }

    private void initializeComponent() {
        setPreferredSize(new Dimension(80, 80));
        setOpaque(false);

        // TODO [CONCURRENCY]: Uncomment to start animation timer
        /* Timer animationTimer = new Timer(100, e -> {
             if (isMoving) {
                 animationOffset = (animationOffset + 5) % 50;
                 repaint();
             }
         });
         animationTimer.start(); */
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Draw AGV body
        drawAGVBody(g2d, centerX, centerY);

        // Draw wheels
        drawWheels(g2d, centerX, centerY);

        // Draw cargo if present
        if (hasCargo) {
            drawCargo(g2d, centerX, centerY);
        }

        // Draw battery indicator
        drawBatteryIndicator(g2d, centerX, centerY);

        // Draw AGV ID label
        drawIdLabel(g2d, centerX, centerY);
    }

    private void drawAGVBody(Graphics2D g2d, int centerX, int centerY) {
        // AGV body - orange rectangle with rounded corners
        int bodyWidth = 40;
        int bodyHeight = 30;
        int bodyX = centerX - bodyWidth / 2;
        int bodyY = centerY - bodyHeight / 2;

        // Main body
        g2d.setColor(new Color(255, 152, 0)); // Orange
        RoundRectangle2D body = new RoundRectangle2D.Double(bodyX, bodyY, bodyWidth, bodyHeight, 5, 5);
        g2d.fill(body);

        // Border
        g2d.setColor(new Color(245, 124, 0)); // Darker orange
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(body);

        // Add some detail lines
        g2d.setColor(new Color(230, 100, 0));
        g2d.drawLine(bodyX + 5, centerY, bodyX + bodyWidth - 5, centerY);
    }

    private void drawWheels(Graphics2D g2d, int centerX, int centerY) {
        int bodyWidth = 40;
        int bodyHeight = 30;
        int wheelRadius = 4;

        // Left wheels
        g2d.setColor(new Color(51, 51, 51));
        g2d.fillOval(centerX - bodyWidth / 2 + 5 - wheelRadius,
                     centerY + bodyHeight / 2 + 3 - wheelRadius,
                     wheelRadius * 2, wheelRadius * 2);
        g2d.fillOval(centerX + bodyWidth / 2 - 5 - wheelRadius,
                     centerY + bodyHeight / 2 + 3 - wheelRadius,
                     wheelRadius * 2, wheelRadius * 2);
    }

    private void drawCargo(Graphics2D g2d, int centerX, int centerY) {
        int cargoWidth = 25;
        int cargoHeight = 15;
        int cargoX = centerX - cargoWidth / 2;
        int cargoY = centerY - 30; // Above AGV

        // Cargo box
        g2d.setColor(new Color(139, 69, 19)); // Brown
        g2d.fillRect(cargoX, cargoY, cargoWidth, cargoHeight);

        // Border
        g2d.setColor(new Color(101, 50, 14));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(cargoX, cargoY, cargoWidth, cargoHeight);

        // Tape lines
        g2d.setColor(new Color(200, 150, 100));
        g2d.drawLine(cargoX, cargoY + cargoHeight / 2, cargoX + cargoWidth, cargoY + cargoHeight / 2);
    }

    private void drawBatteryIndicator(Graphics2D g2d, int centerX, int centerY) {
        int batteryWidth = 24;
        int batteryHeight = 10;
        int batteryX = centerX + 25;
        int batteryY = centerY - 25;

        // Battery outline
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(batteryX, batteryY, batteryWidth, batteryHeight);

        // Battery terminal
        g2d.fillRect(batteryX + batteryWidth, batteryY + 3, 2, 4);

        // Battery fill based on level
        Color fillColor;
        if (batteryLevel > 60) {
            fillColor = new Color(76, 175, 80); // Green
        } else if (batteryLevel > 30) {
            fillColor = new Color(255, 152, 0); // Orange
        } else {
            fillColor = new Color(244, 67, 54); // Red
        }

        int fillWidth = (int) (batteryWidth * (batteryLevel / 100.0));
        g2d.setColor(fillColor);
        g2d.fillRect(batteryX + 1, batteryY + 1, fillWidth - 1, batteryHeight - 1);

        // Battery percentage text
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 8));
        String batteryText = batteryLevel + "%";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = batteryX - fm.stringWidth(batteryText) - 5;
        int textY = batteryY + batteryHeight - 2;
        g2d.drawString(batteryText, textX, textY);
    }

    private void drawIdLabel(Graphics2D g2d, int centerX, int centerY) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(agvId);
        int textX = centerX - textWidth / 2;
        int textY = centerY + 4;
        g2d.drawString(agvId, textX, textY);
    }

    // Public setters for updating AGV state
    public void setBatteryLevel(int level) {
        this.batteryLevel = Math.max(0, Math.min(100, level));
        repaint();
    }

    public void setHasCargo(boolean hasCargo) {
        this.hasCargo = hasCargo;
        repaint();
    }

    public void setMoving(boolean moving) {
        this.isMoving = moving;
        repaint();
    }

    public void setAgvId(String agvId) {
        this.agvId = agvId;
        repaint();
    }

    // TODO [STATE-ACCESS]: Uncomment to update from real AGV instance
    /* public void updateFromAGV() {
         if (agv != null) {
             SwingUtilities.invokeLater(() -> {
                 this.batteryLevel = agv.getBatteryLevel();
                 this.hasCargo = !agv.getInventoryCell().isEmpty();
                 // Check if AGV is moving by comparing positions over time
                 // this.isMoving = agv.isMoving();
                 repaint();
             });
         }
     }*/

    // TODO [TICK-LISTENER]: Uncomment if AGVComponent implements ITickable
    /* @Override
     public void onTick(int currentTick) {
         updateFromAGV();
     } */

    // Getters
    public String getAgvId() {
        return agvId;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public boolean hasCargo() {
        return hasCargo;
    }

    public boolean isMoving() {
        return isMoving;
    }
}
