package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import de.fachhochschule.dortmund.bads.model.Task;
import de.fachhochschule.dortmund.bads.systems.logic.TaskManagement;
import de.fachhochschule.dortmund.bads.systems.logic.utils.ITickable;
import de.fachhochschule.dortmund.bads.systems.Process;
import de.fachhochschule.dortmund.bads.systems.Operation;
import de.fachhochschule.dortmund.bads.resources.BeveragesBox;

/**
 * Panel for managing orders with form and table display
 */
public class OrderManagementPanel extends JPanel implements ITickable {
	private static final long serialVersionUID = 2248896897692353619L;

	private JTextField customerField;
    private JComboBox<String> beverageCombo;
    private JTextField quantityField;
    private JComboBox<String> priorityCombo;
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;

    // Backend dependency
    private TaskManagement taskManagement;

    public OrderManagementPanel() {
        initializeComponents();
    }

    /**
     * Set the task management system (dependency injection)
     */
    public void setTaskManagement(TaskManagement taskManagement) {
        this.taskManagement = taskManagement;
        // Immediately refresh table when TaskManagement is connected
        if (taskManagement != null) {
            SwingUtilities.invokeLater(this::refresh);
        }
    }

    @Override
    public void onTick(int currentTick) {
        // Update UI every 10 ticks to reduce overhead
        if (currentTick % 10 == 0) {
            SwingUtilities.invokeLater(this::refresh);
        }
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header with title and stats
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Order Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        statsLabel = new JLabel("Tasks: 0 | Pending: 0 | Running: 0 | Completed: 0");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statsLabel.setForeground(new Color(100, 100, 100));
        headerPanel.add(statsLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));

        JPanel formPanel = createOrderForm();
        mainPanel.add(formPanel, BorderLayout.NORTH);

        JScrollPane tableScrollPane = createOrderTable();
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createOrderForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("New Order"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);

        // Customer field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Customer:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        customerField = new JTextField(20);
        customerField.setText(""); // Start empty instead of placeholder
        formPanel.add(customerField, gbc);

        // Beverage combo
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Beverage:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        beverageCombo = new JComboBox<>(new String[]{
            "Beer - Premium Lager",
            "Soda - Cola",
            "Water - Mineral",
            "Juice - Orange",
            "Wine - Red",
            "Spirits - Vodka"
        });
        formPanel.add(beverageCombo, gbc);

