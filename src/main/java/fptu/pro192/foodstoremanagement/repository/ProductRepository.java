package fptu.pro192.foodstoremanagement.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fptu.pro192.foodstoremanagement.model.product.Batch;
import fptu.pro192.foodstoremanagement.model.product.Food;
import fptu.pro192.foodstoremanagement.model.product.Product;
import fptu.pro192.foodstoremanagement.util.MiniCsv;

public class ProductRepository {
    private final String productFilePath = "data/products.csv";
    private final String batchFilePath = "data/batches.csv";
    private final List<Food> products = new ArrayList<>();

    public void loadFromCsv(){
        try{
            List<String[]> productsData = MiniCsv.read(productFilePath);
            for(String[] productData : productsData){
                String id = productData[0];
                String name = productData[1];
                String category = productData[2];
                String unit = productData[3];
                double price = Double.parseDouble(productData[4]);
                double minTemperature = Double.parseDouble(productData[5]);
                double maxTemperature = Double.parseDouble(productData[6]);
                double minHumidity = Double.parseDouble(productData[7]);
                double maxHumidity = Double.parseDouble(productData[8]);
                boolean isDeleted = Boolean.parseBoolean(productData[9]);

                // Product product = Food.createProduct(category, id, name, category, unit, price,
                //         minTemperature, maxTemperature, minHumidity, maxHumidity,
                //         isDeleted);
                products.add(Product.createProduct(category, id, name, category, unit, price,
                        minTemperature, maxTemperature, minHumidity, maxHumidity,
                        isDeleted));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public synchronized void saveToCsv(){
        List<String[]> productsData = new ArrayList<>();
        List<String[]> batchesData = new ArrayList<>();
        for(Food product : products){
            String[] productData = {
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getUnit(),
                    String.valueOf(product.getPrice()),
                    String.valueOf(product.getMinTemperature()),
                    String.valueOf(product.getMaxTemperature()),
                    String.valueOf(product.getMinHumidity()),
                    String.valueOf(product.getMaxHumidity()),
                    String.valueOf(product.isDeleted())
            };
            productsData.add(productData);
        }
        for(Food product : products){
            product.getBatches().forEach(batch -> {
                String[] batchData = {
                        batch.getBatchId(),
                        product.getId(),
                        String.valueOf(batch.getQuantity()),
                        batch.getProductionDate().toString(),
                        batch.getExpirationDate().toString()
                };
                batchesData.add(batchData);
            });
        }
        MiniCsv.writeAtomic(productFilePath, productsData);
        MiniCsv.writeAtomic(batchFilePath, batchesData);
    }

    public Food findById(String id){
        for(Food product : products){
            if(product.getId().equals(id) && !product.isDeleted()){
                return product;
            }
        }
        return null;
    }
    //Returns unmodifiable list of active food items.

    public List<Food> findAll(){
        List<Food> activeProduct = new ArrayList<>();
        for(Food food : products){
            if(!food.isDeleted()){
                activeProduct.add(food);
            }
        }
        return Collections.unmodifiableList(activeProduct);
    }

    public void save(Food food){
        products.add(food);
        saveToCsv();
    }


    public void delete(String id){
        for(int i = 0;i < products.size();i++){
            if(products.get(i).getId().equals(id)){
                products.get(i).setDeleted(true);
                saveToCsv();
                return;
            }
        }
    }

}
