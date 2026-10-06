package fptu.pro192.foodstoremanagement.model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class CustomerRepository {

        private final String filePath = "data/customers.csv"; //
        private final List<Customer> customers = new ArrayList<>(); //
        public void loadFromCsv() {
            customers.clear();

        }

        public synchronized void saveToCsv() {
        }

        public Customer findById(String id) {
            for (Customer customer : customers) {
                if (!customer.isDeleted() && customer.getId().equals(id)) { // Chỉ trả về active customer
                    return customer;
                }
            }
            return null;
        }

        public Customer findByPhone(String phone) {
            String normalizedInput = phone != null ? phone.trim().replaceAll("\\s+", "") : "";
            for (Customer customer : customers) {
                if (!customer.isDeleted() && customer.getPhone() != null) {
                    String normalizedRecord = customer.getPhone().trim().replaceAll("\\s+", "");
                    if (normalizedRecord.equals(normalizedInput)) {
                        return customer;
                    }
                }
            }
            return null;
        }


        public List<Customer> findAll() {
            List<Customer> activeCustomers = new ArrayList<>();
            for (Customer customer : customers) {
                if (!customer.isDeleted()) {
                    activeCustomers.add(customer);
                }
            }
            return Collections.unmodifiableList(activeCustomers);
        }

        public void save(Customer customer) {
            boolean exists = false;
            for (int i = 0; i < customers.size(); i++) {
                if (customers.get(i).getId().equals(customer.getId())) {
                    customers.set(i, customer);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                customers.add(customer);
            }
            saveToCsv();
        }
        public void delete(String id) {
            for (Customer customer : customers) {
                if (customer.getId().equals(id)) {
                    customer.setDeleted(true);
                    saveToCsv();
                    break;
                }
            }
        }
    }

