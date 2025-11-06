package exception;

public class InvalidCheckInDateException extends BookingException {
    public InvalidCheckInDateException() {
        super("Cannot check-in outside of a valid date range");
    }
}
