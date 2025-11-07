package exception;

public class RoomInvalidException extends BookingException {
    public RoomInvalidException() {
        super("Room cannot be null");
    }
}