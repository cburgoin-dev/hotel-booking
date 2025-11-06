package exception;

public class CannotCancelBookingException extends BookingException {
    public CannotCancelBookingException(boolean pastCheckIn, boolean lessThan24h) {
        super(generateMessage(pastCheckIn, lessThan24h));
    }

    private static String generateMessage(boolean pastCheckIn, boolean lessThan24h) {
        if (pastCheckIn) {
            return "Cannot cancel booking after check-in";
        }
        if (lessThan24h) {
            return "Cannot cancel booking less than 24 hours before check-in";
        }
        return "Cannot cancel booking at this time";
    }
}
