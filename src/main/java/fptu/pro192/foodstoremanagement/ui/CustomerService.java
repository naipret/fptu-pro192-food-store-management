package fptu.pro192.foodstoremanagement.ui;

import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.CustomerFactory;
import fptu.pro192.foodstoremanagement.repository.CustomerRepository;
import fptu.pro192.foodstoremanagement.util.InputValidator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

public class CustomerService {

    private final CustomerRepository customerRepo;

    public CustomerService(CustomerRepository customerRepo) {
        this.customerRepo = customerRepo;
    }

    // BR2, BR18, BR25: Thêm khách hàng với chuẩn hóa số điện thoại và kiểm tra trùng lặp
    public void addCustomer(String id, String fullName, String rawPhone, String address, String type) {
        if (!id.matches("^C\\d{5}$")) {
            throw new IllegalArgumentException("ID không hợp lệ. Định dạng yêu cầu: Cxxxxx (x là số).");
        }

        // Kiểm tra xem ID đã tồn tại trong repository chưa
        if (customerRepo.findById(id) != null) {
            throw new IllegalArgumentException("ID khách hàng đã tồn tại.");
        }

        // Chuẩn hóa số điện thoại bằng lớp tiện ích InputValidator
        String normalizedPhone = InputValidator.normalizePhone(rawPhone);

        boolean phoneExists = getAllActive().stream()
            // Đảm bảo c không null trước khi kiểm tra số điện thoại
            .filter(c -> c != null)
            // Dùng Objects.equals để an toàn dù cả 2 bên đều null
            .anyMatch(c -> java.util.Objects.equals(c.getPhone(), normalizedPhone));
        if (phoneExists) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi một khách hàng đang hoạt động.");
        }

        // Tạo đối tượng khách hàng mới thông qua CustomerFactory
        Customer newCustomer = CustomerFactory.create(id, fullName, normalizedPhone, address, type);
        customerRepo.save(newCustomer);
    }

    // Task B6: Cập nhật thông tin có chọn lọc
    public void updateCustomer(String id, String newPhone, String newAddress) {
        Customer customer = customerRepo.findById(id);
        if (customer == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + id);
        }

        // Nếu số điện thoại mới không null và không rỗng, tiến hành chuẩn hóa và cập nhật
        if (newPhone != null && !newPhone.trim().isEmpty()) {
            String normalizedPhone = InputValidator.normalizePhone(newPhone);
            customer.setPhone(normalizedPhone);
        }

        // Nếu địa chỉ mới không null và không rỗng, tiến hành cập nhật
        if (newAddress != null && !newAddress.trim().isEmpty()) {
            customer.setAddress(newAddress);
        }

        // Lưu thay đổi vào file CSV
        customerRepo.saveToCsv();
    }

    // BR27: Xóa mềm (Soft delete)
    public void deleteCustomer(String id) {
        // Tìm khách hàng theo ID
        Customer customer = customerRepo.findById(id);
        if (customer != null) {
            // Đánh dấu khách hàng là đã bị xóa thay vì xóa hoàn toàn khỏi cơ sở dữ liệu
            customer.setDeleted(true);
            customerRepo.saveToCsv(); // Thường cần lưu lại thay đổi sau khi cập nhật trạng thái
        }
    }

    // Lấy danh sách tất cả khách hàng đang hoạt động (chưa bị xóa)
    public List<Customer> getAllActive() {
        // Lấy tất cả khách hàng từ repo, lọc những người có deleted = false và gom lại thành List
        return customerRepo.findAll().stream()
            .filter(c -> !c.isDeleted())
            .collect(Collectors.toList());
    }

    // Tìm kiếm khách hàng theo tên (không phân biệt hoa thường) hoặc số điện thoại
    public List<Customer> search(String keyword) {
        String lowerKeyword = keyword.toLowerCase();

        // Lọc danh sách khách hàng đang hoạt động dựa trên tên hoặc số điện thoại chứa từ khóa
        return getAllActive().stream()
            .filter(c -> c.getFullName().toLowerCase().contains(lowerKeyword) || c.getPhone().contains(keyword))
            .collect(Collectors.toList());
    }
}
