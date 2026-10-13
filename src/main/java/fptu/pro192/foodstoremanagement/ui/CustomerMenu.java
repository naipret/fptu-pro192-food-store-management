package fptu.pro192.foodstoremanagement.ui;

import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.service.CustomerService;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import java.util.List;
import java.util.Scanner;
import fptu.pro192.foodstoremanagement.util.TableFormatter;

public class CustomerMenu extends AbstractMenu {

    private final CustomerService customerService;
    private final Scanner scanner;

    public CustomerMenu(CustomerService customerService, Scanner scanner) {
        super("QUẢN LÝ KHÁCH HÀNG");
        this.customerService = customerService;
        this.scanner = scanner;
    }

    @Override
    protected void renderBody() {
        ConsoleUtil.clearScreen();
        System.out.println("================================================================================");
        System.out.println("                              " + getTitle());
        System.out.println("================================================================================");
        System.out.println("  1. Thêm khách hàng mới");
        System.out.println("  2. Cập nhật thông tin khách hàng");
        System.out.println("  3. Xem danh sách khách hàng");
        System.out.println("  4. Tìm kiếm khách hàng theo Tên / SĐT");
        System.out.println("  5. Xóa khách hàng (Soft Delete)");
        System.out.println("  0. Quay lại Menu chính");
        System.out.println("================================================================================");
        System.out.print("Vui lòng chọn chức năng (0-5): ");
    }

    private String getTitle() {
        return title;
    }

    @Override
    protected boolean handleOption(int choice) {
        switch (choice) {
            case 1:
                addNewCustomer();
                break;
            case 2:
                updateCustomer();
                break;
            case 3:
                showCustomerList();
                break;
            case 4:
                searchCustomer();
                break;
            case 5:
                deleteCustomer();
                break;
            case 0:
                System.out.println("Đang quay lại Menu chính...");
                return false;
            default:
                System.out.println("Lựa chọn không hợp lệ. Vui lòng chọn từ 0 đến 5.");
        }

        System.out.println("\nNhấn Enter để tiếp tục...");
        scanner.nextLine();
        return true;
    }

    private void showCustomerList() {
        List<Customer> activeCustomers = customerService.getAllActive();
        String border = "+--------+---------------------------+--------------+-----------------+--------+";

        // 1. Vẽ đường viền trên
        System.out.println(border);

        // 2. Vẽ tiêu đề (Tổng đúng 80 ký tự tính cả các dấu | và khoảng trắng)
        System.out.printf("| %-6s | %-25s | %-12s | %-15s | %-6s |\n",
            "Ma KH", "Ho Va Ten", "So DT", "Dia Chi", "Hang");

        // 3. Vẽ đường viền ngăn cách
        System.out.println(border);

        // 4. Lặp và in dữ liệu bằng TableFormatter.fit()
        for (Customer c : activeCustomers) {
            String tier = (c.getClass().getSimpleName().contains("VIP")) ? "VIP" : "Regular";

            System.out.printf("| %-6s | %-25s | %-12s | %-15s | %-6s |\n",
                TableFormatter.fit(c.getId(), 6),
                TableFormatter.fit(c.getFullName(), 25),
                TableFormatter.fit(c.getPhone(), 12),
                TableFormatter.fit(c.getAddress(), 15),
                TableFormatter.fit(tier, 6)
            );
        }

        // 5. Vẽ đường viền dưới cùng
        System.out.println(border);
    }
    private void addNewCustomer() {
        ConsoleUtil.clearScreen();
        System.out.println("--- THÊM KHÁCH HÀNG MỚI ---");
        try {
            System.out.print("Nhập loại khách hàng (VIP/Regular): ");
            String type = scanner.nextLine();
            System.out.print("Nhập ID (Định dạng Cxxxxx): ");
            String id = scanner.nextLine();
            System.out.print("Nhập Họ Tên: ");
            String fullName = scanner.nextLine();
            System.out.print("Nhập Số điện thoại: ");
            String phone = scanner.nextLine();
            System.out.print("Nhập Địa chỉ: ");
            String address = scanner.nextLine();

            // Gọi hàm addCustomer từ service để xử lý logic và lưu
            customerService.addCustomer(id, fullName, phone, address, type);
            System.out.println("Thêm khách hàng thành công!");
        } catch (IllegalArgumentException e) {
            System.out.println("Lỗi: " + e.getMessage());
        }
    }

    private void updateCustomer() {
        ConsoleUtil.clearScreen();
        System.out.println("--- CẬP NHẬT THÔNG TIN ---");
        try {
            System.out.print("Nhập ID khách hàng cần cập nhật: ");
            String id = scanner.nextLine();

            System.out.println("(Để trống nếu không muốn thay đổi)");
            System.out.print("Nhập số điện thoại mới: ");
            String newPhone = scanner.nextLine();
            System.out.print("Nhập địa chỉ mới: ");
            String newAddress = scanner.nextLine();

            customerService.updateCustomer(id, newPhone, newAddress);
            System.out.println("Cập nhật thành công!");
        } catch (IllegalArgumentException e) {
            System.out.println("Lỗi: " + e.getMessage());
        }
    }

    private void searchCustomer() {
        ConsoleUtil.clearScreen();
        System.out.println("--- TÌM KIẾM KHÁCH HÀNG ---");
        System.out.print("Nhập tên hoặc số điện thoại: ");
        String keyword = scanner.nextLine();

        List<Customer> results = customerService.search(keyword);

        if (results.isEmpty()) {
            System.out.println("Không tìm thấy khách hàng nào khớp với từ khóa.");
        } else {
            // Hiển thị kết quả tìm được bằng bảng (có thể tái sử dụng logic vẽ bảng ở hàm showCustomerList)
            System.out.println("Tìm thấy " + results.size() + " kết quả:");
            // Ở đây tạm in ra dạng thô, nếu có TableRenderer thì gọi TableRenderer.renderCustomerTable(results);
            for (Customer c : results) {
                System.out.println(c.getId() + " - " + c.getFullName() + " - " + c.getPhone());
            }
        }
    }

    private void deleteCustomer() {
        ConsoleUtil.clearScreen();
        System.out.println("--- XÓA KHÁCH HÀNG ---");
        try {
            System.out.print("Nhập ID khách hàng cần xóa: ");
            String id = scanner.nextLine();

            System.out.print("Bạn có chắc chắn muốn xóa khách hàng " + id + "? (Y/N): ");
            String confirm = scanner.nextLine();

            if (confirm.equalsIgnoreCase("Y")) {
                customerService.deleteCustomer(id);
                System.out.println("Xóa khách hàng thành công.");
            } else {
                System.out.println("Đã hủy thao tác xóa.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Lỗi: " + e.getMessage());
        }
    }

}
