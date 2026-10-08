package fptu.pro192.foodstoremanagement.util;

public class InputValidator {
    public static String normalPhoneNumbers(String rawNumbers){
        if(rawNumbers == null){
            throw new IllegalArgumentException("Phone numbers can not be empty.");
        }
        //MAKE CLEAN PHONENUMBERS(ONLY INCLUDING DIGITS 0-9 AND +
        String Normalized = rawNumbers.replaceAll("[^0-9+]","");
        Normalized = Normalized.replaceFirst("\\+?840","0");

        // REPLACE +84 AND 84 TO 0
        if(Normalized.startsWith("+84")){
            Normalized = "0" + Normalized.substring(3);
        } else if (Normalized.startsWith("84")) {
            Normalized = "0" + Normalized.substring(2);
        }
        //CHECKING FORMAT
        if(!Normalized.matches("^0[35789]\\d{8}$")){
            throw  new IllegalArgumentException("Invalid Vietnamese phone numbers format");
        }
        return Normalized;
    }
}
