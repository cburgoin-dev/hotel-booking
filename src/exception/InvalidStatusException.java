package exception;

public class InvalidStatusException extends ValidationException {
    private final String entity;
    private final String invalidValue;

    public InvalidStatusException(String entity, String invalidValue) {
        super("Invalid status for " + entity + ": '" + invalidValue +"'");
        this.entity = entity;
        this.invalidValue = invalidValue;
    }

    public String getEntity() {
        return entity;
    }

    public String getInvalidValue() {
        return invalidValue;
    }
}
