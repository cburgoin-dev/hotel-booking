package exception;

public class GuestEmailAlreadyExistsException extends GuestException {
    public GuestEmailAlreadyExistsException() {
        super("Email already exists");
    }
}
