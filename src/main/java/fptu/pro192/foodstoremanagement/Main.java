package fptu.pro192.foodstoremanagement;

import fptu.pro192.foodstoremanagement.controller.CustomerController;
import fptu.pro192.foodstoremanagement.controller.OrderController;
import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.controller.ReportController;
import fptu.pro192.foodstoremanagement.view.CustomerMenu;
import fptu.pro192.foodstoremanagement.view.InventoryMenu;
import fptu.pro192.foodstoremanagement.view.MainMenu;
import fptu.pro192.foodstoremanagement.view.ProductMenu;
import fptu.pro192.foodstoremanagement.view.ReportMenu;
import fptu.pro192.foodstoremanagement.view.SalesMenu;

/**
 * Application bootstrap entry point and Composition Root for the Food Store
 * Management System. Instantiates the shared domain controllers, wires the
 * five console submenus into MainMenu, and launches the interactive loop.
 */
public class Main {

    private Main() {
    }

    /**
     * Launches the Food Store Management System console application.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        try {
            ProductController productController = new ProductController();
            CustomerController customerController = new CustomerController();
            OrderController orderController = new OrderController(
                    productController, customerController);
            ReportController reportController = new ReportController(
                    orderController, productController, customerController);

            ProductMenu productMenu = new ProductMenu(productController);
            CustomerMenu customerMenu = new CustomerMenu(customerController);
            SalesMenu salesMenu = new SalesMenu(
                    orderController, productController, customerController);
            InventoryMenu inventoryMenu = new InventoryMenu(productController);
            ReportMenu reportMenu = new ReportMenu(reportController);

            MainMenu mainMenu = new MainMenu(
                    productMenu, customerMenu, salesMenu, inventoryMenu, reportMenu);
            mainMenu.display();
        } catch (Exception e) {
            System.out.println("  [ERROR] Fatal startup error: " + e.getMessage());
        }
    }
}
