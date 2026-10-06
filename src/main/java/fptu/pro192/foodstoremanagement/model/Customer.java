package fptu.pro192.foodstoremanagement.model;

public abstract class Customer {
        protected String id;
        protected String fullName;
        protected String phone;
        protected String address;
        protected boolean isDeleted;

        // Constructors
        public Customer() {
        }

        public Customer(String id, String fullName, String phone, String address) {
            this.id = id;
            this.fullName = fullName;
            this.phone = phone;
            this.address = address;
            this.isDeleted = false;
        }


        public abstract double getDiscountRate();


        public double calculateDiscount(double subtotal) {
            return subtotal * getDiscountRate();
        }

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


