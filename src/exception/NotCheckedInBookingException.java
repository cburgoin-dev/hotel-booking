package exception;

public class NotCheckedInBookingException extends BookingException {
    public NotCheckedInBookingException() {
        super("Booking must be checked-in to check-out");
    }
}
