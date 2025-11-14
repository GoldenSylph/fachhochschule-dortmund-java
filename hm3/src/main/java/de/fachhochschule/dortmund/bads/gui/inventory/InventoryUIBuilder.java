package de.fachhochschule.dortmund.bads.gui.inventory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import de.fachhochschule.dortmund.bads.resources.BeveragesBox;

/**
 * Utility class for building inventory UI components
 */
public class InventoryUIBuilder {
    
    private static final Color SUMMARY_BORDER = new Color(255, 193, 7);
    private static final Color SUMMARY_BG = new Color(255, 243, 205);
    private static final Color VALUE_COLOR = new Color(0, 0, 128);
    private static final Color GREEN = new Color(76, 175, 80);
    private static final Color ORANGE = new Color(255, 152, 0);
    private static final Color RED = new Color(255, 107, 107);

    public static JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 10, 10, 10),
            BorderFactory.createLineBorder(SUMMARY_BORDER)));
        panel.setBackground(SUMMARY_BG);
        return panel;
    }

    public static void addSummaryItem(JPanel panel, String value, String label) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setOpaque(false);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        valueLabel.setForeground(VALUE_COLOR);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        textLabel.setForeground(Color.GRAY);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        item.add(valueLabel);
        item.add(textLabel);
        panel.add(item);
    }

    public static JPanel createInventoryItem(String icon, String name, String location, 
                                            int quantity, double fillRatio) {
        JPanel itemPanel = new JPanel(new BorderLayout(15, 0));
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(212, 208, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        itemPanel.add(createIconLabel(icon), BorderLayout.WEST);
        itemPanel.add(createDetailsPanel(name, location), BorderLayout.CENTER);
        itemPanel.add(createQuantityPanel(quantity, fillRatio), BorderLayout.EAST);

        return itemPanel;
    }

    private static JLabel createIconLabel(String icon) {
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Dialog", Font.PLAIN, 24));
        iconLabel.setPreferredSize(new Dimension(40, 40));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return iconLabel;
    }

    private static JPanel createDetailsPanel(String name, String location) {
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

        return detailsPanel;
    }

    private static JPanel createQuantityPanel(int quantity, double fillRatio) {
        JPanel quantityPanel = new JPanel();
        quantityPanel.setLayout(new BoxLayout(quantityPanel, BoxLayout.Y_AXIS));
        quantityPanel.setOpaque(false);
        quantityPanel.setPreferredSize(new Dimension(120, 40));

        Color quantityColor = getStockColor(fillRatio);

        JLabel quantityLabel = new JLabel(quantity + " units");
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 11));
        quantityLabel.setForeground(quantityColor);
        quantityLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        quantityPanel.add(quantityLabel);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) (fillRatio * 100));
        progressBar.setPreferredSize(new Dimension(100, 10));
        progressBar.setMaximumSize(new Dimension(100, 10));
        progressBar.setForeground(quantityColor);
        progressBar.setBorderPainted(true);
        quantityPanel.add(Box.createVerticalGlue());
        quantityPanel.add(progressBar);

        return quantityPanel;
    }

    private static Color getStockColor(double fillRatio) {
        if (fillRatio >= 0.6) return GREEN;
        if (fillRatio >= 0.3) return ORANGE;
        return RED;
    }

    public static String getBeverageIcon(BeveragesBox.Type type) {
        return switch (type) {
            case REFRIGERATED -> "[R]";
            case AMBIENT -> "[A]";
            case BULK -> "[B]";
        };
    }
}
