package exception;

public class RoomInvalidException extends RoomException {
    public RoomInvalidException() {
        super("Room cannot be null");
    }
}