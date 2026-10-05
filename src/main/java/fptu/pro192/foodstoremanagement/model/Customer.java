package fptu.pro192.foodstoremanagement.model;

public class Customer {
    public abstract class Customers {

        // Fields
        protected String id;          // BR2
        protected String fullName;
        protected String phone;       // BR25
        protected String address;
        protected boolean isDeleted;  // BR27

        // Constructors
        public Customers() {
        }

        public Customers(String id, String fullName, String phone, String address) {
            this.id = id;
            this.fullName = fullName;
            this.phone = phone;
            this.address = address;
            this.isDeleted = false;
        }

        // Strategy contract: Returns discount rate (0.0 to 1.0)
        public abstract double getDiscountRate();

        // BR15 & BR16: Calculates discount amount based on subtotal
        public double calculateDiscount(double subtotal) {
            return subtotal * getDiscountRate();
        }

        // Standard getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public boolean isDeleted() {
            return isDeleted;
        }

        public void setDeleted(boolean deleted) {
            isDeleted = deleted;
        }
    }
}
