package exception;

public class RoomNumberEmptyException extends RoomException {
    public RoomNumberEmptyException() {
        super("Room number cannot be empty");
    }
}
