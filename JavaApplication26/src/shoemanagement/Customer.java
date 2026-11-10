package shoemanagement;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User {
    private final int customerNumber;
    private final List<SalesRecord> purchaseHistory;

    public Customer(String name, int customerNumber) {
        super(name);
        this.customerNumber = customerNumber;
        this.purchaseHistory = new ArrayList<>();
    }

    public int getCustomerNumber() { return customerNumber; }
    public List<SalesRecord> getPurchaseHistory() { return purchaseHistory; }

    public void addPurchase(SalesRecord record) {
        purchaseHistory.add(record);
    }

    @Override 
    public boolean authenticate(String credential) {
        try { 
            return Integer.parseInt(credential) == customerNumber && customerNumber > 0; 
        } catch (NumberFormatException ex) { 
            return false; 
        }
    }
}
