package exception;

public class EmailAlreadyExistsException extends ValidationException {
    public EmailAlreadyExistsException() {
        super("Email already exists");
    }
}
