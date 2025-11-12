package de.fachhochschule.dortmund.bads.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel for managing orders with form and table display
 *
 * INTEGRATION POINTS:
 * - Connect to TaskManagement to create and process tasks
 * - Table should display active tasks from TaskManagement.getTasks()
 * - Create Order button should create Task with proper Process/Operation structure
 */
public class OrderManagementPanel extends JPanel /* implements ITickable */ {

    private JTextField customerField;
    private JComboBox<String> beverageCombo;
    private JTextField quantityField;
    private JComboBox<String> priorityCombo;
    private JTable orderTable;
    private DefaultTableModel tableModel;

    // TODO [CONCURRENCY]: Uncomment to receive TaskManagement reference
    // private TaskManagement taskManagement;

    public OrderManagementPanel() {
        // TODO [CONCURRENCY]: Uncomment to receive TaskManagement reference
        // public OrderManagementPanel(TaskManagement taskManagement) {
        //     this.taskManagement = taskManagement;
        //     initializeComponents();
        // }

        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Order Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(titleLabel, BorderLayout.NORTH);

        // Main panel with form and table
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));

        // Order form
        JPanel formPanel = createOrderForm();
        mainPanel.add(formPanel, BorderLayout.NORTH);

        // Order table
        JScrollPane tableScrollPane = createOrderTable();
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createOrderForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
        customerField = new JTextField("SuperMart Central", 20);
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
            "Juice - Orange"
        });
        formPanel.add(beverageCombo, gbc);

        // Quantity and Priority on same row
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Quantity:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        quantityField = new JTextField("50", 5);
        formPanel.add(quantityField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Priority:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.3;
        priorityCombo = new JComboBox<>(new String[]{"Normal", "High", "Express"});
        formPanel.add(priorityCombo, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JButton createButton = new JButton("Create Order");
        // TODO [CONTROL-BINDING]: Uncomment to create order/task
        // createButton.addActionListener(e -> createOrder());
        buttonPanel.add(createButton);

        JButton processButton = new JButton("Process Order");
        // TODO [CONTROL-BINDING]: Uncomment to process selected order
        // processButton.addActionListener(e -> processSelectedOrder());
        buttonPanel.add(processButton);

        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    private JScrollPane createOrderTable() {
        String[] columnNames = {"Order ID", "Customer", "Beverage", "Qty", "Priority", "Status", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Actions column editable (for buttons)
            }
        };

        orderTable = new JTable(tableModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.setRowHeight(25);

        // Set column widths
        orderTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Order ID
        orderTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Customer
        orderTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Beverage
        orderTable.getColumnModel().getColumn(3).setPreferredWidth(40);  // Qty
        orderTable.getColumnModel().getColumn(4).setPreferredWidth(70);  // Priority
        orderTable.getColumnModel().getColumn(5).setPreferredWidth(70);  // Status
        orderTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Actions

        // Add sample data
        addSampleOrders();

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setPreferredSize(new Dimension(0, 150));

        return scrollPane;
    }

    private void addSampleOrders() {
        // Sample orders
        tableModel.addRow(new Object[]{"ORD-001", "SuperMart Central", "Beer", "50", "Express", "Loading", "Edit | Delete"});
        tableModel.addRow(new Object[]{"ORD-002", "QuickStop West", "Water", "30", "Normal", "Pending", "Edit | Delete"});
        tableModel.addRow(new Object[]{"ORD-003", "City Beverages", "Juice", "25", "Normal", "Pending", "Edit | Delete"});

        // TODO [OBSERVABILITY]: Remove sample data and populate from TaskManagement
        // populateTableFromTaskManagement();
    }

    // TODO [STATE-ACCESS]: Uncomment to populate table from backend
    /* private void populateTableFromTaskManagement() {
         if (taskManagement == null) return;
    
         SwingUtilities.invokeLater(() -> {
             tableModel.setRowCount(0); // Clear existing rows
    
             List<Task> tasks = taskManagement.getTasks();
             for (int i = 0; i < tasks.size(); i++) {
                 Task task = tasks.get(i);
                 // Extract order information from task
                 String orderId = "ORD-" + String.format("%03d", i + 1);
                 String customer = extractCustomerFromTask(task);
                 String beverage = extractBeverageFromTask(task);
                 int quantity = extractQuantityFromTask(task);
                 String priority = extractPriorityFromTask(task);
                 String status = task.getState().toString();
    
                 tableModel.addRow(new Object[]{
                     orderId, customer, beverage, quantity, priority, status, "Edit | Delete"
                 });
             }
         });
     } */

    // TODO [CONTROL-BINDING]: Uncomment to create new order
    /* private void createOrder() {
         String customer = customerField.getText().trim();
         String beverage = (String) beverageCombo.getSelectedItem();
         String quantityStr = quantityField.getText().trim();
         String priority = (String) priorityCombo.getSelectedItem();
    
         if (customer.isEmpty() || quantityStr.isEmpty()) {
             JOptionPane.showMessageDialog(this,
                 "Please fill in all required fields",
                 "Validation Error",
                 JOptionPane.ERROR_MESSAGE);
             return;
         }
    
         try {
             int quantity = Integer.parseInt(quantityStr);
             if (quantity <= 0) {
                 throw new NumberFormatException();
             }
    
             // Create Task through backend
             Task task = createTaskFromOrder(customer, beverage, quantity, priority);
             taskManagement.addTask(task);
    
             // Refresh table
             populateTableFromTaskManagement();
    
             // Clear form
             customerField.setText("");
             quantityField.setText("");
    
             JOptionPane.showMessageDialog(this,
                 "Order created successfully",
                 "Success",
                 JOptionPane.INFORMATION_MESSAGE);
    
         } catch (NumberFormatException e) {
             JOptionPane.showMessageDialog(this,
                 "Quantity must be a positive number",
                 "Validation Error",
                 JOptionPane.ERROR_MESSAGE);
         }
     } */

    // TODO [CONTROL-BINDING]: Uncomment to create Task from order data
    /* private Task createTaskFromOrder(String customer, String beverage, int quantity, String priority) {
         Task task = CoreConfiguration.INSTANCE.newTask();
    
         // Create Process and Operations for this task
         // This depends on your team's Task/Process/Operation structure
         // Example:
         // Process process = CoreConfiguration.INSTANCE.newProcess(...);
         // Operation pickOp = CoreConfiguration.INSTANCE.newOperation(...);
         // Operation loadOp = CoreConfiguration.INSTANCE.newOperation(...);
         // process.addOperation(pickOp);
         // process.addOperation(loadOp);
         // task.addProcess(process);
    
         return task;
     } */

    // TODO [CONTROL-BINDING]: Uncomment to process selected order
    /* private void processSelectedOrder() {
         int selectedRow = orderTable.getSelectedRow();
         if (selectedRow < 0) {
             JOptionPane.showMessageDialog(this,
                 "Please select an order to process",
                 "No Selection",
                 JOptionPane.WARNING_MESSAGE);
             return;
         }
    
         if (taskManagement != null && selectedRow < taskManagement.getTasksCount()) {
             Task task = taskManagement.getTask(selectedRow);
             // Start task execution
             task.start();
    
             // Update table
             populateTableFromTaskManagement();
         }
     } */

    // TODO [TICK-LISTENER]: Uncomment to receive simulation tick updates
    /* @Override
     public void onTick(int currentTick) {
         // Refresh table to show updated task statuses
         populateTableFromTaskManagement();
     } */

    // Getters
    public JTable getOrderTable() {
        return orderTable;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }
}
