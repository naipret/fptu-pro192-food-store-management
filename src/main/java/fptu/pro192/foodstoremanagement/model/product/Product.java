package fptu.pro192.foodstoremanagement.model.product;

public class Product {
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
        switch (type.trim().toUpperCase()) {
            case "FROZEN":
                return new FrozenFood(id, name, category, unit, price);
            case "CHILLED":
                return new ChilledFood(id, name, category, unit, price);
            case "DRY":
                return new DryFood(id, name, category, unit, price);
            default:
                throw new IllegalArgumentException("Unknown product category type: " + type);
        }
    }
}