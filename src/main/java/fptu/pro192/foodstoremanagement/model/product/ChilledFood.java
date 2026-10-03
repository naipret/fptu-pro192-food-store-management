package fptu.pro192.foodstoremanagement.model.product;

public class ChilledFood extends Food {

    
    public ChilledFood(String id, String name, String category, String unit, double price, double minTemperature,
            double maxTemperature, double minHumidity, double maxHumidity, boolean isDeleted) {
        super(id, name, category, unit, price, minTemperature, maxTemperature, minHumidity, maxHumidity, isDeleted);
    }

    public ChilledFood() {
        super();
        this.minTemperature = 0.0;
        this.maxTemperature = 4.0;
        this.minHumidity = 75.0;
        this.maxHumidity = 85.0;
    }

    public ChilledFood(String id, String name, String category, String unit, double price) {
        super(id, name, category, unit, price);
        this.minTemperature = 0.0;
        this.maxTemperature = 4.0;
        this.minHumidity = 75.0;
        this.maxHumidity = 85.0;
    }

    @Override
    public String getStorageInstructions() {
        return "Keep refrigerated. Store between 0.0°C and 4.0°C with humidity maintained at 75.0% - 85.0%.";
    }

    @Override
    public int getDaysBeforeExpiryWarning() {
        return 1;
    }
}
