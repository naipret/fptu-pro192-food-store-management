package fptu.pro192.foodstoremanagement.service;

import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.model.ProductFactory;
import fptu.pro192.foodstoremanagement.repository.ProductRepository;
import fptu.pro192.foodstoremanagement.util.ScannerManager;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;

public class ProductService {
    private final ProductRepository productRepo = new ProductRepository();
    private final Scanner scanner = ScannerManager.getInstance().getScanner();

    

    public ProductService() {

    }

    public void addProduct(Food product, Batch initialBatch, LocalDate today){
        long daysBetween = ChronoUnit.DAYS.between(initialBatch.getExpirationDate(), today);
        if(daysBetween == 1 && product.getCategory().equals("Chilled")){
            System.out.println("WARNING: This chilled product has 1 day or less of shelf life remaining. Do you still want to import? (Y/N): ");
            char choice = scanner.next().charAt(0);
            if(choice != 'y' && choice != 'Y'){
                System.out.println("Import operation cancelled!");
            }
        }
        else if(daysBetween == 7 && product.getCategory().equals("Dry")){
            System.out.println("WARNING: This dry product has 7 day or less of shelf life remaining. Do you still want to import? (Y/N): ");
            char choice = scanner.next().charAt(0);
            if(choice != 'y' && choice != 'Y'){
                System.out.println("Import operation cancelled!");
            }
        }

        productRepo.save(product);
        System.out.println("Product imported successfully");
    }

    public synchronized void deductStockFEFO(String productId, int quantity, LocalDate today){
        Food product = productRepo.findById(productId);
        if(product.getTotalStock() < quantity){
            // To do
        }
        int remainNeeded = quantity;
        List<Batch> batches = product.getActiveBatches();
        for(Batch batch : batches){
            if(batch.getQuantity() >= remainNeeded){
                batch.setQuantity(batch.getQuantity() - remainNeeded);
                break;
            }
            else{
                remainNeeded -= batch.getQuantity();
                batch.setQuantity(0);
            }
        }
    }

    public List<Food> getLowStockProducts(){
        productRepo.loadFromCsv();
        List<Food> lowStockProducts = new ArrayList<>();
        List<Food> products = productRepo.getProducts();
        for(Food product : products){
            int stock = product.getTotalStock();
            if(stock <= 5){
                lowStockProducts.add(product);
            }
        }
        return lowStockProducts;
    }

    public List<Batch> getExpiredBatches(LocalDate today){
        List<Batch> expireBatches = new ArrayList<>();
        List<Food> products = productRepo.getProducts();
        for(Food product : products){
            List<Batch> batches = product.getBatches();
            for(Batch batch : batches){
                if(batch.getDaysRemaining() <= 0){
                    expireBatches.add(batch);
                }
            }
        }
        return expireBatches;
    }

    public List<Batch> getNearExpiryBatches(LocalDate today){
        List<Batch> nearExpireBatches = new ArrayList<>();
        List<Food> products = productRepo.getProducts();
        for(Food product : products){
            List<Batch> batches = product.getBatches();
            for(Batch batch : batches){
                if(batch.getDaysRemaining() <= 7){
                    nearExpireBatches.add(batch);
                }
            }
        }
        return nearExpireBatches;
    }

    public void updateProduct(String id, Double newPrice, Integer newQuantity){
        // Todo
    }

    public void replenishStock(String productId, String batchId, int addQty){

    }

    public List<Food> search(String keyword){

    }
}
