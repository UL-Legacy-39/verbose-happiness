package shoemanagement;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShoeShopManagementSystem2 extends JFrame {
    private static final List<Shoe> shoes = new ArrayList<>();
    private static final List<OrderRequest> pendingOrders = new ArrayList<>();
    
    private DefaultTableModel tableModel;
    private JTable shoeTable;
    private DefaultTableModel adminOrderTableModel;
    private JTable adminOrderTable;
    
    private DefaultTableModel customerTableModel;
    private JTable customerTable;
    private JTextArea customerHistoryArea;
    
    private JTextField nameField, brandField;
    private JTextField sizeField, priceField, quantityField;
    private JComboBox<String> saleShoeCombo;
    private JSpinner saleQuantitySpinner;
    private JLabel stockLabel, userStatusLabel;
    private static boolean adminSession = false;

    public ShoeShopManagementSystem2() {
        setTitle("Shoe Shop Management System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topContainer = new JPanel(new BorderLayout());
        JLabel header = new JLabel("SHOE SHOP MANAGEMENT SYSTEM", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setOpaque(true);
        header.setBackground(new Color(35, 45, 65));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(18, 10, 18, 10));
        topContainer.add(header, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel(new BorderLayout());
        Customer activeCust = CustomerManager.getActiveCustomer();
        String roleText = adminSession ? "Logged in as: Administrator" : 
            (activeCust != null ? "Customer: " + activeCust.getName() + " (ID: " + activeCust.getCustomerNumber() + ")" : "Guest");
        
        userStatusLabel = new JLabel("  Status: " + roleText, SwingConstants.LEFT);
        userStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusPanel.add(userStatusLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Log Out");
        logoutButton.addActionListener(e -> {
            dispose();
            showLoginMenu();
        });
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrapper.add(logoutButton);
        statusPanel.add(btnWrapper, BorderLayout.EAST);
        
        topContainer.add(statusPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", createDashboard());
        
        if (adminSession) {
            tabs.addTab("Inventory", createInventory());
            tabs.addTab("Customer Records", createCustomerSearch());
            tabs.addTab("Order Management", createAdminOrderManagement());
        } else {
            tabs.addTab("Sales", createSales());
        }
        
        add(tabs, BorderLayout.CENTER);

        if (shoes.isEmpty()) {
            shoes.add(new Shoe("Running Shoes", "Nike", 8, 2499.00, 10));
            shoes.add(new Shoe("Classic Sneakers", "Adidas", 7, 1999.00, 8));
        }
        refreshInventory();
    }

    private JPanel createDashboard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(242, 245, 249));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel title = new JLabel("Welcome to the Shoe Shop!");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        panel.add(title, gbc);

        gbc.gridy++;
        stockLabel = new JLabel();
        stockLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        panel.add(stockLabel, gbc);

        gbc.gridy++;
        String desc = adminSession ? 
            "<html>Use Inventory to manage stock.<br>Use Customer Records to view and delete customer accounts.<br>Use Order Management to review and commit/refuse orders.</html>" :
            "<html>Use Sales to select shoes and place purchase orders (Sizes 2-22 supported, max 20 per transaction).</html>";
        JLabel note = new JLabel(desc);
        note.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(note, gbc);

        return panel;
    }

    private JPanel createInventory() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridLayout(2, 5, 8, 5));
        form.setBorder(BorderFactory.createTitledBorder("Manage Shoe Inventory"));

        nameField = new JTextField();
        brandField = new JTextField();
        sizeField = new JTextField();
        priceField = new JTextField();
        quantityField = new JTextField();

        form.add(new JLabel("Shoe Name"));
        form.add(new JLabel("Brand"));
        form.add(new JLabel("Size (2-22)"));
        form.add(new JLabel("Price (PHP)"));
        form.add(new JLabel("Quantity"));

        form.add(nameField);
        form.add(brandField);
        form.add(sizeField);
        form.add(priceField);
        form.add(quantityField);

        JButton addButton = new JButton("Add Shoe");
        addButton.addActionListener(e -> addShoe());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(12);
        JButton searchButton = new JButton("Search");
        JButton sortButton = new JButton("Sort Price");
        JButton updateButton = new JButton("Update Selected (Size/Qty)");
        JButton deleteButton = new JButton("Remove Product");
        JButton showAllButton = new JButton("Reset");

        searchButton.addActionListener(e -> searchShoe(searchField.getText()));
        sortButton.addActionListener(e -> bubbleSort());
        updateButton.addActionListener(e -> updateSelectedShoeSizeAndQty());
        deleteButton.addActionListener(e -> deleteSelectedShoe());
        showAllButton.addActionListener(e -> refreshInventory());

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(sortButton);
        searchPanel.add(updateButton);
        searchPanel.add(deleteButton);
        searchPanel.add(showAllButton);

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(form, BorderLayout.CENTER);
        top.add(addButton, BorderLayout.SOUTH);

        JPanel inventoryTop = new JPanel(new BorderLayout(5, 5));
        inventoryTop.add(top, BorderLayout.CENTER);
        inventoryTop.add(searchPanel, BorderLayout.SOUTH);
        panel.add(inventoryTop, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"Shoe Name", "Brand", "Size", "Price (PHP)", "Stock"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        shoeTable = new JTable(tableModel);
        shoeTable.setRowHeight(25);
        panel.add(new JScrollPane(shoeTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAdminOrderManagement() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Customer Order Requests Review"));

        adminOrderTableModel = new DefaultTableModel(
            new String[]{"Customer Name", "ID", "Shoe", "Brand", "Size", "Qty", "Total (PHP)", "Status"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        adminOrderTable = new JTable(adminOrderTableModel);
        adminOrderTable.setRowHeight(25);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton commitButton = new JButton("Commit / Approve Order");
        JButton refuseButton = new JButton("Refuse / Reject Order");

        commitButton.addActionListener(e -> processAdminOrder(true));
        refuseButton.addActionListener(e -> processAdminOrder(false));

        buttonPanel.add(commitButton);
        buttonPanel.add(refuseButton);

        panel.add(new JScrollPane(adminOrderTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        refreshAdminOrders();
        return panel;
    }

    private void refreshAdminOrders() {
        if (adminOrderTableModel != null) {
            adminOrderTableModel.setRowCount(0);
            for (OrderRequest req : pendingOrders) {
                adminOrderTableModel.addRow(new Object[]{
                    req.getCustomer().getName(),
                    req.getCustomer().getCustomerNumber(),
                    req.getShoe().getName(),
                    req.getShoe().getBrand(),
                    req.getShoe().getSize(),
                    req.getQuantity(),
                    String.format("PHP %.2f", req.getTotalAmount()),
                    req.getStatus()
                });
            }
        }
    }

    private void processAdminOrder(boolean approve) {
        int row = adminOrderTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order request from the table.");
            return;
        }

        OrderRequest req = pendingOrders.get(row);
        if (!req.getStatus().equals("Pending")) {
            JOptionPane.showMessageDialog(this, "This order has already been processed.");
            return;
        }

        Shoe shoe = req.getShoe();
        if (approve) {
            if (req.getQuantity() > shoe.getQuantity()) {
                JOptionPane.showMessageDialog(this, "Cannot commit: Insufficient stock available!");
                return;
            }
            shoe.setQuantity(shoe.getQuantity() - req.getQuantity());
            req.setStatus("Committed");
            
            String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            SalesRecord record = new SalesRecord(shoe.getBrand(), shoe.getName(), shoe.getSize(), req.getQuantity(), shoe.getPrice(), req.getTotalAmount(), dateTime);
            
            // Updates customer record and saves to file immediately
            CustomerManager.updateCustomerPurchase(req.getCustomer(), record);
            
            refreshInventory();
            refreshCustomerTable();
            JOptionPane.showMessageDialog(this, "Order approved, customer record updated, and committed successfully!");
        } else {
            req.setStatus("Refused");
            JOptionPane.showMessageDialog(this, "Order has been refused by admin.");
        }
        refreshAdminOrders();
    }

    private JPanel createCustomerSearch() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Customer Records & Management"));

        customerTableModel = new DefaultTableModel(
            new String[]{"Customer Name", "Customer ID", "Total Purchases"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        customerTable = new JTable(customerTableModel);
        customerTable.setRowHeight(25);
        
        customerHistoryArea = new JTextArea(10, 40);
        customerHistoryArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        customerHistoryArea.setEditable(false);

        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = customerTable.getSelectedRow();
                if (row >= 0) {
                    List<Customer> list = CustomerManager.getCustomers();
                    if (row < list.size()) {
                        Customer c = list.get(row);
                        displayCustomerHistory(c);
                    }
                }
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(new JLabel("All Registered Customers (Select a row to view history):"), BorderLayout.NORTH);
        topPanel.add(new JScrollPane(customerTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(new JLabel("Customer Purchase History & Details:"), BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(customerHistoryArea), BorderLayout.CENTER);

        JButton deleteCustButton = new JButton("Delete Selected Customer");
        deleteCustButton.addActionListener(e -> deleteSelectedCustomer());
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(deleteCustButton);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        splitPane.setDividerLocation(200);
        panel.add(splitPane, BorderLayout.CENTER);

        refreshCustomerTable();
        return panel;
    }

    private void refreshCustomerTable() {
        if (customerTableModel != null) {
            customerTableModel.setRowCount(0);
            List<Customer> list = CustomerManager.getCustomers();
            for (Customer c : list) {
                customerTableModel.addRow(new Object[]{
                    c.getName(),
                    c.getCustomerNumber(),
                    c.getPurchaseHistory().size()
                });
            }
        }
    }

    private void displayCustomerHistory(Customer found) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("CUSTOMER DETAILS\n");
        sb.append("========================================\n");
        sb.append("Name             : ").append(found.getName()).append("\n");
        sb.append("Customer Number  : ").append(found.getCustomerNumber()).append("\n\n");
        sb.append("PURCHASE HISTORY:\n");
        if (found.getPurchaseHistory().isEmpty()) {
            sb.append("  (No purchases recorded yet)\n");
        } else {
            int i = 1;
            for (SalesRecord r : found.getPurchaseHistory()) {
                sb.append(String.format("  [%d] Date: %s\n", i++, r.getDateTime()));
                sb.append(String.format("      Shoe: %s (%s) | Size: %d\n", r.getShoeName(), r.getBrand(), r.getSize()));
                sb.append(String.format("      Qty: %d | Price: PHP %.2f | Total: PHP %.2f\n", r.getQuantity(), r.getPricePerPair(), r.getTotalAmount()));
            }
        }
        customerHistoryArea.setText(sb.toString());
    }

    private void deleteSelectedCustomer() {
        int row = customerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a customer from the table to delete.");
            return;
        }
        List<Customer> list = CustomerManager.getCustomers();
        if (row >= list.size()) return;
        Customer target = list.get(row);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete customer:\n" + target.getName() + " (ID: " + target.getCustomerNumber() + ")?\nThis will remove their record and purchase history permanently.",
            "Confirm Customer Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            CustomerManager.removeCustomer(target);
            refreshCustomerTable();
            customerHistoryArea.setText("");
            JOptionPane.showMessageDialog(this, "Customer deleted successfully.");
        }
    }

    private JPanel createSales() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Select Shoe:"), gbc);

        gbc.gridx = 1;
        saleShoeCombo = new JComboBox<>();
        saleShoeCombo.setPreferredSize(new Dimension(320, 30));
        panel.add(saleShoeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Quantity (Max 20):"), gbc);

        gbc.gridx = 1;
        saleQuantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        panel.add(saleQuantitySpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton sellButton = new JButton("Submit Order Request");
        sellButton.addActionListener(e -> submitOrderRequest());
        panel.add(sellButton, gbc);

        gbc.gridy = 3;
        JButton summaryButton = new JButton("Show Stock Summary");
        summaryButton.addActionListener(e -> showStockSummary());
        panel.add(summaryButton, gbc);

        return panel;
    }

    private void addShoe() {
        try {
            String name = nameField.getText().trim();
            String brand = brandField.getText().trim();
            int size = Integer.parseInt(sizeField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            int quantity = Integer.parseInt(quantityField.getText().trim());

            if (name.isEmpty() || brand.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter the shoe name and brand.");
                return;
            }

            if (size < 2 || size > 22) {
                JOptionPane.showMessageDialog(this, "Shoe size must be between 2 and 22 inclusive.");
                return;
            }

            if (price <= 0 || quantity < 0) {
                JOptionPane.showMessageDialog(this, "Enter valid price and quantity.");
                return;
            }

            shoes.add(new Shoe(name, brand, size, price, quantity));
            refreshInventory();
            nameField.setText(""); brandField.setText(""); sizeField.setText(""); priceField.setText(""); quantityField.setText("");
            JOptionPane.showMessageDialog(this, "Shoe added successfully!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Size, price, and quantity must be numbers.");
        }
    }

    private void updateSelectedShoeSizeAndQty() {
        int row = shoeTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the inventory table to update.");
            return;
        }

        Shoe selected = shoes.get(row);

        String sizeStr = JOptionPane.showInputDialog(this, "Enter new Size (2-22) for [" + selected.getName() + " - " + selected.getBrand() + "]:", selected.getSize());
        if (sizeStr == null) return;

        String qtyStr = JOptionPane.showInputDialog(this, "Enter new Quantity:", selected.getQuantity());
        if (qtyStr == null) return;

        try {
            int newSize = Integer.parseInt(sizeStr.trim());
            int newQty = Integer.parseInt(qtyStr.trim());

            if (newSize < 2 || newSize > 22) {
                JOptionPane.showMessageDialog(this, "Size must be between 2 and 22 inclusive.");
                return;
            }

            if (newQty < 0) {
                JOptionPane.showMessageDialog(this, "Quantity cannot be negative.");
                return;
            }

            shoes.add(new Shoe(selected.getName(), selected.getBrand(), newSize, selected.getPrice(), newQty));
            refreshInventory();
            JOptionPane.showMessageDialog(this, "Updated variant added successfully to stock!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format entered for size or quantity.");
        }
    }

    private void deleteSelectedShoe() {
        int row = shoeTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a shoe product from the inventory table to remove.");
            return;
        }

        Shoe selectedShoe = shoes.get(row);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to completely remove:\n" +
            selectedShoe.getName() + " (" + selectedShoe.getBrand() + ", Size " + selectedShoe.getSize() + ") from stock?",
            "Confirm Product Removal",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            shoes.remove(row);
            refreshInventory();
            JOptionPane.showMessageDialog(this, "Product successfully removed from stock.");
        }
    }

    private Shoe linearSearch(String searchName) {
        for (Shoe shoe : shoes) {
            if (shoe.getName().equalsIgnoreCase(searchName.trim()) || shoe.getBrand().equalsIgnoreCase(searchName.trim())) {
                return shoe;
            }
        }
        return null;
    }

    private void searchShoe(String searchName) {
        if (searchName == null || searchName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a shoe name or brand to search.");
            return;
        }
        Shoe found = linearSearch(searchName);
        if (found == null) {
            JOptionPane.showMessageDialog(this, "Shoe not found: " + searchName, "Search Result", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, 
            "SHOE FOUND\nName: " + found.getName() + "\nBrand: " + found.getBrand() + 
            "\nSize: " + found.getSize() + String.format("\nPrice: PHP %.2f\nStock: %d", found.getPrice(), found.getQuantity()),
            "Search Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private void bubbleSort() {
        for (int i = 0; i < shoes.size() - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < shoes.size() - i - 1; j++) {
                if (shoes.get(j).getPrice() > shoes.get(j + 1).getPrice()) {
                    Shoe temp = shoes.get(j);
                    shoes.set(j, shoes.get(j + 1));
                    shoes.set(j + 1, temp);
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
        refreshInventory();
        JOptionPane.showMessageDialog(this, "Shoes sorted by price from lowest to highest.");
    }

    private void submitOrderRequest() {
        int index = saleShoeCombo.getSelectedIndex();
        if (index < 0 || index >= shoes.size()) {
            JOptionPane.showMessageDialog(this, "Please select a shoe first.");
            return;
        }

        Shoe shoe = shoes.get(index);
        int qty = (Integer) saleQuantitySpinner.getValue();

        if (qty > 20) {
            JOptionPane.showMessageDialog(this, "Purchase limit is a maximum of 20 pairs per transaction.");
            return;
        }

        if (qty > shoe.getQuantity()) {
            JOptionPane.showMessageDialog(this, "Not enough stock! Available: " + shoe.getQuantity());
            return;
        }

        Customer activeCustomer = CustomerManager.getActiveCustomer();
        if (activeCustomer == null) {
            JOptionPane.showMessageDialog(this, "No active customer session found. Please log in as a customer.");
            return;
        }

        double total = shoe.getPrice() * qty;
        OrderRequest orderReq = new OrderRequest(activeCustomer, shoe, qty, total);
        pendingOrders.add(orderReq);

        JOptionPane.showMessageDialog(this,
            "Order request submitted successfully!\n" +
            "Your order is currently pending administrator approval.",
            "Order Submitted",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showStockSummary() {
        StringBuilder summary = new StringBuilder("CURRENT INVENTORY STOCK SUMMARY\n\n");
        for (Shoe shoe : shoes) {
            summary.append(shoe.getName()).append(" (").append(shoe.getBrand()).append(") - Size ").append(shoe.getSize())
                .append(" - Stock: ").append(shoe.getQuantity()).append("\n");
        }
        JOptionPane.showMessageDialog(this, summary.toString(), "Stock Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshInventory() {
        if (tableModel != null) {
            tableModel.setRowCount(0);
            for (Shoe shoe : shoes) {
                tableModel.addRow(new Object[]{
                    shoe.getName(), shoe.getBrand(), shoe.getSize(),
                    String.format("PHP %.2f", shoe.getPrice()), shoe.getQuantity()
                });
            }
        }

        if (saleShoeCombo != null) {
            saleShoeCombo.removeAllItems();
            for (Shoe shoe : shoes) {
                saleShoeCombo.addItem(
                    shoe.getName() + " - " + shoe.getBrand() + " (Size " + shoe.getSize() + ", Stock " + shoe.getQuantity() + ")"
                );
            }
        }

        if (stockLabel != null) {
            int totalPairs = 0;
            for (Shoe shoe : shoes) {
                totalPairs += shoe.getQuantity();
            }
            stockLabel.setText("Products: " + shoes.size() + " | Total pairs in stock: " + totalPairs);
        }
    }

    public static void showLoginMenu() {
        CustomerManager.load();
        while (true) {
            String[] options = {"Admin Login", "Customer Login", "Exit"};
            int choice = JOptionPane.showOptionDialog(
                null, "Welcome to the Shoe Shop Management System\nChoose a login type:",
                "Login Menu", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

            if (choice == 0) {
                JPasswordField passwordField = new JPasswordField();
                int result = JOptionPane.showConfirmDialog(null, passwordField,
                    "Admin Password (1111)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result != JOptionPane.OK_OPTION) continue;
                User user = new Admin();
                if (user.authenticate(new String(passwordField.getPassword()))) {
                    adminSession = true;
                    CustomerManager.setActiveCustomer(null);
                    JOptionPane.showMessageDialog(null, "Admin login successful.");
                    new ShoeShopManagementSystem2().setVisible(true);
                    return;
                }
                JOptionPane.showMessageDialog(null, "Incorrect password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            } else if (choice == 1) {
                List<Customer> allCustomers = CustomerManager.getCustomers();
                
                if (allCustomers.isEmpty()) {
                    String name = JOptionPane.showInputDialog(null, "No existing customers found.\nEnter your name to register:", "Customer Login", JOptionPane.QUESTION_MESSAGE);
                    if (name == null || name.trim().isEmpty()) continue;
                    Customer customer = CustomerManager.register(name.trim());
                    CustomerManager.setActiveCustomer(customer);
                    JOptionPane.showMessageDialog(null,
                        "Customer login successful!\nName: " + customer.getName() +
                        "\nYour customer number is: " + customer.getCustomerNumber() +
                        "\n\nKeep this number for your records.",
                        "Customer Login", JOptionPane.INFORMATION_MESSAGE);
                    adminSession = false;
                    new ShoeShopManagementSystem2().setVisible(true);
                    return;
                } else {
                    String[] loginOptions = {"Select Existing Customer", "Register New Customer"};
                    int subChoice = JOptionPane.showOptionDialog(
                        null, "Choose customer login method:", "Customer Login",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, loginOptions, loginOptions[0]
                    );
                    
                    if (subChoice == 0) {
                        Object[] customerArray = allCustomers.stream()
                            .map(c -> c.getName() + " (ID: " + c.getCustomerNumber() + ")")
                            .toArray();
                        String selectedStr = (String) JOptionPane.showInputDialog(
                            null, "Select your account:", "Existing Customer Login",
                            JOptionPane.QUESTION_MESSAGE, null, customerArray, customerArray[0]
                        );
                        if (selectedStr == null) continue;
                        
                        Customer chosen = null;
                        for (Customer c : allCustomers) {
                            if (selectedStr.contains("(ID: " + c.getCustomerNumber() + ")")) {
                                chosen = c;
                                break;
                            }
                        }
                        if (chosen != null) {
                            CustomerManager.setActiveCustomer(chosen);
                            JOptionPane.showMessageDialog(null, "Welcome back, " + chosen.getName() + "!", "Login Success", JOptionPane.INFORMATION_MESSAGE);
                            adminSession = false;
                            new ShoeShopManagementSystem2().setVisible(true);
                            return;
                        }
                    } else if (subChoice == 1) {
                        String name = JOptionPane.showInputDialog(null, "Enter your name:", "Customer Registration", JOptionPane.QUESTION_MESSAGE);
                        if (name == null || name.trim().isEmpty()) continue;
                        Customer customer = CustomerManager.register(name.trim());
                        CustomerManager.setActiveCustomer(customer);
                        JOptionPane.showMessageDialog(null,
                            "Customer registration successful!\nName: " + customer.getName() +
                            "\nYour customer number is: " + customer.getCustomerNumber() +
                            "\n\nKeep this number for your records.",
                            "Customer Login", JOptionPane.INFORMATION_MESSAGE);
                        adminSession = false;
                        new ShoeShopManagementSystem2().setVisible(true);
                        return;
                    } else {
                        continue;
                    }
                }
            } else {
                System.exit(0);
                return;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> showLoginMenu());
    }
}