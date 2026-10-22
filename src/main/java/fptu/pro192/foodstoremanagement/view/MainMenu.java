package fptu.pro192.foodstoremanagement.view;

/**
 * Top-level 80-column console menu orchestrating navigation across all five
 * application modules: Product Catalog Management, Customer Management,
 * Sales and Checkout Management, Inventory and Batch Management, and
 * Analytical and Revenue Reports.
 */
public class MainMenu extends Menu {

    private static final String[] MENU_OPTIONS = {
            "Products",
            "Customers",
            "Sales",
            "Inventory",
            "Reports"
    };

    private final ProductMenu productMenu;
    private final CustomerMenu customerMenu;
    private final SalesMenu salesMenu;
    private final InventoryMenu inventoryMenu;
    private final ReportMenu reportMenu;

    /**
     * Constructs a MainMenu with explicitly injected submenus.
     *
     * @param productMenu   the Product Catalog submenu
     * @param customerMenu  the Customer Management submenu
     * @param salesMenu     the Sales and Checkout submenu
     * @param inventoryMenu the Inventory and Batch submenu
     * @param reportMenu    the Analytical and Revenue Reports submenu
     * @throws IllegalArgumentException if any submenu is null
     */
    public MainMenu(ProductMenu productMenu,
            CustomerMenu customerMenu,
            SalesMenu salesMenu,
            InventoryMenu inventoryMenu,
            ReportMenu reportMenu) {
        super("FOOD STORE MANAGEMENT SYSTEM", MENU_OPTIONS);
        if (productMenu == null || customerMenu == null || salesMenu == null
                || inventoryMenu == null || reportMenu == null) {
            throw new IllegalArgumentException("All submenu instances must be non-null.");
        }
        this.productMenu = productMenu;
        this.customerMenu = customerMenu;
        this.salesMenu = salesMenu;
        this.inventoryMenu = inventoryMenu;
        this.reportMenu = reportMenu;
    }

    /**
     * Dispatches the user's selected main menu option to the corresponding
     * submenu.
     *
     * @param choice the selected 1-based option index (1..5)
     */
    @Override
    protected void handleChoice(int choice) {
        switch (choice) {
            case 1:
                this.productMenu.display();
                break;
            case 2:
                this.customerMenu.display();
                break;
            case 3:
                this.salesMenu.display();
                break;
            case 4:
                this.inventoryMenu.display();
                break;
            case 5:
                this.reportMenu.display();
                break;
            default:
                System.out.println("  [ERROR] Unsupported menu choice: " + choice);
                break;
        }
    }

    /**
     * Suppresses the "Press Enter to continue" pause when returning from a
     * submenu back to the main menu.
     *
     * @return false so MainMenu immediately re-renders after a submenu exits
     */
    @Override
    protected boolean shouldPauseAfterAction() {
        return false;
    }

    /**
     * Suppresses automatic breadcrumb banners at the top-level main menu.
     *
     * @return false so that submenus render their own contextual breadcrumbs
     */
    @Override
    protected boolean shouldShowBreadcrumb() {
        return false;
    }

    /**
     * Returns the exit label for option [0] on the root menu.
     *
     * @return "Exit"
     */
    @Override
    protected String getZeroOptionLabel() {
        return "Exit";
    }

    /**
     * Prints a farewell status message when exiting the application.
     */
    @Override
    protected void onExit() {
        System.out.println("  [INFO] Thank you for using Food Store Management System. Goodbye!");
    }
}
