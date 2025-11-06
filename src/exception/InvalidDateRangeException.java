package exception;

public class InvalidDateRangeException extends BookingException {
    public InvalidDateRangeException() {
        super("Check-in and check-out must be at least one night apart");
    }
}