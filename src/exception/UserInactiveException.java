package exception;

public class UserInactiveException extends UserException {
    public UserInactiveException() {
        super("User account is inactive");
    }
}
