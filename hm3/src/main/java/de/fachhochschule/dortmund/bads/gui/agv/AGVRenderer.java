package de.fachhochschule.dortmund.bads.gui.agv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import de.fachhochschule.dortmund.bads.resources.AGV;

/**
 * Renderer for AGV visual components
 */
public class AGVRenderer {
    
    private static final int BODY_WIDTH = 40;
    private static final int BODY_HEIGHT = 30;
    private static final int WHEEL_RADIUS = 4;
    private static final int CARGO_WIDTH = 25;
    private static final int CARGO_HEIGHT = 15;
    private static final int BATTERY_WIDTH = 24;
    private static final int BATTERY_HEIGHT = 10;
    
    private static final Color WHEEL_COLOR = new Color(51, 51, 51);
    private static final Color CARGO_COLOR = new Color(139, 69, 19);
    private static final Color CARGO_BORDER = new Color(101, 50, 14);
    private static final Color CARGO_DETAIL = new Color(200, 150, 100);
    
    public static void drawBody(Graphics2D g2d, int centerX, int centerY, AGV.AGVState state) {
        int x = centerX - BODY_WIDTH / 2;
        int y = centerY - BODY_HEIGHT / 2;
        
        Color bodyColor = getColorForState(state);
        g2d.setColor(bodyColor);
        RoundRectangle2D body = new RoundRectangle2D.Double(x, y, BODY_WIDTH, BODY_HEIGHT, 5, 5);
        g2d.fill(body);
        
        g2d.setColor(bodyColor.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(body);
        
        g2d.setColor(bodyColor.darker().darker());
        g2d.drawLine(x + 5, centerY, x + BODY_WIDTH - 5, centerY);
    }
    
    public static void drawWheels(Graphics2D g2d, int centerX, int centerY) {
        g2d.setColor(WHEEL_COLOR);
        int y = centerY + BODY_HEIGHT / 2 + 3 - WHEEL_RADIUS;
        g2d.fillOval(centerX - BODY_WIDTH / 2 + 5 - WHEEL_RADIUS, y, WHEEL_RADIUS * 2, WHEEL_RADIUS * 2);
        g2d.fillOval(centerX + BODY_WIDTH / 2 - 5 - WHEEL_RADIUS, y, WHEEL_RADIUS * 2, WHEEL_RADIUS * 2);
    }
    
    public static void drawCargo(Graphics2D g2d, int centerX, int centerY) {
        int x = centerX - CARGO_WIDTH / 2;
        int y = centerY - 30;
        
        g2d.setColor(CARGO_COLOR);
        g2d.fillRect(x, y, CARGO_WIDTH, CARGO_HEIGHT);
        
        g2d.setColor(CARGO_BORDER);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(x, y, CARGO_WIDTH, CARGO_HEIGHT);
        
        g2d.setColor(CARGO_DETAIL);
        g2d.drawLine(x, y + CARGO_HEIGHT / 2, x + CARGO_WIDTH, y + CARGO_HEIGHT / 2);
    }
    
    public static void drawBattery(Graphics2D g2d, int centerX, int centerY, int batteryLevel) {
        int x = centerX + 25;
        int y = centerY - 25;
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(x, y, BATTERY_WIDTH, BATTERY_HEIGHT);
        g2d.fillRect(x + BATTERY_WIDTH, y + 3, 2, 4);
        
        Color fillColor = batteryLevel > 60 ? new Color(76, 175, 80) 
            : batteryLevel > 30 ? new Color(255, 152, 0) 
            : new Color(244, 67, 54);
        
        int fillWidth = (int) (BATTERY_WIDTH * (batteryLevel / 100.0));
        g2d.setColor(fillColor);
        g2d.fillRect(x + 1, y + 1, fillWidth - 1, BATTERY_HEIGHT - 1);
        
        drawCenteredText(g2d, batteryLevel + "%", x - 5, y + BATTERY_HEIGHT - 2, 
            new Font("Arial", Font.BOLD, 8), Color.BLACK, true);
    }
    
    public static void drawState(Graphics2D g2d, int centerX, int centerY, AGV.AGVState state) {
        if (state == null || state == AGV.AGVState.IDLE) return;
        
        String text = switch (state) {
            case CHARGING -> "CHG";
            case BUSY -> "BSY";
            case MOVING_TO_CHARGE -> "->C";
            case WAITING_FOR_CHARGE -> "WAIT";
            default -> "";
        };
        
        if (!text.isEmpty()) {
            drawCenteredText(g2d, text, centerX, centerY - 20, 
                new Font("Arial", Font.BOLD, 9), Color.BLACK, false);
        }
    }
    
    public static void drawId(Graphics2D g2d, int centerX, int centerY, String agvId) {
        drawCenteredText(g2d, agvId, centerX, centerY + 4, 
            new Font("Arial", Font.BOLD, 10), Color.WHITE, false);
    }
    
    private static void drawCenteredText(Graphics2D g2d, String text, int x, int y, 
                                        Font font, Color color, boolean rightAlign) {
        g2d.setColor(color);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = rightAlign ? x : x - fm.stringWidth(text) / 2;
        g2d.drawString(text, textX, y);
    }
    
    private static Color getColorForState(AGV.AGVState state) {
        return switch (state) {
            case CHARGING -> new Color(76, 175, 80);
            case BUSY -> new Color(33, 150, 243);
            case MOVING_TO_CHARGE -> new Color(255, 193, 7);
            case WAITING_FOR_CHARGE -> new Color(255, 152, 0);
            default -> new Color(255, 152, 0);
        };
    }
}
