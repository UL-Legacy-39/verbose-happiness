package shoemanagement;


public class Shoe extends Product {
    private int size;
    private String brand;

    public Shoe(String name, String brand, int size, double price, int quantity) {
        super(name, price, quantity);
        this.brand = brand;
        this.size = size;
    }

    public int getSize() { return size; }
    public String getBrand() { return brand; }
    public void setSize(int size) { this.size = size; }
    public void setBrand(String brand) { this.brand = brand; }
}