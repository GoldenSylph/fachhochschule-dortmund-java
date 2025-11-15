package de.fachhochschule.dortmund.bads.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fachhochschule.dortmund.bads.model.Area;
import de.fachhochschule.dortmund.bads.model.Storage;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;
import de.fachhochschule.dortmund.bads.resources.Truck;

/**
 * Logistics Control Panel - Shows truck inventory
 */
public class LogisticsControlPanel extends JDialog {
    private static final long serialVersionUID = -4829107482938475829L;
    private static final Logger LOGGER = LogManager.getLogger(LogisticsControlPanel.class);
    
    private List<Truck> trucks;
    
    public LogisticsControlPanel(Frame owner, Area cityArea, Storage warehouse, 
                                  List<Truck> trucks) {
        super(owner, "Logistics Control Panel", true);
        this.trucks = trucks != null ? trucks : new ArrayList<>();
        LOGGER.info("Initializing Logistics Control Panel - Trucks: {}", this.trucks.size());
        initializeComponents();
        setSize(700, 500);
        setLocationRelativeTo(owner);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        add(createTruckOverviewPanel(), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(_ -> refreshTruckOverview());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(_ -> dispose());
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTruckOverviewPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Truck Overview"));
        if (trucks.isEmpty()) {
            panel.add(new JLabel("No trucks available."));
            return panel;
        }
        for (int i = 0; i < trucks.size(); i++) {
            Truck truck = trucks.get(i);
            JPanel truckPanel = new JPanel(new BorderLayout(10, 10));
            truckPanel.setBorder(BorderFactory.createTitledBorder("Truck " + (i + 1)));
            // Truck position
            String posStr = getTruckPositionString(truck);
            JLabel posLabel = new JLabel("Current Position: " + posStr);
            truckPanel.add(posLabel, BorderLayout.NORTH);
            // Inventory
            DefaultListModel<String> invModel = new DefaultListModel<>();
            if (truck.getInventoryCell() != null && !truck.getInventoryCell().getStoredBoxes().isEmpty()) {
                for (BeveragesBox box : truck.getInventoryCell().getStoredBoxes()) {
                    invModel.addElement(box.getBeverageName() + " (" + box.getType() + ") - " + box.getQuantity() + " bottles");
                }
            } else {
                invModel.addElement("Empty - No cargo loaded");
            }
            JList<String> invList = new JList<>(invModel);
            invList.setVisibleRowCount(3);
            truckPanel.add(new JScrollPane(invList), BorderLayout.CENTER);
            panel.add(truckPanel);
        }
        return panel;
    }

    private String getTruckPositionString(Truck truck) {
        // If truck has a route and has started moving (currentLocationIdx > 0)
        if (truck.getRoute() != null && !truck.getRoute().isEmpty() && 
            truck.getCurrentLocationIdx() > 0 && 
            truck.getCurrentLocationIdx() <= truck.getRoute().size()) {
            Area.Point p = truck.getRoute().get(truck.getCurrentLocationIdx() - 1);
            return "(" + p.x() + ", " + p.y() + ")";
        } 
        // If truck has a route but hasn't started moving yet (currentLocationIdx == 0)
        else if (truck.getRoute() != null && !truck.getRoute().isEmpty() && 
                 truck.getCurrentLocationIdx() == 0) {
            // Show start point or first route point
            if (truck.getStartPoint() != null) {
                Area.Point p = truck.getStartPoint();
                return "(" + p.x() + ", " + p.y() + ") - At Start";
            } else if (!truck.getRoute().isEmpty()) {
                Area.Point p = truck.getRoute().get(0);
                return "(" + p.x() + ", " + p.y() + ") - Route Start";
            }
        }
        // Fallback to start point if route not available
        else if (truck.getStartPoint() != null) {
            Area.Point p = truck.getStartPoint();
            return "(" + p.x() + ", " + p.y() + ") - Parked";
        }
        
        return "Not Deployed";
    }

    private void refreshTruckOverview() {
        getContentPane().removeAll();
        initializeComponents();
        revalidate();
        repaint();
    }
}