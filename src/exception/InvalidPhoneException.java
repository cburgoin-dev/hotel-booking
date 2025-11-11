package exception;

public class InvalidPhoneException extends ValidationException {
    public InvalidPhoneException(boolean isEmpty) {
        super(generateMessage(isEmpty));
    }

    private static String generateMessage(boolean isEmpty) {
        if (isEmpty) {
            return "Phone cannot be empty";
        } else {
            return "Phone number format is not valid";
        }
    }
}
