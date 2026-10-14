package fptu.pro192.foodstoremanagement.ui;

import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.VIPCustomer;
import fptu.pro192.foodstoremanagement.service.CustomerService;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.ScannerManager;
import fptu.pro192.foodstoremanagement.util.TableFormatter;

import java.util.List;
import java.util.Scanner;

/**
 * Console user interface menu for managing customer operations.
 * Conforms strictly to 80-column ASCII terminal layout and Template Method Pattern.
 */
public class CustomerMenu extends AbstractMenu {

    private final CustomerService customerService;
    private final Scanner scanner;

    private static final String TABLE_BORDER =
            "+--------+---------------------------+--------------+-----------------+--------+";

    public CustomerMenu(CustomerService customerService) {
        this(customerService, ScannerManager.getInstance().getScanner());
    }

    public CustomerMenu(CustomerService customerService, Scanner scanner) {
        super("QUẢN LÝ KHÁCH HÀNG");
        this.customerService = customerService;
        this.scanner = scanner != null ? scanner : ScannerManager.getInstance().getScanner();
    }

    @Override
    protected void renderBody() {
        System.out.println("  1. Thêm khách hàng mới");
        System.out.println("  2. Cập nhật thông tin khách hàng");
        System.out.println("  3. Xem danh sách khách hàng");
        System.out.println("  4. Tìm kiếm khách hàng theo Tên / SĐT");
        System.out.println("  5. Xóa khách hàng (Soft Delete)");
    }

    @Override
    protected int getMaxOption() {
        return 5;
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
            default:
                return false;
        }
        return true;
    }

    /**
     * Renders customer collection conforming strictly to 80-column ASCII table layout.
     * Section 5.2.2: | Mã KH (6) | Họ Và Tên (25) | Số ĐT (12) | Địa Chỉ (15) | Hạng (6) |
     *
     * @param customerList The list of customers to display
     */
    private void renderCustomerTable(List<Customer> customerList) {
        if (customerList == null || customerList.isEmpty()) {
            System.out.println("Chưa có dữ liệu khách hàng nào.");
            return;
        }

        System.out.println(TABLE_BORDER);
        System.out.printf("| %-6s | %-25s | %-12s | %-15s | %-6s |\n",
                "Mã KH", "Họ Và Tên", "Số ĐT", "Địa Chỉ", "Hạng");
        System.out.println(TABLE_BORDER);

        for (Customer c : customerList) {
            String tier = (c instanceof VIPCustomer) ? "VIP" : "Regular";
            System.out.printf("| %-6s | %-25s | %-12s | %-15s | %-6s |\n",
                    TableFormatter.fit(c.getId(), 6),
                    TableFormatter.fit(c.getFullName(), 25),
                    TableFormatter.fit(c.getPhone(), 12),
                    TableFormatter.fit(c.getAddress(), 15),
                    TableFormatter.fit(tier, 6));
        }

        System.out.println(TABLE_BORDER);
    }

    private void showCustomerList() {
        ConsoleUtil.clearScreen();
        System.out.println("--- DANH SÁCH KHÁCH HÀNG ---");
        List<Customer> activeCustomers = customerService.getAllActive();
        renderCustomerTable(activeCustomers);
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

            customerService.addCustomer(id, fullName, phone, address, type);
            System.out.println("Thêm khách hàng thành công!");
        } catch (IllegalArgumentException e) {
            System.out.println("Lỗi: " + e.getMessage());
        }
    }

    private void updateCustomer() {
        ConsoleUtil.clearScreen();
        System.out.println("--- CẬP NHẬT THÔNG TIN KHÁCH HÀNG ---");
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
            System.out.println("Không tìm thấy khách hàng nào khớp với từ khóa: " + keyword);
        } else {
            System.out.println("Tìm thấy " + results.size() + " khách hàng:");
            renderCustomerTable(results);
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

            if (confirm.trim().equalsIgnoreCase("Y")) {
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
