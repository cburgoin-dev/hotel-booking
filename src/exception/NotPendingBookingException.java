package exception;

public class NotPendingBookingException extends BookingException {
    public NotPendingBookingException() {
        super("Booking must be pending to be confirmed");
    }
}
