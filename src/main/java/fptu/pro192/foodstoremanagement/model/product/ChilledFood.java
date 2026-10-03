package fptu.pro192.foodstoremanagement.model.product;

public class ChilledFood extends Food {
    public ChilledFood() {
        super();
    }

    public ChilledFood(String id, String name, String category, String unit, double price) {
        super(id, name, category, unit, price);
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
