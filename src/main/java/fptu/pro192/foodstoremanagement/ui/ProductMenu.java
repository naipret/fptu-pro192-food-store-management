package fptu.pro192.foodstoremanagement.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.ChilledFood;
import fptu.pro192.foodstoremanagement.model.DryFood;
import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.model.FrozenFood;
import fptu.pro192.foodstoremanagement.model.ProductFactory;
import fptu.pro192.foodstoremanagement.service.ProductService;
import fptu.pro192.foodstoremanagement.util.*;
public class ProductMenu extends AbstractMenu {
    Scanner sc = ScannerManager.getInstance().getScanner();

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
                handleAddNewProduct();
                return true;
            case 2:
                handleUpdateProductInfo();
                return true;
            case 3:
                handleViewAllProducts();
                return true;
            case 4:
                handleSearchProducts();
                return true;
            case 5:
                handleViewLowStockProducts();
                return true;
            case 6:
                handleViewExpiredProducts();
                return true;
            case 7:
                handleViewNearExpiryProducts();
                return true;
            case 8:
                handleRestockProduct();
                return true;
            case 9:
                handleDeleteProduct();
                return true;
            case 0:
                return false;
            default:
        return false;
        }
    }

        // 1. Add new product
    private void handleAddNewProduct() {
        while(true){
            String id, name, category,unit;
            double price;
            id = ConsoleUtil.readString("Input ID:", false);
            name = ConsoleUtil.readString("Input name:", false);
            category = ConsoleUtil.readString("Input category:", false);
            unit = ConsoleUtil.readString("Input unit:", false);
            System.out.println("Input price: ");
            price = sc.nextDouble();
            if(category.toLowerCase().equals("frozen")){
                Food product = ProductFactory.createProduct(category, id, name, category, unit, price, FrozenFood.DEFAULT_MIN_TEMP, FrozenFood.DEFAULT_MAX_TEMP, FrozenFood.DEFAULT_MIN_HUMIDITY, FrozenFood.DEFAULT_MAX_HUMIDITY, false);
                
            }
            
            else if(category.toLowerCase().equals("chilled")){
                Food product = ProductFactory.createProduct(category, id, name, category, unit, price, ChilledFood.DEFAULT_MIN_TEMP, ChilledFood.DEFAULT_MAX_TEMP, ChilledFood.DEFAULT_MIN_HUMIDITY, ChilledFood.DEFAULT_MAX_HUMIDITY, false);
            }
            
            else if(category.toLowerCase().equals("dry")){
                Food product = ProductFactory.createProduct(category, id, name, category, unit, price, DryFood.DEFAULT_MIN_TEMP, DryFood.DEFAULT_MAX_TEMP, DryFood.DEFAULT_MIN_HUMIDITY, DryFood.DEFAULT_MAX_HUMIDITY, false);
            }
            break;
        }  
        
    }

    // 2. Update product information
    private void handleUpdateProductInfo() {
        
    }

    // 3. View all products
    private void handleViewAllProducts() {
        
        List<Food> products = new ArrayList<>();
        ProductService tmpObject = new ProductService();
        products = tmpObject.getAllProducts();
        for(Food product : products){
            // ke khung kem voi header theo nguyen tac
        }
    }

    // 4. Search products by name / category
    private void handleSearchProducts() {
        ProductService tmpObject = new ProductService(); 
        String key = ConsoleUtil.readString("Input Keyword: ", false);
        List<Food> products = tmpObject.search(key);
        for(Food product : products){
            // ke khung kem voi header theo nguyen tac
        }
    }

    // 5. View low stock products
    private void handleViewLowStockProducts() {
        ProductService tmpObject = new ProductService();
        List<Food> products = tmpObject.getLowStockProducts();
        for(Food product : products){
            // ke khung view kem voi header theo nguyen tac
        }
    }

    // 6. View expired products
    private void handleViewExpiredProducts() {
        ProductService tmpObject = new ProductService();
        List<Batch> batchs = tmpObject.getExpiredBatches(null);
        for(Batch batch : batchs){
            // ke khung view kem voi header theo nguyen tac, in ra thong tin cua san pham kem ngay het han
        }
    }

    // 7. View near-expiry products
    private void handleViewNearExpiryProducts() {
        ProductService tmpObject = new ProductService();
        List<Batch> batchs = tmpObject.getNearExpiryBatches(null);
        for(Batch batch : batchs){
            // ke khung view kem voi header theo nguyen tac, in ra thong tin cua san pham kem ngay con lai truoc khi het han
        }
    }

    // 8. Update inventory / Restock product
    private void handleRestockProduct() {
        // TODO: Implement stock update / FEFO intake logic
    }

    // 9. Other option
    private void handleOtherOption() {
        // TODO: Implement other action logic
    }

    private void handleDeleteProduct() {
        // TODO: Implement other action logic
    }

}
