package fptu.pro192.foodstoremanagement.model.product;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Food {
    protected String id;
    protected String name;
    protected String category;
    protected String unit;
    protected double price;
    protected double minTemperature;
    protected double maxTemperature;
    protected double minHumidity;
    protected double maxHumidity;
    protected boolean isDeleted;
    protected List<Batch> batches;

    public Food() {

    }
    
    
    
    public Food(String id, String name, String category, String unit, double price, double minTemperature,
            double maxTemperature, double minHumidity, double maxHumidity, boolean isDeleted) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.price = price;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.minHumidity = minHumidity;
        this.maxHumidity = maxHumidity;
        this.isDeleted = isDeleted;
    }



    public Food(String id, String name, String category, String unit, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.price = price;
    }

    public Food(String id, String name, String category, String unit, double price, List<Batch> batches) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.price = price;
        this.batches = batches;
    }

    public int getTotalStock() {
        int sum = 0;
        for (Batch batch : this.batches) {
            if (!batch.isExpired()) {
                sum += batch.getQuantity();
            }
        }
        return sum;
    }

    public List<Batch> getActiveBatches(LocalDate today) {
        List<Batch> activeBatches = new ArrayList<>();
        for (Batch batch : this.batches) {
            if (!batch.isExpired()) {
                activeBatches.add(batch);
            }
        }

        activeBatches.sort((b1, b2) -> Integer.compare((int) b1.getDaysRemaning(), (int) b2.getDaysRemaning()));

        return activeBatches;
    }

    public void addBatch(Batch batch) {
        batches.add(batch);
    }

    public List<Batch> getBatches(){
        return Collections.unmodifiableList(batches);
    }

    public String getId() {
        return id;
    }



    public String getName() {
        return name;
    }



    public String getCategory() {
        return category;
    }



    public String getUnit() {
        return unit;
    }



    public double getPrice() {
        return price;
    }



    public double getMinTemperature() {
        return minTemperature;
    }



    public double getMaxTemperature() {
        return maxTemperature;
    }



    public double getMinHumidity() {
        return minHumidity;
    }



    public double getMaxHumidity() {
        return maxHumidity;
    }



    public boolean isDeleted() {
        return isDeleted;
    }

    

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }



    abstract String getStorageInstructions();

    abstract int getDaysBeforeExpiryWarning();

}
