package exception;

public class GuestInvalidException extends GuestException {
    public GuestInvalidException() {
        super("Guest cannot be null");
    }
}