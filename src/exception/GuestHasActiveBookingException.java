package exception;

public class GuestHasActiveBookingException extends BookingException {
    public GuestHasActiveBookingException() {
        super("Guest already has an active booking");
    }
}
