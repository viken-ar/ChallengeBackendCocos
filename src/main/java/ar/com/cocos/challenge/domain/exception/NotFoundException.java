package ar.com.cocos.challenge.domain.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Class<?> entityClass, Object id) {
        super(entityClass.getSimpleName() + " not found with id: " + id);
    }
}
