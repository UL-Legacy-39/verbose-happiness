package shoemanagement;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomerManager {
    private static final String FILE_NAME = "customers.txt";
    private static final List<Customer> customers = new ArrayList<>();
    private static Customer activeCustomer = null;

    public static void setActiveCustomer(Customer customer) {
        activeCustomer = customer;
    }

    public static Customer getActiveCustomer() {
        return activeCustomer;
    }

    public static List<Customer> getCustomers() {
        load();
        return customers;
    }

    public static Customer register(String name) {
        load();
        Random random = new Random();
        int number;
        do { 
            number = random.nextInt(100000) + 1; 
        } while (findByNumber(number) != null);
        
        Customer customer = new Customer(name, number);
        customers.add(customer);
        save();
        return customer;
    }

    public static void updateCustomerPurchase(Customer customer, SalesRecord record) {
        load();
        Customer target = findByNumber(customer.getCustomerNumber());
        if (target != null) {
            target.addPurchase(record);
            save();
        }
    }

    public static void removeCustomer(Customer customer) {
        load();
        customers.removeIf(c -> c.getCustomerNumber() == customer.getCustomerNumber());
        save();
    }

    public static Customer find(String query) {
        load();
        String target = query.trim();
        for (Customer customer : customers) {
            if (customer.getName().equalsIgnoreCase(target) ||
                String.valueOf(customer.getCustomerNumber()).equals(target)) {
                return customer;
            }
        }
        return null;
    }

    private static Customer findByNumber(int number) {
        for (Customer c : customers) {
            if (c.getCustomerNumber() == number) return c;
        }
        return null;
    }

    public static void load() {
        customers.clear();
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            Customer currentCustomer = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.startsWith("C|")) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        String name = parts[1];
                        try {
                            int num = Integer.parseInt(parts[2]);
                            currentCustomer = new Customer(name, num);
                            customers.add(currentCustomer);
                        } catch (NumberFormatException ignored) {}
                    }
                } else if (line.startsWith("P|") && currentCustomer != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 8) {
                        try {
                            String brand = parts[1];
                            String shoeName = parts[2];
                            int size = Integer.parseInt(parts[3]);
                            int qty = Integer.parseInt(parts[4]);
                            double price = Double.parseDouble(parts[5]);
                            double total = Double.parseDouble(parts[6]);
                            String date = parts[7];
                            currentCustomer.addPurchase(new SalesRecord(brand, shoeName, size, qty, price, total, date));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void save() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Customer c : customers) {
                writer.println("C|" + c.getName().replace("|", " ") + "|" + c.getCustomerNumber());
                for (SalesRecord p : c.getPurchaseHistory()) {
                    writer.println("P|" + p.getBrand() + "|" + p.getShoeName() + "|" + p.getSize() + "|" + 
                                   p.getQuantity() + "|" + p.getPricePerPair() + "|" + p.getTotalAmount() + "|" + p.getDateTime());
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Could not save customer records.");
        }
    }
}