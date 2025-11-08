package exception;

public class InvalidBookingStatusException extends BookingException {
    public InvalidBookingStatusException(String status) {
        super("Invalid booking status: " + status);
    }
}
