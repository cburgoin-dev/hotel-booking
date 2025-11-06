package exception;

public class NotConfirmedBookingException extends BookingException {
    public NotConfirmedBookingException() {
        super("Booking must be confirmed to check-in");
    }
}
