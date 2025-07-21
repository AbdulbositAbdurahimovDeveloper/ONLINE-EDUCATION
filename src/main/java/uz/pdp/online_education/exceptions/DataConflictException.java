package uz.pdp.online_education.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an operation would result in a data conflict, such as creating a user
 * with a username or email that already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DataConflictException extends RuntimeException {

    public DataConflictException(String message) {
        super(message);
    }

    public DataConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}