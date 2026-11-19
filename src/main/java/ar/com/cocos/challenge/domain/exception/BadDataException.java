package ar.com.cocos.challenge.domain.exception;

public class BadDataException extends RuntimeException {
    public BadDataException(String message) {
        super(message);
    }
}
