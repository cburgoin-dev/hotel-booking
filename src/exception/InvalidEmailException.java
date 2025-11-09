package exception;

public class InvalidEmailException extends ValidationException {
    public InvalidEmailException(boolean isEmpty) {
        super(generateMessage(isEmpty));
    }

    private static String generateMessage(boolean isEmpty) {
        if (isEmpty) {
            return "Email cannot be empty";
        } else {
            return "Email format is not valid";
        }
    }
}

