package fptu.pro192.foodstoremanagement.model.product;


public class FrozenFood extends Food {

    

    



    public FrozenFood(String id, String name, String category, String unit, double price, double minTemperature,
            double maxTemperature, double minHumidity, double maxHumidity, boolean isDeleted) {
        super(id, name, category, unit, price, minTemperature, maxTemperature, minHumidity, maxHumidity, isDeleted);
    }



    public FrozenFood() {
        super();
        this.minTemperature = -25.0;
        this.maxTemperature = -18.0;
        this.minHumidity = 85.0;
        this.maxHumidity = 95.0;
    }



    public FrozenFood(String id, String name, String category, String unit, double price) {
        super(id, name, category, unit, price);
        this.minTemperature = -25.0;
        this.maxTemperature = -18.0;
        this.minHumidity = 85.0;
        this.maxHumidity = 95.0;

    }
    
    

    @Override
    public String getStorageInstructions() {
        return "Keep frozen. Store at temperatures between -25.0°C and -18.0°C with humidity between 85.0% and 95.0%.";
    }

    @Override
    public int getDaysBeforeExpiryWarning() {
        return 14;
    }
}
