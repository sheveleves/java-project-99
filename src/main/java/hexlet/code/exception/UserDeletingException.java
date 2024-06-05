package hexlet.code.exception;

public class UserDeletingException extends RuntimeException {
    public UserDeletingException(String message) {
        super(message);
    }
}
