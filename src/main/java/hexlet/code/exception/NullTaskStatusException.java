package hexlet.code.exception;

public class NullTaskStatusException extends RuntimeException {
    public NullTaskStatusException(String message) {
        super(message);
    }
}
