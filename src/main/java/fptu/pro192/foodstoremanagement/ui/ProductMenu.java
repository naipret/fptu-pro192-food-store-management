package fptu.pro192.foodstoremanagement.ui;

import fptu.pro192.foodstoremanagement.service.ProductService;

public class ProductMenu extends AbstractMenu {

    public ProductMenu(String title) {
        super(title);
    }

    protected void renderBody() {
        System.out.println("1. Add new product");
        System.out.println("2. Update product information");
        System.out.println("3. View all products");
        System.out.println("4. Search products by name / category");
        System.out.println("5. View low stock products");
        System.out.println("6. View expired products");
        System.out.println("7. View near-expiry products");
        System.out.println("8. Update inventory / Restock product");
        System.out.println("9. Other option");
        System.out.println("0. Back to main menu");
    }

    protected boolean handleOption(int choice) {
        
        switch (choice) {
            case 1:

                break;

            case 2:

                break;

            case 3:

                break;

            case 4:

                break;

            case 5:

                break;

            case 6:

                break;

            case 7:

                break;

            case 8:

                break;

            case 9:

                break;

            case 0:

                break;

            default:
                break;
        }
        return true;
    }

}
