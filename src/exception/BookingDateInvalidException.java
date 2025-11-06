package exception;

public class BookingDateInvalidException extends BookingException {
    public BookingDateInvalidException() {
        super("Check-in date must be before check-out date");
    }
}