package exception;

public class RoomInvalidPriceException extends RoomException {
    public RoomInvalidPriceException() {
        super("Room price must be positive");
    }
}
