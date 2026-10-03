package fptu.pro192.foodstoremanagement.model.product;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Batch {
    private String batchId; //(e.g., B00001)
    private String productId; //(foreign key linking to Food.id)
    private LocalDate importDate;
    private LocalDate productionDate;
    private LocalDate expirationDate;
    private int quantity;
    
    
    
    public Batch() {
    }



    public Batch(String batchId, String productId, LocalDate importDate, LocalDate productionDate,
            LocalDate expirationDate, int quantity) {
        this.batchId = batchId;
        this.productId = productId;
        this.importDate = importDate;
        this.productionDate = productionDate;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
    }

    public boolean isExpired(){
        LocalDate currentDate = LocalDate.now();
        if(currentDate.isAfter(this.expirationDate) || currentDate.isEqual(this.expirationDate)){
            return true;
        }
        return false;
    }

    public int isCloseToExpiry(LocalDate currentDate, int warningDays){
        
        return 0;
    }

    public long getDaysRemaning(){

        return ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);

    }



    public int getQuantity() {
        return quantity;
    }
    

}
