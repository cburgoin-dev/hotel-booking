package exception;

public class RoomUnavailableException extends BookingException {
    public RoomUnavailableException() {
        super("Room is not available for the selected dates");
    }
}