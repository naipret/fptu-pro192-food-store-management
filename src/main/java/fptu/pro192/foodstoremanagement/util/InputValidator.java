package fptu.pro192.foodstoremanagement.util;

public class InputValidator {
    public static String NormalPhoneNumbers(String rawNumers){
        if(rawNumers == null){
            throw new IllegalArgumentException("Phone numbers can not be empty.");
        }
        //MAKE CLEAN PHONENUMBERS(ONLY INCLUDING DIGITS 0-9 AND +
        String normalized = rawNumers.replaceAll("[^0-9+]","");
        // REPLACE +84 AND 84 TO 0
        if(normalized.startsWith("+84")){
            normalized = "0" + normalized.substring(3);
        } else if (normalized.startsWith("84")) {
            normalized = "0" + normalized.substring(2);
        }
        //CHECKING FORMAT
        if(!normalized.matches("^0[35789/d{8}]")){
            throw  new IllegalArgumentException("Invalid Vietnamese phone numbers format");
        }
        return normalized;
    }
}
