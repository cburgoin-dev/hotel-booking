package exception;

public class GuestNameEmptyException extends GuestException {
    public GuestNameEmptyException(boolean emptyFirstName, boolean emptyLastName) {
        super(generateMessage(emptyFirstName, emptyLastName));
    }

    private static String generateMessage(boolean emptyFirstName, boolean emptyLastName) {
        if (emptyFirstName) {
            return "First name cannot be empty";
        }
        if (emptyLastName) {
            return "Last name cannot be empty";
        }
        return "Name cannot be empty";
    }
}