        // Quantity and Priority
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Quantity:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        quantityField = new JTextField("100", 5);
        formPanel.add(quantityField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Priority:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.3;
        priorityCombo = new JComboBox<>(new String[]{"Low (5)", "Normal (7)", "High (9)", "Express (10)"});
        priorityCombo.setSelectedIndex(1); // Default to Normal
        formPanel.add(priorityCombo, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JButton createButton = new JButton("Create Order");
        createButton.addActionListener(_ -> createOrder());
        buttonPanel.add(createButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(_ -> refresh());
        buttonPanel.add(refreshButton);

        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    private JScrollPane createOrderTable() {
        String[] columnNames = {"Task ID", "Priority", "Processes", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderTable = new JTable(tableModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.setRowHeight(25);

        orderTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        orderTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        orderTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        orderTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Active Tasks"));

        return scrollPane;
    }

    /**
     * Refresh the task table and stats from TaskManagement
     */
    public void refresh() {
        refreshTaskTable();
        updateStats();
    }

    private void refreshTaskTable() {
        if (taskManagement == null) {
            tableModel.setRowCount(0);
            return;
        }

        tableModel.setRowCount(0);
        
        List<Task> tasks = taskManagement.getAllTasks();
        for (Task task : tasks) {
            Object[] row = {
                "T-" + task.getTaskId(),
                task.getTaskPriority(),
                task.getProcessCount(),
                getStatusString(task)
            };
            tableModel.addRow(row);
        }
    }

    private void updateStats() {
        if (taskManagement == null) {
            statsLabel.setText("Tasks: 0 | Pending: 0 | Running: 0 | Completed: 0");
            return;
        }

        List<Task> tasks = taskManagement.getAllTasks();
        int total = tasks.size();
        int pending = 0;
        int running = 0;
        int completed = 0;

        for (Task task : tasks) {
            Thread.State state = task.getState();
            switch (state) {
                case NEW -> pending++;
                case RUNNABLE, BLOCKED, WAITING, TIMED_WAITING -> running++;
                case TERMINATED -> completed++;
            }
        }

        statsLabel.setText(String.format(
            "Tasks: %d | Pending: %d | Running: %d | Completed: %d",
            total, pending, running, completed
        ));
    }

    private String getStatusString(Task task) {
        Thread.State state = task.getState();
        return switch (state) {
            case NEW -> "⏳ Pending";
            case RUNNABLE -> "▶ Running";
            case BLOCKED, WAITING, TIMED_WAITING -> "⏸ Waiting";
            case TERMINATED -> "✓ Completed";
        };
    }

    private void createOrder() {
        try {
            String customer = customerField.getText().trim();
            String beverageSelection = (String) beverageCombo.getSelectedItem();
            String quantityText = quantityField.getText().trim();
            
            // Validate inputs
            if (customer.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a customer name", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "Quantity must be greater than 0", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get priority from combo box
            int priorityIndex = priorityCombo.getSelectedIndex();
            int priority = switch (priorityIndex) {
                case 0 -> 5;  // Low
                case 1 -> 7;  // Normal
                case 2 -> 9;  // High
                case 3 -> 10; // Express
                default -> 7;
            };

            if (taskManagement == null) {
                JOptionPane.showMessageDialog(this,
                    "Task management system not connected",
                    "System Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create task with priority
            Task task = new Task(priority);
            
            // Parse beverage details and create BeveragesBox
            BeveragesBox.Type boxType = determineBeverageType(beverageSelection);
            String beverageName = extractBeverageName(beverageSelection);
            
            // Create BeveragesBox resource
            // Standard box dimensions: 30x30x40 cm
            BeveragesBox beverageBox = new BeveragesBox(
                boxType, 
                beverageName, 
                30,  // width
                30,  // height
                40,  // length
                quantity
            );
            
            // Create Operation and add the beverage box as a resource
            Operation operation = new Operation();
            operation.addResource(beverageBox);
            
            // Create Process and add the operation
            Process process = new Process();
            process.addOperation(operation);
            
            // Add process to task
            task.addProcess(process);

            // Submit to TaskManagement
            boolean added = taskManagement.addTask(task);
            
            if (added) {
                // Show confirmation
                JOptionPane.showMessageDialog(this,
                    String.format(
                        "Order created successfully!\n\n" +
                        "Task ID: T-%d\n" +
                        "Customer: %s\n" +
                        "Beverage: %s\n" +
                        "Quantity: %d units\n" +
                        "Priority: %d (%s)\n" +
                        "Box Type: %s",
                        task.getTaskId(), customer, beverageName, quantity, priority,
                        priorityCombo.getSelectedItem(), boxType
                    ),
                    "Order Created",
                    JOptionPane.INFORMATION_MESSAGE);

                // Clear form for next order
                customerField.setText("");
                quantityField.setText("100");
                priorityCombo.setSelectedIndex(1);

                // Refresh table
                refresh();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to create task",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Quantity must be a valid number",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error creating order: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Determine the beverage box type based on the beverage selection
     */
    private BeveragesBox.Type determineBeverageType(String beverageSelection) {
        if (beverageSelection == null) {
            return BeveragesBox.Type.AMBIENT;
        }
        
        String lower = beverageSelection.toLowerCase();
        if (lower.contains("beer") || lower.contains("soda") || lower.contains("water")) {
            return BeveragesBox.Type.REFRIGERATED;
        } else if (lower.contains("juice") || lower.contains("wine")) {
            return BeveragesBox.Type.REFRIGERATED;
        } else if (lower.contains("spirits")) {
            return BeveragesBox.Type.AMBIENT;
        }
        
        return BeveragesBox.Type.AMBIENT;
    }
    
    /**
     * Extract the beverage name from the combo box selection
     */
    private String extractBeverageName(String beverageSelection) {
        if (beverageSelection == null || !beverageSelection.contains(" - ")) {
            return beverageSelection != null ? beverageSelection : "Unknown";
        }
        
        // Extract the part after " - "
        String[] parts = beverageSelection.split(" - ", 2);
        return parts.length > 1 ? parts[1] : beverageSelection;
    }
}