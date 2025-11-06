package exception;

public class CapacityExceededException extends BookingException {
    public CapacityExceededException() {
        super("Number of guests exceeds room capacity");
    }
}
