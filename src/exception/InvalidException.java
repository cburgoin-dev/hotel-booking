package exception;

public class InvalidException extends ValidationException {
    public InvalidException(String entity) {
        super(entity + " cannot be null");
    }
}