package exception;

public class InvalidPasswordException extends ValidationException {
    public enum Reason {
        EMPTY,
        TOO_SHORT,
        NO_UPPERCASE,
        NO_NUMBER,
        NO_SPECIAL_CHAR,
        INVALID
    }

    public InvalidPasswordException(Reason reason) {
        super(generateMessage(reason));
    }

    private static String generateMessage(Reason reason) {
        return switch (reason) {
            case EMPTY -> "Password cannot be empty";
            case TOO_SHORT -> "Password must be at least 8 characters long";
            case NO_UPPERCASE -> "Password must contain at least one uppercase letter";
            case NO_NUMBER -> "Password must contain at least one number";
            case NO_SPECIAL_CHAR -> "Password must contain at least one special character";
            case INVALID -> "Password is incorrect";
        };
    }
}

