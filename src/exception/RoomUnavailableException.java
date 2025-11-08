package exception;

public class RoomUnavailableException extends RoomException {
    public RoomUnavailableException() {
        super("Room is not available for the selected dates");
    }
}