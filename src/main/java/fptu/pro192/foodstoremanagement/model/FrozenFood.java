package fptu.pro192.foodstoremanagement.model;

/**
 * Concrete food subclass representing frozen goods requiring deep freeze preservation.
 * Standard HACCP Range: -25.0°C to -18.0°C, Humidity 85.0% to 95.0%.
 * Expiration warning threshold: 14 days.
 */
public class FrozenFood extends Food {

    public static final double DEFAULT_MIN_TEMP = -25.0;
    public static final double DEFAULT_MAX_TEMP = -18.0;
    public static final double DEFAULT_MIN_HUMIDITY = 85.0;
    public static final double DEFAULT_MAX_HUMIDITY = 95.0;
    public static final int EXPIRY_WARNING_DAYS = 14;

    public FrozenFood() {
        super();
        this.minTemperature = DEFAULT_MIN_TEMP;
        this.maxTemperature = DEFAULT_MAX_TEMP;
        this.minHumidity = DEFAULT_MIN_HUMIDITY;
        this.maxHumidity = DEFAULT_MAX_HUMIDITY;
    }

    public FrozenFood(String id, String name, String category, String unit, double price,
            double minTemperature, double maxTemperature, double minHumidity,
            double maxHumidity, boolean isDeleted) {
        super(id, name, category, unit, price, minTemperature, maxTemperature, minHumidity, maxHumidity, isDeleted);
    }

    public FrozenFood(String id, String name, String category, String unit, double price) {
        super(id, name, category, unit, price, DEFAULT_MIN_TEMP, DEFAULT_MAX_TEMP, DEFAULT_MIN_HUMIDITY, DEFAULT_MAX_HUMIDITY, false);
    }

    @Override
    public String getStorageInstructions() {
        return "Keep frozen. Store at temperatures between -25.0°C and -18.0°C with humidity between 85.0% and 95.0%.";
    }

    @Override
    public int getDaysBeforeExpiryWarning() {
        return EXPIRY_WARNING_DAYS;
    }
}
