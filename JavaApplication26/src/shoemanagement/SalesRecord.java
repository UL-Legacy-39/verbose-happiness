package shoemanagement;

public class SalesRecord {
    private String brand;
    private String shoeName;
    private int size;
    private int quantity;
    private double pricePerPair;
    private double totalAmount;
    private String dateTime;

    public SalesRecord(String brand, String shoeName, int size, int quantity, double pricePerPair, double totalAmount, String dateTime) {
        this.brand = brand;
        this.shoeName = shoeName;
        this.size = size;
        this.quantity = quantity;
        this.pricePerPair = pricePerPair;
        this.totalAmount = totalAmount;
        this.dateTime = dateTime;
    }

    public String getBrand() { return brand; }
    public String getShoeName() { return shoeName; }
    public int getSize() { return size; }
    public int getQuantity() { return quantity; }
    public double getPricePerPair() { return pricePerPair; }
    public double getTotalAmount() { return totalAmount; }
    public String getDateTime() { return dateTime; }
}