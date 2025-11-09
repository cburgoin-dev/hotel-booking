package exception;

public class InvalidRoleException extends ValidationException {
    public InvalidRoleException(boolean isEmpty, String role) {
        super(generateMessage(isEmpty, role));
    }

    private static String generateMessage(boolean isEmpty, String role) {
        if (isEmpty) {
            return "Role cannot be empty";
        } else {
            return "Invalid user role: " + role;
        }
    }
}

