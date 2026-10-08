package fptu.pro192.foodstoremanagement.model;

/**
 * Concrete food subclass representing shelf-stable dry foods stored at ambient temperatures.
 * Standard HACCP Range: 15.0°C to 25.0°C, Humidity 30.0% to 60.0%. Expiration warning threshold: 7
 * days (BR20.2).
 */
public class DryFood extends Food {

    public static final double DEFAULT_MIN_TEMP = 15.0;
    public static final double DEFAULT_MAX_TEMP = 25.0;
    public static final double DEFAULT_MIN_HUMIDITY = 30.0;
    public static final double DEFAULT_MAX_HUMIDITY = 60.0;
    public static final int EXPIRY_WARNING_DAYS = 7; // BR20.2

    public DryFood() {
        super();
        this.minTemperature = DEFAULT_MIN_TEMP;
        this.maxTemperature = DEFAULT_MAX_TEMP;
        this.minHumidity = DEFAULT_MIN_HUMIDITY;
        this.maxHumidity = DEFAULT_MAX_HUMIDITY;
    }

    public DryFood(String id, String name, String category, String unit, double price,
            double minTemperature, double maxTemperature, double minHumidity, double maxHumidity,
            boolean isDeleted) {
        super(id, name, category, unit, price, minTemperature, maxTemperature, minHumidity,
                maxHumidity, isDeleted);
    }

    public DryFood(String id, String name, String category, String unit, double price) {
        super(id, name, category, unit, price, DEFAULT_MIN_TEMP, DEFAULT_MAX_TEMP,
                DEFAULT_MIN_HUMIDITY, DEFAULT_MAX_HUMIDITY, false);
    }

    @Override
    public String getStorageInstructions() {
        return "Store in a cool, dry place. Keep temperature between 15.0°C and 25.0°C and humidity between 30.0% and 60.0%.";
    }

    @Override
    public int getDaysBeforeExpiryWarning() {
        return EXPIRY_WARNING_DAYS;
    }
}
