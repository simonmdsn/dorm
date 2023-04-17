@Table(name = "customers")
public class Customer extends Person {

    private String address;
    private String phone;

    public Customer(String address, String phone) {
        this.address = address;
        this.phone = phone;
    }

    public Customer(String name, String lastName, String address, String phone) {
        super(name, lastName);
        this.address = address;
        this.phone = phone;
    }

    public Customer(int id, String name, String lastName, String address, String phone) {
        super(id, name, lastName);
        this.address = address;
        this.phone = phone;
    }

    public String address() {
        return address;
    }

    public String phone() {
        return phone;
    }

    public void address(String address) {
        this.address = address;
    }

    public void phone(String phone) {
        this.phone = phone;
    }
}
