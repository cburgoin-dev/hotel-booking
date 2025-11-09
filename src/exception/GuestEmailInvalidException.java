package exception;

public class GuestEmailInvalidException extends GuestException {
    public GuestEmailInvalidException(boolean isEmpty) {
        super(generateMessage(isEmpty));
    }

    private static String generateMessage(boolean isEmpty) {
        if (isEmpty) {
            return "Email cannot be empty";
        } else {
            return "Email format is not valid";
        }
    }
}
