package hexlet.code.exception;

public class TaskStatusDeletingException extends RuntimeException {
    public TaskStatusDeletingException(String message) {
        super(message);
    }
}
