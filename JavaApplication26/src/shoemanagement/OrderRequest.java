
package shoemanagement;

public class OrderRequest {
    private Customer customer;
    private Shoe shoe;
    private int quantity;
    private double totalAmount;
    private String status; // "Pending", "Committed", "Refused"

    public OrderRequest(Customer customer, Shoe shoe, int quantity, double totalAmount) {
        this.customer = customer;
        this.shoe = shoe;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = "Pending";
    }

    public Customer getCustomer() { return customer; }
    public Shoe getShoe() { return shoe; }
    public int getQuantity() { return quantity; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}