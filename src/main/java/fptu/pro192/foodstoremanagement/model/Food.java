package fptu.pro192.foodstoremanagement.model;

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

    abstract String getStorageInstructions();

    abstract int getDaysBeforeExpiryWarning();

}
