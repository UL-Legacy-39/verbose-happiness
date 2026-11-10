package shoemanagement;

public class Admin extends User {
    public Admin() { super("Admin"); }
    @Override 
    public boolean authenticate(String credential) {
        return "1111".equals(credential);
    }
}