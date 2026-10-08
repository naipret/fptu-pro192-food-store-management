package fptu.pro192.foodstoremanagement.model;

/**
 * Concrete food subclass representing chilled/fresh foods requiring refrigeration.
 * Standard HACCP Range: 0.0°C to 4.0°C, Humidity 75.0% to 85.0%.
 * Expiration warning threshold: 1 day (BR20.1).
 */
public class ChilledFood extends Food {

    public static final double DEFAULT_MIN_TEMP = 0.0;
    public static final double DEFAULT_MAX_TEMP = 4.0;
    public static final double DEFAULT_MIN_HUMIDITY = 75.0;
    public static final double DEFAULT_MAX_HUMIDITY = 85.0;
    public static final int EXPIRY_WARNING_DAYS = 1; // BR20.1

    public ChilledFood() {
        super();
        this.minTemperature = DEFAULT_MIN_TEMP;
        this.maxTemperature = DEFAULT_MAX_TEMP;
        this.minHumidity = DEFAULT_MIN_HUMIDITY;
        this.maxHumidity = DEFAULT_MAX_HUMIDITY;
    }

    public ChilledFood(String id, String name, String category, String unit, double price,
            double minTemperature, double maxTemperature, double minHumidity,
            double maxHumidity, boolean isDeleted) {
        super(id, name, category, unit, price, minTemperature, maxTemperature, minHumidity, maxHumidity, isDeleted);
    }

    public ChilledFood(String id, String name, String category, String unit, double price) {
        super(id, name, category, unit, price, DEFAULT_MIN_TEMP, DEFAULT_MAX_TEMP, DEFAULT_MIN_HUMIDITY, DEFAULT_MAX_HUMIDITY, false);
    }

    @Override
    public String getStorageInstructions() {
        return "Keep refrigerated. Store between 0.0°C and 4.0°C with humidity maintained at 75.0% - 85.0%.";
    }

    @Override
    public int getDaysBeforeExpiryWarning() {
        return EXPIRY_WARNING_DAYS;
    }
}
