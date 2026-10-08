package fptu.pro192.foodstoremanagement.model;

/**
 * Factory class implementing the Factory Method pattern to instantiate the appropriate
 * Food concrete subclass (FrozenFood, ChilledFood, DryFood) based on category type string.
 */
public final class ProductFactory {

    private ProductFactory() {
        // Prevent instantiation of utility factory class
    }

    /**
     * Instantiates the corresponding Food subclass based on type string.
     *
     * @param type Product category type (e.g. "FROZEN", "CHILLED", "DRY")
     * @param id Unique product ID
     * @param name Descriptive food name
     * @param category Product category
     * @param unit Measurement unit
     * @param price Unit price
     * @param minTemperature Lower temperature bound
     * @param maxTemperature Upper temperature bound
     * @param minHumidity Lower humidity bound
     * @param maxHumidity Upper humidity bound
     * @param isDeleted Soft delete status flag
     * @return Concrete Food instance
     * @throws IllegalArgumentException If type is unknown or domain invariants are violated
     */
    public static Food create(
            String type,
            String id,
            String name,
            String category,
            String unit,
            double price,
            double minTemperature,
            double maxTemperature,
            double minHumidity,
            double maxHumidity,
            boolean isDeleted) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Product category type cannot be null or empty");
        }

        switch (type.trim().toUpperCase()) {
            case "FROZEN":
                return new FrozenFood(id, name, category, unit, price,
                        minTemperature, maxTemperature, minHumidity, maxHumidity, isDeleted);
            case "CHILLED":
                return new ChilledFood(id, name, category, unit, price,
                        minTemperature, maxTemperature, minHumidity, maxHumidity, isDeleted);
            case "DRY":
                return new DryFood(id, name, category, unit, price,
                        minTemperature, maxTemperature, minHumidity, maxHumidity, isDeleted);
            default:
                throw new IllegalArgumentException("Unknown product category type: " + type);
        }
    }

    /**
     * Alias method for backward compatibility.
     */
    public static Food createProduct(
            String type,
            String id,
            String name,
            String category,
            String unit,
            double price,
            double minTemperature,
            double maxTemperature,
            double minHumidity,
            double maxHumidity,
            boolean isDeleted) {
        return create(type, id, name, category, unit, price,
                minTemperature, maxTemperature, minHumidity, maxHumidity, isDeleted);
    }
}
