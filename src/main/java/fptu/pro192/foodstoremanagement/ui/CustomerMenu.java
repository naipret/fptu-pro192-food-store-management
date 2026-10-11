package fptu.pro192.foodstoremanagement.ui;

import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import java.util.List;
import java.util.Scanner;

public class CustomerMenu extends AbstractMenu {

    private final CustomerService customerService;
    private final Scanner scanner;

    public CustomerMenu(CustomerService customerService, Scanner scanner) {
        super("QUẢN LÝ KHÁCH HÀNG");
        this.customerService = customerService;
        this.scanner = scanner;
    }

    @Override
    public void displayMenu() {
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
        return "";
    }

    @Override
    public boolean processOption(int choice) {
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
        // (Giả sử bạn vẫn có ConsoleUtil.clearScreen(), nếu không có thì xóa dòng này)
        // ConsoleUtil.clearScreen();

        System.out.println("--- DANH SÁCH KHÁCH HÀNG ---");
        List<Customer> activeCustomers = customerService.getAllActive();

        if (activeCustomers.isEmpty()) {
            System.out.println("Chưa có dữ liệu khách hàng nào.");
        } else {
            // 1. Vẽ đường viền trên của bảng (đảm bảo tổng chiều dài < 80 ký tự)
            System.out.println("+--------+----------------------+-----------------+-------------------------+");

            // 2. Vẽ dòng tiêu đề cột
            System.out.printf("| %-6s | %-20s | %-15s | %-23s |\n", "ID", "Họ Tên", "Số ĐT", "Địa chỉ");

            // 3. Vẽ đường viền ngăn cách tiêu đề và nội dung
            System.out.println("+--------+----------------------+-----------------+-------------------------+");

            // 4. Lặp qua từng khách hàng và in dữ liệu
            for (Customer c : activeCustomers) {
                System.out.printf("| %-6s | %-20s | %-15s | %-23s |\n",
                    c.getId(),
                    truncateText(c.getFullName(), 20), // Cắt bớt nếu tên quá dài
                    c.getPhone(),
                    truncateText(c.getAddress(), 23)   // Cắt bớt nếu địa chỉ quá dài
                );
            }

            // 5. Vẽ đường viền dưới cùng của bảng
            System.out.println("+--------+----------------------+-----------------+-------------------------+");
        }
    }

    // Hàm hỗ trợ: Cắt chuỗi nếu dài hơn mức cho phép để không làm vỡ khung bảng
    private String truncateText(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        // Nếu dài quá, cắt lấy phần đầu và thêm "..." ở cuối
        return text.substring(0, maxLength - 3) + "...";
    }

    // Các hàm addNewCustomer, updateCustomer, searchCustomer, deleteCustomer
    // sẽ gọi các hàm tương ứng trong CustomerService và xử lý nhập liệu.
    // (Lược bớt chi tiết nhập liệu để tập trung vào cấu trúc Menu theo yêu cầu)
    private void addNewCustomer() { /* Logic nhập liệu và gọi customerService.addCustomer */ }
    private void updateCustomer() { /* Logic nhập liệu và gọi customerService.updateCustomer */ }
    private void searchCustomer() { /* Logic nhập liệu và gọi customerService.search */ }
    private void deleteCustomer() { /* Logic nhập liệu và gọi customerService.deleteCustomer */ }

    @Override
    protected void renderBody() {

    }

    @Override
    protected boolean handleOption(int choice) {
        return false;
    }
}
