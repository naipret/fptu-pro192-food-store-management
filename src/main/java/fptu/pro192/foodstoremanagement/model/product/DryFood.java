package fptu.pro192.foodstoremanagement.model.product;

public class DryFood extends Food {
    
    public DryFood() {
        super();
        this.minTemperature = 15.0;
        this.maxTemperature = 25.0;
        this.minHumidity = 30.0;
        this.maxHumidity = 60.0;
    }

    public DryFood(String id, String name, String category, String unit, double price) {
        super(id, name, category, unit, price);
        this.minTemperature = 15.0;
        this.maxTemperature = 25.0;
        this.minHumidity = 30.0;
        this.maxHumidity = 60.0;
    }

    @Override 
    public String getStorageInstructions() {
        return "Store in a cool, dry place. Keep temperature between 15.0°C and 25.0°C and humidity between 30.0% and 60.0%";
    }

    @Override 
    public int getDaysBeforeExpiryWarning() {
        return 7;
    }
    
}
