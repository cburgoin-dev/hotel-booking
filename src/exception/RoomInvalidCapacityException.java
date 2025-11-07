package exception;

public class RoomInvalidCapacityException extends RoomException {
    public RoomInvalidCapacityException() {
        super("Room capacity must be greater tan 0");
    }
}
